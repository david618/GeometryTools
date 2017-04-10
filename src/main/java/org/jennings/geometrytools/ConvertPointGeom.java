/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jennings.geometrytools;

import com.esri.core.geometry.OperatorExportToJson;
import com.esri.core.geometry.Point;

import com.esri.core.geometry.SpatialReference;

/**
 *
 * @author david
 */
public class ConvertPointGeom {
    
    
    public static void main(String[] args) {
        
        Point pt = new Point(-91.1, 34);
        
        
        
        SpatialReference sr = SpatialReference.create(4326);
        
        String json = OperatorExportToJson.local().execute(sr, pt);
        
        System.out.println(json);
        
        
        
    }
}
