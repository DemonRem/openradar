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
package de.knewcleus.openradar.view.stdroutes;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;

import de.knewcleus.openradar.view.Converter2D;
import de.knewcleus.openradar.view.map.IMapViewerAdapter;

public class StdRouteCurve extends AStdRouteElement {

    private final Point2D geoEndPoint;
    private final Point2D geoControlPoint;

    public StdRouteCurve(StdRoute route, IMapViewerAdapter mapViewAdapter, AStdRouteElement previous,
                       String start, String end, String controlPoint,String stroke, String lineWidth, String color, String arrows) {

        super(mapViewAdapter, route.getPoint(start,previous),stroke,lineWidth,arrows,color);
        this.geoEndPoint = route.getPoint(end,previous);
        this.geoControlPoint = route.getPoint(controlPoint,previous);
    }

    @Override
    public Rectangle2D paint(Graphics2D g2d, IMapViewerAdapter mapViewAdapter) {

        Point2D bowStartPoint = getDisplayPoint(geoReferencePoint);
        Point2D bowControlPoint = getDisplayPoint(geoControlPoint);
        Point2D bowEndPoint = getDisplayPoint(geoEndPoint);

        Path2D path = new Path2D.Double();
        path.append(new QuadCurve2D.Double(bowStartPoint.getX(),bowStartPoint.getY(),bowControlPoint.getX(),bowControlPoint.getY(),bowEndPoint.getX(),bowEndPoint.getY()),false);
        if(color!=null) {
            g2d.setColor(color);
        }
        Stroke origStroke = g2d.getStroke();
        if(stroke!=null) {
            g2d.setStroke(stroke);
        }
        g2d.draw(path);
        g2d.setStroke(origStroke);

        if("both".equalsIgnoreCase(arrows) || "start".equalsIgnoreCase(arrows)) {
            double heading = Converter2D.getDirection(bowControlPoint,bowStartPoint);
            this.paintArrow(g2d, bowStartPoint, heading, arrowSize, false);
        }
        if("both".equalsIgnoreCase(arrows) || "end".equalsIgnoreCase(arrows)) {
            double heading = Converter2D.getDirection(bowControlPoint,bowEndPoint);
            this.paintArrow(g2d, bowEndPoint, heading, arrowSize, true);
        }

        return path.getBounds2D();
    }

    @Override
    public Point2D getEndPoint() {
        return geoEndPoint;
    }

    @Override
    public boolean contains(Point p) {
        return false;
    }
}
