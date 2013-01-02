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
package de.knewcleus.openradar.radardata.fgmp;

import java.math.BigDecimal;

import de.knewcleus.fgfs.Units;
import de.knewcleus.fgfs.location.Ellipsoid;
import de.knewcleus.fgfs.location.GeodToCartTransformation;
import de.knewcleus.fgfs.location.Position;
import de.knewcleus.fgfs.location.Quaternion;
import de.knewcleus.fgfs.location.Vector3D;
import de.knewcleus.fgfs.multiplayer.Player;
import de.knewcleus.fgfs.multiplayer.protocol.PositionMessage;
import de.knewcleus.openradar.radardata.IRadarDataPacket;

public class TargetStatus extends Player {
	private static final GeodToCartTransformation geodToCartTransformation=new GeodToCartTransformation(Ellipsoid.WGS84);
	
	protected volatile Position geodeticPosition=new Position();
    private volatile Position lastPosition = new Position();
    private volatile Vector3D linearVelocityHF = new Vector3D();
    private volatile String frequency;
    
	protected volatile double groundSpeed=0f;
	protected volatile double trueCourse=0f;
    protected volatile double heading=0f;

	public TargetStatus(String callsign) {
		super(callsign);
	}
	
	public IRadarDataPacket getRadarDataPacket() {
		return new RadarDataPacket(this);
	}
	
	public Position getGeodeticPosition() {
		return geodeticPosition;
	}
	
	public double getGroundSpeed() {
		return groundSpeed;
	}
	
	public double getTrueCourse() {
		return trueCourse;
	}
	
	public Position getLastPostion() {
	    return lastPosition;
	}
	
	@Override
	public void updatePosition(long t, PositionMessage packet) {
		super.updatePosition(t, packet);
		lastPosition = geodeticPosition; 
		geodeticPosition=geodToCartTransformation.backward(getCartesianPosition());
		
		groundSpeed=getLinearVelocity().getLength()*Units.MPS;
		
		final Quaternion hf2gcf=Quaternion.fromLatLon(geodeticPosition.getY(), geodeticPosition.getX());
		final Quaternion gcf2hf=hf2gcf.inverse();
		final Quaternion bf2hf=gcf2hf.multiply(orientation);
		linearVelocityHF=bf2hf.transform(getLinearVelocity());
		
		trueCourse=Math.atan2(linearVelocityHF.getY(), linearVelocityHF.getX())*Units.RAD;
		
		if (trueCourse<=0*Units.DEG) {
			trueCourse+=360*Units.DEG;
		} else if (trueCourse>360*Units.DEG) {
			trueCourse-=360*Units.DEG;
		}
		
        heading = bf2hf.getAngle();
		
		String freq = (String)packet.getProperty("sim/multiplay/transmission-freq-hz");
		if(freq!=null) {
    		BigDecimal bdFreq = new BigDecimal(freq);
            bdFreq = bdFreq.divide(new BigDecimal(1000000));
            frequency = String.format("%1.1f", bdFreq);
		}
		// model may change too if you exit and return with same callsign
		this.model = packet.getModel();
	}
	
	public String getFrequency() {
	    return frequency;
	}
	/**
	 * Returns he velocity vector aligned with the global coordinates
	 * 
	 * @return
	 */
	public Vector3D getLinearVelocityGlobal() {
	    return linearVelocityHF;
	}

    public double getHeading() {
        return heading;
    }
    
    public String toString() { return callsign; }
}
