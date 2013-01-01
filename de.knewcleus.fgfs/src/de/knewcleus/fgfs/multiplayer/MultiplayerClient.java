/**
 * Copyright (C) 2008-2009 Ralf Gerlich 
 * Copyright (C) 2012 Wolfram Wagner
 * 
 * This file is part of OpenRadar.
 * 
 * OpenRadar is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OpenRadar is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OpenRadar. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Diese Datei ist Teil von OpenRadar.
 * 
 * OpenRadar ist Freie Software: Sie k�nnen es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder sp�teren ver�ffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 * 
 * OpenRadar wird in der Hoffnung, dass es n�tzlich sein wird, aber OHNE JEDE
 * GEW�HELEISTUNG, bereitgestellt; sogar ohne die implizite Gew�hrleistung der
 * MARKTF�HIGKEIT oder EIGNUNG F�R EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License f�r weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.fgfs.multiplayer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import de.knewcleus.fgfs.location.Position;
import de.knewcleus.fgfs.multiplayer.protocol.MultiplayerPacket;
import de.knewcleus.fgfs.multiplayer.protocol.PositionMessage;

public abstract class MultiplayerClient<T extends Player> extends AbstractMultiplayerEndpoint<T> {
    protected static Logger logger = Logger.getLogger("de.knewcleus.fgfs.multiplayer");
    private final static long APPLICATION_START_TIME_MILLIS = System.currentTimeMillis();
    protected final InetAddress serverAddress;
    protected final int serverPort;
    protected final int localPort;
    protected final List<OutgoingMessage> chatQueue = Collections.synchronizedList(new ArrayList<OutgoingMessage>());
    protected volatile OutgoingMessage activeChatMessage = null;
    protected String frequency = "000.00";

    protected volatile long lastPositionUpdateTimeMillis;

    public MultiplayerClient(IPlayerRegistry<T> playerRegistry, String mpServer, int mpServerPort, int mpLocalPort) throws IOException {
        super(playerRegistry, mpLocalPort);
        this.serverAddress = InetAddress.getByName(mpServer);
        this.serverPort = mpServerPort;
        this.localPort = mpLocalPort;
        lastPositionUpdateTimeMillis = System.currentTimeMillis();

        Thread chatPositionSender = new Thread("OpenRadar - messageSender") {
            @Override
            public void run() {
                int counter = 0;
                while (!Thread.interrupted()) {
                    try {
                        synchronized (this) {
                            // send every second if queue is empty, otherwise
                            // every 250 ms.
                            if (!chatQueue.isEmpty() || activeChatMessage != null || counter > 1) {
                                sendPositionUpdate();
                                counter = 0;
                            } else {
                                counter++;
                            }
                        }

                        sleep(250);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        chatPositionSender.setDaemon(true);
        chatPositionSender.start();

    }

    // public void sendPacket(MultiplayerPacket mppacket) throws
    // MultiplayerException {
    // sendPacket(serverAddress, serverPort, mppacket);
    // }

    // @Override
    // protected void update() {
    // super.update();
    // // sendPositionUpdate(); moved to thread chatPositionSender to remove
    // link between sending and reception
    // }

    protected synchronized void sendPositionUpdate() {
        PositionMessage positionMessage = new PositionMessage();
        positionMessage.setTime((double) (System.currentTimeMillis() - APPLICATION_START_TIME_MILLIS) / 1000d);
        positionMessage.setLag(0d);
        positionMessage.setPosition(getPosition());
        positionMessage.setOrientation(getOrientation());
        positionMessage.setLinearVelocity(getLinearVelocity());
        positionMessage.setModel(getModel());

        if (activeChatMessage != null) {
            if (System.currentTimeMillis() - activeChatMessage.firstSendTime > 3000 && activeChatMessage.sendCounter > 9) {
                // stop sending after 3 seconds
                activeChatMessage = null;
            }
        }
        if (activeChatMessage == null && chatQueue.size() > 0) {
            // there is a chat message waiting to be sent
            activeChatMessage = chatQueue.remove(0);
            activeChatMessage.firstSendTime = System.currentTimeMillis();
        }
        // todo activate when all users have updated
        // positionMessage.putProperty("sim/multiplay/transmission-freq-hz",
        // this.frequency);
        // send it
        if (activeChatMessage != null) {
            positionMessage.putProperty("sim/multiplay/chat", activeChatMessage.message);
            activeChatMessage.sendCounter++;
            // System.out.println("Chat message sent: Counter "+activeChatMessage.sendCounter+", ms since first transmission: "+(System.currentTimeMillis()-activeChatMessage.firstSendTime));
        } else {
            positionMessage.putProperty("sim/multiplay/chat", "");
        }

        // if(chatQueue.size()>2) {
        // System.out.println("WARNING: Chat outgoing queue size: "+chatQueue.size());
        // }
        MultiplayerPacket mppacket = new MultiplayerPacket(getCallsign(), positionMessage);
        try {
            sendPacket(serverAddress, serverPort, mppacket);
            // System.out.println("Message sent");
        } catch (MultiplayerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        lastPositionUpdateTimeMillis = System.currentTimeMillis();
    }

    /**
     * Adds the message to the outgoing queue...
     * 
     * @param message
     */
    public void sendChatMessage(String f, String message) {
        // chatQueue is synchronized itself
        chatQueue.add(new OutgoingMessage(message));
        if (!"".equals(f.trim())) {
            f = f.replaceAll(",", ".");
            BigDecimal bd = new BigDecimal(f).multiply(new BigDecimal(1000000));
            this.frequency = String.format("%1.0f", bd);
        } else {
            this.frequency = "100000000";
        }
    }

    /**
     * RECEPTION: This method is called by the receiving thread. It does not
     * modify any local variables.
     */
    @Override
    protected void processPacket(T player, MultiplayerPacket mppacket) throws MultiplayerException {
        if (mppacket.getMessage() instanceof PositionMessage) {
            PositionMessage positionMessage = (PositionMessage) mppacket.getMessage();
            String chatMessage = positionMessage.getProperty("sim/multiplay/chat");
            // if(player.callsign.startsWith("TE")) {
            // System.out.println(player.callsign+": '" +chatMessage+"'");
            // }
            if (chatMessage != null && !chatMessage.isEmpty()) {
                if (!"hello".equalsIgnoreCase(chatMessage)) {
                    String frequency = "";
                    String f = (String) positionMessage.getProperty("sim/multiplay/transmission-freq-hz");
                    if (f != null) {
                        BigDecimal bdFreq = new BigDecimal((String) positionMessage.getProperty("sim/multiplay/transmission-freq-hz"));
                        bdFreq = bdFreq.divide(new BigDecimal(1000000));
                        frequency = String.format("%1.1f", bdFreq);
                    }
                    notifyChatListeners(mppacket.getCallsign(), frequency, chatMessage);
                }
            }

            player.updatePosition(System.currentTimeMillis(), positionMessage);
        } else {
            // MP Server messages deprecated
            // ChatMessage chatMessage = (ChatMessage)mppacket.getMessage();
            // notifyChatListeners(mppacket.getCallsign(),
            // chatMessage.getMessage());
        }
    }

    public abstract String getModel();

    public abstract String getCallsign();

    public abstract Position getPosition();

    public abstract Position getOrientation();

    public abstract Position getLinearVelocity();

    private class OutgoingMessage {
        volatile long firstSendTime = 0l;
        String message;
        volatile int sendCounter;

        public OutgoingMessage(String message) {
            this.message = message;
        }
    }
}
