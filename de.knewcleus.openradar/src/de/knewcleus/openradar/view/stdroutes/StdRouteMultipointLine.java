/**
 * Copyright (C) 2013 Wolfram Wagner
 *
 * This file is part of OpenRadar.
 *
 * OpenRadar is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenRadar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenRadar. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * Diese Datei ist Teil von OpenRadar.
 *
 * OpenRadar ist Freie Software: Sie k�nnen es unter den Bedingungen der GNU General Public License, wie von der Free
 * Software Foundation, Version 3 der Lizenz oder (nach Ihrer Option) jeder sp�teren ver�ffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 *
 * OpenRadar wird in der Hoffnung, dass es n�tzlich sein wird, aber OHNE JEDE GEW�HELEISTUNG, bereitgestellt; sogar ohne
 * die implizite Gew�hrleistung der MARKTF�HIGKEIT oder EIGNUNG F�R EINEN BESTIMMTEN ZWECK. Siehe die GNU General Public
 * License f�r weitere Details.
 *
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem Programm erhalten haben. Wenn nicht, siehe
 * <http://www.gnu.org/licenses/>.
 */
package de.knewcleus.openradar.view.stdroutes;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.knewcleus.openradar.view.map.IMapViewerAdapter;

/**
 *
 * @author Wolfram Wagner
 *
 */
public class StdRouteMultipointLine extends AStdRouteElement {

    private final List<Point2D> geoPoints = new ArrayList<Point2D>();
    private final Point2D geoEndPoint;
    private final boolean close;

    public StdRouteMultipointLine(StdRoute route, IMapViewerAdapter mapViewAdapter, AStdRouteElement previous, List<String> points, String close,
            String stroke, String lineWidth, String color) {
        super(mapViewAdapter, route.getPoint(points.get(0), previous), stroke, lineWidth, null, color);

        for (String point : points) {
            Point2D geoPoint = route.getPoint(point, previous);
            if (point == null) {
                throw new IllegalArgumentException("MulipointLine: Point " + point + " not found!");
            }
            geoPoints.add(geoPoint);
        }
        geoEndPoint = geoPoints.get(geoPoints.size()-1);
        this.close = !"false".equals(close);
    }

    @Override
    public Rectangle2D paint(Graphics2D g2d, IMapViewerAdapter mapViewAdapter) {

        Point2D lastDisplayPoint = null;
        if (color != null) {
            g2d.setColor(color);
        }
        Path2D path = new Path2D.Double();

        for (Point2D geoPoint : geoPoints) {
            Point2D displayPoint = getDisplayPoint(geoPoint);
            if (lastDisplayPoint != null) {
                path.append(new Line2D.Double(lastDisplayPoint, displayPoint), false);
            }
            lastDisplayPoint=displayPoint;
        }

        if (close) {
            path.closePath();
        }

        Stroke origStroke = g2d.getStroke();
        if(stroke!=null) {
            g2d.setStroke(stroke);
        }

        g2d.draw(path);

        g2d.setStroke(origStroke);

        return path.getBounds2D();
    }

    @Override
    public Point2D getEndPoint() {
        return geoEndPoint;
    }

}
