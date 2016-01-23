/**
 * Copyright (C) 2013,2015 Wolfram Wagner
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
 * OpenRadar ist Freie Software: Sie können es unter den Bedingungen der GNU
 * General Public License, wie von der Free Software Foundation, Version 3 der
 * Lizenz oder (nach Ihrer Option) jeder späteren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 *
 * OpenRadar wird in der Hoffnung, dass es nützlich sein wird, aber OHNE JEDE
 * GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite Gewährleistung der
 * MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK. Siehe die GNU General
 * Public License für weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.view.stdroutes;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.knewcleus.openradar.gui.setup.AirportData;
import de.knewcleus.openradar.view.Converter2D;
import de.knewcleus.openradar.view.map.IMapViewerAdapter;

public abstract class AStdRouteElement {

    protected final double magDeclination;
    protected final Point2D geoReferencePoint;
    protected final IMapViewerAdapter mapViewerAdapter;
    protected final String arrows;
    protected final int arrowSize;
    protected final StdRouteAttributes attributes;
    
    public AStdRouteElement(AirportData data, IMapViewerAdapter mapViewAdapter, Point2D geoReferencePoint, String arrows, StdRouteAttributes attributes) {
        this.mapViewerAdapter = mapViewAdapter;
        this.geoReferencePoint= geoReferencePoint;
        this.magDeclination = data.getMagneticDeclination();

        if(arrows!=null) {
            if(arrows.contains(",")) {
                int sep = arrows.indexOf(",");
                this.arrows = arrows.substring(0,sep);
                this.arrowSize = Integer.parseInt(arrows.substring(sep+1));
            } else {
                this.arrows = arrows;
                this.arrowSize = 15;
            }
        } else {
            this.arrows = null;
            this.arrowSize = 15;
        }

        this.attributes=attributes;
    }

    public Point2D getGeoReferencePoint() {
        return geoReferencePoint;
    }

    public abstract Rectangle2D paint(Graphics2D g2d, IMapViewerAdapter mapViewAdapter);

    public Rectangle2D doPaint(Graphics2D g2d, IMapViewerAdapter mapViewAdapter, Boolean selected) {
        attributes.applyAttributes(g2d, selected);
        Rectangle2D extent = paint(g2d, mapViewAdapter);
        attributes.restoreAttributes(g2d);
        return extent;
    }

    public Point2D getDisplayPoint(Point2D geoPoint) {
        Point2D logicalPoint = mapViewerAdapter.getProjection().toLogical(geoPoint);
        return mapViewerAdapter.getLogicalToDeviceTransform().transform(logicalPoint, null);
    }

    public abstract Point2D getEndPoint();

    protected void paintArrow(Graphics2D g2d, Point2D tipPoint, double heading, int size, boolean tip) {

        if(!tip) {
            // the arrow is at the end side / where the feather normally is

            // the tip point is not at the end
            //heading = heading+180;
            tipPoint = Converter2D.getMapDisplayPoint(tipPoint, heading, size);
            // and the heading to paint it is at the other side
        }

        Point2D point1 = Converter2D.getMapDisplayPoint(tipPoint, heading-180+20, size);
        Point2D point2 = Converter2D.getMapDisplayPoint(tipPoint, heading-180-20, size);

        Path2D path = new Path2D.Double();
        path.append(new Line2D.Double(tipPoint, point1),false);
        path.append(new Line2D.Double(point1, point2),true);
        path.closePath();

        g2d.fill(path);

    }

    public abstract boolean contains(Point p);

    public void mouseClicked(MouseEvent e) {}

    public boolean isClickable() {
		return false;
	}
}
