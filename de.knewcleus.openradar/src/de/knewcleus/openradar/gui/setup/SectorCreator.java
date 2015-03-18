/**
 * Copyright (C) 2012,2015 Wolfram Wagner
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
package de.knewcleus.openradar.gui.setup;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

/**
 * This class bundles the code to find an airport location and download
 * airports.
 *
 * @author Wolfram Wagner
 */
public abstract class SectorCreator {

    // "http://mapserver.flightgear.org/dlaction?layer=<layername>&xmin=<degree>&xmax=<degree>&ymin=<degree>&ymax=<degree>";
    private static String address = "http://mapserver.flightgear.org/dlsingle?";
    private static String[] layers = { "v0_landmass", "cs_urban", "cs_lake", "osm_river", "apt_airfield", "apt_runway", "apt_tarmac" };
    private static double mapWidth = 10; // degrees
    private static double mapHeight = 10; // degrees
    private static final Logger log = Logger.getLogger(SectorCreator.class);
    
    public static Point2D findLocationOf(String searchTerm) {
        ZipFile zif = null;;
        BufferedReader ir = null;
        Point2D position = null;
        
        try {
            final File inputFile = new File("data/AptNav.zip");
            zif = new ZipFile(inputFile);
            Enumeration<? extends ZipEntry> entries = zif.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipentry = entries.nextElement();
                if (zipentry.getName().equals("apt.dat")) {
                    ir = new BufferedReader(new InputStreamReader(zif.getInputStream(zipentry)));
                    break;
                }
            } 
            if(ir==null) return null;
            
            String line = ir.readLine();
            while(line!=null) {
                if(line.startsWith("1 ")) {
                    StringTokenizer st = new StringTokenizer(line," \t");
                    st.nextElement(); // code "1"
                    st.nextElement(); //
                    st.nextElement(); //
                    st.nextElement(); //
                    String airportCode = st.nextToken();
                    StringBuilder name = new StringBuilder();
                    while(st.hasMoreElements()) {
                        if(name.length()>0) name.append(" ");
                        name.append(st.nextToken());
                    }
                    if(airportCode.equalsIgnoreCase(searchTerm)) {
                        
                        // seach runway
                        line = ir.readLine();
                        while(line!=null) {
                            if(line.startsWith("100 ")) {
                                position = SetupController.getRunwayPosition(line);
                                return position;
                            }
                            else if(line.startsWith("1 ")) {
                                // next airport
                                break;
                            }
                            // next line
                            line = ir.readLine();
                        }
                    } else {
                        // airport name did not match
                        line = ir.readLine();
                    }
                }
                if(!line.startsWith("1 ")) {
                    line = ir.readLine();
                }
            }
        } catch (IOException e) {
            log.error("Error while reading xplane aptdat file!",e);
        } finally {
            if(zif!=null) {
                try {
                    zif.close();
                } catch (IOException e) { }
            }
            if(ir!=null) {
                try {
                    ir.close();
                } catch (IOException e) {}
            }
        }
        return position;
    }

    public static void downloadData(AirportData data, SetupDialog setupDialog) throws IOException {

        //data.setAirportPosition(findLocationOf(data.getAirportCode())); // find lon and lat of airport

        // 2x2 degrees
        Point2D upperLeftCorner = new Point2D.Double(data.getLon() - mapWidth / 2, data.getLat() - mapHeight / 2);
        Point2D lowerRightCorner = new Point2D.Double(data.getLon() + mapWidth / 2, data.getLat() + mapHeight / 2);

        File dir = new File("data" + File.separator + data.getAirportCode());
        if (!dir.exists())
            dir.mkdirs();

        for (int i = 0; i < layers.length; i++) {
            setupDialog.setStatus((i) * 100 / layers.length, "Downloading " + layers[i] + "...");
            downloadZip(data, layers[i], upperLeftCorner, lowerRightCorner);
            setupDialog.setStatus((i + 1) * 100 / layers.length, "..." + layers[i] + " downloaded");
        }
        setupDialog.setStatus(0, "Ready.");
        // Create sector.properties
        Properties p = new Properties();
        p.put("airportName", data.getAirportName());
        p.put("metarSource", data.getAirportCode());
        p.put("lat", Double.toString(data.getLat()));
        p.put("lon", Double.toString(data.getLon()));
       
        // 20130915 changed to use the simgear model
        // p.put("magneticDeclination", Double.toString(loadMagneticDeclination(data)));
       
        SetupController.saveSectorProperties(data.getAirportCode(), p);
    }

    private static void downloadZip(AirportData data, String layer, Point2D upperLeftCorner, Point2D lowerRightCorner) throws IOException {
        HttpURLConnection con = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {

            File targetFile = new File("data" + File.separator + data.getAirportCode() + File.separator + layer + ".zip");
            if (targetFile.exists()) {
                targetFile.delete();
            }
            String sUrl = address
                    + String.format("layer=%1$s&xmin=%2$1.2f&xmax=%3$1.2f&ymin=%4$1.2f&ymax=%5$1.2f", layer, upperLeftCorner.getX(), lowerRightCorner.getX(),
                            upperLeftCorner.getY(), lowerRightCorner.getY());
            URL url = new URL(sUrl.replaceAll(",", "."));
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            byte[] buffer = new byte[4096];
            if (responseCode == 200) {
                in = con.getInputStream();
                int byteCount = -1;
                out = new FileOutputStream(targetFile);
                while ((byteCount = in.read(buffer)) > 0) {
                    out.write(buffer, 0, byteCount);
                }
            } else {
                throw new IOException("Server responded with error " + responseCode + " " + con.getResponseMessage());
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (con != null) {
                con.disconnect();
            }
        }
    }

    /**
     *
     * URL:
     * http://www.ngdc.noaa.gov/geomag-web/calculators/calculateDeclination?
     * startYear
     * =2012&startMonth=11&startDay=15&resultFormat=csv&lon1Hemisphere=W
     * &lon1=122.220624012373
     * &lat1Hemisphere=N&lat1=37.7214006137091&browserRequest=true
     *
     * ################################################## # Declination Values
     * ################################################## # 5 Fields: # (1) Date
     * in decimal years # (2) Latitude in decimal degrees # (3) Longitude in
     * decimal degrees # (4) Declination in decimal degrees # (5) Change in
     * declination in decimal minutes / year # # Magnetic Model: IGRF #
     * Elevation: 0.00000 km ##################################################
     *
     * 2012.87158,37.72140,-122.22060,13.98877,-6.17181
     *
     * @param data
     * @return
     */
    public static double loadMagneticDeclination(AirportData data) throws IOException {
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
        Date today = new Date();
        boolean isEast = data.getLon() >= 0;
        boolean isNorth = data.getLat() >= 0;

        double declination = 0d; // fallback
//www.ngdc.noaa.gov/geomagmodels/Declination.jsp
        StringBuffer sbUrl = new StringBuffer("http://www.ngdc.noaa.gov/geomag-web/calculators/calculateDeclination?");
        sbUrl.append("startYear=").append(sdfYear.format(today));
        sbUrl.append("&startMonth=").append(sdfMonth.format(today));
        sbUrl.append("&startDay=").append(sdfDay.format(today));
        sbUrl.append("&resultFormat=csv");
        sbUrl.append("&lon1Hemisphere=").append((isEast ? "E" : "W"));
        sbUrl.append("&lon1=").append(String.format("%.12f", Math.abs(data.getLon())));
        sbUrl.append("&lat1Hemisphere=").append((isNorth ? "N" : "S"));
        sbUrl.append("&lat1=").append(String.format("%.12f", Math.abs(data.getLat())));
        sbUrl.append("&browserRequest=false");

        HttpURLConnection con = null;
        BufferedReader br = null;
        try {
            con = (HttpURLConnection) new URL(sbUrl.toString()).openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), Charset.forName("ISO-8859-1")));
                String line = br.readLine();
                while (line != null) {
                    if (!line.trim().startsWith("#") && line.trim().length() > 0) {
                        StringTokenizer st = new StringTokenizer(line, ",");
                        st.nextToken(); // date in a weird format
                        st.nextToken(); // latitude
                        st.nextToken(); // longitude
                        String sDeclination = st.nextToken(); // declination
                        declination = Double.parseDouble(sDeclination);
                        break;
                    }
                    line = br.readLine();
                }
            } else {
                throw new IOException("Server responded with error " + responseCode + " " + con.getResponseMessage());
            }
        } finally {
            if (br != null) {
                br.close();
            }
            if (con != null) {
                con.disconnect();
            }
        }
        return declination;
    }

}
