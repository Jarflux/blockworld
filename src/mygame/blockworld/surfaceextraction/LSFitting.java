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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.vecmath.Color3f;
import mygame.blockworld.Block;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;
import mygame.blockworld.Coordinate;

/**
 *
 * @author Nathan
 */
public class LSFitting implements MeshCreator {
    
    private static List<Vector2f> addTextureCoords(List<Vector2f> texCoord, int texId, boolean swap) {
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
        return texCoord;
    }
    
    private static final float SMALLEST_FEATURE_DISTANCE = .1f;
    
    private static final int BLOCK_SMOOTHNESS = 3; //min 0
    private static Vector3f calculateVertexPosition(BlockWorld world, int x, int y, int z) {
        float xOriginal = x - .5f;
        float yOriginal = y - .5f;
        float zOriginal = z - .5f;
        
        if(BLOCK_SMOOTHNESS == 0) {
            return new Vector3f(xOriginal, yOriginal, zOriginal);
        }
        
        Set<Coordinate> connectedCorners = new HashSet<Coordinate>();
        Coordinate.findConnectedCorners(world, new Coordinate(x, y, z), false, false, true, BLOCK_SMOOTHNESS, connectedCorners);
        
        float pX = 0f;
        float pY = 0f;
        float pZ = 0f;
        float u = 0f;
        float v = 0f;
        float w = 0f;
        
        float samples = 0;
        
        for(Coordinate corner : connectedCorners) {
            //int distance = Math.abs(x - corner.x) + Math.abs(y - corner.y) + Math.abs(z - corner.z);
            Vector3f normal = world.getChunk(corner.x, corner.y, corner.z, true).getNormal(corner.x, corner.y, corner.z);
            if(!normal.equals(Vector3f.ZERO)) {
                pX += corner.x - .5f;
                pY += corner.y - .5f;
                pZ += corner.z - .5f;
                
                u += normal.x;
                v += normal.y;
                w += normal.z;
                
                samples += 1f;
            }
        }
        pX /= samples;
        pY /= samples;
        pZ /= samples;
        
        u = u / samples;
        v = v / samples;
        w = w / samples;
        
        //calculate t from the formula ux + vy + wz + t = 0; (u,v,w) is the normal
        float t = (- u * pX - v * pY - w * pZ);
        
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
        List<Vector2f> texCoords = new ArrayList<Vector2f>();
        List<Integer> indices = new ArrayList<Integer>();
        List<Vector4f> light = new ArrayList<Vector4f>();
        Chunk currentChunk;
        int index = 0;
        for (int i = chunk.getX(); i < chunk.getX() + Chunk.CHUNK_SIZE; i++) {
            for (int j = chunk.getY(); j < chunk.getY() + Chunk.CHUNK_SIZE; j++) {
                for (int k = chunk.getZ(); k < chunk.getZ() + Chunk.CHUNK_SIZE; k++) {
                    Block block = world.getBlock(i, j, k);
                    if (block != null) {
                        List<Vector3f> verticesBlock = new LinkedList<Vector3f>();
                        List<Vector3f> normalsBlock = new LinkedList<Vector3f>();
                        List<Vector2f> texCoordsBlock = new LinkedList<Vector2f>();
                        List<Integer> indicesBlock = new LinkedList<Integer>();
                        int indexBlock = 0;
                        
                        //Check top
                        currentChunk = world.getChunk(i, j + 1, k, true);
                        if (currentChunk.get(i, j + 1, k) == null) {
                            verticesBlock.add(calculateVertexPosition(world, i, j + 1, k));
                            verticesBlock.add(calculateVertexPosition(world, i, j + 1, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j + 1, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j + 1, k));
                            normalsBlock.add(currentChunk.getNormal(i, j + 1, k));
                            normalsBlock.add(currentChunk.getNormal(i, j + 1, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j + 1, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j + 1, k));
                            addTextureCoords(texCoordsBlock, block.getTextureTop(), false);
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 1);
                            indicesBlock.add(indexBlock + 2); // triangle 1
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 2);
                            indicesBlock.add(indexBlock + 3); // triangle 2
                            indexBlock = indexBlock + 4;
                        }
                        //Check bottom
                        currentChunk = world.getChunk(i, j - 1, k, true);
                        if (currentChunk.get(i, j - 1, k) == null) {
                            verticesBlock.add(calculateVertexPosition(world, i, j, k));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j, k));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i, j, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i, j, k));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j, k));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i, j, k + 1));
                            addTextureCoords(texCoordsBlock, block.getTextureBottom(), false);
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 1);
                            indicesBlock.add(indexBlock + 2); // triangle 1
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 2);
                            indicesBlock.add(indexBlock + 3); // triangle 2
                            indexBlock = indexBlock + 4;
                        }
                        //Check right
                        currentChunk = world.getChunk(i + 1, j, k, true);
                        if (currentChunk.get(i + 1, j, k) == null) {
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j, k));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j + 1, k));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j + 1, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j, k));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j + 1, k));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j + 1, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j, k + 1));
                            addTextureCoords(texCoordsBlock, block.getTextureRight(), true);
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 1);
                            indicesBlock.add(indexBlock + 2); // triangle 1
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 2);
                            indicesBlock.add(indexBlock + 3); // triangle 2
                            indexBlock = indexBlock + 4;
                        }
                        //Check left
                        currentChunk = world.getChunk(i - 1, j, k, true);
                        if (currentChunk.get(i - 1, j, k) == null) {
                            verticesBlock.add(calculateVertexPosition(world, i, j, k));
                            verticesBlock.add(calculateVertexPosition(world, i, j, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i, j + 1, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i, j + 1, k));
                            normalsBlock.add(currentChunk.getNormal(i, j, k));
                            normalsBlock.add(currentChunk.getNormal(i, j, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i, j + 1, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i, j + 1, k));
                            addTextureCoords(texCoordsBlock, block.getTextureLeft(), false);
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 1);
                            indicesBlock.add(indexBlock + 2); // triangle 1
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 2);
                            indicesBlock.add(indexBlock + 3); // triangle 2
                            indexBlock = indexBlock + 4;
                        }
                        //Check back
                        currentChunk = world.getChunk(i, j, k + 1, true);
                        if (currentChunk.get(i, j, k + 1) == null) {
                            verticesBlock.add(calculateVertexPosition(world, i, j, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j + 1, k + 1));
                            verticesBlock.add(calculateVertexPosition(world, i, j + 1, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i, j, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j + 1, k + 1));
                            normalsBlock.add(currentChunk.getNormal(i, j + 1, k + 1));
                            addTextureCoords(texCoordsBlock, block.getTextureBack(), false);
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 1);
                            indicesBlock.add(indexBlock + 2); // triangle 1
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 2);
                            indicesBlock.add(indexBlock + 3); // triangle 2
                            indexBlock = indexBlock + 4;
                        }
                        //Check front
                        currentChunk = world.getChunk(i, j, k - 1, true);
                        if (currentChunk.get(i, j, k - 1) == null) {
                            verticesBlock.add(calculateVertexPosition(world, i, j, k));
                            verticesBlock.add(calculateVertexPosition(world, i, j + 1, k));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j + 1, k));
                            verticesBlock.add(calculateVertexPosition(world, i + 1, j, k));
                            normalsBlock.add(currentChunk.getNormal(i, j, k));
                            normalsBlock.add(currentChunk.getNormal(i, j + 1, k));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j + 1, k));
                            normalsBlock.add(currentChunk.getNormal(i + 1, j, k));
                            addTextureCoords(texCoordsBlock, block.getTextureFront(), true);
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 1);
                            indicesBlock.add(indexBlock + 2); // triangle 1
                            indicesBlock.add(indexBlock);
                            indicesBlock.add(indexBlock + 2);
                            indicesBlock.add(indexBlock + 3); // triangle 2
                            indexBlock = indexBlock + 4;
                        }
                        /*
                        //Check if 2 vertices are reasonably close to eachother, if so merge them, their normals & change the indices
                        final float MIN_DISTANCE = .3f;
                        for(int index1 = 0; index1 < verticesBlock.size(); index1++) {
                            //TODO calculate & add light
                            Vector3f vertex1 = verticesBlock.get(index1);
                            Vector3f normal1 = verticesBlock.get(index1);
                            for(int index2 = index1+1; index2 < verticesBlock.size(); index2++) {
                                Vector3f vertex2 = verticesBlock.get(index2);
                                Vector3f normal2 = verticesBlock.get(index2);
                                if(vertex1.distance(vertex2) <= MIN_DISTANCE) {
                                    Vector3f averageVertex = new Vector3f((vertex1.x + vertex2.x) / 2f, (vertex1.y + vertex2.y) / 2f, (vertex1.z + vertex2.z) / 2f);
                                    Vector3f averageNormal = new Vector3f((normal1.x + normal2.x) / 2f, (normal1.y + normal2.y) / 2f, (normal1.z + normal2.z) / 2f);
                                    verticesBlock.set(index1, averageVertex);
                                    normalsBlock.set(index1, averageNormal);
                                    verticesBlock.set(index2, averageVertex);
                                    normalsBlock.set(index2, averageNormal);
                                    for(int triangleIndex = 0; triangleIndex < indicesBlock.size(); triangleIndex += 3) {
                                        boolean contains1 = indicesBlock.get(triangleIndex) == index1 || indicesBlock.get(triangleIndex + 1) == index1 || indicesBlock.get(triangleIndex + 2) == index1;
                                        boolean contains2 = indicesBlock.get(triangleIndex) == index2 || indicesBlock.get(triangleIndex + 1) == index2 || indicesBlock.get(triangleIndex + 2) == index2;
                                        if(contains1 && contains2) {
                                            indicesBlock.remove(triangleIndex + 2);
                                            indicesBlock.remove(triangleIndex + 1);
                                            indicesBlock.remove(triangleIndex);
                                            triangleIndex -= 3;
                                        }
                                    }
                                }
                            }
                        }
                        */
                        vertices.addAll(verticesBlock);
                        normals.addAll(normalsBlock);
                        texCoords.addAll(texCoordsBlock);
                        for(int counter = 0; counter < indicesBlock.size(); counter++) {
                            indices.add(indicesBlock.get(counter) + index);
                        }
                        index += indexBlock;
                    }
                }
            }
        }
        if (vertices.isEmpty()) {
            return null;
        }
        Vector3f[] verticesSimpleType = new Vector3f[vertices.size()];
        Vector3f[] normalsSimpleType = new Vector3f[vertices.size()];
        Vector2f[] texCoordSimpleType = new Vector2f[vertices.size()];
        Vector4f[] lightSimpleType = new Vector4f[vertices.size()];  //shader
        int[] indicesSimpleType = new int[indices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            verticesSimpleType[i] = vertices.get(i);
            normalsSimpleType[i] = normals.get(i);
            texCoordSimpleType[i] = texCoords.get(i);
            //lightSimpleType[i] = light.get(i);  //shader
        }
        for (int i = 0; i < indices.size(); i++) {
            indicesSimpleType[i] = indices.get(i);
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
