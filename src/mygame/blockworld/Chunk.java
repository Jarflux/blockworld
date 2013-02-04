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
    protected final int fXC, fYC, fZC;
    protected BulletAppState fPhysicsState;
    protected RigidBodyControl fChunkPhysics = null;
    protected Object fChunkGeneratorData = null;
    protected boolean fNeedsUpdate = false;
    protected static ChunkGenerator fChunkGenerator = new FlatTerrainGenerator();
    private static LightingCalculator fLightingCalculator = new Lighting();
    protected static MeshCreator fMeshCreator = new LSFitting();
    private MeshCreator fPreviousCreator = fMeshCreator;

    public Chunk(BlockWorld world, ChunkColumn chunkColumn, Node rootNode, BulletAppState physicsState, int xC, int yC, int zC) {
        fXC = xC;
        fYC = yC;
        fZC = zC;
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
        if (fNeedsUpdate && y >= getY() && y < (getY() + Chunk.CHUNK_SIZE)) {
            int[][] highestBlockMap = fChunkColumn.getHighestBlockMap();
            for (int x = getX(); x < getX() + CHUNK_SIZE; x++) {
                labeltest:
                for (int z = getZ(); z < getZ() + CHUNK_SIZE; z++) {
                    if (y > highestBlockMap[MathUtil.PosMod(x, CHUNK_SIZE)][MathUtil.PosMod(z, CHUNK_SIZE)] || get(x, y, z) != null) {
                        continue labeltest;
                    }
                    float lightValue = fChunkColumn.getSunlightValue(x, y + 1, z);
                    if (lightValue > Lighting.MIN_LIGHT_VALUE) {
                        setSunlightValue(x, y, z, lightValue);
                        continue labeltest;
                    }
                    lightValue = fWorld.getSunlightValue(x, y, z);
                    if (((fWorld.getBlock(x - 1, y, z) == null) || (fWorld.getBlock(x, y + 1, z) == null)) && fWorld.getSunlightValue(x - 1, y + 1, z) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(x - 1, y + 1, z) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    if (((fWorld.getBlock(x + 1, y, z) == null) || (fWorld.getBlock(x, y + 1, z) == null)) && fWorld.getSunlightValue(x + 1, y + 1, z) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(x + 1, y + 1, z) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    if (((fWorld.getBlock(x, y, z - 1) == null) || (fWorld.getBlock(x, y + 1, z) == null)) && fWorld.getSunlightValue(x, y + 1, z - 1) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(x, y + 1, z - 1) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    if (((fWorld.getBlock(x, y, z + 1) == null) || (fWorld.getBlock(x, y + 1, z) == null)) && fWorld.getSunlightValue(x, y + 1, z + 1) > Lighting.MIN_LIGHT_VALUE) {
                        lightValue = MathUtil.RelativeAdd(lightValue, (fWorld.getSunlightValue(x, y + 1, z + 1) * Lighting.SUNLIGHT_DEGRADING_CONSTANT));
                    }
                    fWorld.setSunlightValue(x, y, z, lightValue);
                }
            }
        }
    }

    public void updateCaveSunlight() {
        if (fNeedsUpdate) {
            int[][] highestBlockMap = fChunkColumn.getHighestBlockMap();
            for (int y = getY() + CHUNK_SIZE - 1; y >= getY(); y--) {   // overloop alle blocken in de Chunk
                for (int x = getX(); x < getX() + CHUNK_SIZE; x++) {
                    for (int z = getZ(); z < getZ() + CHUNK_SIZE; z++) {
                        if (y < highestBlockMap[MathUtil.PosMod(x, CHUNK_SIZE)][MathUtil.PosMod(z, CHUNK_SIZE)] - 1 && get(x, y, z) == null) {
                            for (int i = (x - 1); i <= (x + 1); i++) {       // loop over alle buren van de block in het vlak x, z
                                for (int k = (z - 1); k <= (z + 1); k++) {
                                    float neighbourSunlightValue = fWorld.getSunlightValue(i, y, k);
                                    if (neighbourSunlightValue > Lighting.MIN_LIGHT_VALUE) {
                                        float[][][] diffuseMap = Lighting.calculateDiffuseMap(fWorld, i, y, k, neighbourSunlightValue);
                                        for (int xd = 0; xd < diffuseMap.length; xd++) {                // overloop alle lichtwaarden in de diffusemap
                                            for (int yd = 0; yd < diffuseMap.length; yd++) {
                                                for (int zd = 0; zd < diffuseMap.length; zd++) {
                                                    int xA = i + xd - (diffuseMap.length / 2);
                                                    int yA = y + yd - (diffuseMap.length / 2);
                                                    int zA = k + zd - (diffuseMap.length / 2);
                                                    if (diffuseMap[xd][yd][zd] > fWorld.getSunlightValue(xA, yA, zA)) {
                                                        setSunlightValue(xA, yA, zA, diffuseMap[xd][yd][zd]);
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
                    float[][][] RedDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getX(), b.getY(), b.getZ(), blockColor.x);
                    float[][][] GreenDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getX(), b.getY(), b.getZ(), blockColor.y);
                    float[][][] BlueDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getX(), b.getY(), b.getZ(), blockColor.z);
                    for (int xd = 0; xd < RedDiffuseMap.length; xd++) {
                        for (int yd = 0; yd < RedDiffuseMap.length; yd++) {
                            for (int zd = 0; zd < RedDiffuseMap.length; zd++) {
                                int xA = b.getX() + xd - (RedDiffuseMap.length / 2);
                                int yA = b.getY() + yd - (RedDiffuseMap.length / 2);
                                int zA = b.getZ() + zd - (RedDiffuseMap.length / 2);
                                Vector3f lightColor = fWorld.getConstantLightColor(xA, yA, zA);
                                float newRedLightValue = lightColor.x;
                                float newGreenLightValue = lightColor.y;
                                float newBlueLightValue = lightColor.z;
                                if (RedDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newRedLightValue = (lightColor.x + RedDiffuseMap[xd][yd][zd]) / (1 + (lightColor.x * RedDiffuseMap[xd][yd][zd]));
                                }
                                if (GreenDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newGreenLightValue = (lightColor.y + GreenDiffuseMap[xd][yd][zd]) / (1 + (lightColor.y * GreenDiffuseMap[xd][yd][zd]));
                                }
                                if (BlueDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newBlueLightValue = (lightColor.z + BlueDiffuseMap[xd][yd][zd]) / (1 + (lightColor.z * BlueDiffuseMap[xd][yd][zd]));
                                }
                                fWorld.setConstantLightColor(xA, yA, zA, new Vector3f(newRedLightValue, newGreenLightValue, newBlueLightValue));
                            }
                        }
                    }
                }
                if (b.isPulseLightSource()) {
                    Vector3f blockColor = b.getPulseLightValue();
                    float[][][] RedDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getX(), b.getY(), b.getZ(), blockColor.x);
                    float[][][] GreenDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getX(), b.getY(), b.getZ(), blockColor.y);
                    float[][][] BlueDiffuseMap = Lighting.calculateDiffuseMap(fWorld, b.getX(), b.getY(), b.getZ(), blockColor.z);
                    for (int xd = 0; xd < RedDiffuseMap.length; xd++) {
                        for (int yd = 0; yd < RedDiffuseMap.length; yd++) {
                            for (int zd = 0; zd < RedDiffuseMap.length; zd++) {
                                int xA = b.getX() + xd - (RedDiffuseMap.length / 2);
                                int yA = b.getY() + yd - (RedDiffuseMap.length / 2);
                                int zA = b.getZ() + zd - (RedDiffuseMap.length / 2);
                                Vector3f lightColor = fWorld.getPulseLightColor(xA, yA, zA);
                                float newRedLightValue = lightColor.x;
                                float newGreenLightValue = lightColor.y;
                                float newBlueLightValue = lightColor.z;
                                if (RedDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newRedLightValue = (lightColor.x + RedDiffuseMap[xd][yd][zd]) / (1 + (lightColor.x * RedDiffuseMap[xd][yd][zd]));
                                }
                                if (GreenDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newGreenLightValue = (lightColor.y + GreenDiffuseMap[xd][yd][zd]) / (1 + (lightColor.y * GreenDiffuseMap[xd][yd][zd]));
                                }
                                if (BlueDiffuseMap[xd][yd][zd] > 0.001f) {
                                    newBlueLightValue = (lightColor.z + BlueDiffuseMap[xd][yd][zd]) / (1 + (lightColor.z * BlueDiffuseMap[xd][yd][zd]));
                                }
                                fWorld.setPulseLightColor(xA, yA, zA, new Vector3f(newRedLightValue, newGreenLightValue, newBlueLightValue));
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
        Mesh mesh = fMeshCreator.calculateMesh(fWorld, fLightingCalculator, fXC, fYC, fZC, fXC + CHUNK_SIZE, fYC + CHUNK_SIZE, fZC + CHUNK_SIZE);
        if (mesh == null) {
            fChunkMesh = null;
            return;
        }
        fChunkMesh = new Geometry("Chunk:" + fXC + "." + fYC + "." + fZC, mesh);
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

    public Block get(int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if (fBlocks[xC][yC][zC] != null) {
            return fBlocks[xC][yC][zC];
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
            Block b = fBlocks[xC][yC][zC];
            fBlocks[xC][yC][zC] = null;
            scheduleUpdateBlockNormals(x, y, z);
            blockRemoved(b);
            fNeedsUpdate = true;
        }
    }

    public boolean addBlock(Block b) {
        return addBlock(b, true);
    }
    
    public boolean addBlock(Block b, boolean updateNormals) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(b.getX(), CHUNK_SIZE);
        yC = MathUtil.PosMod(b.getY(), CHUNK_SIZE);
        zC = MathUtil.PosMod(b.getZ(), CHUNK_SIZE);
        if (fBlocks[xC][yC][zC] != null) {
            return false;
        } else {
            fBlocks[xC][yC][zC] = b;
            if(updateNormals) {
                scheduleUpdateBlockNormals(b.getX(), b.getY(), b.getZ());
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

    public int getX() {
        return fXC;
    }

    public int getY() {
        return fYC;
    }

    public int getZ() {
        return fZC;
    }

    private String generateKey(int x, int y, int z) {
        return "" + x + ":" + y + ":" + z;
    }

    public float getSunlightValue(int x, int y, int z) {
        Float value = fSunLightMap.get(generateKey(x, y, z));
        if (value == null) {
            return fChunkColumn.getDirectSunlight(x, y, z);
        } else {
            return value;
        }
    }

    public void setSunlightValue(int x, int y, int z, float value) {
        fSunLightMap.put(generateKey(x, y, z), value);
    }

    public Vector3f getConstantLightColor(int x, int y, int z) {
        Vector3f color = fConstantLightMap.get(generateKey(x, y, z));
        if (color == null) {
            return new Vector3f(0f, 0f, 0f);
        } else {
            return color;
        }
    }

    public void setConstantLightColor(int x, int y, int z, Vector3f color) {
        fConstantLightMap.put(generateKey(x, y, z), color);
    }

    public Vector3f getPulseLightColor(int x, int y, int z) {
        Vector3f color = fPulseLightMap.get(generateKey(x, y, z));
        if (color == null) {
            return new Vector3f(0f, 0f, 0f);
        } else {
            return color;
        }
    }

    public void setPulseLightColor(int x, int y, int z, Vector3f color) {
        fPulseLightMap.put(generateKey(x, y, z), color);
    }
    
    public void scheduleUpdateBlockNormals(int x, int y, int z) {
        scheduleUpdateNormal(x, y, z);
        scheduleUpdateNormal(x, y, z + 1);
        scheduleUpdateNormal(x, y + 1, z);
        scheduleUpdateNormal(x, y + 1, z + 1);
        scheduleUpdateNormal(x + 1, y, z);
        scheduleUpdateNormal(x + 1, y, z + 1);
        scheduleUpdateNormal(x + 1, y + 1, z);
        scheduleUpdateNormal(x + 1, y + 1, z + 1);
    }
    
    public void scheduleUpdateNormal(int x, int y, int z) {
        Coordinate coordinate = new Coordinate(x, y, z);
        Set<Coordinate> connectedCorners = calculateNormal(coordinate);
        for(Coordinate corner : connectedCorners) {
            Chunk cnk = fWorld.getChunk(corner.x, corner.y, corner.z, false);
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
    public Vector3f getNormal(int x, int y, int z) {
        int xC = MathUtil.PosMod(x, CHUNK_SIZE);
        int yC = MathUtil.PosMod(y, CHUNK_SIZE);
        int zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if(fNormals[xC][yC][zC] == null) {
            calculateNormal(new Coordinate(x, y, z));
        }
        return fNormals[xC][yC][zC];
    }
    
}
