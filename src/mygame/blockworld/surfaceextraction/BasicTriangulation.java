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
import mygame.LightingCalculator;
import mygame.MathUtil;
import mygame.blockworld.Block;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;
import mygame.blockworld.ChunkColumn;

/**
 *
 * @author Nathan
 */
public class BasicTriangulation implements MeshCreator {

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

    public static Mesh basicTriangulation(BlockContainer blockContainer, LightingCalculator lighting, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Vector2f> texCoord = new ArrayList<Vector2f>();
        List<Integer> indexes = new ArrayList<Integer>();
        List<Vector4f> light = new ArrayList<Vector4f>();
        int index = 0;
        for (int i = xMin; i < xMax; i++) {
            for (int j = yMin; j < yMax; j++) {
                for (int k = zMin; k < zMax; k++) {
                    Block block = blockContainer.getBlock(i, j, k);
                    if (block != null) {

                        //Check top
                        if (blockContainer.getBlock(i, j + 1, k) == null) {
                            vertices.add(new Vector3f(i - .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k - .5f));
                            normals.add(new Vector3f(0, 1, 0));
                            normals.add(new Vector3f(0, 1, 0));
                            normals.add(new Vector3f(0, 1, 0));
                            normals.add(new Vector3f(0, 1, 0));
                            addTextureCoords(texCoord, block.getTextureTop(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(lighting.calculateLight(blockContainer, i, j + 1, k));
                            light.add(lighting.calculateLight(blockContainer, i, j + 1, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j + 1, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j + 1, k));


                        }

                        //Check bottom
                        if (blockContainer.getBlock(i, j - 1, k) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j - .5f, k + .5f));
                            normals.add(new Vector3f(0, -1, 0));
                            normals.add(new Vector3f(0, -1, 0));
                            normals.add(new Vector3f(0, -1, 0));
                            normals.add(new Vector3f(0, -1, 0));
                            addTextureCoords(texCoord, block.getTextureBottom(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(lighting.calculateLight(blockContainer, i, j, k));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j, k));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i, j, k + 1));


                        }
                        //Check right
                        if (blockContainer.getBlock(i + 1, j, k) == null) {
                            vertices.add(new Vector3f(i + .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k + .5f));
                            normals.add(new Vector3f(1, 0, 0));
                            normals.add(new Vector3f(1, 0, 0));
                            normals.add(new Vector3f(1, 0, 0));
                            normals.add(new Vector3f(1, 0, 0));
                            addTextureCoords(texCoord, block.getTextureRight(), true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(lighting.calculateLight(blockContainer, i + 1, j, k));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j + 1, k));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j + 1, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j, k + 1));

                        }
                        //Check left
                        if (blockContainer.getBlock(i - 1, j, k) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i - .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k - .5f));
                            normals.add(new Vector3f(-1, 0, 0));
                            normals.add(new Vector3f(-1, 0, 0));
                            normals.add(new Vector3f(-1, 0, 0));
                            normals.add(new Vector3f(-1, 0, 0));
                            addTextureCoords(texCoord, block.getTextureLeft(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(lighting.calculateLight(blockContainer, i, j, k));
                            light.add(lighting.calculateLight(blockContainer, i, j, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i, j + 1, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i, j + 1, k));

                        }
                        //Check back
                        if (blockContainer.getBlock(i, j, k + 1) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k + .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k + .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k + .5f));
                            normals.add(new Vector3f(0, 0, 1));
                            normals.add(new Vector3f(0, 0, 1));
                            normals.add(new Vector3f(0, 0, 1));
                            normals.add(new Vector3f(0, 0, 1));
                            addTextureCoords(texCoord, block.getTextureBack(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(lighting.calculateLight(blockContainer, i, j, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j + 1, k + 1));
                            light.add(lighting.calculateLight(blockContainer, i, j + 1, k + 1));
                        }
                        //Check front
                        if (blockContainer.getBlock(i, j, k - 1) == null) {
                            vertices.add(new Vector3f(i - .5f, j - .5f, k - .5f));
                            vertices.add(new Vector3f(i - .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j + .5f, k - .5f));
                            vertices.add(new Vector3f(i + .5f, j - .5f, k - .5f));
                            normals.add(new Vector3f(0, 0, -1));
                            normals.add(new Vector3f(0, 0, -1));
                            normals.add(new Vector3f(0, 0, -1));
                            normals.add(new Vector3f(0, 0, -1));
                            addTextureCoords(texCoord, block.getTextureFront(), true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(lighting.calculateLight(blockContainer, i, j, k));
                            light.add(lighting.calculateLight(blockContainer, i, j + 1, k));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j + 1, k));
                            light.add(lighting.calculateLight(blockContainer, i + 1, j, k));

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

    public Mesh calculateMesh(BlockContainer blockContainer, LightingCalculator lighting, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return basicTriangulation(blockContainer, lighting, xMin, yMin, zMin, xMax, yMax, zMax);
    }
}
