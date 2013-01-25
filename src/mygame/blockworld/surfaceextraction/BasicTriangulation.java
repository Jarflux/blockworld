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
import java.util.List;
import mygame.MathUtil;
import mygame.blockworld.Block;
import mygame.blockworld.BlockInfo;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Nathan
 */
public class BasicTriangulation implements MeshCreator {
    private int textureSize = 1024;
    
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

    public static Mesh basicTriangulation(BlockWorld world, Chunk chunk) {
        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Vector2f> texCoord = new ArrayList<Vector2f>();
        List<Integer> indexes = new ArrayList<Integer>();
        List<Vector4f> light = new ArrayList<Vector4f>();
        int index = 0;
        int[][] fHighestBlockMap = world.getHighestBlockMap(chunk.getX(), chunk.getZ());

        for (int i = chunk.getX(); i < chunk.getX() + Chunk.CHUNK_SIZE; i++) {
            for (int j = chunk.getY(); j < chunk.getY() + Chunk.CHUNK_SIZE; j++) {
                for (int k = chunk.getZ(); k < chunk.getZ() + Chunk.CHUNK_SIZE; k++) {
                    Block block = world.getBlock(i, j, k);
                    if (block != null) {

                        Vector4f lightAlpha = new Vector4f(0.08f, 0.0f, 0.0f, 0.0f); 
                        //Check top
                        if (world.getChunk(i, j + 1, k, true).get(i, j + 1, k) == null) {
                            vertices.add(new Vector3f(i - .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k - .5f));
                            normals.add(new Vector3f(0, 1, 0));
                            normals.add(new Vector3f(0, 1, 0));
                            normals.add(new Vector3f(0, 1, 0));
                            normals.add(new Vector3f(0, 1, 0));
                            addTextureCoords(texCoord, BlockInfo.TopSides[block.getBlockValue()], false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightAlpha = new Vector4f(world.getSunlightValue(i, j+1, k), 0.0f, 0.0f, 0.0f);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            
                        }
                        //Check bottom
                        if (world.getChunk(i, j - 1, k, true).get(i, j - 1, k) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j - .5f, k + .5f));
                            normals.add(new Vector3f(0, -1, 0));
                            normals.add(new Vector3f(0, -1, 0));
                            normals.add(new Vector3f(0, -1, 0));
                            normals.add(new Vector3f(0, -1, 0));
                            addTextureCoords(texCoord, BlockInfo.BottomSides[block.getBlockValue()], false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            // bottom can have sunlight?
                            lightAlpha = new Vector4f(world.getSunlightValue(i, j-1, k), 0.0f, 0.0f, 0.0f);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            
                        }
                        //Check right
                        if (world.getChunk(i + 1, j, k, true).get(i + 1, j, k) == null) {
                            vertices.add(new Vector3f(i + .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k + .5f));
                            normals.add(new Vector3f(1, 0, 0));
                            normals.add(new Vector3f(1, 0, 0));
                            normals.add(new Vector3f(1, 0, 0));
                            normals.add(new Vector3f(1, 0, 0));
                            addTextureCoords(texCoord, BlockInfo.RightSides[block.getBlockValue()], true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightAlpha = new Vector4f(world.getSunlightValue(i+1, j, k), 0.0f, 0.0f, 0.0f);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                        //Check left
                        if (world.getChunk(i - 1, j, k, true).get(i - 1, j, k) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i - .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k - .5f));
                            normals.add(new Vector3f(-1, 0, 0));
                            normals.add(new Vector3f(-1, 0, 0));
                            normals.add(new Vector3f(-1, 0, 0));
                            normals.add(new Vector3f(-1, 0, 0));
                            addTextureCoords(texCoord, BlockInfo.LeftSides[block.getBlockValue()], false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightAlpha = new Vector4f(world.getSunlightValue(i-1, j, k), 0.0f, 0.0f, 0.0f);
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                        //Check back
                        if (world.getChunk(i, j, k + 1, true).get(i, j, k + 1) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k + .5f));
                            normals.add(new Vector3f(0, 0, 1));
                            normals.add(new Vector3f(0, 0, 1));
                            normals.add(new Vector3f(0, 0, 1));
                            normals.add(new Vector3f(0, 0, 1));
                            addTextureCoords(texCoord, BlockInfo.BackSides[block.getBlockValue()], false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightAlpha = new Vector4f(world.getSunlightValue(i, j, k+1), 0.0f, 0.0f, 0.0f);
                            
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                            light.add(lightAlpha);
                        }
                        //Check front
                        if (world.getChunk(i, j, k - 1, true).get(i, j, k - 1) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k - .5f));
                            normals.add(new Vector3f(0, 0, -1));
                            normals.add(new Vector3f(0, 0, -1));
                            normals.add(new Vector3f(0, 0, -1));
                            normals.add(new Vector3f(0, 0, -1));
                            addTextureCoords(texCoord, BlockInfo.FrontSides[block.getBlockValue()], true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;
                            
                            lightAlpha = new Vector4f(world.getSunlightValue(i, j, k-1), 0.0f, 0.0f, 0.0f);
                            
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
        return basicTriangulation(world, chunk);
    }
}
