/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import mygame.Lighting;
import mygame.LightingCalculator;
import mygame.MathUtil;
import mygame.blockworld.chunkgenerators.ChunkGenerator;
import mygame.blockworld.chunkgenerators.FlatTerrainGenerator;
import mygame.blockworld.surfaceextraction.BasicTriangulation;
import mygame.blockworld.surfaceextraction.LSFitting;
import mygame.blockworld.surfaceextraction.MeshCreator;

/**
 *
 * @author Nathan & Ben
 */
public class Chunk {

    public static final int NORMAL_SMOOTHNESS = 3; //min 1
    
    private static final Logger logger = Logger.getLogger(Chunk.class.getName());
    public static final int CHUNK_SIZE = 16;
    protected Block[][][] fBlocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    private Vector3f[][][] fNormals = new Vector3f[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
    protected Map<String, Float> fSunLightMap = new HashMap<String, Float>();
    protected Map<String, Vector3f> fConstantLightMap = new HashMap<String, Vector3f>();
    protected Map<String, Vector3f> fPulseLightMap = new HashMap<String, Vector3f>();
    protected List<Block> fLightSources = new ArrayList<Block>();
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    protected Geometry fChunkMesh = null;
    protected Node fRootNode;
    protected BlockWorld fWorld;
    protected ChunkColumn fChunkColumn;
    protected boolean fVisible = false;
    protected final Coordinate fCoordinate;
    protected BulletAppState fPhysicsState;
    protected RigidBodyControl fChunkPhysics = null;
    protected Object fChunkGeneratorData = null;
    protected boolean fNeedsUpdate = false;
    protected static ChunkGenerator fChunkGenerator = new FlatTerrainGenerator();
    private static LightingCalculator fLightingCalculator = new Lighting();
    protected static MeshCreator fMeshCreator = new LSFitting();
    private MeshCreator fPreviousCreator = fMeshCreator;

    public Chunk(BlockWorld world, ChunkColumn chunkColumn, Node rootNode, BulletAppState physicsState, Coordinate coordinate) {
        fCoordinate = coordinate;
        fWorld = world;
        fRootNode = rootNode;
        fChunkColumn = chunkColumn;
        fPhysicsState = physicsState;

        fListeners.add(new ChunkListener() {
            public void blockAdded(Chunk chunk, Block block) {
                if (block.isLightSource()) {
                    fLightSources.add(block);
                }

            }

            public void blockRemoved(Chunk chunk, Block block) {
                if (block.isLightSource()) {
                    fLightSources.remove(block);
                }
            }
        });

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

    public void removeLight() {
        if (fNeedsUpdate) {
            fSunLightMap.clear();
            fConstantLightMap.clear();
            fPulseLightMap.clear();
        }
    }

    public void updateVisualMesh() {
        if (fNeedsUpdate || fPreviousCreator != fMeshCreator) {
            updateChunkVisualMesh();
            fMeshCreator = fPreviousCreator;
        }
    }

    public void updatePhysicsMesh() {
        if (fNeedsUpdate) {
            updateChunkPhysicsMesh();
            fNeedsUpdate = false;
        }
    }

    public void updateSunlight(int y) {
        if (fNeedsUpdate && y >= getCoordinate().y && y < (getCoordinate().y + Chunk.CHUNK_SIZE)) {
            int[][] highestBlockMap = fChunkColumn.getHighestBlockMap();
            for (int x = getCoordinate().x; x < getCoordinate().x + CHUNK_SIZE; x++) {
                labeltest:
                for (int z = getCoordinate().z; z < getCoordinate().z + CHUNK_SIZE; z++) {
                    if (y > highestBlockMap[MathUtil.PosMod(x, CHUNK_SIZE)][MathUtil.PosMod(z, CHUNK_SIZE)] || getBlock(new Coordinate(x, y, z)) != null) {
                        continue labeltest;
                    }
                    float lightValue = fChunkColumn.getSunlightValue(new Coordinate(x, y + 1, z));
                    if (lightValue > Lighting.MIN_LIGHT_VALUE) {
                        setSunlightValue(new Coordinate(x, y, z), lightValue);
                        continue labeltest;
                    }
                    lightValue = fWorld.getSunlightValue(new Coordinate(x, y, z));
                    if (((fWorld.getBlock(new Coordinate(x - 1, y, z)) == null) || (fWorld.getBlock(new Coordinate(x, y + 1, z)) == null)) && fWorld.getSunlightValue(new Coordinate(x - 1, y + 1, z)) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(new Coordinate(x - 1, y + 1, z)) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    if (((fWorld.getBlock(new Coordinate(x + 1, y, z)) == null) || (fWorld.getBlock(new Coordinate(x, y + 1, z)) == null)) && fWorld.getSunlightValue(new Coordinate(x + 1, y + 1, z)) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(new Coordinate(x + 1, y + 1, z)) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    if (((fWorld.getBlock(new Coordinate(x, y, z - 1)) == null) || (fWorld.getBlock(new Coordinate(x, y + 1, z)) == null)) && fWorld.getSunlightValue(new Coordinate(x, y + 1, z - 1)) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(new Coordinate(x, y + 1, z - 1)) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    if (((fWorld.getBlock(new Coordinate(x, y, z + 1)) == null) || (fWorld.getBlock(new Coordinate(x, y + 1, z)) == null)) && fWorld.getSunlightValue(new Coordinate(x, y + 1, z + 1)) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(new Coordinate(x, y + 1, z + 1)) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    fWorld.setSunlightValue(new Coordinate(x, y, z), lightValue);
                }
            }
        }
    }

    public void updateCaveSunlight() {
        if (fNeedsUpdate) {
            int[][] highestBlockMap = fChunkColumn.getHighestBlockMap();
            for (int y = fCoordinate.y + CHUNK_SIZE - 1; y >= fCoordinate.y; y--) {   // overloop alle blocken in de Chunk
                for (int x = fCoordinate.x; x < fCoordinate.x + CHUNK_SIZE; x++) {
                    for (int z = fCoordinate.z; z < fCoordinate.z + CHUNK_SIZE; z++) {
                        if (y < highestBlockMap[MathUtil.PosMod(x, CHUNK_SIZE)][MathUtil.PosMod(z, CHUNK_SIZE)] - 1 && getBlock(new Coordinate(x, y, z)) == null) {
                            for (int i = (x - 1); i <= (x + 1); i++) {       // loop over alle buren van de block in het vlak x, z
                                for (int k = (z - 1); k <= (z + 1); k++) {
                                    float neighbourSunlightValue = fWorld.getSunlightValue(new Coordinate(i, y, k));
                                    if (neighbourSunlightValue > Lighting.MIN_LIGHT_VALUE) {
                                        float[][][] diffuseMap = Lighting.calculateDiffuseMap(fWorld, new Coordinate(i, y, k), neighbourSunlightValue);
                                        for (int xd = 0; xd < diffuseMap.length; xd++) {                // overloop alle lichtwaarden in de diffusemap
                                            for (int yd = 0; yd < diffuseMap.length; yd++) {
                                                for (int zd = 0; zd < diffuseMap.length; zd++) {
                                                    Coordinate absCoordinate = new Coordinate(i + xd - (diffuseMap.length / 2), 
                                                            y + yd - (diffuseMap.length / 2),k + zd - (diffuseMap.length / 2));
                                                    if (diffuseMap[xd][yd][zd] > fWorld.getSunlightValue(absCoordinate)) {
                                                        setSunlightValue(absCoordinate, diffuseMap[xd][yd][zd]);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void updateLightSources() {
        if (fNeedsUpdate) {
            for (Block b : fLightSources) {
                if (b.isConstantLightSource()) {
                    Vector3f blockColor = b.getConstantLightValue();
                    float[][][] redDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getCoordinate(), blockColor.x);
                    float[][][] greenDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getCoordinate(), blockColor.y);
                    float[][][] blueDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getCoordinate(), blockColor.z);
                    for (int xd = 0; xd < redDiffuseMap.length; xd++) {
                        for (int yd = 0; yd < redDiffuseMap.length; yd++) {
                            for (int zd = 0; zd < redDiffuseMap.length; zd++) {
                                Coordinate blockCoordinate = b.getCoordinate();
                                Coordinate absCoordinate = new Coordinate(blockCoordinate.x + xd - (redDiffuseMap.length / 2), 
                                                            blockCoordinate.y + yd - (redDiffuseMap.length / 2),blockCoordinate.z + zd - (redDiffuseMap.length / 2));
                                Vector3f lightColor = fWorld.getConstantLightColor(absCoordinate);
                                float newRedLightValue = lightColor.x;
                                float newGreenLightValue = lightColor.y;
                                float newBlueLightValue = lightColor.z;
                                if (redDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newRedLightValue = (lightColor.x + redDiffuseMap[xd][yd][zd]) / (1 + (lightColor.x * redDiffuseMap[xd][yd][zd]));
                                }
                                if (greenDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newGreenLightValue = (lightColor.y + greenDiffuseMap[xd][yd][zd]) / (1 + (lightColor.y * greenDiffuseMap[xd][yd][zd]));
                                }
                                if (blueDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newBlueLightValue = (lightColor.z + blueDiffuseMap[xd][yd][zd]) / (1 + (lightColor.z * blueDiffuseMap[xd][yd][zd]));
                                }
                                fWorld.setConstantLightColor(absCoordinate, new Vector3f(newRedLightValue, newGreenLightValue, newBlueLightValue));
                            }
                        }
                    }
                }
                if (b.isPulseLightSource()) {
                    Vector3f blockColor = b.getPulseLightValue();
                    float[][][] redDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getCoordinate(), blockColor.x);
                    float[][][] greenDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getCoordinate(), blockColor.y);
                    float[][][] blueDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getCoordinate(), blockColor.z);
                    for (int xd = 0; xd < redDiffuseMap.length; xd++) {
                        for (int yd = 0; yd < redDiffuseMap.length; yd++) {
                            for (int zd = 0; zd < redDiffuseMap.length; zd++) {
                                Coordinate blockCoordinate = b.getCoordinate();
                                Coordinate absCoordinate = new Coordinate(blockCoordinate.x + xd - (redDiffuseMap.length / 2), 
                                                            blockCoordinate.y + yd - (redDiffuseMap.length / 2),blockCoordinate.z + zd - (redDiffuseMap.length / 2));
                                Vector3f lightColor = fWorld.getPulseLightColor(absCoordinate);
                                float newRedLightValue = lightColor.x;
                                float newGreenLightValue = lightColor.y;
                                float newBlueLightValue = lightColor.z;
                                if (redDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newRedLightValue = (lightColor.x + redDiffuseMap[xd][yd][zd]) / (1 + (lightColor.x * redDiffuseMap[xd][yd][zd]));
                                }
                                if (greenDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newGreenLightValue = (lightColor.y + greenDiffuseMap[xd][yd][zd]) / (1 + (lightColor.y * greenDiffuseMap[xd][yd][zd]));
                                }
                                if (blueDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newBlueLightValue = (lightColor.z + blueDiffuseMap[xd][yd][zd]) / (1 + (lightColor.z * blueDiffuseMap[xd][yd][zd]));
                                }
                                fWorld.setPulseLightColor(absCoordinate, new Vector3f(newRedLightValue, newGreenLightValue, newBlueLightValue));
                            }
                        }
                    }
                }
            }
        }
    }

    protected void updateChunkVisualMesh() {
        fPreviousCreator = fMeshCreator;
        if (!isVisible()) {
            return;
        }
        if (fChunkMesh != null) {
            fRootNode.detachChild(fChunkMesh);
        }
        Mesh mesh = fMeshCreator.calculateMesh(fWorld, fLightingCalculator, fCoordinate.x, fCoordinate.y, fCoordinate.z, 
                fCoordinate.x + CHUNK_SIZE, fCoordinate.y + CHUNK_SIZE, fCoordinate.z + CHUNK_SIZE);
        if (mesh == null) {
            fChunkMesh = null;
            return;
        }
        fChunkMesh = new Geometry("Chunk:" + fCoordinate.x + "." + fCoordinate.y + "." + fCoordinate.z, mesh);
        fChunkMesh.setMaterial(fWorld.getBlockMat());
        fRootNode.attachChild(fChunkMesh);

    }

    protected void updateChunkPhysicsMesh() {
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
        /*Mesh shape = BasicTriangulation.basicTriangulation(fWorld, this);
         if (shape == null) {
         return;
         }
         Geometry nodeShape = new Geometry("Chunk:" + fXC + "." + fYC + "." + fZC, shape);
         */
        CollisionShape chunkShape =
                CollisionShapeFactory.createMeshShape(fChunkMesh/*nodeShape*/);
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

    protected void blockAdded(Block block) {
        for (ChunkListener listener : fListeners) {
            listener.blockAdded(this, block);
        }
    }

    protected void blockRemoved(Block block) {
        for (ChunkListener listener : fListeners) {
            listener.blockRemoved(this, block);
        }
    }

    public Block getBlock(Coordinate coordinate) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(coordinate.x, CHUNK_SIZE);
        yC = MathUtil.PosMod(coordinate.y, CHUNK_SIZE);
        zC = MathUtil.PosMod(coordinate.z, CHUNK_SIZE);
        if (fBlocks[xC][yC][zC] != null) {
            return fBlocks[xC][yC][zC];
        }
        return null;
    }

    public void fillChunk() {
        fChunkGenerator.fillChunk(fWorld, this);
    }

    
    public void removeBlock(Coordinate coordinate) {
        Coordinate relCoordinate = new Coordinate(MathUtil.PosMod(coordinate.x, CHUNK_SIZE), 
                MathUtil.PosMod(coordinate.y, CHUNK_SIZE),
                MathUtil.PosMod(coordinate.z, CHUNK_SIZE) );
        if (fBlocks[relCoordinate.x][relCoordinate.y][relCoordinate.z] != null) {
            Block b = fBlocks[relCoordinate.x][relCoordinate.y][relCoordinate.z];
            fBlocks[relCoordinate.x][relCoordinate.y][relCoordinate.z] = null;
            scheduleUpdateBlockNormals(coordinate);
            blockRemoved(b);
            fNeedsUpdate = true;
        }
    }

    public boolean addBlock(Block b) {
        return addBlock(b, true);
    }
    
    public boolean addBlock(Block b, boolean updateNormals) {
        Coordinate coordinate = new Coordinate(MathUtil.PosMod(b.getCoordinate().x, CHUNK_SIZE), 
                MathUtil.PosMod(b.getCoordinate().y, CHUNK_SIZE),
                MathUtil.PosMod(b.getCoordinate().z, CHUNK_SIZE) );
        if (fBlocks[coordinate.x][coordinate.y][coordinate.z] != null) {
            return false;
        } else {
            fBlocks[coordinate.x][coordinate.y][coordinate.z] = b;
            if(updateNormals) {
                scheduleUpdateBlockNormals(b.getCoordinate());
            }
            blockAdded(b);
            fNeedsUpdate = true;
            return true;
        }
    }

    void save(BufferedWriter fileWriter) throws IOException {
        /*fileWriter.write(fXC + ":" + fYC + ":" + fZC + '\n');
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
         }*/
    }

    void load(BufferedReader fileReader) throws IOException {
        /*for (int i = 0; i < CHUNK_SIZE; i++) {
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
         fNeedsUpdate = true;*/
    }

    public Object getGeneratorData() {
        return fChunkGeneratorData;
    }

    void setGeneratorData(Object object) {
        fChunkGeneratorData = object;
    }

    public Coordinate getCoordinate() {
        return fCoordinate;
    }

    private String generateKey(Coordinate coordinate) {
        return "" + coordinate.x + ":" + coordinate.y + ":" + coordinate.z;
    }

    public float getSunlightValue(Coordinate coordinate) {
        Float value = fSunLightMap.get(generateKey(coordinate));
        if (value == null) {
            return fChunkColumn.getDirectSunlight(coordinate);
        } else {
            return value;
        }
    }

    public void setSunlightValue(Coordinate coordinate, float value) {
        fSunLightMap.put(generateKey(coordinate), value);
    }

    public Vector3f getConstantLightColor(Coordinate coordinate) {
        Vector3f color = fConstantLightMap.get(generateKey(coordinate));
        if (color == null) {
            return new Vector3f(0f, 0f, 0f);
        } else {
            return color;
        }
    }

    public void setConstantLightColor(Coordinate coordinate, Vector3f color) {
        fConstantLightMap.put(generateKey(coordinate), color);
    }

    public Vector3f getPulseLightColor(Coordinate coordinate) {
        Vector3f color = fPulseLightMap.get(generateKey(coordinate));
        if (color == null) {
            return new Vector3f(0f, 0f, 0f);
        } else {
            return color;
        }
    }

    public void setPulseLightColor(Coordinate coordinate, Vector3f color) {
        fPulseLightMap.put(generateKey(coordinate), color);
    }
    
    public void scheduleUpdateBlockNormals(Coordinate coordinate) {
        scheduleUpdateNormal(coordinate);
        scheduleUpdateNormal(new Coordinate(coordinate.x, coordinate.y, coordinate.z + 1));
        scheduleUpdateNormal(new Coordinate(coordinate.x, coordinate.y + 1, coordinate.z));
        scheduleUpdateNormal(new Coordinate(coordinate.x, coordinate.y + 1, coordinate.z + 1));
        scheduleUpdateNormal(new Coordinate(coordinate.x + 1, coordinate.y, coordinate.z));
        scheduleUpdateNormal(new Coordinate(coordinate.x + 1, coordinate.y, coordinate.z + 1));
        scheduleUpdateNormal(new Coordinate(coordinate.x + 1, coordinate.y + 1, coordinate.z));
        scheduleUpdateNormal(new Coordinate(coordinate.x + 1, coordinate.y + 1, coordinate.z + 1));
    }
    
    public void scheduleUpdateNormal(Coordinate coordinate) {
        Set<Coordinate> connectedCorners = calculateNormal(coordinate);
        for(Coordinate corner : connectedCorners) {
            Chunk cnk = fWorld.getChunk(corner, false);
            if(cnk != null) {
                //cnk.fNormalsToUpdate.add(corner);
                cnk.fNormals[MathUtil.PosMod(corner.x, CHUNK_SIZE)][MathUtil.PosMod(corner.y, CHUNK_SIZE)][MathUtil.PosMod(corner.z, CHUNK_SIZE)] = null;
            }
        }
    }
    
    private Set<Coordinate> calculateNormal(Coordinate coordinate) {
        Vector3f position = new Vector3f(coordinate.x, coordinate.y, coordinate.z);
        Vector3f normal = new Vector3f(0,0,0);
        Set<Coordinate> connectedCorners = new HashSet<Coordinate>();
        Coordinate.findConnectedCorners(fWorld, coordinate, true, false, false, NORMAL_SMOOTHNESS, connectedCorners);
        boolean inverted = false;
        if(connectedCorners.size() <= 1) {
            inverted = true;
            Coordinate.findConnectedCorners(fWorld, coordinate, false, true, false, NORMAL_SMOOTHNESS, connectedCorners);
        }
        for(Coordinate corner : connectedCorners) {
            normal.addLocal(position.subtract(new Vector3f(corner.x, corner.y, corner.z)));
        }
        if(inverted) {
            normal = Vector3f.ZERO.subtract(normal);
        }
        normal.normalizeLocal();
        fNormals[MathUtil.PosMod(coordinate.x, CHUNK_SIZE)][MathUtil.PosMod(coordinate.y, CHUNK_SIZE)][MathUtil.PosMod(coordinate.z, CHUNK_SIZE)] = normal;
        connectedCorners.remove(coordinate);
        return connectedCorners;
    }
    
    //private List<Coordinate> fNormalsToUpdate = new LinkedList<Coordinate>();
    /*
    public void updateNormals() {
        for(Coordinate toUpdate : fNormalsToUpdate) {
            calculateNormal(toUpdate);
        }
        fNormalsToUpdate.clear();
    }
    */
    public Vector3f getNormal(Coordinate coordinate) {
        int xC = MathUtil.PosMod(coordinate.x, CHUNK_SIZE);
        int yC = MathUtil.PosMod(coordinate.y, CHUNK_SIZE);
        int zC = MathUtil.PosMod(coordinate.z, CHUNK_SIZE);
        if(fNormals[xC][yC][zC] == null) {
            calculateNormal(coordinate);
        }
        return fNormals[xC][yC][zC];
    }
    
}
