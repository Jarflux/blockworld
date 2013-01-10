/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import mygame.MathUtil;

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
    public int fXC, fYC, fZC;
    protected BulletAppState fPhysicsState;
    protected RigidBodyControl fChunkPhysics = null;
    protected Object fChunkGeneratorData = null;
    
    public Chunk(BlockWorld world, Node rootNode, BulletAppState physicsState, int xC, int yC, int zC) {
        fXC = xC; fYC = yC; fZC = zC;
        fWorld = world;
        fRootNode = rootNode;
        fPhysicsState = physicsState;
    }
    
    private void addTextureCoords(List<Vector2f> texCoord, int texId, boolean swap) {
        float texIdX = texId % 16;
        float texIdY = (texId - texIdX) / 16;
        if(swap) {
            texCoord.add(new Vector2f(texIdX/16f,texIdY/16f));
            texCoord.add(new Vector2f(texIdX/16f,(texIdY+1f)/16f));
            texCoord.add(new Vector2f((texIdX+1f)/16f,(texIdY+1f)/16f));
            texCoord.add(new Vector2f((texIdX+1f)/16f,texIdY/16f));
        }else{
            texCoord.add(new Vector2f(texIdX/16f,texIdY/16f));
            texCoord.add(new Vector2f((texIdX+1f)/16f,texIdY/16f));
            texCoord.add(new Vector2f((texIdX+1f)/16f,(texIdY+1f)/16f));
            texCoord.add(new Vector2f(texIdX/16f,(texIdY+1f)/16f));
        }
    }
    
    private Mesh createVisualMesh() {
        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector2f> texCoord = new ArrayList<Vector2f>();
        List<Integer> indexes = new ArrayList<Integer>();
        int index = 0;
        for(int i = fXC; i < fXC + Chunk.CHUNK_SIZE; i++) {
            for(int j = fYC; j < fYC + Chunk.CHUNK_SIZE; j++) {
                for(int k = fZC; k < fZC + Chunk.CHUNK_SIZE; k++) {
                    Integer block = fWorld.get(i, j, k);
                    if(block != null) {
                        //Check top
                        if(fWorld.getChunk(i, j+1, k, true).get(i, j+1, k) == null) {
                            vertices.add(new Vector3f(i-.5f, j+.5f, k-.5f));
                            vertices.add(new Vector3f(i-.5f, j+.5f, k+.5f));
                            vertices.add(new Vector3f(i+.5f, j+.5f, k+.5f));
                            vertices.add(new Vector3f(i+.5f, j+.5f, k-.5f));
                            addTextureCoords(texCoord, BlockInfo.TopSides[block], false);
                            indexes.add(index); indexes.add(index+1); indexes.add(index+2); // triangle 1
                            indexes.add(index); indexes.add(index+2); indexes.add(index+3); // triangle 2
                            index = index + 4;
                        }
                        //Check bottem
                        if(fWorld.getChunk(i, j-1, k, true).get(i, j-1, k) == null) {
                            vertices.add(new Vector3f(i-.5f, j-.5f, k-.5f));
                            vertices.add(new Vector3f(i+.5f, j-.5f, k-.5f));
                            vertices.add(new Vector3f(i+.5f, j-.5f, k+.5f));
                            vertices.add(new Vector3f(i-.5f, j-.5f, k+.5f));
                            addTextureCoords(texCoord, BlockInfo.BottomSides[block], false);
                            indexes.add(index); indexes.add(index+1); indexes.add(index+2); // triangle 1
                            indexes.add(index); indexes.add(index+2); indexes.add(index+3); // triangle 2
                            index = index + 4;
                        }
                        //Check right
                        if(fWorld.getChunk(i+1, j, k, true).get(i+1, j, k) == null) {
                            vertices.add(new Vector3f(i+.5f, j-.5f, k-.5f));
                            vertices.add(new Vector3f(i+.5f, j+.5f, k-.5f));
                            vertices.add(new Vector3f(i+.5f, j+.5f, k+.5f));
                            vertices.add(new Vector3f(i+.5f, j-.5f, k+.5f));
                            addTextureCoords(texCoord, BlockInfo.RightSides[block], true);
                            indexes.add(index); indexes.add(index+1); indexes.add(index+2); // triangle 1
                            indexes.add(index); indexes.add(index+2); indexes.add(index+3); // triangle 2
                            index = index + 4;
                        }
                        //Check left
                        if(fWorld.getChunk(i-1, j, k, true).get(i-1, j, k) == null) {
                            vertices.add(new Vector3f(i-.5f, j-.5f, k-.5f));
                            vertices.add(new Vector3f(i-.5f, j-.5f, k+.5f));
                            vertices.add(new Vector3f(i-.5f, j+.5f, k+.5f));
                            vertices.add(new Vector3f(i-.5f, j+.5f, k-.5f));
                            addTextureCoords(texCoord, BlockInfo.LeftSides[block], false);
                            indexes.add(index); indexes.add(index+1); indexes.add(index+2); // triangle 1
                            indexes.add(index); indexes.add(index+2); indexes.add(index+3); // triangle 2
                            index = index + 4;
                        }
                        //Check back
                        if(fWorld.getChunk(i, j, k+1, true).get(i, j, k+1) == null) {
                            vertices.add(new Vector3f(i-.5f, j-.5f, k+.5f));
                            vertices.add(new Vector3f(i+.5f, j-.5f, k+.5f));
                            vertices.add(new Vector3f(i+.5f, j+.5f, k+.5f));
                            vertices.add(new Vector3f(i-.5f, j+.5f, k+.5f));
                            addTextureCoords(texCoord, BlockInfo.BackSides[block], false);
                            indexes.add(index); indexes.add(index+1); indexes.add(index+2); // triangle 1
                            indexes.add(index); indexes.add(index+2); indexes.add(index+3); // triangle 2
                            index = index + 4;
                        }
                        //Check front
                        if(fWorld.getChunk(i, j, k-1, true).get(i, j, k-1) == null) {
                            vertices.add(new Vector3f(i-.5f, j-.5f, k-.5f));
                            vertices.add(new Vector3f(i-.5f, j+.5f, k-.5f));
                            vertices.add(new Vector3f(i+.5f, j+.5f, k-.5f));
                            vertices.add(new Vector3f(i+.5f, j-.5f, k-.5f));
                            addTextureCoords(texCoord, BlockInfo.FrontSides[block], true);
                            indexes.add(index); indexes.add(index+1); indexes.add(index+2); // triangle 1
                            indexes.add(index); indexes.add(index+2); indexes.add(index+3); // triangle 2
                            index = index + 4;
                        }
                    }
                }
            }
        }
        if(index == 0) {
            return null;
        }
        Mesh mesh = new Mesh();
        Vector3f[] verticesSimpleType = new Vector3f[vertices.size()];
        Vector2f[] texCoordSimpleType = new Vector2f[vertices.size()];
        int[] indexesSimpleType = new int[indexes.size()];
        for(int i = 0; i < vertices.size(); i++) {
            verticesSimpleType[i] = vertices.get(i);
            texCoordSimpleType[i] = texCoord.get(i);
        }
        for(int i = 0; i < indexes.size(); i++) {
            indexesSimpleType[i] = indexes.get(i);
        }
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(verticesSimpleType));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoordSimpleType));        
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indexesSimpleType));
        mesh.updateCounts();
        mesh.updateBound();
        return mesh;
    }
    
    public void update() {
        updateVisualMesh();
        updatePhysicsMesh();
    }
    
    protected void updateVisualMesh() {
        if(!isVisible()) {
            return;
        }
        if(fChunkMesh != null) {
            fRootNode.detachChild(fChunkMesh);
        }
        Mesh mesh = createVisualMesh();
        if(mesh == null) {
            fChunkMesh = null;
            return;
        }
        fChunkMesh = new Geometry("Chunk:" + fXC + "." + fYC + "." + fZC, mesh);
        fChunkMesh.setMaterial(fWorld.getBlockMat());
        if(fWorld.getAtlas() != null) {
            fWorld.getAtlas().applyCoords(fChunkMesh);
        }
        fRootNode.attachChild(fChunkMesh);
    }
    
    protected void updatePhysicsMesh() {
        if(!isVisible()) {
            return;
        }
        if(fChunkMesh == null) {
            if(fChunkPhysics != null) {
                fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
            }
            fChunkPhysics = null;
            return;
        }
        if(fChunkPhysics != null) {
            fChunkMesh.removeControl(fChunkPhysics);
            fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
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
    
    public void showChunk() {
        if(!fVisible) {
            fVisible = true;
            update();
        }
    }
    
    public void hideChunk() {
        if(fVisible) {
            if(fChunkMesh != null) {
                fRootNode.detachChild(fChunkMesh);
                if(fChunkPhysics != null) {
                    fChunkMesh.removeControl(fChunkPhysics);
                    fPhysicsState.getPhysicsSpace().remove(fChunkPhysics);
                }
            }
            fVisible = false;
        }
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
        ChunkGenerator cnkGenerator= new ChunkGenerator();
        cnkGenerator.fillChunk(this);
    }
    
    public void removeBlock(int x, int y, int z) {
        int xC, yC, zC;
        xC = MathUtil.PosMod(x, CHUNK_SIZE);
        yC = MathUtil.PosMod(y, CHUNK_SIZE);
        zC = MathUtil.PosMod(z, CHUNK_SIZE);
        if(fBlocks[xC][yC][zC] != null) {
            fBlocks[xC][yC][zC] = null;
            blockRemoved(fBlocks[xC][yC][zC], x, y, z);
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
            return true;
        }
    }
    
    public Object getGeneratorData(){
        return fChunkGeneratorData;
    }

    void setGeneratorData(Object object) {
        fChunkGeneratorData = object;
    }
    
}
