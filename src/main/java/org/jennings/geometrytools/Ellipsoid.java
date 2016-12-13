
/*
 * Ellipsoid.java
 *
 * Created on February 28, 2008, 11:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jennings.geometrytools;

/**
 *
 * @author 345678
 */
public class Ellipsoid {


    
    /** Creates a new instance of Ellipsoid */
    public Ellipsoid() {
    }

    public Ellipsoid(String name, double a, double b, double f) {
        this.name = name;
        this.a = a;
        this.b = b;
        this.f = f;
    }
    
    /**
     * Holds value of property name.
     */
    private String name;

    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Holds value of property a.
     */
    private double a;

    /**
     * Getter for property a.
     * @return Value of property a.
     */
    public double getA() {
        return this.a;
    }

    /**
     * Setter for property a.
     * @param a New value of property a.
     */
    public void setA(double a) {
        this.a = a;
    }

    /**
     * Holds value of property b.
     */
    private double b;

    /**
     * Getter for property b.
     * @return Value of property b.
     */
    public double getB() {
        return this.b;
    }

    /**
     * Setter for property b.
     * @param b New value of property b.
     */
    public void setB(double b) {
        this.b = b;
    }

    /**
     * Holds value of property f.
     */
    private double f;

    /**
     * Getter for property f.
     * @return Value of property f.
     */
    public double getF() {
        return this.f;
    }

    /**
     * Setter for property f.
     * @param f New value of property f.
     */
    public void setF(double f) {
        this.f = f;
    }
    
}
