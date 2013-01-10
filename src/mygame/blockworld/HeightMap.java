/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Nathan & Ben
 */
public class HeightMap{
    private Map<Integer, Map<Integer, Serializable>> fHeightMaps = new HashMap<Integer, Map<Integer, Serializable>>();
    
    // CODE INCOMPLETE
    public float[][] getHeightMap(int x,int y){
        return (float[][]) fHeightMaps.get(x).get(y);
    } 
    // CODE INCOMPLETE
    public void setHeightMap(int x, int y, float[][] floatMap){   
        if(fHeightMaps.get(x)!= null){
            if(fHeightMaps.get(x).get(y) != null){
                fHeightMaps.get(x).put(y,floatMap);
            }
        }
    }          
}
