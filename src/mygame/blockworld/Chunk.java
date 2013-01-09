/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Nathan & Ben
 */
public class Chunk {
    
    private static final Logger logger = Logger.getLogger(Chunk.class.getName());
    public static final int CHUNK_SIZE = 8;
    protected Geometry[][][] fBlocks = new Geometry[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    protected Node fChunkRoot;
    protected Node fRootNode;
    protected boolean fVisible = false;
    protected BulletAppState fPhysicsState;
    protected RigidBodyControl fChunkPhysics = null;

    public Chunk(Node rootNode, BulletAppState physicsState, int xC, int yC, int zC) {
        fChunkRoot = new Node("Chunk:" + xC + "." + yC + "." + zC);
        fRootNode = rootNode;
        fPhysicsState = physicsState;
    }
    
    public void updatePhysicsMesh() {
        if(!isVisible()) {
            return;
        }
        if(fChunkPhysics != null) {
            fChunkRoot.removeControl(fChunkPhysics);
            fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
        }
        CollisionShape chunkShape =
            CollisionShapeFactory.createMeshShape((Node) fChunkRoot);
        fChunkPhysics = new RigidBodyControl(chunkShape, 0);
        fChunkRoot.addControl(fChunkPhysics);
        fPhysicsState.getPhysicsSpace().add(fChunkPhysics);
    }
    
    public void addChunkListener(ChunkListener listener) {
        fListeners.add(listener);
    }
    
    public void removeChunkListener(ChunkListener listener) {
        fListeners.remove(listener);
    }
    
    public void showChunk() {
        if(!fVisible) {
            fRootNode.attachChild(fChunkRoot);
            fVisible = true;
            updatePhysicsMesh();
        }
    }
    
    public void hideChunk() {
        if(fVisible) {
            fRootNode.detachChild(fChunkRoot);
            fChunkRoot.removeControl(fChunkPhysics);
            fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
            fVisible = false;
        }
    }
    
    public boolean isVisible() {
        return fVisible;
    }
    
    protected void blockAdded(Geometry block, int x, int y, int z) {
        for(ChunkListener listener : fListeners) {
            listener.blockAdded(this, block, x, y, z);
        }
    }
    
    protected void blockRemoved(Geometry block, int x, int y, int z) {
        for(ChunkListener listener : fListeners) {
            listener.blockRemoved(this, block, x, y, z);
        }
    }
    
    public Geometry get(int x, int y, int z) {
        int xC, yC, zC;
        xC = Math.abs(x % CHUNK_SIZE);
        yC = Math.abs(y % CHUNK_SIZE);
        zC = Math.abs(z % CHUNK_SIZE);
        return fBlocks[xC][yC][zC];
    }
    
    public void removeBlock(int x, int y, int z) {
        int xC, yC, zC;
        xC = Math.abs(x % CHUNK_SIZE);
        yC = Math.abs(y % CHUNK_SIZE);
        zC = Math.abs(z % CHUNK_SIZE);
        if(fBlocks[xC][yC][zC] != null) {
            fChunkRoot.detachChild(fBlocks[xC][yC][zC]);
            blockRemoved(fBlocks[xC][yC][zC], x, y, z);
            fBlocks[xC][yC][zC] = null;
        }
    }
    
    public boolean addBlock(Geometry block, int x, int y, int z) {
        int xC, yC, zC;
        xC = Math.abs(x % CHUNK_SIZE);
        yC = Math.abs(y % CHUNK_SIZE);
        zC = Math.abs(z % CHUNK_SIZE);
        if(fBlocks[xC][yC][zC] != null) {
            return false;
        }else{
            fBlocks[xC][yC][zC] = block;
            fChunkRoot.attachChild(block);
            blockAdded(block, x, y, z);
            return true;
        }
    }
    
}
