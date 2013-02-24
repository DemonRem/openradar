/**
 * Copyright (C) 2012,2013 Wolfram Wagner
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
package de.knewcleus.openradar.view.objects;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import de.knewcleus.fgfs.navdata.impl.Intersection;
import de.knewcleus.openradar.gui.setup.AirportData;
import de.knewcleus.openradar.view.map.IMapViewerAdapter;

public class FixSymbol extends AViewObject {

    private AirportData data;
    private Intersection fix;
    private int defaultMaxScale;
    private Color defaultColor;


    public FixSymbol(AirportData data, Intersection fix, int minScale, int maxScale) {
        super(Color.lightGray);
        this.data = data;

        this.fix = fix;
        this.defaultMaxScale=maxScale;
        this.defaultColor=Color.lightGray;

        setMinScalePath(minScale);
        setMaxScalePath(maxScale);
    }

    @Override
    protected void constructPath(Point2D currentDisplayPosition, Point2D newDisplayPosition, IMapViewerAdapter mapViewAdapter) {

        path = new Path2D.Double();

        Color highLightColor = data.getNavaidDB().getNavaidHighlightColor(data,fix.getIdentification());

        if(highLightColor!=null) {
            this.maxScalePath=Integer.MAX_VALUE;
            this.color = highLightColor;
        } else {
            this.maxScalePath=defaultMaxScale;
            this.color = defaultColor;
        }

        if(data.getRadarObjectFilterState("FIX") || highLightColor!=null) {
            final double x, y;
            x = newDisplayPosition.getX();
            y = newDisplayPosition.getY();
            path.moveTo(x -3 , y + 3 );
            path.lineTo(x, y);
            path.lineTo(x + 3, y + 3);
            path.lineTo(x -3 , y + 3);
        }
    }

}
