/*
 * DistanceBearing.java
 *
 * Created on February 28, 2008, 12:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jennings.geometrytools;

/**
 *
 * @author 345678
 */
public class DistanceBearing {
    
    /** Creates a new instance of DistanceBearing */
    public DistanceBearing() {
    }

    /**
     * Holds value of property distance.
     */
    private double distance;

    /**
     * Getter for property distance.
     * @return Value of property distance.
     */
    public double getDistance() {
        return this.distance;
    }

    /**
     * Setter for property distance.
     * @param distance New value of property distance.
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Holds value of property bearing1to2.
     */
    private double bearing1to2;

    /**
     * Getter for property bearing.
     * @return Value of property bearing.
     */
    public double getBearing1to2() {
        return this.bearing1to2;
    }

    /**
     * Setter for property bearing.
     * @param bearing New value of property bearing.
     */
    public void setBearing1to2(double bearing1to2) {
        this.bearing1to2 = bearing1to2;
    }

    /**
     * Holds value of property bearing2to1.
     */
    private double bearing2to1;

    /**
     * Getter for property bearing2to1.
     * @return Value of property bearing2to1.
     */
    public double getBearing2to1() {
        return this.bearing2to1;
    }

    /**
     * Setter for property bearing2to1.
     * @param bearing2to1 New value of property bearing2to1.
     */
    public void setBearing2to1(double bearing2to1) {
        this.bearing2to1 = bearing2to1;
    }

    /**
     * Holds value of property message.
     */
    private String message;

    /**
     * Getter for property message.
     * @return Value of property message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Setter for property message.
     * @param message New value of property message.
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
}
