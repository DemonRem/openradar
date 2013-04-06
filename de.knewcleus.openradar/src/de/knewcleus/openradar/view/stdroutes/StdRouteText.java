/**
 * Copyright (C) 2013 Wolfram Wagner
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

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.knewcleus.openradar.view.map.IMapViewerAdapter;
/**
 * line
 * .begin "last"(previous end point), Navaid code or geo coordinates ("lat,lon")
 * .end (optional if angle and length is given) Navaid code or geo coordinates ("lat,lon")
 * .angle (displayed and used for calculation if .end has been omitted
 * .length (used for calculation if angle is given and .end has been omitted)
 * .beginOffset
 * .endOffset (if end is given)
 * .stroke (optional if differs from normal line) alternatives: "dashed","dots"
 * .lineWidth (optional)
 * .arrows (optional) "none","start","end","both"
 *
 *
 * @author Wolfram Wagner
 *
 */
public class StdRouteText extends AStdRouteElement {

    private Double angle;
    private final String font;
    private final Float fontSize;
    private final String text;

    public StdRouteText(StdRoute route, IMapViewerAdapter mapViewAdapter, AStdRouteElement previous,
                        String position, String angle, String font, String fontSize,
                        String color, String text) {
        super(mapViewAdapter, route.getPoint(position,previous),null,null,null,color);

        this.angle = angle !=null ? Double.parseDouble(angle) : 0;
        this.font = font!=null ? font : "Arial";
        this.fontSize = fontSize !=null ? Float.parseFloat(fontSize) : 10;
        this.text=text;
    }

    @Override
    public Rectangle2D paint(Graphics2D g2d, IMapViewerAdapter mapViewAdapter) {

        if(color!=null) {
            g2d.setColor(color);
        }
        Font origFont = g2d.getFont();
        if(font != null) {
            g2d.setFont(new Font(font,Font.PLAIN,Math.round(fontSize)));
        }

        Point2D displayPoint = getDisplayPoint(geoReferencePoint);
        AffineTransform oldTransform = g2d.getTransform();
        AffineTransform newTransform = new AffineTransform();
        newTransform.setToRotation(Math.toRadians(angle), displayPoint.getX(), displayPoint.getY());
        g2d.transform(newTransform);

        Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(text, g2d);
        g2d.drawString(text, (int)(displayPoint.getX()-bounds.getWidth()/2), (int)(displayPoint.getY()+bounds.getHeight()/2-2));

        g2d.setFont(origFont);
        g2d.setTransform(oldTransform);

        bounds.setRect(displayPoint.getX(), displayPoint.getY(),bounds.getWidth(),bounds.getHeight());
        return bounds;
    }

    @Override
    public Point2D getEndPoint() {
        return geoReferencePoint;
    }

}
