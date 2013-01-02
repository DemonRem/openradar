/**
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
package de.knewcleus.openradar.view;

import de.knewcleus.openradar.gui.contacts.GuiRadarContact.State;
import de.knewcleus.openradar.rpvd.RadarTargetView;

/**
 * This class exists to paint radar contacts in a sequence, 
 * 
 * (1) at first the uncontrolled
 * (2) the controlled
 * (3) the important
 * (4) the selected 
 * 
 * @author Wolfram Wagner
 *
 */
public class LayeredRadarContactView extends LayeredView {

    
    public LayeredRadarContactView( IViewerAdapter mapViewAdapter ) {
        super(mapViewAdapter);
    }
    
    @Override
    public synchronized void traverse(IViewVisitor visitor) {
        RadarTargetView selectedView = null;
        // paint the expired
        for (IView view: views) {
            if(view instanceof RadarTargetView) {
                RadarTargetView radarView = (RadarTargetView)view;
                if(radarView.getTrackDisplayState().getGuiContact().isExpired()) {
                    view.accept(visitor);
                }
            }
        }
        // paint the inactive
        for (IView view: views) {
            if(view instanceof RadarTargetView) {
                RadarTargetView radarView = (RadarTargetView)view;
                if(!radarView.getTrackDisplayState().getGuiContact().isActive()) {
                    view.accept(visitor);
                }
            }
        }
        // paint the uncontrolled
        for (IView view: views) {
            if(view instanceof RadarTargetView) {
                RadarTargetView radarView = (RadarTargetView)view;
                if(radarView.getTrackDisplayState().getGuiContact().isSelected()) {
                    selectedView = radarView;
                }
                if(radarView.getTrackDisplayState().getGuiContact().getState()==State.UNCONTROLLED 
                        && !radarView.getTrackDisplayState().getGuiContact().isSelected()
                        && radarView.getTrackDisplayState().getGuiContact().isActive()) {
                    view.accept(visitor);
                }
            }
        }
        // paint the controlled
        for (IView view: views) {
            if(view instanceof RadarTargetView) {
                RadarTargetView radarView = (RadarTargetView)view;
                if(radarView.getTrackDisplayState().getGuiContact().getState()==State.CONTROLLED 
                        && !radarView.getTrackDisplayState().getGuiContact().isSelected()
                        && radarView.getTrackDisplayState().getGuiContact().isActive()) {
                    view.accept(visitor);
                }
            }
        }
        // paint the important
        for (IView view: views) {
            if(view instanceof RadarTargetView) {
                RadarTargetView radarView = (RadarTargetView)view;
                if(radarView.getTrackDisplayState().getGuiContact().getState()==State.IMPORTANT && 
                        !radarView.getTrackDisplayState().getGuiContact().isSelected()
                        && radarView.getTrackDisplayState().getGuiContact().isActive()) { 
                    view.accept(visitor);
                }
            }
        }
        // paint the selected
        if(selectedView!=null) {
            selectedView.accept(visitor);
        }

    }
}
