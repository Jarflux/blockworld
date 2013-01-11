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
public class HeightMap {

    private Map<Integer, Map<Integer, Serializable>> fHeightMaps = new HashMap<Integer, Map<Integer, Serializable>>();

    public Float[][] getHeightMap(int x, int y) {
        if (fHeightMaps.get(x) != null) {
            if (fHeightMaps.get(x).get(y) != null) {
                return (Float[][]) fHeightMaps.get(x).get(y);
            }
        }
        return null;
    }

    public void setHeightMap(int x, int y, Float[][] floatMap) {
        if (getHeightMap(x, y) == null) {
            if (fHeightMaps.get(x) != null) {  
                fHeightMaps.get(x).put(y, floatMap);
            } else {
                Map<Integer, Serializable> yMap = new HashMap<Integer, Serializable>();
                yMap.put(y, floatMap);
                fHeightMaps.put(x, yMap);
            }
        }
    }
}
