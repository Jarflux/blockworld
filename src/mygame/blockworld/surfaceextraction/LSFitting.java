///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package mygame.blockworld.surfaceextraction;
//
//import com.jme3.math.Vector2f;
//import com.jme3.math.Vector3f;
//import com.jme3.math.Vector4f;
//import com.jme3.scene.Mesh;
//import com.jme3.scene.VertexBuffer;
//import com.jme3.util.BufferUtils;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import mygame.blockworld.Block;
//import mygame.blockworld.BlockWorld;
//import mygame.blockworld.Chunk;
//
///**
// *
// * @author Nathan
// */
//public class LSFitting implements MeshCreator {
//    
//    private static void addTextureCoords(List<Vector2f> texCoord, int texId, boolean swap) {
//        float texIdX = texId % 16;
//        float texIdY = (texId - texIdX) / 16;
//        if (swap) {
//            texCoord.add(new Vector2f(texIdX / 16f, texIdY / 16f));
//            texCoord.add(new Vector2f(texIdX / 16f, (texIdY + 1f) / 16f));
//            texCoord.add(new Vector2f((texIdX + 1f) / 16f, (texIdY + 1f) / 16f));
//            texCoord.add(new Vector2f((texIdX + 1f) / 16f, texIdY / 16f));
//        } else {
//            texCoord.add(new Vector2f(texIdX / 16f, texIdY / 16f));
//            texCoord.add(new Vector2f((texIdX + 1f) / 16f, texIdY / 16f));
//            texCoord.add(new Vector2f((texIdX + 1f) / 16f, (texIdY + 1f) / 16f));
//            texCoord.add(new Vector2f(texIdX / 16f, (texIdY + 1f) / 16f));
//        }
//    }
//    
//    private static final int NORMAL_SMOOTHNESS = 2; //min 1
//    
//    //wrong but simple normal calculation
//    //TODO calculate normal using least squares aproximation
//    private static Vector3f calculateNormal(BlockWorld world, Chunk chunk, int x, int y, int z)
//    {   
//        Vector3f position = new Vector3f(x - .5f, y - .5f, z - .5f);
//        Vector3f normal = new Vector3f(0,0,0);
//        Set<Coordinate> connectedCorners = new HashSet<Coordinate>();
//        findConnectedCorners(world, chunk, new Coordinate(x, y, z), false, true, NORMAL_SMOOTHNESS, connectedCorners);
//        for(Coordinate corner : connectedCorners) {
//            normal.addLocal(position.subtract(new Vector3f(corner.x - .5f, corner.y - .5f, corner.z - .5f)));
//        }
//        return normal.normalizeLocal();
//    }
//    
//    private static final int BLOCK_SMOOTHNESS = 2; //min 0
//    private static Vector3f calculateVertexPosition(BlockWorld world, Chunk chunk, int x, int y, int z) {
//        float xOriginal = x - .5f;
//        float yOriginal = y - .5f;
//        float zOriginal = z - .5f;
//        
//        if(BLOCK_SMOOTHNESS == 0) {
//            return new Vector3f(xOriginal, yOriginal, zOriginal);
//        }
//        
//        Set<Coordinate> connectedCorners = new HashSet<Coordinate>();
//        findConnectedCorners(world, chunk, new Coordinate(x, y, z), false, false, BLOCK_SMOOTHNESS, connectedCorners);
//        
//        float u = 0f;
//        float v = 0f;
//        float w = 0f;
//        float t = 0f;
//        
//        float samples = 0;
//        
//        for(Coordinate corner : connectedCorners) {
//            int distance = Math.abs(x - corner.x) + Math.abs(y - corner.y) + Math.abs(z - corner.z);
//            Vector3f normal = calculateNormal(world, chunk, corner.x, corner.y, corner.z);
//            if(!normal.equals(Vector3f.ZERO)) {
//                u += normal.x * distance;
//                v += normal.y * distance;
//                w += normal.z * distance;
//                //calculate t from the formula ux + vy + wz + t = 0; (u,v,w) is the normal
//                t += (- normal.x * (corner.x - .5f) - normal.y * (corner.y - .5f) - normal.z * (corner.z - .5f)) * distance;
//                
//                samples += 1f * distance;
//            }
//        }
//        u = u / samples;
//        v = v / samples;
//        w = w / samples;
//        t = t / samples;
//        
//        //calculate r from the formula projectedPoint = originalPoint + r * normal
//        float r = (xOriginal * u + yOriginal * v + zOriginal * w + t) / (u * u + v * v + w * w);
//        float xP = xOriginal - r * u;
//        float yP = yOriginal - r * v;
//        float zP = zOriginal - r * w;
//
//        Vector3f newPosition = new Vector3f(xP, yP, zP);
//        
//        return newPosition;
//    }
//    
//    private static class Coordinate {
//        public int x;
//        public int y;
//        public int z;
//
//        public Coordinate(int x, int y, int z) {
//            this.x = x;
//            this.y = y;
//            this.z = z;
//        }
//        
//    }
//    
//    /**
//     * Finds the corners connected to the start corner.
//     * @param world
//     * @param chunk
//     * @param start Corner for which to find the connected corners. Corners are identified by the block coordinates who have that corner as the one with the lowest x, y & z values.
//     * @param followGroundEdges If the algoritm must follow edges that are touching 4 empty blocks
//     * @param followAirEdges If the algoritm must follow edges that are touching 4 filled blocks
//     * @param recursionDepth Maximum distance from the given coordinate to find connected corners
//     * @param connectedCoordinates The set that will be filled with the connected corners
//     */
//    private static void findConnectedCorners(BlockWorld world, Chunk chunk, Coordinate start, boolean followGroundEdges, boolean followAirEdges, int recursionDepth, Set<Coordinate> connectedCoordinates) {
//        connectedCoordinates.add(start);
//        if(recursionDepth == 0) {
//            return;
//        }
//        
//        int block000 = world.getBlock(start.x-1, start.y-1, start.z-1) != null ? 1 : 0;
//        int block001 = world.getBlock(start.x-1, start.y-1, start.z) != null ? 1 : 0;
//        int block010 = world.getBlock(start.x-1, start.y, start.z-1) != null ? 1 : 0;
//        int block011 = world.getBlock(start.x-1, start.y, start.z) != null ? 1 : 0;
//        int block100 = world.getBlock(start.x, start.y-1, start.z-1) != null ? 1 : 0;
//        int block101 = world.getBlock(start.x, start.y-1, start.z) != null ? 1 : 0;
//        int block110 = world.getBlock(start.x, start.y, start.z-1) != null ? 1 : 0;
//        int block111 = world.getBlock(start.x, start.y, start.z) != null ? 1 : 0;
//        
//        int edgeXNeg = block000 + block001 + block010 + block011;
//        int edgeXPos = block100 + block101 + block110 + block111;
//        int edgeYNeg = block000 + block001 + block100 + block101;
//        int edgeYPos = block010 + block011 + block110 + block111;
//        int edgeZNeg = block000 + block010 + block100 + block110;
//        int edgeZPos = block001 + block011 + block101 + block111;
//        
//        if((followGroundEdges || edgeXNeg >= 1) && (followAirEdges || edgeXNeg < 4)) {
//            findConnectedCorners(world, chunk, new Coordinate(start.x-1,start.y,start.z), followGroundEdges, followAirEdges, recursionDepth-1, connectedCoordinates);
//        }
//        if((followGroundEdges || edgeXPos >= 1) && (followAirEdges || edgeXPos < 4)) {
//            findConnectedCorners(world, chunk, new Coordinate(start.x+1,start.y,start.z), followGroundEdges, followAirEdges, recursionDepth-1, connectedCoordinates);
//        }
//        if((followGroundEdges || edgeYNeg >= 1) && (followAirEdges || edgeYNeg < 4)) {
//            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y-1,start.z), followGroundEdges, followAirEdges, recursionDepth-1, connectedCoordinates);
//        }
//        if((followGroundEdges || edgeYPos >= 1) && (followAirEdges || edgeYPos < 4)) {
//            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y+1,start.z), followGroundEdges, followAirEdges, recursionDepth-1, connectedCoordinates);
//        }
//        if((followGroundEdges || edgeZNeg >= 1) && (followAirEdges || edgeZNeg < 4)) {
//            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y,start.z-1), followGroundEdges, followAirEdges, recursionDepth-1, connectedCoordinates);
//        }
//        if((followGroundEdges || edgeZPos >= 1) && (followAirEdges || edgeZPos < 4)) {
//            findConnectedCorners(world, chunk, new Coordinate(start.x,start.y,start.z+1), followGroundEdges, followAirEdges, recursionDepth-1, connectedCoordinates);
//        }
//    }
//
//    public static Mesh computeLSFit(BlockWorld world, Chunk chunk) {
//        List<Vector3f> vertices = new ArrayList<Vector3f>();
//        List<Vector3f> normals = new ArrayList<Vector3f>();
//        List<Vector2f> texCoord = new ArrayList<Vector2f>();
//        List<Integer> indexes = new ArrayList<Integer>();
//        List<Vector4f> light = new ArrayList<Vector4f>();
//        int index = 0;
//        Vector4f lightAlpha;
//        Vector3f lightColor;
//        for (int i = chunk.getX(); i < chunk.getX() + Chunk.CHUNK_SIZE; i++) {
//            for (int j = chunk.getY(); j < chunk.getY() + Chunk.CHUNK_SIZE; j++) {
//                for (int k = chunk.getZ(); k < chunk.getZ() + Chunk.CHUNK_SIZE; k++) {
//                    Block block = world.getBlock(i, j, k);
//                    if (block != null) {
//
//                        //Check top
//                        if (world.getChunk(i, j + 1, k, true).get(i, j + 1, k) == null) {
//                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k));
//                            normals.add(calculateNormal(world, chunk, i, j + 1, k));
//                            normals.add(calculateNormal(world, chunk, i, j + 1, k + 1));
//                            normals.add(calculateNormal(world, chunk, i + 1, j + 1, k + 1));
//                            normals.add(calculateNormal(world, chunk, i + 1, j + 1, k));
//                            addTextureCoords(texCoord, block.getTextureTop(), false);
//                            indexes.add(index);
//                            indexes.add(index + 1);
//                            indexes.add(index + 2); // triangle 1
//                            indexes.add(index);
//                            indexes.add(index + 2);
//                            indexes.add(index + 3); // triangle 2
//                            index = index + 4;
//                            
//                            lightColor = world.getConstantLightColor(i, j+1, k);
//                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j+1, k));
//                            
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            
//                        }
//                        //Check bottom
//                        if (world.getChunk(i, j - 1, k, true).get(i, j - 1, k) == null) {
//                            vertices.add(calculateVertexPosition(world, chunk, i, j, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j, k + 1));
//                            normals.add(calculateNormal(world, chunk, i, j, k));
//                            normals.add(calculateNormal(world, chunk, i + 1, j, k));
//                            normals.add(calculateNormal(world, chunk, i + 1, j, k + 1));
//                            normals.add(calculateNormal(world, chunk, i, j, k + 1));
//                            addTextureCoords(texCoord, block.getTextureBottom(), false);
//                            indexes.add(index);
//                            indexes.add(index + 1);
//                            indexes.add(index + 2); // triangle 1
//                            indexes.add(index);
//                            indexes.add(index + 2);
//                            indexes.add(index + 3); // triangle 2
//                            index = index + 4;
//                            
//                            lightColor = world.getConstantLightColor(i, j-1, k);
//                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j-1, k));
//                            
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            
//                        }
//                        //Check right
//                        if (world.getChunk(i + 1, j, k, true).get(i + 1, j, k) == null) {
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k + 1));
//                            normals.add(calculateNormal(world, chunk, i + 1, j, k));
//                            normals.add(calculateNormal(world, chunk, i + 1, j + 1, k));
//                            normals.add(calculateNormal(world, chunk, i + 1, j + 1, k + 1));
//                            normals.add(calculateNormal(world, chunk, i + 1, j, k + 1));
//                            addTextureCoords(texCoord, block.getTextureRight(), true);
//                            indexes.add(index);
//                            indexes.add(index + 1);
//                            indexes.add(index + 2); // triangle 1
//                            indexes.add(index);
//                            indexes.add(index + 2);
//                            indexes.add(index + 3); // triangle 2
//                            index = index + 4;
//                            
//                            lightColor = world.getConstantLightColor(i+1, j, k);
//                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i+1, j, k));
//                            
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                        }
//                        //Check left
//                        if (world.getChunk(i - 1, j, k, true).get(i - 1, j, k) == null) {
//                            vertices.add(calculateVertexPosition(world, chunk, i, j, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k));
//                            normals.add(calculateNormal(world, chunk, i, j, k));
//                            normals.add(calculateNormal(world, chunk, i, j, k + 1));
//                            normals.add(calculateNormal(world, chunk, i, j + 1, k + 1));
//                            normals.add(calculateNormal(world, chunk, i, j + 1, k));
//                            addTextureCoords(texCoord, block.getTextureLeft(), false);
//                            indexes.add(index);
//                            indexes.add(index + 1);
//                            indexes.add(index + 2); // triangle 1
//                            indexes.add(index);
//                            indexes.add(index + 2);
//                            indexes.add(index + 3); // triangle 2
//                            index = index + 4;
//                            
//                            lightColor = world.getConstantLightColor(i-1, j, k);
//                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i-1, j, k));
//                            
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                        }
//                        //Check back
//                        if (world.getChunk(i, j, k + 1, true).get(i, j, k + 1) == null) {
//                            vertices.add(calculateVertexPosition(world, chunk, i, j, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k + 1));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k + 1));
//                            normals.add(calculateNormal(world, chunk, i, j, k + 1));
//                            normals.add(calculateNormal(world, chunk, i + 1, j, k + 1));
//                            normals.add(calculateNormal(world, chunk, i + 1, j + 1, k + 1));
//                            normals.add(calculateNormal(world, chunk, i, j + 1, k + 1));
//                            addTextureCoords(texCoord, block.getTextureBack(), false);
//                            indexes.add(index);
//                            indexes.add(index + 1);
//                            indexes.add(index + 2); // triangle 1
//                            indexes.add(index);
//                            indexes.add(index + 2);
//                            indexes.add(index + 3); // triangle 2
//                            index = index + 4;
//                            
//                            lightColor = world.getConstantLightColor(i, j, k+1);
//                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j, k+1));
//                            
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                        }
//                        //Check front
//                        if (world.getChunk(i, j, k - 1, true).get(i, j, k - 1) == null) {
//                            vertices.add(calculateVertexPosition(world, chunk, i, j, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i, j + 1, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j + 1, k));
//                            vertices.add(calculateVertexPosition(world, chunk, i + 1, j, k));
//                            normals.add(calculateNormal(world, chunk, i, j, k));
//                            normals.add(calculateNormal(world, chunk, i, j + 1, k));
//                            normals.add(calculateNormal(world, chunk, i + 1, j + 1, k));
//                            normals.add(calculateNormal(world, chunk, i + 1, j, k));
//                            addTextureCoords(texCoord, block.getTextureFront(), true);
//                            indexes.add(index);
//                            indexes.add(index + 1);
//                            indexes.add(index + 2); // triangle 1
//                            indexes.add(index);
//                            indexes.add(index + 2);
//                            indexes.add(index + 3); // triangle 2
//                            index = index + 4;
//                            
//                            lightColor = world.getConstantLightColor(i, j, k-1);
//                            lightAlpha = new Vector4f(lightColor.x, lightColor.y, lightColor.z,  world.getSunlightValue(i, j, k-1));
//                            
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                            light.add(lightAlpha);
//                        }
//                    }
//                }
//            }
//        }
//        if (index == 0) {
//            return null;
//        }
//        Vector3f[] verticesSimpleType = new Vector3f[vertices.size()];
//        Vector3f[] normalsSimpleType = new Vector3f[vertices.size()];
//        Vector2f[] texCoordSimpleType = new Vector2f[vertices.size()];
//        Vector4f[] lightSimpleType = new Vector4f[vertices.size()];  //shader
//        int[] indicesSimpleType = new int[indexes.size()];
//        for (int i = 0; i < vertices.size(); i++) {
//            verticesSimpleType[i] = vertices.get(i);
//            normalsSimpleType[i] = normals.get(i);
//            texCoordSimpleType[i] = texCoord.get(i);
//            lightSimpleType[i] = light.get(i);  //shader
//        }
//        for (int i = 0; i < indexes.size(); i++) {
//            indicesSimpleType[i] = indexes.get(i);
//        }
//        Mesh mesh = new Mesh();
//        mesh.setBuffer(VertexBuffer.Type.Color, 4, BufferUtils.createFloatBuffer(lightSimpleType)); //shader
//        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(verticesSimpleType));
//        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normalsSimpleType));
//        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoordSimpleType));
//        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indicesSimpleType));
//        mesh.updateCounts();
//        mesh.updateBound();
//        return mesh;
//    }
//
//    public Mesh calculateMesh(BlockWorld world, Chunk chunk) {
//        return computeLSFit(world, chunk);
//    }
//}
