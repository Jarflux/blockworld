/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nathan & Ben
 */
public class BlockWorld {
    private static final Logger logger = Logger.getLogger(BlockWorld.class.getName());
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    private ChunkListener fGeneralListener = new ChunkListener() {

        public void blockAdded(Chunk chunk, Integer block, int x, int y, int z) {
            for(ChunkListener listener : fListeners) {
                listener.blockAdded(chunk, block, x, y, z);
            }
        }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            for(ChunkListener listener : fListeners) {
                listener.blockRemoved(chunk, block, x, y, z);
            }
        }
    };
    private ChunkListener fPhysicsUpdater = new ChunkListener() {

        public void blockAdded(Chunk chunk, Integer block, int x, int y, int z) {
            chunk.update();
        }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            chunk.update();
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

    public Material getBlockMat() {
        return fBlockMat;
    }
    protected BulletAppState fPhysicsState;

    public BlockWorld(Node rootNode, Material blockMat, BulletAppState physicsState) {
        this.fRootNode = rootNode;
        this.fBlockMat = blockMat;
        this.fPhysicsState = physicsState;
        fListeners.add(fPhysicsUpdater);
    }

    public Chunk getChunk(int x, int y, int z, boolean createChunk) {
        double fx = x;
        double fy = y;
        double fz = z;
        int xC = (int) Math.floor(fx / Chunk.CHUNK_SIZE);
        int yC = (int) Math.floor(fy / Chunk.CHUNK_SIZE);
        int zC = (int) Math.floor(fz / Chunk.CHUNK_SIZE);

        Chunk cnk = null;
        Map<Integer, Map<Integer, Chunk>> mYZ = null;
        Map<Integer, Chunk> mZ = null;
        mYZ = fChunks.get(xC);  // zoek chunks met juiste X
        if (mYZ != null) {
            mZ = mYZ.get(yC);  // zoek chunks met juiste X en Y 
            if (mZ != null) {
                cnk = mZ.get(zC);  // zoek chunks met juiste Z 
            }
        }
      
        if(cnk == null && createChunk){              // Chunk met juiste x, y , z bestaat niet
           cnk = new Chunk(this, fRootNode, fPhysicsState, xC*Chunk.CHUNK_SIZE, yC*Chunk.CHUNK_SIZE, zC*Chunk.CHUNK_SIZE);
           if(yC < 0 && yC > -2) {
               cnk.fillChunk();
           }
           cnk.addChunkListener(fGeneralListener);
           if(mZ == null){                            
               mZ = new HashMap<Integer, Chunk>();
           }
           mZ.put(zC, cnk);
           if(mYZ == null){
               mYZ = new HashMap<Integer, Map<Integer, Chunk>>();         
           }
           mYZ.put(yC, mZ);
           fChunks.put(xC, mYZ);
        }
        return cnk;
    }
    
    public Integer get(int x, int y, int z) {
        Chunk cnk = getChunk(x, y, z, false);
        if (cnk != null) {
            return cnk.get(x, y, z);
        }
        return null;
    }

    public void removeBlock(int x, int y, int z) {
        Chunk cnk = getChunk(x, y, z, false);
        if (cnk != null) {
            cnk.removeBlock(x, y, z);
        }
    }
    
    public boolean addBlock(Integer block, int x, int y, int z) {
        return getChunk(x, y, z, true).addBlock(block, x, y, z);
    }

    
    
}
