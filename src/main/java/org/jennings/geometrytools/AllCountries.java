/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jennings.geometrytools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author david
 */
public class AllCountries {
    
    
    public void createGeojson() {
        
        GreatCircle gc = new GreatCircle();
        
        try {
            BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("outfile.txt"), "UTF-8"));
            BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(new File("allCountries.txt"))));
            
            String line;
            long n = 0;
            
            JSONObject featureCollection = new JSONObject();
            featureCollection.put("type","FeatureCollection");

            JSONArray features = new JSONArray();            
            
            while ((line = fin.readLine()) != null) {
                n++;
                
                JSONObject feature = new JSONObject();
                
                String field[] = line.split("\t");
                
                JSONObject properties = new JSONObject();

                double latitude = Double.parseDouble(field[4]);
                double longitude = Double.parseDouble(field[5]);
                
                
                properties.put("geonameid", Integer.parseInt(field[0]));
                properties.put("name", field[1]);
                properties.put("asciiname", field[2]);
                properties.put("alternatenames", field[3]);
                properties.put("latitude", latitude);
                properties.put("longitude", longitude);
                
                properties.put("longitude", Double.parseDouble(field[5]));
                
                feature.put("properties", properties);
                
                JSONObject coordinates = gc.createCirle(longitude, latitude, 0.050, 30, false);
                
                JSONObject geom = new JSONObject();
                geom.put("type", "Polygon");
                geom.put("coordinates", coordinates.get("coordinates"));
                
                feature.put("geometry", geom);
                
                feature.put("type","Feature");
                
                features.put(feature);                
                               
                if (n == 10000) break;
            }
            
            featureCollection.put("features",features);
            
            //System.out.println(featureCollection.toString());
            fout.write(featureCollection.toString());
            
            System.out.println(n);
            
            fin.close();
            fout.close();
                                    
        } catch (Exception e) {
            
            e.printStackTrace();
        }
    }
    
    
    public static void main(String[] args) {
        
        AllCountries t = new AllCountries();
        
        t.createGeojson();
        
        
    }
}
