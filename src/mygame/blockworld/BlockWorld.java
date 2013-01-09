/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
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
        
        public void blockAdded(Chunk chunk, Geometry block, int x, int y, int z) {
            for (ChunkListener listener : fListeners) {
                listener.blockAdded(chunk, block, x, y, z);
            }
        }

        public void blockRemoved(Chunk chunk, Geometry block, int x, int y, int z) {
            for (ChunkListener listener : fListeners) {
                listener.blockRemoved(chunk, block, x, y, z);
            }
        }
    };
    private ChunkListener fPhysicsUpdater = new ChunkListener() {
        public void blockAdded(Chunk chunk, Geometry block, int x, int y, int z) {
            chunk.updatePhysicsMesh();
        }

        public void blockRemoved(Chunk chunk, Geometry block, int x, int y, int z) {
            chunk.updatePhysicsMesh();
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
    protected BulletAppState fPhysicsState;

    public BlockWorld(Node rootNode, Material blockMat, BulletAppState physicsState) {
        this.fRootNode = rootNode;
        this.fBlockMat = blockMat;
        this.fPhysicsState = physicsState;
        fListeners.add(fPhysicsUpdater);
    }

    private Geometry createBlock(int x, int y, int z) {
        logger.log(Level.INFO, "Created block at: x = {0}, y = {1}, z = {2}", new Object[]{x, y, z});
        Box b = new Box(new Vector3f(x, y, z), .5f, .5f, .5f);
        Geometry geom = new Geometry("Box", b);
        geom.setMaterial(fBlockMat);
        return geom;
    }

    private void fillChunk(Chunk cnk, int xC, int yC, int zC) {
        for (int x = xC; x < xC + Chunk.CHUNK_SIZE; x++) {
            for (int y = yC; y < yC + Chunk.CHUNK_SIZE; y++) {
                for (int z = zC; z < zC + Chunk.CHUNK_SIZE; z++) {
                    if (y < 100 && y > 100 - 5) {
                        Geometry block = createBlock(x, y, z);
                        cnk.addBlock(block, x, y, z);
                    }
                }
            }
        }
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
        if (cnk == null) {              // Chunk met juiste x, y , z bestaat niet
            cnk = new Chunk(fRootNode, fPhysicsState, xC * Chunk.CHUNK_SIZE, yC * Chunk.CHUNK_SIZE, zC * Chunk.CHUNK_SIZE);
            fillChunk(cnk, xC * Chunk.CHUNK_SIZE, yC * Chunk.CHUNK_SIZE, zC * Chunk.CHUNK_SIZE);
            cnk.addChunkListener(fGeneralListener);
            if (mZ == null) {
                mZ = new HashMap<Integer, Chunk>();
            }
            mZ.put(zC, cnk);
            if (mYZ == null) {
                mYZ = new HashMap<Integer, Map<Integer, Chunk>>();
            }
            mYZ.put(yC, mZ);
            fChunks.put(xC, mYZ);
        }
        return cnk;
    }

    public Geometry get(int x, int y, int z) {
        logger.log(Level.INFO, "Get: x = {0}, y = {1}, z = {2}", new Object[]{x, y, z});
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

    public boolean addBlock(int x, int y, int z) {
        Geometry block = createBlock(x, y, z);
        return getChunk(x, y, z, true).addBlock(block, x, y, z);
    }

    public boolean addBlock(Geometry block, int x, int y, int z) {
        return getChunk(x, y, z, true).addBlock(block, x, y, z);
    }
}
