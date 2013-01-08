/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nathan
 */
public class BlockWorld {
    
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    private ChunkListener fGeneralListener = new ChunkListener() {

        public void blockAdded(Geometry block, int x, int y, int z) {
            for(ChunkListener listener : fListeners) {
                listener.blockAdded(block, x, y, z);
            }
        }

        public void blockRemoved(Geometry block, int x, int y, int z) {
            for(ChunkListener listener : fListeners) {
                listener.blockRemoved(block, x, y, z);
            }
        }
    };
    
    public void addChunkListener(ChunkListener listener) {
        fListeners.add(listener);
    }
    
    public void removeChunkListener(ChunkListener listener) {
        fListeners.remove(listener);
    }
    
    protected Map<Integer, Map<Integer, Map<Integer, Chunk>>> fChunks = new HashMap<Integer, Map<Integer, Map<Integer, Chunk>>>();
    protected Node fRootNode;
    protected Material fBlockMat;

    public BlockWorld(Node fRootNode, Material fBlockMat) {
        this.fRootNode = fRootNode;
        this.fBlockMat = fBlockMat;
    }
    
    private Geometry createBlock(int x, int y, int z) {
        System.out.println("Created block at: " + x + "," + y + "," + z);
        Box b = new Box(new Vector3f(x, y, z), .5f, .5f, .5f);
        Geometry geom = new Geometry("Box", b);
        geom.setMaterial(fBlockMat);
        return geom;
    }
    
    private void fillChunk(Chunk cnk, int xC, int yC, int zC) {
        for(int x = xC; x < xC + Chunk.CHUNK_SIZE; x++) {
            for(int y = yC; y < yC + Chunk.CHUNK_SIZE; y++) {
                for(int z = zC; z < zC + Chunk.CHUNK_SIZE; z++) {
                    Geometry block = createBlock(x, y, z);
                    cnk.addBlock(block, x, y, z);
                }
            }
        }
    }
    
    public Chunk getChunk(int x, int y, int z, boolean createChunk) {
        int xC = x / Chunk.CHUNK_SIZE;
        int yC = y / Chunk.CHUNK_SIZE;
        int zC = z / Chunk.CHUNK_SIZE;
        Map<Integer, Map<Integer, Chunk>> mYZ = fChunks.get(xC);
        if(mYZ == null) {
            mYZ = new HashMap<Integer, Map<Integer, Chunk>>();
            fChunks.put(xC, mYZ);
        }
        Map<Integer, Chunk> mZ = mYZ.get(yC);
        if(mZ == null) {
            mZ = new HashMap<Integer, Chunk>();
            mYZ.put(yC, mZ);
        }
        Chunk cnk = mZ.get(zC);
        if(cnk == null && createChunk) {
            cnk = new Chunk(fRootNode, xC*Chunk.CHUNK_SIZE, yC*Chunk.CHUNK_SIZE, zC*Chunk.CHUNK_SIZE);
            cnk.addChunkListener(fGeneralListener);
            //if(zC < 0) {
                fillChunk(cnk, xC*Chunk.CHUNK_SIZE, yC*Chunk.CHUNK_SIZE, zC*Chunk.CHUNK_SIZE);
            //}
            mZ.put(zC, cnk);
        }
        return cnk;
    }
    
    public Geometry get(int x, int y, int z) {
        Chunk cnk = getChunk(x, y, z, false);
        if(cnk != null) {
            return cnk.get(x, y, z);
        }
        return null;
    }
    
    public void removeBlock(int x, int y, int z) {
        getChunk(x, y, z, true).removeBlock(x, y, z);
    }
    
    public boolean addBlock(Geometry block, int x, int y, int z) {
        return getChunk(x, y, z, true).addBlock(block, x, y, z);
    }
    
}
