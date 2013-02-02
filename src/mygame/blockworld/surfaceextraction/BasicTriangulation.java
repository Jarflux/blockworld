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

    public static Mesh basicTriangulation(BlockWorld world, Chunk chunk) {
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

                            light.add(getAvgLightAlpha(world, i, j + 1, k));
                            light.add(getAvgLightAlpha(world, i, j + 1, k + 1));
                            light.add(getAvgLightAlpha(world, i + 1, j + 1, k + 1));
                            light.add(getAvgLightAlpha(world, i + 1, j + 1, k));


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
                            addTextureCoords(texCoord, block.getTextureBottom(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(getAvgLightAlpha(world, i, j, k));
                            light.add(getAvgLightAlpha(world, i + 1, j, k));
                            light.add(getAvgLightAlpha(world, i + 1, j, k + 1));
                            light.add(getAvgLightAlpha(world, i, j, k + 1));


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
                            addTextureCoords(texCoord, block.getTextureRight(), true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(getAvgLightAlpha(world, i + 1, j, k));
                            light.add(getAvgLightAlpha(world, i + 1, j + 1, k));
                            light.add(getAvgLightAlpha(world, i + 1, j + 1, k + 1));
                            light.add(getAvgLightAlpha(world, i + 1, j, k + 1));

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
                            addTextureCoords(texCoord, block.getTextureLeft(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(getAvgLightAlpha(world, i, j, k));
                            light.add(getAvgLightAlpha(world, i, j, k + 1));
                            light.add(getAvgLightAlpha(world, i, j + 1, k + 1));
                            light.add(getAvgLightAlpha(world, i, j + 1, k));

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
                            addTextureCoords(texCoord, block.getTextureBack(), false);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(getAvgLightAlpha(world, i, j, k + 1));
                            light.add(getAvgLightAlpha(world, i + 1, j, k + 1));
                            light.add(getAvgLightAlpha(world, i + 1, j + 1, k + 1));
                            light.add(getAvgLightAlpha(world, i, j + 1, k + 1));
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
                            addTextureCoords(texCoord, block.getTextureFront(), true);
                            indexes.add(index);
                            indexes.add(index + 1);
                            indexes.add(index + 2); // triangle 1
                            indexes.add(index);
                            indexes.add(index + 2);
                            indexes.add(index + 3); // triangle 2
                            index = index + 4;

                            light.add(getAvgLightAlpha(world, i, j, k));
                            light.add(getAvgLightAlpha(world, i, j + 1, k));
                            light.add(getAvgLightAlpha(world, i + 1, j + 1, k));
                            light.add(getAvgLightAlpha(world, i + 1, j, k));

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

    /*krijg vertex coordinaten x, y, z mee (.5 waarden)
     zet divider, sunlight, red, green, blue op 0
     zoek de absolute coordinaat(xA.yA,zA) van het blockje linksonder de vertex -> x -0.5, y-0.5, z-0.5 
     for xA -> xA +1     
     for yA -> yA +1
     for zA -> zA +1
     if block(xA,yA,zA) != null {
     divider ++
     sunlight += getSunlight
     red += getConstantLightColor.x
     ..
     }
     }
     }
     if divider = 0
     return kleur 0,0,0,0
     else 
     return kleur red/divider, green /divider,..  
     */
    public static Vector4f getAvgLightAlpha(BlockWorld world, int x, int y, int z) {
        int constantSamples = 0;
        int pulseSamples = 0;
        int sunSamples = 0;
        float constantLightColorRed = 0;
        float constantLightColorGreen = 0;
        float constantLightColorBlue = 0;
        float pulseLightColorRed = 0;
        float pulseLightColorGreen = 0;
        float pulseLightColorBlue = 0;
        float sunlight = 0;
        Block b;
        for (int i = x - 1; i <= x; i++) {
            for (int j = y - 1; j <= y; j++) {
                for (int k = z - 1; k <= z; k++) {
                    b = world.getBlock(i, j, k);
                    if (b==null) {
                        Vector3f constantLightColor = world.getConstantLightColor(i, j, k);
                        if(constantLightColor.x > 0.0f || constantLightColor.y > 0.0f || constantLightColor.z > 0.0f){
                            constantLightColorRed += constantLightColor.x;
                            constantLightColorGreen += constantLightColor.y;
                            constantLightColorBlue += constantLightColor.z;
                            constantSamples++;
                        }
                        Vector3f pulseLightColor = world.getPulseLightColor(i, j, k);
                        if(pulseLightColor.x > 0.0f || pulseLightColor.y > 0.0f || pulseLightColor.z > 0.0f){
                            pulseLightColorRed += pulseLightColor.x;
                            pulseLightColorGreen += pulseLightColor.y;
                            pulseLightColorBlue += pulseLightColor.z;
                            pulseSamples++;
                        }
                        sunlight = sunlight + world.getSunlightValue(i, j, k);
                        sunSamples++;
                    }
                }
            }
        }
//        float ambientOcclusion = 1f;
//        if (maxSamples < 3) {
//            ambientOcclusion = 0.65f;
//        }
//        if (maxSamples < 2) {
//            ambientOcclusion = 0.3f;
//        }
//        if (maxSamples == 0) {
//            return new Vector4f(0f, 0f, 0f, 0f);
//        }

        float constantRed = constantLightColorRed / constantSamples;
        float constantGreen = constantLightColorGreen / constantSamples;
        float constantBlue = constantLightColorBlue / constantSamples;

        float pulseRed = pulseLightColorRed / pulseSamples;
        float pulseGreen = pulseLightColorGreen / pulseSamples;
        float pulseBlue = pulseLightColorBlue / pulseSamples;

        return new Vector4f(MathUtil.packColorIntoFloat(constantRed, constantGreen, constantBlue), MathUtil.packColorIntoFloat(pulseRed, pulseGreen, pulseBlue), 0f, sunlight / sunSamples);
    }

    public Mesh calculateMesh(BlockWorld world, Chunk chunk) {
        return basicTriangulation(world, chunk);
    }
}
