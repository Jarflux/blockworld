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
import mygame.blockworld.Coordinate;

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
    
    private static final float SMALLEST_FEATURE_DISTANCE = .1f;
    
    private static final int BLOCK_SMOOTHNESS = 3; //min 0
    private static Vector3f calculateVertexPosition(BlockWorld world, int x, int y, int z) {
        float xOriginal = x - .5f;
        float yOriginal = y - .5f;
        float zOriginal = z - .5f;
        /*
        if(BLOCK_SMOOTHNESS == 0) {
            return new Vector3f(xOriginal, yOriginal, zOriginal);
        }
        */
        Set<Coordinate> connectedCorners = new HashSet<Coordinate>();
        Coordinate.findConnectedCorners(world, new Coordinate(x, y, z), false, false, true, BLOCK_SMOOTHNESS, connectedCorners);
        
        float u = 0f;
        float v = 0f;
        float w = 0f;
        float t = 0f;
        
        float samples = 0;
        
        for(Coordinate corner : connectedCorners) {
            int distance = Math.abs(x - corner.x) + Math.abs(y - corner.y) + Math.abs(z - corner.z);
            Vector3f normal = world.getChunk(corner.x, corner.y, corner.z, true).getNormal(corner.x, corner.y, corner.z);
            if(!normal.equals(Vector3f.ZERO)) {
                u += normal.x * distance;
                v += normal.y * distance;
                w += normal.z * distance;
                //calculate t from the formula ux + vy + wz + t = 0; (u,v,w) is the normal
                t += (- normal.x * (corner.x - .5f) - normal.y * (corner.y - .5f) - normal.z * (corner.z - .5f)) * distance;
                
                samples += 1f * distance;
            }
        }
        u = u / samples;
        v = v / samples;
        w = w / samples;
        t = t / samples;
        
        //calculate r from the formula projectedPoint = originalPoint + r * normal
        float r = (xOriginal * u + yOriginal * v + zOriginal * w + t) / (u * u + v * v + w * w);
        float xP = xOriginal - r * u + .5f;
        float yP = yOriginal - r * v + .5f;
        float zP = zOriginal - r * w + .5f;
        /*
        xP = Math.min(xP, xOriginal + .5f - SMALLEST_FEATURE_DISTANCE/2f);
        xP = Math.max(xP, xOriginal - .5f + SMALLEST_FEATURE_DISTANCE/2f);
        yP = Math.min(yP, yOriginal + .5f - SMALLEST_FEATURE_DISTANCE/2f);
        yP = Math.max(yP, yOriginal - .5f + SMALLEST_FEATURE_DISTANCE/2f);
        zP = Math.min(zP, zOriginal + .5f - SMALLEST_FEATURE_DISTANCE/2f);
        zP = Math.max(zP, zOriginal - .5f + SMALLEST_FEATURE_DISTANCE/2f);
        */
        /*
        xP = Math.round(xP / SMALLEST_FEATURE_DISTANCE) * SMALLEST_FEATURE_DISTANCE;
        yP = Math.round(yP / SMALLEST_FEATURE_DISTANCE) * SMALLEST_FEATURE_DISTANCE;
        zP = Math.round(zP / SMALLEST_FEATURE_DISTANCE) * SMALLEST_FEATURE_DISTANCE;
        */
        Vector3f newPosition = new Vector3f(xP, yP, zP);
        
        return newPosition;
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
        Chunk currentChunk;
        for (int i = chunk.getX(); i < chunk.getX() + Chunk.CHUNK_SIZE; i++) {
            for (int j = chunk.getY(); j < chunk.getY() + Chunk.CHUNK_SIZE; j++) {
                for (int k = chunk.getZ(); k < chunk.getZ() + Chunk.CHUNK_SIZE; k++) {
                    Block block = world.getBlock(i, j, k);
                    if (block != null) {

                        //Check top
                        currentChunk = world.getChunk(i, j + 1, k, true);
                        if (currentChunk.get(i, j + 1, k) == null) {
                            vertices.add(calculateVertexPosition(world, i, j + 1, k));
                            vertices.add(calculateVertexPosition(world, i, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, i + 1, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, i + 1, j + 1, k));
                            normals.add(currentChunk.getNormal(i, j + 1, k));
                            normals.add(currentChunk.getNormal(i, j + 1, k + 1));
                            normals.add(currentChunk.getNormal(i + 1, j + 1, k + 1));
                            normals.add(currentChunk.getNormal(i + 1, j + 1, k));
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
                        currentChunk = world.getChunk(i, j - 1, k, true);
                        if (currentChunk.get(i, j - 1, k) == null) {
                            vertices.add(calculateVertexPosition(world, i, j, k));
                            vertices.add(calculateVertexPosition(world, i + 1, j, k));
                            vertices.add(calculateVertexPosition(world, i + 1, j, k + 1));
                            vertices.add(calculateVertexPosition(world, i, j, k + 1));
                            normals.add(currentChunk.getNormal(i, j, k));
                            normals.add(currentChunk.getNormal(i + 1, j, k));
                            normals.add(currentChunk.getNormal(i + 1, j, k + 1));
                            normals.add(currentChunk.getNormal(i, j, k + 1));
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
                        currentChunk = world.getChunk(i + 1, j, k, true);
                        if (currentChunk.get(i + 1, j, k) == null) {
                            vertices.add(calculateVertexPosition(world, i + 1, j, k));
                            vertices.add(calculateVertexPosition(world, i + 1, j + 1, k));
                            vertices.add(calculateVertexPosition(world, i + 1, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, i + 1, j, k + 1));
                            normals.add(currentChunk.getNormal(i + 1, j, k));
                            normals.add(currentChunk.getNormal(i + 1, j + 1, k));
                            normals.add(currentChunk.getNormal(i + 1, j + 1, k + 1));
                            normals.add(currentChunk.getNormal(i + 1, j, k + 1));
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
                        currentChunk = world.getChunk(i - 1, j, k, true);
                        if (currentChunk.get(i - 1, j, k) == null) {
                            vertices.add(calculateVertexPosition(world, i, j, k));
                            vertices.add(calculateVertexPosition(world, i, j, k + 1));
                            vertices.add(calculateVertexPosition(world, i, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, i, j + 1, k));
                            normals.add(currentChunk.getNormal(i, j, k));
                            normals.add(currentChunk.getNormal(i, j, k + 1));
                            normals.add(currentChunk.getNormal(i, j + 1, k + 1));
                            normals.add(currentChunk.getNormal(i, j + 1, k));
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
                        currentChunk = world.getChunk(i, j, k + 1, true);
                        if (currentChunk.get(i, j, k + 1) == null) {
                            vertices.add(calculateVertexPosition(world, i, j, k + 1));
                            vertices.add(calculateVertexPosition(world, i + 1, j, k + 1));
                            vertices.add(calculateVertexPosition(world, i + 1, j + 1, k + 1));
                            vertices.add(calculateVertexPosition(world, i, j + 1, k + 1));
                            normals.add(currentChunk.getNormal(i, j, k + 1));
                            normals.add(currentChunk.getNormal(i + 1, j, k + 1));
                            normals.add(currentChunk.getNormal(i + 1, j + 1, k + 1));
                            normals.add(currentChunk.getNormal(i, j + 1, k + 1));
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
                        currentChunk = world.getChunk(i, j, k - 1, true);
                        if (currentChunk.get(i, j, k - 1) == null) {
                            vertices.add(calculateVertexPosition(world, i, j, k));
                            vertices.add(calculateVertexPosition(world, i, j + 1, k));
                            vertices.add(calculateVertexPosition(world, i + 1, j + 1, k));
                            vertices.add(calculateVertexPosition(world, i + 1, j, k));
                            normals.add(currentChunk.getNormal(i, j, k));
                            normals.add(currentChunk.getNormal(i, j + 1, k));
                            normals.add(currentChunk.getNormal(i + 1, j + 1, k));
                            normals.add(currentChunk.getNormal(i + 1, j, k));
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
        //mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(lightSimpleType)); //shader
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
