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
import com.jme3.scene.VertexBuffer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import mygame.Lighting;
import mygame.MathUtil;
import mygame.blockworld.chunkgenerators.ChunkGenerator;
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
    protected Block[][][] fBlocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    protected Map<String, Float> fLightMap = new HashMap<String, Float>();
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    protected Geometry fChunkMesh = null;
    protected Node fRootNode;
    protected BlockWorld fWorld;
    protected ChunkColumn fChunkColumn;
    protected boolean fVisible = false;
    protected final int fXC, fYC, fZC;
    protected BulletAppState fPhysicsState;
    protected RigidBodyControl fChunkPhysics = null;
    protected Object fChunkGeneratorData = null;
    protected boolean fNeedsUpdate = false;
    protected ChunkGenerator fChunkGenerator = new LandscapeChunkGenerator();
    protected static MeshCreator fMeshCreator = new BasicTriangulation();
    private MeshCreator fPreviousCreator = fMeshCreator;

    public Chunk(BlockWorld world, ChunkColumn chunkColumn, Node rootNode, BulletAppState physicsState, int xC, int yC, int zC) {
        fXC = xC;
        fYC = yC;
        fZC = zC;
        fWorld = world;
        fRootNode = rootNode;
        fChunkColumn = chunkColumn;
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
        if (fNeedsUpdate || fPreviousCreator != fMeshCreator) {
            updateLight();
            updateVisualMesh();
            updatePhysicsMesh();
            fNeedsUpdate = false;
            fPreviousCreator = fMeshCreator;
        }
    }

    protected void updateLight() {
        fLightMap.clear();

        int[][] highestBlockMap = fChunkColumn.getHighestBlockMap();
        for (int i = 0; i < CHUNK_SIZE; i++) {
            for (int j = 0; j < CHUNK_SIZE; j++) {
                int highestBlockY = highestBlockMap[i][j];
                if ((highestBlockY >= getY()) && (highestBlockY < (getY() + CHUNK_SIZE))) {
                    // highestblock +1 because u need the sunlight value above the highest block
                    float[][][] diffuseMap = Lighting.calculateDiffuseMap(fWorld, i + getX(), highestBlockY+1, j + getZ(), fChunkColumn.getDirectSunlight(i + getX(), highestBlockY+1, j + getZ()));
                    for (int xd = 0; xd < diffuseMap.length; xd++) {
                        for (int yd = 0; yd < diffuseMap.length; yd++) {
                            for (int zd = 0; zd < diffuseMap.length; zd++) {
                                if(diffuseMap[xd][yd][zd] > 0.001f){ 
                                    int xA = i + getX() + xd - (diffuseMap.length/2);
                                    int yA = highestBlockY + 1 + yd - (diffuseMap.length/2);
                                    int zA = j + getZ() + zd - (diffuseMap.length/2);
                                    float sunlightValue = fWorld.getSunlightValue(xA, yA, zA);
                                    
                                    float newSunlightValue = (sunlightValue + diffuseMap[xd][yd][zd]) / (1 + (sunlightValue * diffuseMap[xd][yd][zd]));
                                    fWorld.setSunlightValue(xA, yA, zA, newSunlightValue );                
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void updateVisualMesh() {
        if (!isVisible()) {
            return;
        }
        if (fChunkMesh != null) {
            fRootNode.detachChild(fChunkMesh);
        }
        Mesh mesh = fMeshCreator.calculateMesh(fWorld, this);
        //mesh = fixMesh(mesh);
        if (mesh == null) {
            fChunkMesh = null;
            return;
        }
        fChunkMesh = new Geometry("Chunk:" + fXC + "." + fYC + "." + fZC, mesh);
        fChunkMesh.setMaterial(fWorld.getBlockMat());
        fRootNode.attachChild(fChunkMesh);
    }

    private Mesh fixMesh(Mesh mesh) {
        int triangleCount = mesh.getTriangleCount();
        int[] indices = new int[3];
        Set<Entry<Integer, Integer>> unmatched = new HashSet<Entry<Integer, Integer>>();
        for(int i = 0; i < triangleCount; i++) {
            mesh.getTriangle(i, indices);
            for(int j = 0; j < 3; j++) {
                int lowIndex = Math.min(indices[j], indices[(j+1)%3]);
                int highIndex = Math.max(indices[j], indices[(j+1)%3]);
                Entry<Integer, Integer> edge = new HashMap.SimpleEntry<Integer, Integer>(lowIndex, highIndex);
                boolean isBorderEdge = false;
                for(int k = 0; k < 3; k++) {
                    int posLow = Math.round((Float)mesh.getBuffer(VertexBuffer.Type.Position).getElementComponent(lowIndex, k)) % CHUNK_SIZE;
                    int posHigh = Math.round((Float)mesh.getBuffer(VertexBuffer.Type.Position).getElementComponent(highIndex, k)) % CHUNK_SIZE;
                    isBorderEdge |= (posLow == 0 && posHigh == 0);
                    isBorderEdge |= (posLow == CHUNK_SIZE - 1 && posHigh == CHUNK_SIZE - 1);
                }
                if(isBorderEdge) {
                    continue;
                }
                if(unmatched.contains(edge)) {
                    unmatched.remove(edge);
                }else{
                    unmatched.add(edge);
                }
            }
        }
        List<Integer> newTriangles = new ArrayList<Integer>(unmatched.size());
        Set<Entry<Integer, Integer>> loneEdges = new HashSet<Entry<Integer, Integer>>();
        while(!unmatched.isEmpty()) {
            Entry<Integer, Integer> edge = unmatched.iterator().next();
            unmatched.remove(edge);
            List<Entry<Integer, Integer>> neighbours = new ArrayList<Entry<Integer, Integer>>(2);
            for(Entry<Integer, Integer> it : unmatched) {
                if(edge.getKey() == it.getKey() || edge.getKey() == it.getValue()
                        || edge.getValue() == it.getKey() || edge.getValue() == it.getValue()) {
                    neighbours.add(it);
                }
            }
            for(Entry<Integer, Integer> it : neighbours) {
                unmatched.remove(it);
            }
            if(neighbours.isEmpty()) {
                loneEdges.add(edge);
            }else if(neighbours.size() == 1 || neighbours.size() == 2) {
                int otherIndex = neighbours.get(0).getKey();
                if(otherIndex == edge.getKey()) {
                    otherIndex = neighbours.get(0).getValue();
                    if(neighbours.size() == 1) {
                        unmatched.add(new HashMap.SimpleEntry<Integer, Integer>(otherIndex, edge.getValue()));
                    }else{
                        if( !( (neighbours.get(1).getKey() == otherIndex && neighbours.get(1).getValue() == edge.getValue()) 
                                || (neighbours.get(1).getKey() == edge.getValue() && neighbours.get(1).getValue() == otherIndex) ) ) {
                            throw new UnknownError();
                        }
                    }
                }else if(otherIndex == edge.getValue()) {
                    otherIndex = neighbours.get(0).getValue();
                    
                }else{
                    
                }
                
                newTriangles.add(edge.getKey());
                newTriangles.add(edge.getValue());
                newTriangles.add(otherIndex);
                
                newTriangles.add(edge.getKey());
                newTriangles.add(otherIndex);
                newTriangles.add(edge.getValue());
                
                
            }else{
                throw new UnknownError();
            }
        }
        if(unmatched.size() > 0) {
            System.out.println("Number of unmatched edges = " + unmatched.size());
            System.out.println("Total number of unmatched edges = " + NrUnmatchedEdges);
        }
        return mesh;
    }
    
    public static int NrUnmatchedEdges = 0;
    
    protected void updatePhysicsMesh() {
        if (!isVisible()) {
            return;
        }
        if (fChunkPhysics != null) {
            fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
        }
        if (fChunkMesh == null) {
            fChunkPhysics = null;
            return;
        }
        if (fChunkPhysics != null) {
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
        if (!visible) {
            if (fChunkMesh != null) {
                fRootNode.detachChild(fChunkMesh);
            }
            if (fChunkPhysics != null) {
                fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
            }
        } else {
            if (fChunkMesh != null) {
                fRootNode.attachChild(fChunkMesh);
            }
            if (fChunkPhysics != null) {
                fPhysicsState.getPhysicsSpace().add(fChunkPhysics);
            }
        }
        fVisible = visible;
    }

    public boolean isVisible() {
        return fVisible;
    }

    protected void blockAdded(Integer block, int x, int y, int z) {
        for (ChunkListener listener : fListeners) {
            listener.blockAdded(this, block, x, y, z);
        }
    }

    protected void blockRemoved(Integer block, int x, int y, int z) {
        for (ChunkListener listener : fListeners) {
            listener.blockRemoved(this, block, x, y, z);
        }
    }

    public Integer get(int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if (fBlocks[xC][yC][zC] != null) {
            return fBlocks[xC][yC][zC].getBlockValue();
        }
        return null;
    }

    public void fillChunk() {
        fChunkGenerator.fillChunk(fWorld, this);
    }

    public void removeBlock(int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if (fBlocks[xC][yC][zC] != null) {
            fBlocks[xC][yC][zC] = null;
            blockRemoved(null, x, y, z);
            fNeedsUpdate = true;
        }
    }

    public boolean addBlock(Integer blockValue, int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if (fBlocks[xC][yC][zC] != null) {
            return false;
        } else {
            fBlocks[xC][yC][zC] = new Block(x, y, z, blockValue);
            blockAdded(blockValue, x, y, z);
            fNeedsUpdate = true;
            return true;
        }
    }

    void save(BufferedWriter fileWriter) throws IOException {
        fileWriter.write(fXC + ":" + fYC + ":" + fZC + '\n');
        for (int i = 0; i < CHUNK_SIZE; i++) {
            for (int j = 0; j < CHUNK_SIZE; j++) {
                for (int k = 0; k < CHUNK_SIZE; k++) {
                    if (fBlocks[i][j][k] != null) {
                        fileWriter.write(fBlocks[i][j][k].getBlockValue());
                    } else {
                        fileWriter.write(-1);
                    }
                }
            }
            fileWriter.write('\n');
        }
    }

    void load(BufferedReader fileReader) throws IOException {
        for (int i = 0; i < CHUNK_SIZE; i++) {
            String line = fileReader.readLine();
            for (int j = 0; j < CHUNK_SIZE; j++) {
                for (int k = 0; k < CHUNK_SIZE; k++) {
                    int blockValue = line.charAt(j * CHUNK_SIZE + k);
                    if (blockValue == 65535) {
                        fBlocks[i][j][k] = null;
                    } else {
                        fBlocks[i][j][k] = new Block(getX() + i, getY() + j, getZ() + k, blockValue);
                    }
                }
            }
        }
        fNeedsUpdate = true;
    }

    public Object getGeneratorData() {
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

    private Float getLight(int x, int y, int z) {
        return fLightMap.get(parseKey(x, y, z));
    }

    private void setLight(int x, int y, int z, float value) {
        fLightMap.put(parseKey(x, y, z), value);
    }

    private String parseKey(int x, int y, int z) {
        return "" + x + ":" + y + ":" + z;
    }

    public float getSunlightValue(int x, int y, int z) {
        Float value = getLight(x, y, z);
        if (value == null) {
            return fChunkColumn.getDirectSunlight(x, y, z);
        } else {
            return value;
        }
    }
    
    public void setSunlightValue(int x, int y, int z, float value) {
        setLight(x, y, z, value);
    }
}
