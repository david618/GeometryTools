/*
 * GreatCircle.java
 *
 * Created on January 26, 2007, 12:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.jennings.geometrytools;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import org.apache.log4j.Category;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author jenningd
 */
public class GreatCircle {

    static final Category log = Category.getInstance(GreatCircle.class);

    private final double D2R = Math.PI / 180.0;
    private final double R2D = 180.0 / Math.PI;
    private final double RadiusEarth = 6371000;  //meters

    private final static DecimalFormat df8 = new DecimalFormat("###0.00000000");
    private final static DecimalFormat df5 = new DecimalFormat("###0.00000");
    private final static DecimalFormat df3 = new DecimalFormat("###0.000");

    /**
     * Creates a new instance of GreatCircle
     */
    public GreatCircle() {

    }

    /**
     *
     * Turns the stack trace into a string.
     *
     * @param t An Exeception Type
     * @return String
     */
    private String getStackTrace(Throwable t) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public DistanceBearing getDistanceBearing(double lon1, double lat1, double lon2, double lat2) {

        DistanceBearing distB = new DistanceBearing();

        CoordinateChecker cc = new CoordinateChecker();

        String strErrorMsg = "OK";

        /*
         * Returns the distance from point 1 to point 2 in km 
         *
         */
        double gcDist = 0.0;
        double bearing = 0.0;

        try {

            int intErrorNum = 0;
            if ((intErrorNum = cc.isValidLat(lat1)) < 0) {
                // invalid Lat1                 
                strErrorMsg = cc.getErrorMessage(intErrorNum);
            } else if ((intErrorNum = cc.isValidLat(lat2)) < 0) {
                // invalid lat2                 
                strErrorMsg = cc.getErrorMessage(intErrorNum);
            } else if ((intErrorNum = cc.isValidLon(lon1)) < 0) {
                // invalid lon1
                strErrorMsg = cc.getErrorMessage(intErrorNum);
            } else if ((intErrorNum = cc.isValidLon(lon2)) < 0) {
                // invalid lon2
                strErrorMsg = cc.getErrorMessage(intErrorNum);
            } else {

                // Allow for lon values 180 to 360 (adjust them to -180 to 0)
                if (lon1 > 180.0 && lon1 <= 360) {
                    lon1 = lon1 - 360;
                }
                if (lon2 > 180.0 && lon2 <= 360) {
                    lon2 = lon2 - 360;
                }

                double lon1R = lon1 * D2R;
                double lat1R = lat1 * D2R;
                double lon2R = lon2 * D2R;
                double lat2R = lat2 * D2R;

                if (lat1 == 90 || lat1 == -90) {
                    double l = 90 - lat2;
                    gcDist = RadiusEarth * l * D2R / 1000.0;
                    if (lat1 == -90) {
                        gcDist = Math.PI * RadiusEarth / 1000 - gcDist;
                    }

                } else {

                    boolean useLawCosines = true;

                    if (useLawCosines) {
                        double lambda = Math.abs(lon2R - lon1R);

                        double x1 = Math.cos(lat2R) * Math.sin(lambda);

                        double x2 = Math.cos(lat1R) * Math.sin(lat2R)
                                - Math.sin(lat1R) * Math.cos(lat2R) * Math.cos(lambda);

                        double x3 = Math.sin(lat1R) * Math.sin(lat2R)
                                + Math.cos(lat1R) * Math.cos(lat2R) * Math.cos(lambda);

                        double x4 = Math.sqrt(x1 * x1 + x2 * x2);

                        double sigma = Math.atan2(x4, x3);

                        gcDist = sigma * RadiusEarth / 1000.0;

                        double y1 = Math.sin(lon2R - lon1R) * Math.cos(lat2R);

                        double y2 = Math.cos(lat1R) * Math.sin(lat2R)
                                - Math.sin(lat1R) * Math.cos(lat2R) * Math.cos(lon2R - lon1R);

                        double y3 = Math.atan2(y1, y2);

                        bearing = (y3 * R2D) % 360;

                    } else {
                        // Haversine formula 
                        double dLat = lat2R - lat1R;
                        double dLon = lon2R - lon1R;
                        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                                + Math.cos(lat1R) * Math.cos(lat2R)
                                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

                        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

                        gcDist = RadiusEarth / 1000.0 * c;

                        double y = Math.sin(dLon) * Math.cos(lat2R);
                        double x = Math.cos(lat1R) * Math.sin(lat2R) - Math.sin(lat1R) * Math.cos(lat2R) * Math.cos(dLon);

                        bearing = (Math.atan2(y, x) * R2D) % 360;

                    }

                }
            }
        } catch (Exception e) {
            gcDist = -1000;
            bearing = -1000;
            strErrorMsg = "Unexpected Java Error.  Contact the provider if this error persists.";
        }

        distB.setDistance(new Double(df3.format(gcDist)).doubleValue());

        distB.setBearing1to2(new Double(df5.format(bearing)).doubleValue());
        // return angle on Great Circle is just 180 degrees in the other direction
        distB.setBearing2to1(new Double(df5.format((bearing + 180) % 360)).doubleValue());

        if (gcDist < -1000) {
            strErrorMsg = cc.getErrorMessage(gcDist);
        }

        distB.setMessage(strErrorMsg);

        return distB;
    }

    /*
     * Finds a new coordinate pair given a current point and the bearing from due north.
     * 
     *  Program assumes user inputs a valid CoordPair (-90 < lat < 90 and -180 < lon < 180)
     *  a valid bearing (-180 < bearning < 180) and reasonable distance in km (<50,000 km).
     *
     *  14 May 2007:  This code needs some work.  I'm getting wrong answers in some cases
     *
     */
    public NewCoordinate getNewCoordPair(double lon, double lat, double distance, double bearing) {

        NewCoordinate nc = new NewCoordinate();
        CoordinateChecker cc = new CoordinateChecker();
        String strErrorMsg = "OK";

        double lat1 = lat;
        double lon1 = lon;
        double lat2 = 0.0;
        double lon2 = 0.0;
        int intErrorNum = 0;

        boolean bln360 = false;

        try {

            if (distance * 1000 > RadiusEarth * Math.PI) {
                // Greater than half way around the world politely say no
                strErrorMsg = "Distance specified " + distance + " must be less than half the the circumference of the Earth (" + RadiusEarth * Math.PI + ")";
            } else if (bearing < -180 || bearing > 180) {
                strErrorMsg = "Bearing must be in the range of -180 to 180";

            } else if ((intErrorNum = cc.isValidLat(lat1)) < 0) {
                // invalid Lat1                 
                strErrorMsg = cc.getErrorMessage(intErrorNum);
            } else if ((intErrorNum = cc.isValidLon(lon1)) < 0) {
                // invalid lon1
                strErrorMsg = cc.getErrorMessage(intErrorNum);
            } else {

                // Allow for lon values 180 to 360 (adjust them to -180 to 0)
                double lonDD = lon1;
                if (lonDD > 180.0 && lonDD <= 360) {
                    lonDD = lonDD - 360;
                    lon1 = lonDD;
                    bln360 = true;
                }

                double alpha;
                double l;
                double k;
                double gamma;
                double phi;
                double theta;
                double hdng2;

                double hdng = bearing;

                if (hdng < 0) {
                    hdng = hdng + 360;
                }

                // Round the input            
                BigDecimal bd = new BigDecimal(hdng);
                bd = bd.setScale(6, BigDecimal.ROUND_HALF_UP);
                hdng = bd.doubleValue();

                double dist = distance * 1000;

                if (lat1 == 90 || lat1 == -90) {
                    // hdng doesn't make a lot of since at the poles assume this is just the lon
                    lon2 = hdng;
                    alpha = dist / RadiusEarth;
                    if (lat1 == 90) {
                        lat2 = 90 - alpha * R2D;
                    } else {
                        lat2 = -90 + alpha * R2D;
                    }

                } else if (hdng == 0 || hdng == 360) {
                    // going due north within some rounded number
                    alpha = dist / RadiusEarth;
                    lat2 = lat1 + alpha * R2D;
                    lon2 = lon1;
                } else if (hdng == 180) {
                    // going due south witin some rounded number
                    alpha = dist / RadiusEarth;
                    lat2 = lat1 - alpha * R2D;
                    lon2 = lon1;
                } else if (hdng == 90) {
                    lat2 = lat1;
                    l = 90 - lat1;
                    alpha = dist / RadiusEarth / Math.sin(l * D2R);
                    //phi = Math.asin(Math.sin(alpha)/ Math.sin(l*D2R));                 
                    lon2 = lon1 + alpha * R2D;
                } else if (hdng == 270) {
                    lat2 = lat1;
                    l = 90 - lat1;
                    alpha = dist / RadiusEarth / Math.sin(l * D2R);
                    //phi = Math.asin(Math.sin(alpha)/ Math.sin(l*D2R));                       
                    lon2 = lon1 - alpha * R2D;
                } else if (hdng > 0 && hdng < 180) {
                    l = 90 - lat1;
                    alpha = dist / RadiusEarth;
                    k = Math.acos(Math.cos(alpha) * Math.cos(l * D2R)
                            + Math.sin(alpha) * Math.sin(l * D2R) * Math.cos(hdng * D2R));
                    lat2 = 90 - k * R2D;
                    //phi = Math.asin(Math.sin(hdng*D2R) * Math.sin(alpha)/ Math.sin(k)); 
                    phi = Math.acos((Math.cos(alpha) - Math.cos(k) * Math.cos(l * D2R))
                            / (Math.sin(k) * Math.sin(l * D2R)));
                    lon2 = lon1 + phi * R2D;
                    theta = Math.sin(phi) * Math.sin(l * D2R) / Math.sin(alpha);
                    hdng2 = 180 - theta * R2D;
                } else if (hdng > 180 && hdng < 360) {
                    gamma = 360 - hdng;
                    l = 90 - lat1;
                    alpha = dist / RadiusEarth;
                    k = Math.acos(Math.cos(alpha) * Math.cos(l * D2R)
                            + Math.sin(alpha) * Math.sin(l * D2R) * Math.cos(gamma * D2R));
                    lat2 = 90 - k * R2D;
                    //phi = Math.asin(Math.sin(gamma*D2R) * Math.sin(alpha)/ Math.sin(k));                       
                    phi = Math.acos((Math.cos(alpha) - Math.cos(k) * Math.cos(l * D2R))
                            / (Math.sin(k) * Math.sin(l * D2R)));
                    lon2 = lon1 - phi * R2D;
                    theta = Math.sin(phi) * Math.sin(l * D2R) / Math.sin(alpha);
                    hdng2 = 180 - theta * R2D;
                }

                int decimalPlaces = 12;
                bd = new BigDecimal(lat2);
                bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
                lat2 = bd.doubleValue();

                bd = new BigDecimal(lon2);
                bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP);
                lon2 = bd.doubleValue();

                if (lat2 > 90) {
                    lat2 = 180 - lat2;
                    lon2 = (lon2 + 180) % 360;
                }

                if (lon2 > 180) {
                    lon2 = lon2 - 360;
                }

                if (lat2 < -90) {
                    lat2 = 180 - lat2;
                    lon2 = (lon2 + 180) % 360;
                }
                if (lon2 < -180) {
                    lon2 = lon2 + 360;
                }

                // adjust the lon back to 360 scale if input was like that
                if (bln360) {
                    if (lon2 < 0) {
                        lon2 = lon2 + 360;
                    }
                }

            }

        } catch (Exception e) {
            lon2 = -1000;
            lat2 = -1000;
            strErrorMsg = "Unexpected Java Error.  Contact the provider if this error persists.";
        }

        //nc.setLat(new Double(df8.format(lat2)).doubleValue());
        //nc.setLon(new Double(df8.format(lon2)).doubleValue());
        nc.setLat(lat2);
        nc.setLon(lon2);
        nc.setMessage(strErrorMsg);

        return nc;
    }

    private void runRandomWorldTest() {
        // Begin Test random world
        // Test the code with random coordinates around the world
        Random rnd = new Random();

        double lat1;
        double lon1;
        double lat2;
        double lon2;
        String strLine;

        try {
            FileOutputStream fos = new FileOutputStream("C:\\temp\\GreatCircleRunRandomWorldTest.txt");

            OutputStreamWriter osw = new OutputStreamWriter(fos);

            int i = 0;

            while (i < 30000) {
                lat1 = (1000.0 * rnd.nextGaussian()) % 90;
                lon1 = (1000.0 * rnd.nextGaussian()) % 180;
                lat2 = (1000.0 * rnd.nextGaussian()) % 90;
                lon2 = (1000.0 * rnd.nextGaussian()) % 180;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                double dist = distB.getDistance();
                double head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }

            osw.close();
            fos.close();

        } catch (Exception e) {
            log.error(getStackTrace(e));
        }
        // End Test random world

    }

    private void runRandomDistBearTest() {
        // Begin Test Random Distances and Bearings
        // Test the code with random coordinates around the world
        Random rnd = new Random();

        double lat1;
        double lon1;
        double lat2;
        double lon2;
        String strLine;

        try {
            FileOutputStream fos = new FileOutputStream("C:\\temp\\GreatCircleRunRandomDistBearTest.txt");

            OutputStreamWriter osw = new OutputStreamWriter(fos);

            int i = 0;

            while (i < 30000) {
                lat1 = (1000.0 * rnd.nextGaussian()) % 90;
                lon1 = (1000.0 * rnd.nextGaussian()) % 180;

                double dist = Math.abs((40000.0 * rnd.nextGaussian()) % 20000.0);
                double head = (1000.0 * rnd.nextGaussian()) % 180.0;

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);

                lat2 = nc.getLat();
                lon2 = nc.getLon();

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                double dist2 = distB.getDistance();
                double head2 = distB.getBearing1to2();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist2) + ":";
                strLine += String.valueOf(head2);

                osw.write(strLine + "\n");

                i++;
            }

            osw.close();
            fos.close();

        } catch (Exception e) {
            log.error(getStackTrace(e));
        }
        // End Test random distances and bearings        
    }

    private void runRandomTroubleSpotTest() {
        // Test the code with random coordinates around latitude 90, -90, and lon 180
        // Begin Test trouble spots
        Random rnd = new Random();

        double lat1;
        double lon1;
        double lat2;
        double lon2;
        String strLine;

        try {
            FileOutputStream fos = new FileOutputStream("C:\\temp\\GreatCircleRunRandomTroubleSpotTest.txt");

            OutputStreamWriter osw = new OutputStreamWriter(fos);

            double dist = 0.0;
            double head = 0.0;

            // 1000 points staring near 90N
            int i = 0;
            while (i < 1000) {
                lat1 = 89.0 + rnd.nextInt(1000) / 1000.0;
                lon1 = (1000.0 * rnd.nextGaussian()) % 180;
                lat2 = (1000.0 * rnd.nextGaussian()) % 90;
                lon2 = (1000.0 * rnd.nextGaussian()) % 180;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                dist = distB.getDistance();
                head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);

                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }
            // 1000 points staring near 90S
            i = 0;
            while (i < 1000) {
                lat1 = -89.0 - rnd.nextInt(1000) / 1000.0;
                lon1 = (1000.0 * rnd.nextGaussian()) % 180;
                lat2 = (1000.0 * rnd.nextGaussian()) % 90;
                lon2 = (1000.0 * rnd.nextGaussian()) % 180;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                dist = distB.getDistance();
                head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }

            // 1000 points ending near 90N
            i = 0;
            while (i < 1000) {
                lat1 = (1000.0 * rnd.nextGaussian()) % 90;
                lon1 = (1000.0 * rnd.nextGaussian()) % 180;
                lat2 = 89.0 + rnd.nextInt(1000) / 1000.0;
                lon2 = (1000.0 * rnd.nextGaussian()) % 180;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                dist = distB.getDistance();
                head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }

            // 1000 points ending near 90S
            i = 0;
            while (i < 1000) {
                lat1 = (1000.0 * rnd.nextGaussian()) % 90;
                lon1 = (1000.0 * rnd.nextGaussian()) % 180;
                lat2 = -89.0 - rnd.nextInt(1000) / 1000.0;
                lon2 = (1000.0 * rnd.nextGaussian()) % 180;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                dist = distB.getDistance();
                head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }

            // 1000 points crossing near 180 crossing to near -180
            i = 0;
            while (i < 1000) {
                lat1 = (1000.0 * rnd.nextGaussian()) % 90;
                lon1 = 170.0 + 10 * rnd.nextInt(1000) / 1000.0;
                lat2 = (1000.0 * rnd.nextGaussian()) % 90;
                lon2 = -170.0 - 10 * rnd.nextInt(1000) / 1000.0;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                dist = distB.getDistance();
                head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }
            // 1000 points crossing near 180 crossing to near -180
            i = 0;
            while (i < 1000) {
                lat1 = (1000.0 * rnd.nextGaussian()) % 90;
                lon1 = -170.0 - 10 * rnd.nextInt(1000) / 1000.0;
                lat2 = (1000.0 * rnd.nextGaussian()) % 90;
                lon2 = 170.0 + 10 * rnd.nextInt(1000) / 1000.0;

                DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
                dist = distB.getDistance();
                head = distB.getBearing1to2();

                NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
                double newLon = nc.getLon();
                double newLat = nc.getLat();

                strLine = String.valueOf(lon1) + ":";
                strLine += String.valueOf(lat1) + ":";
                strLine += String.valueOf(lon2) + ":";
                strLine += String.valueOf(lat2) + ":";
                strLine += String.valueOf(dist) + ":";
                strLine += String.valueOf(head) + ":";
                strLine += String.valueOf(newLon) + ":";
                strLine += String.valueOf(newLat);

                osw.write(strLine + "\n");

                i++;
            }

            osw.close();
            fos.close();

        } catch (Exception e) {
            log.error(getStackTrace(e));
        }
        // End of Test Trouble spots

    }

    private void runSinglePointTest() {
        // Test the code with one point

        double lon1 = 137.6389;
        double lat1 = -73.8183;
        double lon2 = -39.8347;
        double lat2 = -45.0278;

        System.out.println("Point 1 = (" + lon1 + "," + lat1 + ")");
        System.out.println("Point 2 = (" + lon2 + "," + lat2 + ")");

        // Find the distance and bearing
        DistanceBearing distB = getDistanceBearing(lon1, lat1, lon2, lat2);
        double dist = distB.getDistance();
        double head = distB.getBearing1to2();

        System.out.println("distance = " + dist);
        System.out.println("heading = " + head);

        //dist = 1000000;
        //head = 128343.234;
        // Given the first point and distance and bearing find 2nd point
        NewCoordinate nc = getNewCoordPair(lon1, lat1, dist, head);
        double newLon = nc.getLon();
        double newLat = nc.getLat();

        System.out.println("New Point = (" + newLon + "," + newLat + ")");

    }

//    public String createCirle(double clon, double clat, double radius, Integer numPoints, boolean esriGeom) {
//        // Return either Esri Geometry or GeoJSON as String
//        
//        
//        StringBuffer geomString = new StringBuffer("");
//        
//        if (numPoints == null) {
//            numPoints = 20;
//        }
//
//               
//        if (esriGeom) {
//            
//            geomString.append("{\"rings\": [[");
//            double d = 360.0 / (numPoints - 1);
//            int i = 0;
//            double v = 0.0;
//            NewCoordinate nc1 = new NewCoordinate();
//            while (i < numPoints - 1) {
//                v = i * d - 180.0;            
//                i++;
//                //System.out.println(i + ":" + v);
//                NewCoordinate nc = getNewCoordPair(clon, clat, radius, v);
//                if (i == 1) nc1 = nc; 
//                geomString.append("[" + nc.getLon() + ", " + nc.getLat() + "], ");                                                              
//            }        
//            geomString.append("[" + nc1.getLon() + ", " + nc1.getLat() + "]]]}");
//                                   
//        } else {
//            
//        }
//        
//        
//        
//
//        return geomString.toString();
//
//    }
    public JSONObject createCirle(double clon, double clat, double radius, Integer numPoints, boolean esriGeom) {
        // Return either Esri Geometry or GeoJSON as String

        JSONObject geom = new JSONObject();

        if (numPoints == null) {
            numPoints = 20;
        }

        if (esriGeom) {

            double d = 360.0 / (numPoints - 1);
            int i = 0;
            double v = 0.0;
            NewCoordinate nc1 = new NewCoordinate();
            JSONArray exteriorRing = new JSONArray();
            while (i < numPoints - 1) {
                v = i * d - 180.0;
                i++;

                NewCoordinate nc = getNewCoordPair(clon, clat, radius, v);
                if (i == 1) {
                    nc1 = nc;
                }
                JSONArray coord = new JSONArray("[" + nc.getLon() + ", " + nc.getLat() + "], ");

                exteriorRing.put(coord);

            }

            JSONArray coord = new JSONArray("[" + nc1.getLon() + ", " + nc1.getLat() + "], ");
            exteriorRing.put(coord);            
            
            JSONArray poly = new JSONArray();
            poly.put(exteriorRing);

            geom.put("rings", poly);

        } else {

            double d = 360.0 / (numPoints - 1);
            int i = 0;
            double v = 0.0;
            NewCoordinate nc1 = new NewCoordinate();
            JSONArray exteriorRing = new JSONArray();
            while (i < numPoints - 1) {
                v = 180.0 - i * d;  // counterclockwise
                i++;

                NewCoordinate nc = getNewCoordPair(clon, clat, radius, v);
                if (i == 1) {
                    nc1 = nc;
                }
                JSONArray coord = new JSONArray("[" + nc.getLon() + ", " + nc.getLat() + "], ");

                exteriorRing.put(coord);

            }
            
            JSONArray coord = new JSONArray("[" + nc1.getLon() + ", " + nc1.getLat() + "], ");
            exteriorRing.put(coord);

            JSONArray poly = new JSONArray();
            poly.put(exteriorRing);

            geom.put("coordinates", poly);            
            
        }

        return geom;

    }

    public String generateRandomWords(int numchars) {
        Random random = new Random();
        char[] word = new char[numchars];
        for (int j = 0; j < word.length; j++) {
            word[j] = (char) ('a' + random.nextInt(26));
        }
        return new String(word);
    }

    public static void main(String[] args) {
        GreatCircle gc = new GreatCircle();
//        gc.runSinglePointTest();
//        gc.runRandomWorldTest();
//        gc.runRandomTroubleSpotTest();
//        gc.runRandomDistBearTest();

//        NewCoordinate nc = gc.getNewCoordPair(23, 0, 0.001132, 90);
//        double lon = nc.getLon();
//        double lat = nc.getLat();
//
//        System.out.format("%10.10f\n", lon);
//        System.out.format("%10.10f\n", lat);
//
        Random rnd = new Random();

        double lonmax = 179;
        double lonmin = -179;
        double latmax = 85;
        double latmin = -85;

        // In Kilometers
        double maxsize = 30;  
        double minsize = 20; 

        boolean esriGeom = true;

        if (esriGeom) {
            JSONArray features = new JSONArray();

            for (int i = 1; i <= 10; i++) {

                double rndlon = rnd.nextDouble() * (lonmax - lonmin) + lonmin;
                double rndlat = rnd.nextDouble() * (latmax - latmin) + latmin;
                double rndsize = rnd.nextDouble() * (maxsize - minsize) + minsize;

                JSONObject geom = gc.createCirle(rndlon, rndlat, rndsize, 200, true);

                JSONObject properties = new JSONObject();

                System.out.println("");

                properties.put("fid", i);
                properties.put("longitude", rndlon);
                properties.put("latitude", rndlat);
                properties.put("size", rndsize);
                properties.put("rndfield1", gc.generateRandomWords(8));
                properties.put("rndfield2", gc.generateRandomWords(8));
                properties.put("rndfield3", gc.generateRandomWords(8));
                properties.put("rndfield4", gc.generateRandomWords(8));

                JSONObject feature = new JSONObject();
                feature.put("feature", properties);

                features.put(feature);

                properties.put("geometry", geom);

            }

            JSONObject json = new JSONObject();
            json.put("features", features);

            System.out.println(json.toString());

            try {
            
                FileWriter fw = new FileWriter("sample.json");
                
                fw.write(json.toString() + "\n");
                
                fw.close();
               
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            
        } else {
            // GeoJSON
            
            JSONObject featureCollection = new JSONObject();
            featureCollection.put("type","FeatureCollection");

            JSONArray features = new JSONArray();
            
            for (int i = 1; i <= 10; i++) {

                JSONObject feature = new JSONObject();
                
                double rndlon = rnd.nextDouble() * (lonmax - lonmin) + lonmin;
                double rndlat = rnd.nextDouble() * (latmax - latmin) + latmin;
                double rndsize = rnd.nextDouble() * (maxsize - minsize) + minsize;

                JSONObject properties = new JSONObject();
                                        
                properties.put("fid", i);
                properties.put("longitude", rndlon);
                properties.put("latitude", rndlat);
                properties.put("size", rndsize);
                properties.put("rndfield1", gc.generateRandomWords(8));
                properties.put("rndfield2", gc.generateRandomWords(8));
                properties.put("rndfield3", gc.generateRandomWords(8));
                properties.put("rndfield4", gc.generateRandomWords(8));

                feature.put("properties", properties);
                
                JSONObject coordinates = gc.createCirle(rndlon, rndlat, rndsize, 20, false);
                
                JSONObject geom = new JSONObject();
                geom.put("type", "Polygon");
                geom.put("coordinates", coordinates.get("coordinates"));
                
                feature.put("geometry", geom);
                
                feature.put("type","Feature");
                
                features.put(feature);
                
                
            }
            
            featureCollection.put("features",features);
                        
            
            System.out.println(featureCollection.toString());
            
            try {
            
                FileWriter fw = new FileWriter("sample.gjson");
                
                fw.write(featureCollection.toString() + "\n");
                
                fw.close();
               
            } catch (Exception e) {
                e.printStackTrace();
            }


            
            
        }

    }

}
