/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import mygame.blockworld.chunkgenerators.LandscapeChunkGenerator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import mygame.MathUtil;
import mygame.blockworld.chunkgenerators.ChunkGenerator;
import mygame.blockworld.chunkgenerators.FlatTerrainGenerator;
import mygame.blockworld.surfaceextraction.BasicTriangulation;
import mygame.blockworld.surfaceextraction.MarchingCubes;
import mygame.blockworld.surfaceextraction.MeshCreator;

/**
 *
 * @author Nathan & Ben
 */
public class Chunk {
    
    private static final Logger logger = Logger.getLogger(Chunk.class.getName());
    public static final int CHUNK_SIZE = 16;
    protected Integer[][][] fBlocks = new Integer[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    protected Geometry fChunkMesh = null;
    protected Node fRootNode;
    protected BlockWorld fWorld;
    protected boolean fVisible = false;
    protected final int fXC, fYC, fZC;
    protected BulletAppState fPhysicsState;
    protected RigidBodyControl fChunkPhysics = null;
    protected Object fChunkGeneratorData = null;
    protected boolean fNeedsUpdate = false;
    protected ChunkGenerator fChunkGenerator = new LandscapeChunkGenerator();
    protected static MeshCreator fMeshCreator = new BasicTriangulation();
    private MeshCreator fPreviousCreator = fMeshCreator;
    
    public Chunk(BlockWorld world, Node rootNode, BulletAppState physicsState, int xC, int yC, int zC) {
        fXC = xC; fYC = yC; fZC = zC;
        fWorld = world;
        fRootNode = rootNode;
        fPhysicsState = physicsState;
    }
    
    public static MeshCreator getMeshCreator() {
        return fMeshCreator;
    }
    
    public static void setMeshCreator(MeshCreator meshCreator) {
        fMeshCreator = meshCreator;
    }
    
    public void scheduleUpdate() {
        fNeedsUpdate = true;
    }
    
    public void update() {
        if(fNeedsUpdate || fPreviousCreator != fMeshCreator) {
            updateVisualMesh();
            //updatePhysicsMesh();
            fNeedsUpdate = false;
            fPreviousCreator = fMeshCreator;
        }
    }
    
    protected void updateVisualMesh() {
        if(!isVisible()) {
            return;
        }
        if(fChunkMesh != null) {
            fRootNode.detachChild(fChunkMesh);
        }
        Mesh mesh = fMeshCreator.calculateMesh(fWorld, this);
        if(mesh == null) {
            fChunkMesh = null;
            return;
        }
        fChunkMesh = new Geometry("Chunk:" + fXC + "." + fYC + "." + fZC, mesh);
        fChunkMesh.setMaterial(fWorld.getBlockMat());
        fRootNode.attachChild(fChunkMesh);
    }
    
    protected void updatePhysicsMesh() {
        if(!isVisible()) {
            return;
        }
        if(fChunkPhysics != null) {
            fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
        }
        if(fChunkMesh == null) {
            fChunkPhysics = null;
            return;
        }
        if(fChunkPhysics != null) {
            fChunkMesh.removeControl(fChunkPhysics);
        }
        CollisionShape chunkShape =
            CollisionShapeFactory.createMeshShape(fChunkMesh);
        fChunkPhysics = new RigidBodyControl(chunkShape, 0);
        fChunkMesh.addControl(fChunkPhysics);
        fPhysicsState.getPhysicsSpace().add(fChunkPhysics);
    }
    
    public void addChunkListener(ChunkListener listener) {
        fListeners.add(listener);
    }
    
    public void removeChunkListener(ChunkListener listener) {
        fListeners.remove(listener);
    }
    
    public void setVisible(boolean visible) {
        if(!visible) {
            if(fChunkMesh != null) {
                fRootNode.detachChild(fChunkMesh);
            }
            if(fChunkPhysics != null) {
                fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
            }
        }else{
            if(fChunkMesh != null) {
                fRootNode.attachChild(fChunkMesh);
            }
            if(fChunkPhysics != null) {
                fPhysicsState.getPhysicsSpace().add(fChunkPhysics);
            }
        }
        fVisible = visible;
    }
    
    public boolean isVisible() {
        return fVisible;
    }
    
    protected void blockAdded(Integer block, int x, int y, int z) {
        for(ChunkListener listener : fListeners) {
            listener.blockAdded(this, block, x, y, z);
        }
    }
    
    protected void blockRemoved(Integer block, int x, int y, int z) {
        for(ChunkListener listener : fListeners) {
            listener.blockRemoved(this, block, x, y, z);
        }
    }
    
    public Integer get(int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        return fBlocks[xC][yC][zC];
    }
    
    public void fillChunk() {
        fChunkGenerator.fillChunk(fWorld, this);
    }
    
    public void removeBlock(int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if(fBlocks[xC][yC][zC] != null) {
            fBlocks[xC][yC][zC] = null;
            blockRemoved(fBlocks[xC][yC][zC], x, y, z);
            fNeedsUpdate = true;
        }
    }
    
    public boolean addBlock(Integer block, int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if(fBlocks[xC][yC][zC] != null) {
            return false;
        }else{
            fBlocks[xC][yC][zC] = block;
            blockAdded(block, x, y, z);
            fNeedsUpdate = true;
            return true;
        }
    }

    void save(BufferedWriter fileWriter) throws IOException {
        fileWriter.write(fXC+":"+fYC+":"+fZC+'\n');
        for(int i = 0; i < CHUNK_SIZE; i++) {
            for(int j = 0; j < CHUNK_SIZE; j++) {
                for(int k = 0; k < CHUNK_SIZE; k++) {
                    if(fBlocks[i][j][k] != null) {
                        fileWriter.write(fBlocks[i][j][k]);
                    }else{
                        fileWriter.write(-1);
                    }
                }
            }
            fileWriter.write('\n');
        }
    }
    
    void load(BufferedReader fileReader) throws IOException {
        for(int i = 0; i < CHUNK_SIZE; i++) {
            String line = fileReader.readLine();
            for(int j = 0; j < CHUNK_SIZE; j++) {
                for(int k = 0; k < CHUNK_SIZE; k++) {
                    int block = line.charAt(j * CHUNK_SIZE + k);
                    if(block == 65535) {
                        fBlocks[i][j][k] = null;
                    }else{
                        fBlocks[i][j][k] = block;
                    }
                }
            }
        }
        fNeedsUpdate = true;
    }
    
    public Object getGeneratorData(){
        return fChunkGeneratorData;
    }

    void setGeneratorData(Object object) {
        fChunkGeneratorData = object;
    }

    public int getX() {
        return fXC;
    }

    public int getY() {
        return fYC;
    }

    public int getZ() {
        return fZC;
    }
    
    
    
}
