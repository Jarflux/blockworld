/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Fusion
 */
public class ChunkColumn {
    private Map<Integer, Chunk> fChunks = new HashMap<Integer, Chunk>();
    private Float[][] fHeightMap = null;
    
    public Float[][] getHeigthMap(){
        return fHeightMap;
    }
    
    public void setHeigthMap(Float[][] map){
        fHeightMap = map.clone();
    }
    
    public Chunk get(int y){
        return fChunks.get(y);
    }
    
    public void put(Chunk cnk){
        fChunks.put(cnk.getY(), cnk);
    }
    
    public Iterable<Chunk> values(){ // needed to save data to file
        return fChunks.values();
    }
}
