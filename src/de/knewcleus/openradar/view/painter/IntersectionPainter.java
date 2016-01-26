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
package de.knewcleus.openradar.view.painter;

import java.awt.Font;

import de.knewcleus.fgfs.navdata.model.IIntersection;
import de.knewcleus.openradar.gui.GuiMasterController;
import de.knewcleus.openradar.gui.Palette;
import de.knewcleus.openradar.view.map.IMapViewerAdapter;
import de.knewcleus.openradar.view.objects.FixName;
import de.knewcleus.openradar.view.objects.FixSymbol;

public class IntersectionPainter extends AViewObjectPainter<IIntersection> {

    public IntersectionPainter(GuiMasterController master, IMapViewerAdapter mapViewAdapter, IIntersection fix) {
        super(mapViewAdapter, fix);

            Font font = Palette.BEACON_FONT;

            FixSymbol fs = new FixSymbol(master, fix, 0,100);
            viewObjectList.add(fs);

            FixName fn = new FixName(master, fix, font, Palette.FIX_TEXT, 0 , 100);
            viewObjectList.add(fn);
    }

}
