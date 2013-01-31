/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.surfaceextraction;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mygame.blockworld.Block;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Nathan
 */
public class LSFitting implements MeshCreator {
    
    private static void addTextureCoords(List<Vector2f> texCoord, int texId, boolean swap) {
        float texIdX = texId % 16;
        float texIdY = (texId - texIdX) / 16;
        if (swap) {
            texCoord.add(new Vector2f(texIdX / 16f, texIdY / 16f));
            texCoord.add(new Vector2f(texIdX / 16f, (texIdY + 1f) / 16f));
            texCoord.add(new Vector2f((texIdX + 1f) / 16f, (texIdY + 1f) / 16f));
            texCoord.add(new Vector2f((texIdX + 1f) / 16f, texIdY / 16f));
        } else {
            texCoord.add(new Vector2f(texIdX / 16f, texIdY / 16f));
            texCoord.add(new Vector2f((texIdX + 1f) / 16f, texIdY / 16f));
            texCoord.add(new Vector2f((texIdX + 1f) / 16f, (texIdY + 1f) / 16f));
            texCoord.add(new Vector2f(texIdX / 16f, (texIdY + 1f) / 16f));
        }
    }
    
    //wrong but simple normal calculation
    //TODO calculate normal using least squares aproximation
    private static Vector3f calculateNormal(BlockWorld world, int x, int y, int z)
    {
            Vector3f normal = new Vector3f();
            normal.x = ((world.getBlock(x-1, y, z)==null)? 0f : 1f) - ((world.getBlock(x+1, y, z)==null)? 0f : 1f);
            normal.y = ((world.getBlock(x, y-1, z)==null)? 0f : 1f) - ((world.getBlock(x, y+1, z)==null)? 0f : 1f);
            normal.z = ((world.getBlock(x, y, z-1)==null)? 0f : 1f) - ((world.getBlock(x, y, z+1)==null)? 0f : 1f);
            return normal.normalizeLocal();
    }
    
    private static final int BLOCK_SMOOTHNESS = 1;
    private static Vector3f calculateVertexPosition(BlockWorld world, Chunk chunk, int x, int y, int z) {
        Set<Coordinate> connectedCorners = new HashSet<Coordinate>();
        findConnectedCorners(world, chunk, new Coordinate(x, y, z), BLOCK_SMOOTHNESS, connectedCorners);
        
        for(Coordinate corner : connectedCorners) {
            Vector3f normal = calculateNormal(world, corner.x, corner.y, corner.z);
            float t = - normal.x * (corner.x - .5f) - normal.y * (corner.y - .5f) - normal.z * (corner.z - .5f);
            //TODO given plane determine the projection of the corner in orthogonal to the plane
        }
        
        //TODO use the resulting projections to generate a value for the position of the corner
        
        float xOriginal = x - .5f;
        float yOriginal = y - .5f;
        float zOriginal = z - .5f;
        
        return new Vector3f(xOriginal, yOriginal, zOriginal);
    }
    
    private static class Coordinate {
        public int x;
        public int y;
        public int z;

        public Coordinate(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
    }
    
    /**
     * Finds the corners connected (via edges that are part of the surface) to the start corner.
     * @param world
     * @param chunk
     * @param start Corner for which to find the connected corners. Corners are identified by the block coordinates who have that corner as the one with the lowest x, y & z values.
     * @param recursionDepth Maximum distance from the given coordinate to find connected corners
     * @param connectedCoordinates The set that will be filled with the connected corners
     */
    public static void findConnectedCorners(BlockWorld world, Chunk chunk, Coordinate start, int recursionDepth, Set<Coordinate> connectedCoordinates) {
        connectedCoordinates.add(start);
        if(recursionDepth == 0) {
            return;
        }
        
        int block000 = world.getBlock(start.x-1, start.y-1, start.z-1) != null ? 1 : 0;
        int block001 = world.getBlock(start.x-1, start.y-1, start.z) != null ? 1 : 0;
        int block010 = world.getBlock(start.x-1, start.y, start.z-1) != null ? 1 : 0;
        int block011 = world.getBlock(start.x-1, start.y, start.z) != null ? 1 : 0;
        int block100 = world.getBlock(start.x, start.y-1, start.z-1) != null ? 1 : 0;
        int block101 = world.getBlock(start.x, start.y-1, start.z) != null ? 1 : 0;
        int block110 = world.getBlock(start.x, start.y, start.z-1) != null ? 1 : 0;
        int block111 = world.getBlock(start.x, start.y, start.z) != null ? 1 : 0;
        
        int edgeXNeg = block000 + block001 + block010 + block011;
        int edgeXPos = block100 + block101 + block110 + block111;
        int edgeYNeg = block000 + block001 + block100 + block101;
        int edgeYPos = block010 + block011 + block110 + block111;
        int edgeZNeg = block000 + block010 + block100 + block110;
        int edgeZPos = block001 + block011 + block101 + block111;
        
        if(edgeXNeg >= 1 && edgeXNeg < 4) {
            findConnectedCorners(world, chunk, new Coordinate(start.x-1,start.y,start.z), recursionDepth-1, connectedCoordinates);
        }
        if(edgeXPos >= 1 && edgeXPos < 4) {
            findConnectedCorners(world, chunk, new Coordinate(start.x+1,start.y,start.z), recursionDepth-1, connectedCoordinates);
        }
        if(edgeYNeg >= 1 && edgeYNeg < 4) {
            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y-1,start.z), recursionDepth-1, connectedCoordinates);
        }
        if(edgeYPos >= 1 && edgeYPos < 4) {
            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y+1,start.z), recursionDepth-1, connectedCoordinates);
        }
        if(edgeZNeg >= 1 && edgeZNeg < 4) {
            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y,start.z-1), recursionDepth-1, connectedCoordinates);
        }
        if(edgeZPos >= 1 && edgeZPos < 4) {
            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y,start.z+1), recursionDepth-1, connectedCoordinates);
        }
    }

    public static Mesh computeLSFit(BlockWorld world, Chunk chunk) {
        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Vector2f> texCoord = new ArrayList<Vector2f>();
        List<Integer> indexes = new ArrayList<Integer>();
        List<Vector4f> light = new ArrayList<Vector4f>();
        int index = 0;
        Vector4f lightAlpha;
        Vector3f lightColor;
        for (int i = chunk.getX(); i < chunk.getX() + Chunk.CHUNK_SIZE; i++) {
            for (int j = chunk.getY(); j < chunk.getY() + Chunk.CHUNK_SIZE; j++) {
                for (int k = chunk.getZ(); k < chunk.getZ() + Chunk.CHUNK_SIZE; k++) {
                    Block block = world.getBlock(i, j, k);
                    if (block != null) {

                        //Check top
                        if (world.getChunk(i, j + 1, k, true).get(i, j + 1, k) == null) {
                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k));
                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k));
                            normals.add(calculateNormal(world, i, j + 1, k));
                            normals.add(calculateNormal(world, i, j + 1, k + 1));
                            normals.add(calculateNormal(world, i + 1, j + 1, k + 1));
                            normals.add(calculateNormal(world, i + 1, j + 1, k));
                            addTextureCoords(texCoord, block.getTextureTop(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightColor = world.getLightColor(i, j+1, k);
                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j+1, k));
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            
                        }
                        //Check bottom
                        if (world.getChunk(i, j - 1, k, true).get(i, j - 1, k) == null) {
                            vertices.add(calculateVertexPosition(world, chunk, i, j, k));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i, j, k + 1));
                            normals.add(calculateNormal(world, i, j, k));
                            normals.add(calculateNormal(world, i + 1, j, k));
                            normals.add(calculateNormal(world, i + 1, j, k + 1));
                            normals.add(calculateNormal(world, i, j, k + 1));
                            addTextureCoords(texCoord, block.getTextureBottom(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightColor = world.getLightColor(i, j-1, k);
                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j-1, k));
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            
                        }
                        //Check right
                        if (world.getChunk(i + 1, j, k, true).get(i + 1, j, k) == null) {
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k + 1));
                            normals.add(calculateNormal(world, i + 1, j, k));
                            normals.add(calculateNormal(world, i + 1, j + 1, k));
                            normals.add(calculateNormal(world, i + 1, j + 1, k + 1));
                            normals.add(calculateNormal(world, i + 1, j, k + 1));
                            addTextureCoords(texCoord, block.getTextureRight(), true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightColor = world.getLightColor(i+1, j, k);
                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i+1, j, k));
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                        //Check left
                        if (world.getChunk(i - 1, j, k, true).get(i - 1, j, k) == null) {
                            vertices.add(calculateVertexPosition(world, chunk, i, j, k));
                            vertices.add(calculateVertexPosition(world, chunk, i, j, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k));
                            normals.add(calculateNormal(world, i, j, k));
                            normals.add(calculateNormal(world, i, j, k + 1));
                            normals.add(calculateNormal(world, i, j + 1, k + 1));
                            normals.add(calculateNormal(world, i, j + 1, k));
                            addTextureCoords(texCoord, block.getTextureLeft(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightColor = world.getLightColor(i-1, j, k);
                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i-1, j, k));
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                        //Check back
                        if (world.getChunk(i, j, k + 1, true).get(i, j, k + 1) == null) {
                            vertices.add(calculateVertexPosition(world, chunk, i, j, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k + 1));
                            normals.add(calculateNormal(world, i, j, k + 1));
                            normals.add(calculateNormal(world, i + 1, j, k + 1));
                            normals.add(calculateNormal(world, i + 1, j + 1, k + 1));
                            normals.add(calculateNormal(world, i, j + 1, k + 1));
                            addTextureCoords(texCoord, block.getTextureBack(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightColor = world.getLightColor(i, j, k+1);
                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j, k+1));
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                        //Check front
                        if (world.getChunk(i, j, k - 1, true).get(i, j, k - 1) == null) {
                            vertices.add(calculateVertexPosition(world, chunk, i, j, k));
                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k));
                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k));
                            normals.add(calculateNormal(world, i, j, k));
                            normals.add(calculateNormal(world, i, j + 1, k));
                            normals.add(calculateNormal(world, i + 1, j + 1, k));
                            normals.add(calculateNormal(world, i + 1, j, k));
                            addTextureCoords(texCoord, block.getTextureFront(), true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightColor = world.getLightColor(i, j, k-1);
                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j, k-1));
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                    }
                }
            }
        }
        if (index == 0) {
            return null;
        }
        Vector3f[] verticesSimpleType = new Vector3f[vertices.size()];
        Vector3f[] normalsSimpleType = new Vector3f[vertices.size()];
        Vector2f[] texCoordSimpleType = new Vector2f[vertices.size()];
        Vector4f[] lightSimpleType = new Vector4f[vertices.size()];  //shader
        int[] indicesSimpleType = new int[indexes.size()];
        for (int i = 0; i < vertices.size(); i++) {
            verticesSimpleType[i] = vertices.get(i);
            normalsSimpleType[i] = normals.get(i);
            texCoordSimpleType[i] = texCoord.get(i);
            lightSimpleType[i] = light.get(i);  //shader
        }
        for (int i = 0; i < indexes.size(); i++) {
            indicesSimpleType[i] = indexes.get(i);
        }
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(lightSimpleType)); //shader
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(verticesSimpleType));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normalsSimpleType));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoordSimpleType));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indicesSimpleType));
        mesh.updateCounts();
        mesh.updateBound();
        return mesh;
    }

    public Mesh calculateMesh(BlockWorld world, Chunk chunk) {
        return computeLSFit(world, chunk);
    }
}
