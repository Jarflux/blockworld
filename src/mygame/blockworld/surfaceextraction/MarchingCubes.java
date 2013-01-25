/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.surfaceextraction;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Nathan
 */
public class MarchingCubes implements MeshCreator{
    
    //vGetNormal() finds the gradient of the scalar field at a point
    //This gradient can be used as a very accurate vertx normal for lighting calculations
    private static Vector3f getNormal(BlockWorld world, int x, int y, int z)
    {
            Vector3f normal = new Vector3f();
            normal.x = ((world.getBlock(x-1, y, z)==null)? 0f : 1f) - ((world.getBlock(x+1, y, z)==null)? 0f : 1f);
            normal.y = ((world.getBlock(x, y-1, z)==null)? 0f : 1f) - ((world.getBlock(x, y+1, z)==null)? 0f : 1f);
            normal.z = ((world.getBlock(x, y, z-1)==null)? 0f : 1f) - ((world.getBlock(x, y, z+1)==null)? 0f : 1f);
            return normal.normalizeLocal();
    }
    
    private static class MeshPart {
        public Vector3f[] vertices;
        public Vector3f[] normals;
        public Vector2f[] texCoords;
        public int[] indices;
    }
    
    /* Code in comment is used to calculate the tables used below
     */
    private static char[] caseIDs = {0, 1, 1+2, 1+4, 2+16+32, 1+2+16+32, 2+16+32+8, 1+32+4+128, 1+16+32+128, 2+16+32+128, 1+64, 1+2+64, 2+8+64, 1+32+8+64, 1+16+32+64, 2+8+16+32+64+128, 2+4+8+16+32+128};
    
    private static int[][][] caseTriangles = {
            {},
            {{0,8,3}},
            {{9,8,1}, {8,3,1}},
            {{0,10,1}, {8,10,0}, {3,2,8}, {8,2,10}},
            {{8,0,1}, {1,7,8}, {7,1,5}},
            {{3,1,7}, {7,1,5}},
            {{0,3,8}, {11,2,5}, {2,1,5}, {11,5,7}},
            {{1,0,9}, {8,7,4}, {10,5,6}, {2,11,3}},
            {{0,9,5}, {5,6,0}, {0,6,3}, {3,6,11}},
            {{0,11,8}, {0,1,11}, {1,5,11}, {5,6,11}},
            {{3,10,6}, {5,0,8}, {0,5,10}, {10,3,0}, {8,3,6}, {8,6,5}},
            {{8,5,9}, {3,1,10}, {6,3,10}, {6,8,3}, {6,5,8}},
            {{10,2,1}, {0,5,9}, {0,6,5}, {0,3,6}, {3,11,6}},
            {{2,0,10}, {0,9,10}, {8,6,4}, {8,11,6}},
            {{0,9,10}, {0,7,3}, {7,10,6}, {0,10,7}},
            {{8,0,3}, {1,10,2}}, //inverse of case 1+4
            {{8,0,3}, {5,6,10}}, //inverse of case 1+64
        };
    
    private static Vector3f[] mirror(Vector3f mirror, Vector3f[] vertices) {
        Vector3f[] result = new Vector3f[vertices.length];
        for(int i = 0; i < vertices.length; i++) {
            if(vertices[i] != null) {
                result[i] = mirror.subtract(vertices[i]);
                result[i].x = Math.abs(result[i].x);
                result[i].y = Math.abs(result[i].y);
                result[i].z = Math.abs(result[i].z);
            }else{
                result[i] = null;
            }
        }
        return result;
    }
    
    private static Vector3f[] mirrorXY(Vector3f[] vertices) {
        return mirror(new Vector3f(0, 0, 1), vertices);
    }
    
    private static Vector3f[] mirrorXZ(Vector3f[] vertices) {
        return mirror(new Vector3f(0, 1, 0), vertices);
    }
    
    private static Vector3f[] mirrorYZ(Vector3f[] vertices) {
        return mirror(new Vector3f(1, 0, 0), vertices);
    }
    
    private static Vector3f[] rotate(Matrix3f rotationMatrix, Vector3f translationVector, Vector3f[] vertices) {
        Vector3f[] result = new Vector3f[vertices.length];
        for(int i = 0; i < vertices.length; i++) {
            if(vertices[i] != null) {
                result[i] = rotationMatrix.mult(vertices[i].subtract(translationVector)).add(translationVector);
            }else{
                result[i] = null;
            }
        }
        return result;
    }
    
    private static Vector3f[] rotateXY(int direction, Vector3f[] vertices) {
        return rotate(new Matrix3f(0, -direction, 0, direction, 0, 0, 0, 0, 1), new Vector3f(.5f, 0f, .5f), vertices);
    }
    
    private static Vector3f[] rotateXZ(int direction, Vector3f[] vertices) {
        return rotate(new Matrix3f(0, 0, direction, 0, 1, 0, -direction, 0, 0), new Vector3f(.5f, 0f, .5f), vertices);
    }
    
    private static Vector3f[] rotateYZ(int direction, Vector3f[] vertices) {
        return rotate(new Matrix3f(1, 0, 0, 0, 0, -direction, 0, direction, 0), new Vector3f(0f, .5f, .5f), vertices);
    }
    
    private static Vector3f[] invert(Vector3f[] filledCase, Vector3f[] vertices) {
        Vector3f[] result = new Vector3f[vertices.length];
        for(int i = 0; i < vertices.length; i++) {
            boolean found = false;
            for(int j = 0; !found && j < vertices.length; j++) {
                found |= (vertices[j] != null) && (filledCase[i].distanceSquared(vertices[j]) < .01f);
            }
            if(found) {
                result[i] = null;
            }else{
                result[i] = filledCase[i].clone();
            }
        }
        return result;
    }
    
    private static void completeTransformations(Vector3f[][] vertices) {
        completeTransformations(null, vertices);
    }
    
    private static void completeTransformations(Vector3f[] filledCase, Vector3f[][] vertices) {
        if(filledCase == null) {
            vertices[1] = vertices[0];
        }else{
            vertices[1] = invert(filledCase, vertices[0]);
        }
        
        vertices[2] = mirrorXY(vertices[0]);
        vertices[3] = mirrorXY(vertices[1]);
        
        vertices[4] = mirrorXZ(vertices[0]);
        vertices[5] = mirrorXZ(vertices[1]);
        vertices[6] = mirrorXZ(vertices[2]);
        vertices[7] = mirrorXZ(vertices[3]);
        
        int vertexIndex = 8;
        for(int i = 0; i < 8; i++, vertexIndex++) {
            vertices[vertexIndex] = mirrorYZ(vertices[i]);
        }
        for(int i = 0; i < 16; i++, vertexIndex++) {
            vertices[vertexIndex] = rotateXZ(1, vertices[i]);
        }
        for(int i = 0; i < 32; i++, vertexIndex++) {
            vertices[vertexIndex] = rotateYZ(1, vertices[i]);
        }
        for(int i = 0; i < 64; i++, vertexIndex++) {
            vertices[vertexIndex] = rotateXZ(1, vertices[i]);
        }
    }
    
    private static boolean[] triangleNeedsSwitch = {false, true, true, false, true, false, false, true,
                                                    true, false, false, true, false, true, true, false};
    
    public static void main(String[] args) {
        Vector3f[][] vertices = new Vector3f[128][12];
        vertices[0] = new Vector3f[12];
        for(int edge = 0; edge < 12; edge++) {
            vertices[0][edge] = new Vector3f(a2fVertexOffset[ a2iEdgeConnection[edge][0] ][0]  +  .5f * a2fEdgeDirection[edge][0], a2fVertexOffset[ a2iEdgeConnection[edge][0] ][1]  +  .5f * a2fEdgeDirection[edge][1], a2fVertexOffset[ a2iEdgeConnection[edge][0] ][2]  +  .5f * a2fEdgeDirection[edge][2]);
        }
        completeTransformations(vertices);
        
        Vector3f[] filledUpCorners = new Vector3f[8];
        for(int corner = 0; corner < 8; corner++) {
            filledUpCorners[corner] = new Vector3f(a2fVertexOffset[corner][0], a2fVertexOffset[corner][1], a2fVertexOffset[corner][2]);
        }
        
        Vector3f[][] resultingVertices = new Vector3f[256][];
        int[][] resultingTriangles = new int[256][];
        
        //fill arrays
        for(int caseCounter = 0; caseCounter < 17; caseCounter++) {
            char caseId = caseIDs[caseCounter];
            Vector3f[][] corners = new Vector3f[128][8];
            corners[0] = new Vector3f[8];
            for(int corner = 0; corner < 8; corner++) {
                if((caseId & 1<<corner) == 1<<corner) {
                    corners[0][corner] = new Vector3f(a2fVertexOffset[corner][0], a2fVertexOffset[corner][1], a2fVertexOffset[corner][2]);
                }else{
                    corners[0][corner] = null;
                }
            }
            if(caseCounter == 3 || caseCounter == 10 || caseCounter == 15 || caseCounter == 16) {
                completeTransformations(corners);
            }else{
                completeTransformations(filledUpCorners, corners);
            }
            for(int permutation = 0; permutation < 128; permutation++) {
                char permutationId = 0;
                for(int corner = 0; corner < 8; corner++) {
                    if(corners[permutation][corner] != null) {
                        for(int cornerId = 0; cornerId < 8; cornerId++) {
                            if(Math.abs(corners[permutation][corner].x - a2fVertexOffset[cornerId][0]) < 0.01
                                    && Math.abs(corners[permutation][corner].y - a2fVertexOffset[cornerId][1]) < 0.01
                                    && Math.abs(corners[permutation][corner].z - a2fVertexOffset[cornerId][2]) < 0.01) {
                                permutationId |= 1<<cornerId;
                            }
                        }
                    }
                }
                if(resultingVertices[permutationId] != null) {
                    continue;
                }
                int[] trianglePermutation = new int[caseTriangles[caseCounter].length*3];
                Vector3f[] verticesPermutation = new Vector3f[12];
                for(int triangleCounter = 0; triangleCounter < caseTriangles[caseCounter].length; triangleCounter++) {
                    if(!triangleNeedsSwitch[permutation%16]) {
                        trianglePermutation[triangleCounter*3+0] = caseTriangles[caseCounter][triangleCounter][1];
                        trianglePermutation[triangleCounter*3+1] = caseTriangles[caseCounter][triangleCounter][0];
                    }else{
                        trianglePermutation[triangleCounter*3+0] = caseTriangles[caseCounter][triangleCounter][0];
                        trianglePermutation[triangleCounter*3+1] = caseTriangles[caseCounter][triangleCounter][1];
                    }
                    trianglePermutation[triangleCounter*3+2] = caseTriangles[caseCounter][triangleCounter][2];
                    
                    verticesPermutation[caseTriangles[caseCounter][triangleCounter][0]] = vertices[permutation][caseTriangles[caseCounter][triangleCounter][0]];
                    verticesPermutation[caseTriangles[caseCounter][triangleCounter][1]] = vertices[permutation][caseTriangles[caseCounter][triangleCounter][1]];
                    verticesPermutation[caseTriangles[caseCounter][triangleCounter][2]] = vertices[permutation][caseTriangles[caseCounter][triangleCounter][2]];
                }
                resultingVertices[permutationId] = verticesPermutation;
                resultingTriangles[permutationId] = trianglePermutation;
            }
        }
        int nullCount = 0;
        System.out.println("private static float[][][] vertices = {");
        for(int i = 0; i < 256; i++) {
            System.out.print("\t\t\t{");
            if(resultingVertices[i] == null) {
                System.out.println("case is null, problem !!!},");
                nullCount++;
                continue;
            }
            for(int j = 0; j < resultingVertices[i].length; j++) {
                if(resultingVertices[i][j] == null) {
                    System.out.print("null,");
                }else{
                    System.out.print("{" + resultingVertices[i][j].x + "f," + resultingVertices[i][j].y + "f," + resultingVertices[i][j].z + "f},");
                }
            }
            System.out.println("},");
        }
        System.out.println("\t\t};");
        if(nullCount != 0) {
            System.out.println("Number of errors = " + nullCount);
            return;
        }
        System.out.println("private static int[][] triangles = {");
        for(int i = 0; i < 256; i++) {
            System.out.print("\t\t\t{");
            if(resultingTriangles[i] == null) {
                System.out.println("case is null, problem !!!},");
                continue;
            }
            for(int j = 0; j < resultingTriangles[i].length; j++) {
                System.out.print(resultingTriangles[i][j] + ",");
            }
            System.out.println("},");
        }
        System.out.println("\t\t};");
    }
    
    private static float[][][] sVERTICES = {
			{null,null,null,null,null,null,null,null,null,null,null,null,},
			{{0.5f,0.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},null,null,{1.0f,0.5f,0.0f},null,null,null,null,{1.0f,0.0f,0.5f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},null,null,},
			{{0.5f,1.0f,0.0f},null,null,{1.0f,0.5f,0.0f},null,null,null,null,{1.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,null,},
			{{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},null,null,null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,null,null,},
			{{0.5f,1.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,null,null,},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,null,},
			{{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{{0.5f,1.0f,0.0f},{0.0f,1.0f,0.5f},null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},null,null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.0f,0.5f},null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},null,null,null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},null,null,null,},
			{null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},null,null,null,null,},
			{{0.5f,0.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,null,null,},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},{0.0f,0.0f,0.5f},null,null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,},
			{null,{1.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,1.0f,0.0f},null,},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},null,null,null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,null,{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},},
			{{0.5f,0.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},},
			{null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},null,},
			{{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,null,null,{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,0.0f,1.0f},},
			{{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},null,null,{0.0f,0.5f,1.0f},},
			{{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},null,null,null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,0.0f,1.0f},null,null,{1.0f,0.5f,1.0f},null,null,null,null,{1.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{0.0f,0.0f,0.5f},null,null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,null,null,},
			{{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,1.0f,0.0f},null,},
			{{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,null,{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},},
			{{1.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},null,null,null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,1.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},},
			{{0.5f,1.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{null,{0.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},null,null,{1.0f,0.5f,1.0f},},
			{{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,null,null,{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,0.0f,1.0f},},
			{{0.5f,0.0f,1.0f},{0.0f,0.0f,0.5f},null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},null,null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},{1.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,1.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,1.0f,0.0f},},
			{{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},{1.0f,1.0f,0.5f},null,null,null,},
			{null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},null,},
			{{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,1.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,1.0f,0.0f},},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},{0.0f,1.0f,0.5f},null,null,null,},
			{{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,0.5f,1.0f},{0.0f,0.5f,0.0f},{1.0f,0.5f,0.0f},{1.0f,0.5f,1.0f},},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},null,},
			{null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},null,},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},null,null,},
			{{0.5f,1.0f,1.0f},null,null,{1.0f,0.5f,1.0f},null,null,null,null,{1.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},null,},
			{{1.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},null,null,null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,},
			{{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,null,null,{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},null,null,{0.5f,1.0f,1.0f},},
			{{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{0.0f,1.0f,0.5f},null,null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,null,{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{{0.5f,1.0f,0.0f},{0.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,null,{1.0f,0.5f,1.0f},},
			{{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{{0.5f,0.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{0.0f,1.0f,0.5f},null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{null,{0.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,null,{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{null,{1.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},null,{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},null,},
			{{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},null,{0.0f,0.5f,0.0f},{1.0f,0.5f,0.0f},{1.0f,0.5f,1.0f},{0.0f,0.5f,1.0f},},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,{1.0f,0.5f,1.0f},},
			{null,{0.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{1.0f,0.0f,0.5f},null,{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,null,{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,{1.0f,0.5f,1.0f},},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,null,{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{null,{1.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,null,{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,null,},
			{null,{1.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,},
			{{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,},
			{{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},null,null,{0.5f,1.0f,1.0f},},
			{{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},null,{0.5f,1.0f,1.0f},null,{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,{1.0f,0.5f,0.0f},},
			{null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},null,null,null,null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,},
			{{0.5f,0.0f,0.0f},null,{0.5f,1.0f,0.0f},null,{0.5f,0.0f,1.0f},null,{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},null,{0.5f,0.0f,1.0f},null,{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,{1.0f,0.5f,0.0f},},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,},
			{{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},null,null,{0.5f,1.0f,1.0f},},
			{null,{0.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,null,},
			{{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},null,null,null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{{0.5f,1.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,null,null,},
			{{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,null,null,{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},null,null,{0.5f,1.0f,1.0f},},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},null,null,null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,null,},
			{{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,null,{0.0f,0.5f,1.0f},},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,null,{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},{0.0f,1.0f,0.5f},null,null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,},
			{null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},null,},
			{{0.5f,1.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,},
			{{0.5f,1.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,1.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,null,null,},
			{{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,},
			{{0.5f,1.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},null,},
			{{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},{0.0f,1.0f,0.5f},null,null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,null,{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,null,{0.0f,0.5f,1.0f},},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},null,null,null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},null,},
			{{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,null,null,{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},null,null,{0.5f,1.0f,1.0f},},
			{{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,1.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},null,null,null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,null,},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,},
			{null,{0.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,},
			{{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},null,null,{0.5f,1.0f,1.0f},},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},null,{0.5f,0.0f,1.0f},null,{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,{1.0f,0.5f,0.0f},},
			{{0.5f,0.0f,0.0f},null,{0.5f,1.0f,0.0f},null,{0.5f,0.0f,1.0f},null,{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,},
			{null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},null,null,null,null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},null,{0.5f,1.0f,1.0f},null,{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,{1.0f,0.5f,0.0f},},
			{{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,},
			{{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},null,null,{0.5f,1.0f,1.0f},},
			{{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,},
			{null,{1.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,null,},
			{{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,null,{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},},
			{null,{1.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,null,{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,{1.0f,0.5f,1.0f},},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,null,{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},},
			{null,{0.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{1.0f,0.0f,0.5f},null,{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},null,},
			{{0.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,{1.0f,0.5f,1.0f},},
			{{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},null,{0.0f,0.5f,0.0f},{1.0f,0.5f,0.0f},{1.0f,0.5f,1.0f},{0.0f,0.5f,1.0f},},
			{null,{1.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},null,{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},{0.5f,0.0f,1.0f},null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,null,{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},},
			{null,{0.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{{0.5f,1.0f,1.0f},{0.0f,1.0f,0.5f},null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,0.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,1.0f,0.0f},{0.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,null,{1.0f,0.5f,1.0f},},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,null,{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},{0.5f,1.0f,1.0f},null,},
			{{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},{0.5f,1.0f,1.0f},{0.0f,1.0f,0.5f},null,null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,},
			{{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,null,null,{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},null,null,{0.5f,1.0f,1.0f},},
			{{1.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},null,null,null,{0.5f,1.0f,0.0f},null,{0.5f,1.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},{0.5f,1.0f,0.0f},null,null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},null,},
			{{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,1.0f,1.0f},null,{0.5f,0.0f,0.0f},null,},
			{{0.5f,0.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,1.0f,1.0f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{{0.5f,1.0f,1.0f},null,null,{1.0f,0.5f,1.0f},null,null,null,null,{1.0f,1.0f,0.5f},null,null,null,},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},null,null,},
			{null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},null,},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},null,},
			{{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,0.5f,1.0f},{0.0f,0.5f,0.0f},{1.0f,0.5f,0.0f},{1.0f,0.5f,1.0f},},
			{{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},{0.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,1.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,1.0f,0.0f},},
			{null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},{0.0f,1.0f,0.5f},null,},
			{{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},{1.0f,1.0f,0.5f},null,null,null,},
			{{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,1.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,1.0f,0.0f},},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},{1.0f,1.0f,0.5f},null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,null,null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},{0.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,1.0f},{1.0f,0.0f,0.5f},null,null,null,},
			{null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},null,null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.0f,0.5f},null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,},
			{{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,null,null,{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,0.0f,1.0f},},
			{{0.5f,0.0f,0.0f},{0.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},null,null,{1.0f,0.5f,1.0f},},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{0.0f,0.0f,0.5f},null,null,{0.0f,1.0f,0.5f},},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,},
			{null,{0.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},null,},
			{{0.5f,1.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{{0.5f,1.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},},
			{{1.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},null,null,null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,null,{1.0f,1.0f,0.5f},{1.0f,0.0f,0.5f},{0.0f,0.0f,0.5f},},
			{{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,1.0f,0.0f},null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.5f,1.0f},null,null,null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},{0.0f,0.0f,0.5f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{0.0f,0.0f,0.5f},null,null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,1.0f},null,},
			{{0.5f,0.0f,1.0f},null,null,{1.0f,0.5f,1.0f},null,null,null,null,{1.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},null,null,null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,1.0f},{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},null,null,{0.0f,0.5f,1.0f},},
			{{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,null,null,{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,0.0f,0.0f},null,null,{0.5f,0.0f,1.0f},},
			{null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,{1.0f,1.0f,0.5f},{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},null,},
			{{0.5f,0.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,null,{1.0f,0.0f,0.5f},null,{0.0f,1.0f,0.5f},},
			{{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},{0.5f,0.0f,1.0f},null,null,{0.0f,1.0f,0.5f},{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},},
			{{0.0f,1.0f,0.5f},{0.5f,1.0f,0.0f},null,null,null,{0.5f,0.0f,0.0f},null,{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,},
			{{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},{0.0f,1.0f,0.5f},{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},null,{0.5f,1.0f,0.0f},null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{1.0f,0.0f,0.5f},null,null,{1.0f,1.0f,0.5f},},
			{null,{1.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,1.0f},null,{0.5f,1.0f,0.0f},{0.5f,0.0f,0.0f},{0.5f,0.0f,1.0f},null,},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{1.0f,1.0f,0.5f},null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,},
			{{0.5f,0.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{{0.5f,0.0f,1.0f},{0.0f,0.5f,1.0f},null,null,null,{0.0f,0.5f,0.0f},null,{1.0f,0.5f,0.0f},{1.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,1.0f},{1.0f,0.0f,0.5f},{0.5f,0.0f,0.0f},{0.0f,0.0f,0.5f},null,null,null,null,{0.0f,0.5f,1.0f},null,{1.0f,0.5f,0.0f},null,},
			{null,{0.0f,0.5f,0.0f},null,{0.0f,0.5f,1.0f},null,null,null,null,{0.5f,0.0f,1.0f},{0.5f,0.0f,0.0f},null,null,},
			{{0.5f,0.0f,1.0f},null,null,{0.0f,0.5f,1.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,null,null,},
			{null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},null,null,null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.0f,0.5f},null,null,null,{1.0f,1.0f,0.5f},null,{0.0f,1.0f,0.5f},{0.0f,0.5f,0.0f},null,null,null,},
			{{0.5f,0.0f,0.0f},{0.0f,0.0f,0.5f},null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,1.0f,0.5f},{1.0f,0.5f,0.0f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,1.0f,0.5f},{1.0f,1.0f,0.5f},null,null,},
			{{0.5f,1.0f,0.0f},{0.0f,1.0f,0.5f},null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,0.0f,0.5f},{1.0f,0.5f,0.0f},null,null,null,},
			{{0.5f,1.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,0.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,},
			{null,{0.0f,1.0f,0.5f},null,{0.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,null,},
			{{0.5f,1.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,1.0f,0.5f},null,null,null,},
			{{0.5f,1.0f,0.0f},{1.0f,1.0f,0.5f},null,null,null,{1.0f,0.0f,0.5f},null,{0.0f,0.0f,0.5f},{0.0f,0.5f,0.0f},null,null,null,},
			{null,{1.0f,1.0f,0.5f},null,{1.0f,0.0f,0.5f},null,null,null,null,{0.5f,0.0f,0.0f},{0.5f,1.0f,0.0f},null,null,},
			{{0.5f,0.0f,0.0f},{1.0f,0.5f,0.0f},{0.5f,1.0f,0.0f},{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,{1.0f,1.0f,0.5f},null,},
			{{0.5f,1.0f,0.0f},null,null,{1.0f,0.5f,0.0f},null,null,null,null,{1.0f,1.0f,0.5f},null,null,null,},
			{null,{1.0f,0.5f,0.0f},null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,0.0f,0.5f},{1.0f,0.0f,0.5f},null,null,},
			{{0.5f,0.0f,0.0f},null,null,{1.0f,0.5f,0.0f},null,null,null,null,{1.0f,0.0f,0.5f},null,null,null,},
			{{0.5f,0.0f,0.0f},null,null,{0.0f,0.5f,0.0f},null,null,null,null,{0.0f,0.0f,0.5f},null,null,null,},
			{null,null,null,null,null,null,null,null,null,null,null,null,},
		};
private static int[][] sTRIANGLES = {
			{},
			{8,0,3,},
			{0,8,3,},
			{8,9,1,3,8,1,},
			{8,0,3,},
			{10,0,1,10,8,0,2,3,8,2,8,10,},
			{9,8,1,8,3,1,},
			{0,8,1,7,1,8,1,7,5,},
			{0,8,3,},
			{8,9,1,3,8,1,},
			{0,10,1,8,10,0,3,2,8,8,2,10,},
			{8,0,1,1,7,8,7,1,5,},
			{9,8,1,8,3,1,},
			{0,8,1,7,1,8,1,7,5,},
			{8,0,1,1,7,8,7,1,5,},
			{1,3,7,1,7,5,},
			{0,8,3,},
			{8,9,1,3,8,1,},
			{10,0,1,10,8,0,2,3,8,2,8,10,},
			{0,8,1,7,1,8,1,7,5,},
			{3,10,6,5,0,8,0,5,10,10,3,0,8,3,6,8,6,5,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{10,0,1,10,8,0,2,3,8,2,8,10,},
			{0,8,1,7,1,8,1,7,5,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{0,9,5,5,6,0,0,6,3,3,6,11,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{8,0,1,1,7,8,7,1,5,},
			{8,0,3,},
			{0,10,1,8,10,0,3,2,8,8,2,10,},
			{9,8,1,8,3,1,},
			{8,0,1,1,7,8,7,1,5,},
			{0,10,1,8,10,0,3,2,8,8,2,10,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{8,0,1,1,7,8,7,1,5,},
			{9,0,5,6,5,0,6,0,3,6,3,11,},
			{3,10,6,5,0,8,0,5,10,10,3,0,8,3,6,8,6,5,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{0,8,1,7,1,8,1,7,5,},
			{9,8,1,8,3,1,},
			{8,0,1,1,7,8,7,1,5,},
			{0,8,1,7,1,8,1,7,5,},
			{1,3,7,1,7,5,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{8,0,1,1,7,8,7,1,5,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{0,8,1,7,1,8,1,7,5,},
			{2,0,10,0,9,10,8,6,4,8,11,6,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{9,8,1,8,3,1,},
			{0,8,3,},
			{10,3,6,0,5,8,5,0,10,3,10,0,3,8,6,6,8,5,},
			{10,0,1,10,8,0,2,3,8,2,8,10,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{8,9,1,3,8,1,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,8,1,7,1,8,1,7,5,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{10,0,1,10,8,0,2,3,8,2,8,10,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{0,8,1,7,1,8,1,7,5,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{0,9,5,5,6,0,0,6,3,3,6,11,},
			{8,0,1,1,7,8,7,1,5,},
			{0,10,1,8,10,0,3,2,8,8,2,10,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{2,0,10,0,9,10,8,6,4,8,11,6,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{1,0,9,8,7,4,10,5,6,2,11,3,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{0,8,3,10,1,2,},
			{8,9,1,3,8,1,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,8,1,7,1,8,1,7,5,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{8,0,1,1,7,8,7,1,5,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{3,1,7,7,1,5,},
			{8,0,1,1,7,8,7,1,5,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,2,10,9,0,10,6,8,4,11,8,6,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,8,1,7,1,8,1,7,5,},
			{8,9,1,3,8,1,},
			{8,0,1,1,7,8,7,1,5,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{0,9,5,5,6,0,0,6,3,3,6,11,},
			{0,8,1,7,1,8,1,7,5,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,8,1,7,1,8,1,7,5,},
			{8,9,1,3,8,1,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{8,0,3,1,10,2,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,8,3,6,5,10,},
			{8,0,3,1,10,2,},
			{0,8,3,},
			{8,0,3,},
			{0,10,1,8,10,0,3,2,8,8,2,10,},
			{10,3,6,0,5,8,5,0,10,3,10,0,3,8,6,6,8,5,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,10,1,8,10,0,3,2,8,8,2,10,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{9,8,1,8,3,1,},
			{8,0,1,1,7,8,7,1,5,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{8,0,1,1,7,8,7,1,5,},
			{9,0,5,6,5,0,6,0,3,6,3,11,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{0,8,1,7,1,8,1,7,5,},
			{9,8,1,8,3,1,},
			{8,0,1,1,7,8,7,1,5,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{2,0,10,0,9,10,8,6,4,8,11,6,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,8,1,7,1,8,1,7,5,},
			{1,3,7,1,7,5,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{0,8,1,7,1,8,1,7,5,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{8,0,1,1,7,8,7,1,5,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{9,8,1,8,3,1,},
			{10,0,1,10,8,0,2,3,8,2,8,10,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{0,1,9,7,8,4,5,10,6,11,2,3,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{0,2,10,9,0,10,6,8,4,11,8,6,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{8,0,3,1,10,2,},
			{0,8,1,7,1,8,1,7,5,},
			{9,0,5,6,5,0,6,0,3,6,3,11,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{8,0,1,1,7,8,7,1,5,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,8,3,10,1,2,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{8,0,1,1,7,8,7,1,5,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{9,8,1,8,3,1,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,8,3,10,1,2,},
			{0,8,3,6,5,10,},
			{8,0,3,},
			{8,9,1,3,8,1,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{0,2,10,9,0,10,6,8,4,11,8,6,},
			{8,0,1,1,7,8,7,1,5,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{0,8,1,7,1,8,1,7,5,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{3,1,7,7,1,5,},
			{8,0,1,1,7,8,7,1,5,},
			{0,8,1,7,1,8,1,7,5,},
			{8,9,1,3,8,1,},
			{8,0,1,1,7,8,7,1,5,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{0,3,8,11,2,5,2,1,5,11,5,7,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{5,8,9,1,3,10,3,6,10,8,6,3,5,6,8,},
			{8,0,3,5,6,10,},
			{0,9,5,5,6,0,0,6,3,3,6,11,},
			{0,8,1,7,1,8,1,7,5,},
			{2,10,1,5,0,9,6,0,5,3,0,6,11,3,6,},
			{8,0,3,1,10,2,},
			{0,8,1,7,1,8,1,7,5,},
			{8,9,1,3,8,1,},
			{8,0,3,1,10,2,},
			{0,8,3,},
			{0,8,1,7,1,8,1,7,5,},
			{3,0,8,2,11,5,1,2,5,5,11,7,},
			{11,0,8,1,0,11,5,1,11,6,5,11,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{9,0,5,6,5,0,6,0,3,6,3,11,},
			{10,2,1,0,5,9,0,6,5,0,3,6,3,11,6,},
			{8,0,1,1,7,8,7,1,5,},
			{0,8,3,10,1,2,},
			{0,11,8,0,1,11,1,5,11,5,6,11,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{8,5,9,3,1,10,6,3,10,6,8,3,6,5,8,},
			{8,0,3,5,6,10,},
			{8,0,1,1,7,8,7,1,5,},
			{0,8,3,10,1,2,},
			{9,8,1,8,3,1,},
			{8,0,3,},
			{3,1,7,7,1,5,},
			{0,8,1,7,1,8,1,7,5,},
			{8,0,1,1,7,8,7,1,5,},
			{8,9,1,3,8,1,},
			{0,8,1,7,1,8,1,7,5,},
			{8,0,3,1,10,2,},
			{9,8,1,8,3,1,},
			{8,0,3,},
			{8,0,1,1,7,8,7,1,5,},
			{8,9,1,3,8,1,},
			{0,8,3,10,1,2,},
			{0,8,3,},
			{9,8,1,8,3,1,},
			{8,0,3,},
			{0,8,3,},
			{},
		};
    
    //Performs the Marching Cubes algorithm on a single cube
    private static MeshPart marchCube(BlockWorld world, int x, int y, int z, int scale) {
            int /*iCorner,*/ iVertex, iVertexTest/*, iEdge, iTriangle, iEdgeFlags*/;
            char iFlagIndex;
            //float fOffset;
            boolean[] afCubeValue = new boolean[8];
            //Vector3f[] asEdgeVertex = new Vector3f[12];
            //Vector3f[] asEdgeNorm = new Vector3f[12];

            //Make a local copy of the values at the cube's corners
            for(iVertex = 0; iVertex < 8; iVertex++) {
                    afCubeValue[iVertex] = world.getBlock(x + a2fVertexOffset[iVertex][0],
                                                y + a2fVertexOffset[iVertex][1],
                                                z + a2fVertexOffset[iVertex][2], true)
                                                != null;
            }

            //Find which vertices are inside of the surface and which are outside
            iFlagIndex = 0;
            for(iVertexTest = 0; iVertexTest < 8; iVertexTest++) {
                    if(afCubeValue[iVertexTest]) {
                        iFlagIndex |= 1<<iVertexTest;
                    }
            }
            /*
            //Find which edges are intersected by the surface
            iEdgeFlags = aiCubeEdgeFlags[iFlagIndex];

            //If the cube is entirely inside or outside of the surface, then there will be no intersections
            if(iEdgeFlags == 0) {
                return null;
            }
            
            //Find the point of intersection of the surface with each edge
            //Then find the normal to the surface at those points
            for(iEdge = 0; iEdge < 12; iEdge++)
            {
                    //if there is an intersection on this edge
                    if((iEdgeFlags & (1<<iEdge)) == (1<<iEdge)) {
                            fOffset = .5f;//getOffset(afCubeValue[ a2iEdgeConnection[iEdge][0] ], afCubeValue[ a2iEdgeConnection[iEdge][1] ]);
                            
                            asEdgeVertex[iEdge] = new Vector3f();
                            asEdgeVertex[iEdge].x = x + (a2fVertexOffset[ a2iEdgeConnection[iEdge][0] ][0]  +  fOffset * a2fEdgeDirection[iEdge][0]) * scale;
                            asEdgeVertex[iEdge].y = y + (a2fVertexOffset[ a2iEdgeConnection[iEdge][0] ][1]  +  fOffset * a2fEdgeDirection[iEdge][1]) * scale;
                            asEdgeVertex[iEdge].z = z + (a2fVertexOffset[ a2iEdgeConnection[iEdge][0] ][2]  +  fOffset * a2fEdgeDirection[iEdge][2]) * scale;

                            asEdgeNorm[iEdge] = getNormal(world, Math.round(asEdgeVertex[iEdge].x), Math.round(asEdgeVertex[iEdge].y), Math.round(asEdgeVertex[iEdge].z));
                    }
            }
            */
            
            //Compress the vertex && norm arrays
            int nrVertices = 0;
            for(int i = 0; i < sVERTICES[iFlagIndex].length; i++) {
                if(sVERTICES[iFlagIndex][i] != null) {
                    nrVertices++;
                }
            }
            MeshPart meshPart = new MeshPart();
            meshPart.vertices = new Vector3f[nrVertices];
            meshPart.normals = new Vector3f[nrVertices];
            meshPart.texCoords = new Vector2f[nrVertices];
            int[] mapping = new int[sVERTICES[iFlagIndex].length];
            int index = 0;
            for(int i = 0; i < sVERTICES[iFlagIndex].length; i++) {
                if(sVERTICES[iFlagIndex][i] != null) {
                    meshPart.vertices[index] = new Vector3f(x + sVERTICES[iFlagIndex][i][0], y + sVERTICES[iFlagIndex][i][1], z + sVERTICES[iFlagIndex][i][2]);
                    meshPart.normals[index] = getNormal(world, Math.round(x + sVERTICES[iFlagIndex][i][0]), Math.round(y + sVERTICES[iFlagIndex][i][1]), Math.round(z + sVERTICES[iFlagIndex][i][2]));
                    meshPart.texCoords[index] = new Vector2f(0, 0);
                    mapping[i] = index;
                    index++;
                }
            }
            
            meshPart.indices = new int[sTRIANGLES[iFlagIndex].length];
            for(int i = 0; i < sTRIANGLES[iFlagIndex].length; i++) {
                meshPart.indices[i] = mapping[sTRIANGLES[iFlagIndex][i]];
            }
            return meshPart;
            /*
            int nrTriangles = 0;
            //Calculate number of triangles that were found.  There can be up to five per cube
            for(iTriangle = 0; iTriangle < 5; iTriangle++)
            {
                    if(a2iTriangleConnectionTable[iFlagIndex][3*iTriangle] < 0) {
                            break;
                    }
                    nrTriangles++;
            }
            
            //Calculate the indices
            meshPart.indices = new int[nrTriangles*3];
            index = 0;
            for(iTriangle = 0; iTriangle < 5; iTriangle++)
            {
                    if(a2iTriangleConnectionTable[iFlagIndex][3*iTriangle] < 0) {
                            break;
                    }
                    
                    for(iCorner = 0; iCorner < 3; iCorner++) {
                        int cornerVal;
                        if(iCorner == 0) {
                            cornerVal = 1;
                        }else if(iCorner == 1) {
                            cornerVal = 0;
                        }else{
                            cornerVal = iCorner;
                        }
                        
                        iVertex = a2iTriangleConnectionTable[iFlagIndex][3*iTriangle+cornerVal];
                        meshPart.indices[index] = mapping[iVertex];
                        Vector2f texCoord = new Vector2f();
                        if(iCorner == 0) {
                            texCoord.x = 0;
                            texCoord.y = 0;
                        }else if(iCorner == 1) {
                            texCoord.x = 1;
                            texCoord.y = 0;
                        }else if(iCorner == 2) {
                            texCoord.x = 1;
                            texCoord.y = 1;
                        }
                        meshPart.texCoords[mapping[iVertex]] = texCoord;
                        index++;
                    }
            }
            return meshPart;
            */
            
    }
    
    //vMarchingCubes iterates over the entire dataset, calling vMarchCube on each cube
    public static Mesh marchingCubes(BlockWorld world, Chunk chunk) {
        List<MeshPart> meshParts = new ArrayList<MeshPart>();
        int meshVertices = 0;
        int meshIndices = 0;
        for(int x = chunk.getX(); x < chunk.getX() + Chunk.CHUNK_SIZE; x++) {
            for(int y = chunk.getY(); y < chunk.getY() + Chunk.CHUNK_SIZE; y++) {
                for(int z = chunk.getZ(); z < chunk.getZ() + Chunk.CHUNK_SIZE; z++) {
                    MeshPart meshPart = marchCube(world, x, y, z, 1);
                    if(meshPart != null) {
                        meshParts.add(meshPart);
                        meshVertices += meshPart.vertices.length;
                        meshIndices += meshPart.indices.length;
                    }
                }
            }
        }
        Vector3f[] vertices = new Vector3f[meshVertices];
        Vector3f[] normals = new Vector3f[meshVertices];
        Vector2f[] texCoords = new Vector2f[meshVertices];
        
        int[] indices = new int[meshIndices];
        int verticesIndex = 0;
        int indicesIndex = 0;
        for(MeshPart meshPart : meshParts) {
            for(int i = 0; i < meshPart.indices.length; i++) {
                indices[indicesIndex] = verticesIndex + meshPart.indices[i];
                indicesIndex++;
            }
            for(int i = 0; i < meshPart.vertices.length; i++) {
                vertices[verticesIndex] = meshPart.vertices[i];
                normals[verticesIndex] = meshPart.normals[i];
                texCoords[verticesIndex] = meshPart.texCoords[i];
                verticesIndex++;
            }
        }
        
        return fixMesh(vertices, normals, texCoords, indices);
        /*
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));   
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indices));
        mesh.updateCounts();
        mesh.updateBound();
        return mesh;*/
    }
    
    private static Mesh fixMesh(Vector3f[] vertices, Vector3f[] normals, Vector2f[] texCoords, int[] indices) {
        Map<Vector3f, Integer> verticesIndexes = new HashMap<Vector3f, Integer>();
        Set<Map.Entry<Vector3f, Vector3f>> unmatched = new HashSet<Map.Entry<Vector3f, Vector3f>>();
        for(int i = 0; i < indices.length; i++) {
            int index1 = indices[i];
            Vector3f v1 = vertices[index1];
            int index2 = indices[(i/3)*3 + (i+1)%3];
            Vector3f v2 = vertices[index2];
            int indexSwap = 0;
            Vector3f swap = null;
            if(v1.z > v2.z) {
                indexSwap = index1;
                index1 = index2;
                index2 = indexSwap;
                swap = v1;
                v1 = v2;
                v2 = swap;
            }
            if(v1.y > v2.y) {
                indexSwap = index1;
                index1 = index2;
                index2 = indexSwap;
                swap = v1;
                v1 = v2;
                v2 = swap;
            }
            if(v1.x > v2.x) {
                indexSwap = index1;
                index1 = index2;
                index2 = indexSwap;
                swap = v1;
                v1 = v2;
                v2 = swap;
            }
            Map.Entry<Vector3f, Vector3f> edge = new HashMap.SimpleEntry<Vector3f, Vector3f>(v1, v2);
            boolean isBorderEdge = false;
            if(Math.abs( v1.x - Math.floor(v1.x) ) == 0f && v1.x == v2.x) {
                int val = Math.round(v1.x) % Chunk.CHUNK_SIZE;
                isBorderEdge |= val == 0;
                isBorderEdge |= val == Chunk.CHUNK_SIZE - 1;
            }
            if(Math.abs( v1.y - Math.floor(v1.y) ) == 0f && v1.y == v2.y) {
                int val = Math.round(v1.y) % Chunk.CHUNK_SIZE;
                isBorderEdge |= val == 0;
                isBorderEdge |= val == Chunk.CHUNK_SIZE - 1;
            }
            if(Math.abs( v1.z - Math.floor(v1.z) ) == 0f && v1.z == v2.z) {
                int val = Math.round(v1.z) % Chunk.CHUNK_SIZE;
                isBorderEdge |= val == 0;
                isBorderEdge |= val == Chunk.CHUNK_SIZE - 1;
            }
            if(isBorderEdge) {
                continue;
            }
            if(unmatched.contains(edge)) {
                unmatched.remove(edge);
            }else{
                unmatched.add(edge);
                verticesIndexes.put(v1, index1);
                verticesIndexes.put(v2, index2);
            }
        }
        
        if(unmatched.size() > 0) {
            System.out.println("Number of unmatched edges = " + unmatched.size());
            for(Map.Entry<Vector3f, Vector3f> edge : unmatched) {
                System.out.println("Unmatched edge going from " + edge.getKey() + " to " + edge.getValue());
            }
        }
        /*
        List<Integer> newTriangles = new ArrayList<Integer>(unmatched.size());
        Set<Map.Entry<Vector3f, Vector3f>> loneEdges = new HashSet<Map.Entry<Vector3f, Vector3f>>();
        while(!unmatched.isEmpty()) {
            Map.Entry<Vector3f, Vector3f> edge = unmatched.iterator().next();
            unmatched.remove(edge);
            List<Map.Entry<Vector3f, Vector3f>> neighbours = new ArrayList<Map.Entry<Vector3f, Vector3f>>(2);
            for(Map.Entry<Vector3f, Vector3f> it : unmatched) {
                if(edge.getKey().equals(it.getKey()) || edge.getKey().equals(it.getValue())
                        || edge.getValue().equals(it.getKey()) || edge.getValue().equals(it.getValue())) {
                    neighbours.add(it);
                }
            }
            for(Map.Entry<Vector3f, Vector3f> it : neighbours) {
                unmatched.remove(it);
            }
            if(neighbours.isEmpty()) {
                loneEdges.add(edge);
            }else if(neighbours.size() == 1 || neighbours.size() == 2) {
                Vector3f otherV = neighbours.getBlock(0).getKey();
                Vector3f connectingV = null;
                if(otherV.equals(edge.getKey())) {
                    otherV = neighbours.getBlock(0).getValue();
                    connectingV = edge.getValue();
                }else if(otherV.equals(edge.getValue())) {
                    otherV = neighbours.getBlock(0).getValue();
                    connectingV = edge.getKey();
                }else{
                    throw new UnknownError();
                }
                if(neighbours.size() == 1) {
                    unmatched.add(new HashMap.SimpleEntry<Vector3f, Vector3f>(otherV, connectingV));
                }
                
                newTriangles.add(verticesIndexes.getBlock(edge.getKey()));
                newTriangles.add(verticesIndexes.getBlock(edge.getValue()));
                newTriangles.add(verticesIndexes.getBlock(otherV));
                
                newTriangles.add(verticesIndexes.getBlock(edge.getKey()));
                newTriangles.add(verticesIndexes.getBlock(otherV));
                newTriangles.add(verticesIndexes.getBlock(edge.getValue()));
                
                
            }else{
                throw new UnknownError();
            }
        }
        
        if(loneEdges.size() > 0) {
            System.out.println("Number of lone edges = " + loneEdges.size());
        }
        
        int[] newIndices = new int[indices.length + newTriangles.size()];
        System.arraycopy(indices, 0, newIndices, 0, indices.length);
        for(int i = 0; i < newTriangles.size(); i++) {
            newIndices[i+indices.length] = newTriangles.getBlock(i);
        }
        */
        Mesh mesh = new Mesh();
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));   
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indices));
        mesh.updateCounts();
        mesh.updateBound();
        return mesh;
    }
    
    public Mesh calculateMesh(BlockWorld world, Chunk chunk) {
        return marchingCubes(world, chunk);
    }
    
    //These tables are used so that everything can be done in little loops that you can look at all at once
    // rather than in pages and pages of unrolled code.

    //a2fVertexOffset lists the positions, relative to vertex0, of each of the 8 vertices of a cube
    private static final int[][] a2fVertexOffset = {
            {0, 0, 0},{1, 0, 0},{1, 1, 0},{0, 1, 0},
            {0, 0, 1},{1, 0, 1},{1, 1, 1},{0, 1, 1}
    };

    //a2iEdgeConnection lists the index of the endpoint vertices for each of the 12 edges of the cube
    private static final int[][] a2iEdgeConnection = {
            {0,1}, {1,2}, {2,3}, {3,0},
            {4,5}, {5,6}, {6,7}, {7,4},
            {0,4}, {1,5}, {2,6}, {3,7}
    };

    //a2fEdgeDirection lists the direction vector (vertex1-vertex0) for each edge in the cube
    private static final int[][] a2fEdgeDirection = {
            {1, 0, 0},{0, 1, 0},{-1, 0, 0},{0, -1, 0},
            {1, 0, 0},{0, 1, 0},{-1, 0, 0},{0, -1, 0},
            {0, 0, 1},{0, 0, 1},{ 0, 0, 1},{0,  0, 1}
    };

/*
    // For any edge, if one vertex is inside of the surface and the other is outside of the surface
    //  then the edge intersects the surface
    // For each of the 8 vertices of the cube can be two possible states : either inside or outside of the surface
    // For any cube the are 2^8=256 possible sets of vertex states
    // This table lists the edges intersected by the surface for all 256 possible vertex states
    // There are 12 edges.  For each entry in the table, if edge #n is intersected, then bit #n is set to 1
    private static final int[] aiCubeEdgeFlags = {
            0x000, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c, 0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00, 
            0x190, 0x099, 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c, 0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90, 
            0x230, 0x339, 0x033, 0x13a, 0x636, 0x73f, 0x435, 0x53c, 0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30, 
            0x3a0, 0x2a9, 0x1a3, 0x0aa, 0x7a6, 0x6af, 0x5a5, 0x4ac, 0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3, 0xda9, 0xca0, 
            0x460, 0x569, 0x663, 0x76a, 0x066, 0x16f, 0x265, 0x36c, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60, 
            0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0x0ff, 0x3f5, 0x2fc, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0, 
            0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x055, 0x15c, 0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950, 
            0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0x0cc, 0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0, 
            0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc6, 0xdcf, 0xec5, 0xfcc, 0x0cc, 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0, 
            0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c, 0x15c, 0x055, 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650, 
            0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc, 0x2fc, 0x3f5, 0x0ff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0, 
            0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x36c, 0x265, 0x16f, 0x066, 0x76a, 0x663, 0x569, 0x460, 
            0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac, 0x4ac, 0x5a5, 0x6af, 0x7a6, 0x0aa, 0x1a3, 0x2a9, 0x3a0, 
            0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c, 0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x033, 0x339, 0x230, 
            0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c, 0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x099, 0x190, 
            0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c, 0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x000
    };
    
    //  For each of the possible vertex states listed in aiCubeEdgeFlags there is a specific triangulation
    //  of the edge intersection points.  a2iTriangleConnectionTable lists all of them in the form of
    //  0-5 edge triples with the list terminated by the invalid value -1.
    //  For example: a2iTriangleConnectionTable[3] list the 2 triangles formed when corner[0] 
    //  and corner[1] are inside of the surface, but the rest of the cube is not.
    //
    //  I found this table in an example program someone wrote long ago.  It was probably generated by hand
    private static final int[][] a2iTriangleConnectionTable = {
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 1, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 8, 3, 9, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 3, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {9, 2, 10, 0, 2, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {2, 8, 3, 2, 10, 8, 10, 9, 8, -1, -1, -1, -1, -1, -1, -1},
            {3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 11, 2, 8, 11, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 9, 0, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 11, 2, 1, 9, 11, 9, 8, 11, -1, -1, -1, -1, -1, -1, -1},
            {3, 10, 1, 11, 10, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 10, 1, 0, 8, 10, 8, 11, 10, -1, -1, -1, -1, -1, -1, -1},
            {3, 9, 0, 3, 11, 9, 11, 10, 9, -1, -1, -1, -1, -1, -1, -1},
            {9, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 3, 0, 7, 3, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 1, 9, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 1, 9, 4, 7, 1, 7, 3, 1, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 10, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {3, 4, 7, 3, 0, 4, 1, 2, 10, -1, -1, -1, -1, -1, -1, -1},
            {9, 2, 10, 9, 0, 2, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
            {2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4, -1, -1, -1, -1},
            {8, 4, 7, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {11, 4, 7, 11, 2, 4, 2, 0, 4, -1, -1, -1, -1, -1, -1, -1},
            {9, 0, 1, 8, 4, 7, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
            {4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1, -1, -1, -1, -1},
            {3, 10, 1, 3, 11, 10, 7, 8, 4, -1, -1, -1, -1, -1, -1, -1},
            {1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4, -1, -1, -1, -1},
            {4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3, -1, -1, -1, -1},
            {4, 7, 11, 4, 11, 9, 9, 11, 10, -1, -1, -1, -1, -1, -1, -1},
            {9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {9, 5, 4, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 5, 4, 1, 5, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {8, 5, 4, 8, 3, 5, 3, 1, 5, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 10, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {3, 0, 8, 1, 2, 10, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
            {5, 2, 10, 5, 4, 2, 4, 0, 2, -1, -1, -1, -1, -1, -1, -1},
            {2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8, -1, -1, -1, -1},
            {9, 5, 4, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 11, 2, 0, 8, 11, 4, 9, 5, -1, -1, -1, -1, -1, -1, -1},
            {0, 5, 4, 0, 1, 5, 2, 3, 11, -1, -1, -1, -1, -1, -1, -1},
            {2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5, -1, -1, -1, -1},
            {10, 3, 11, 10, 1, 3, 9, 5, 4, -1, -1, -1, -1, -1, -1, -1},
            {4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10, -1, -1, -1, -1},
            {5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3, -1, -1, -1, -1},
            {5, 4, 8, 5, 8, 10, 10, 8, 11, -1, -1, -1, -1, -1, -1, -1},
            {9, 7, 8, 5, 7, 9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {9, 3, 0, 9, 5, 3, 5, 7, 3, -1, -1, -1, -1, -1, -1, -1},
            {0, 7, 8, 0, 1, 7, 1, 5, 7, -1, -1, -1, -1, -1, -1, -1},
            {1, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {9, 7, 8, 9, 5, 7, 10, 1, 2, -1, -1, -1, -1, -1, -1, -1},
            {10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3, -1, -1, -1, -1},
            {8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2, -1, -1, -1, -1},
            {2, 10, 5, 2, 5, 3, 3, 5, 7, -1, -1, -1, -1, -1, -1, -1},
            {7, 9, 5, 7, 8, 9, 3, 11, 2, -1, -1, -1, -1, -1, -1, -1},
            {9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11, -1, -1, -1, -1},
            {2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7, -1, -1, -1, -1},
            {11, 2, 1, 11, 1, 7, 7, 1, 5, -1, -1, -1, -1, -1, -1, -1},
            {9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11, -1, -1, -1, -1},
            {5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0, -1},
            {11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0, -1},
            {11, 10, 5, 7, 11, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 3, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {9, 0, 1, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 8, 3, 1, 9, 8, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
            {1, 6, 5, 2, 6, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 6, 5, 1, 2, 6, 3, 0, 8, -1, -1, -1, -1, -1, -1, -1},
            {9, 6, 5, 9, 0, 6, 0, 2, 6, -1, -1, -1, -1, -1, -1, -1},
            {5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8, -1, -1, -1, -1},
            {2, 3, 11, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {11, 0, 8, 11, 2, 0, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
            {0, 1, 9, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1, -1, -1, -1},
            {5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11, -1, -1, -1, -1},
            {6, 3, 11, 6, 5, 3, 5, 1, 3, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6, -1, -1, -1, -1},
            {3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9, -1, -1, -1, -1},
            {6, 5, 9, 6, 9, 11, 11, 9, 8, -1, -1, -1, -1, -1, -1, -1},
            {5, 10, 6, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 3, 0, 4, 7, 3, 6, 5, 10, -1, -1, -1, -1, -1, -1, -1},
            {1, 9, 0, 5, 10, 6, 8, 4, 7, -1, -1, -1, -1, -1, -1, -1},
            {10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4, -1, -1, -1, -1},
            {6, 1, 2, 6, 5, 1, 4, 7, 8, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7, -1, -1, -1, -1},
            {8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6, -1, -1, -1, -1},
            {7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9, -1},
            {3, 11, 2, 7, 8, 4, 10, 6, 5, -1, -1, -1, -1, -1, -1, -1},
            {5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11, -1, -1, -1, -1},
            {0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6, -1, -1, -1, -1},
            {9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6, -1},
            {8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6, -1, -1, -1, -1},
            {5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11, -1},
            {0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7, -1},
            {6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9, -1, -1, -1, -1},
            {10, 4, 9, 6, 4, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 10, 6, 4, 9, 10, 0, 8, 3, -1, -1, -1, -1, -1, -1, -1},
            {10, 0, 1, 10, 6, 0, 6, 4, 0, -1, -1, -1, -1, -1, -1, -1},
            {8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10, -1, -1, -1, -1},
            {1, 4, 9, 1, 2, 4, 2, 6, 4, -1, -1, -1, -1, -1, -1, -1},
            {3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4, -1, -1, -1, -1},
            {0, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {8, 3, 2, 8, 2, 4, 4, 2, 6, -1, -1, -1, -1, -1, -1, -1},
            {10, 4, 9, 10, 6, 4, 11, 2, 3, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6, -1, -1, -1, -1},
            {3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10, -1, -1, -1, -1},
            {6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1, -1},
            {9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3, -1, -1, -1, -1},
            {8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1, -1},
            {3, 11, 6, 3, 6, 0, 0, 6, 4, -1, -1, -1, -1, -1, -1, -1},
            {6, 4, 8, 11, 6, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {7, 10, 6, 7, 8, 10, 8, 9, 10, -1, -1, -1, -1, -1, -1, -1},
            {0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10, -1, -1, -1, -1},
            {10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0, -1, -1, -1, -1},
            {10, 6, 7, 10, 7, 1, 1, 7, 3, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7, -1, -1, -1, -1},
            {2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9, -1},
            {7, 8, 0, 7, 0, 6, 6, 0, 2, -1, -1, -1, -1, -1, -1, -1},
            {7, 3, 2, 6, 7, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7, -1, -1, -1, -1},
            {2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7, -1},
            {1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11, -1},
            {11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1, -1, -1, -1, -1},
            {8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6, -1},
            {0, 9, 1, 11, 6, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0, -1, -1, -1, -1},
            {7, 11, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {3, 0, 8, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 1, 9, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {8, 1, 9, 8, 3, 1, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
            {10, 1, 2, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 10, 3, 0, 8, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
            {2, 9, 0, 2, 10, 9, 6, 11, 7, -1, -1, -1, -1, -1, -1, -1},
            {6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8, -1, -1, -1, -1},
            {7, 2, 3, 6, 2, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {7, 0, 8, 7, 6, 0, 6, 2, 0, -1, -1, -1, -1, -1, -1, -1},
            {2, 7, 6, 2, 3, 7, 0, 1, 9, -1, -1, -1, -1, -1, -1, -1},
            {1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6, -1, -1, -1, -1},
            {10, 7, 6, 10, 1, 7, 1, 3, 7, -1, -1, -1, -1, -1, -1, -1},
            {10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8, -1, -1, -1, -1},
            {0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7, -1, -1, -1, -1},
            {7, 6, 10, 7, 10, 8, 8, 10, 9, -1, -1, -1, -1, -1, -1, -1},
            {6, 8, 4, 11, 8, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {3, 6, 11, 3, 0, 6, 0, 4, 6, -1, -1, -1, -1, -1, -1, -1},
            {8, 6, 11, 8, 4, 6, 9, 0, 1, -1, -1, -1, -1, -1, -1, -1},
            {9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6, -1, -1, -1, -1},
            {6, 8, 4, 6, 11, 8, 2, 10, 1, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6, -1, -1, -1, -1},
            {4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9, -1, -1, -1, -1},
            {10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3, -1},
            {8, 2, 3, 8, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1},
            {0, 4, 2, 4, 6, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8, -1, -1, -1, -1},
            {1, 9, 4, 1, 4, 2, 2, 4, 6, -1, -1, -1, -1, -1, -1, -1},
            {8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1, -1, -1, -1, -1},
            {10, 1, 0, 10, 0, 6, 6, 0, 4, -1, -1, -1, -1, -1, -1, -1},
            {4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3, -1},
            {10, 9, 4, 6, 10, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 9, 5, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 3, 4, 9, 5, 11, 7, 6, -1, -1, -1, -1, -1, -1, -1},
            {5, 0, 1, 5, 4, 0, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
            {11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5, -1, -1, -1, -1},
            {9, 5, 4, 10, 1, 2, 7, 6, 11, -1, -1, -1, -1, -1, -1, -1},
            {6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5, -1, -1, -1, -1},
            {7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2, -1, -1, -1, -1},
            {3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6, -1},
            {7, 2, 3, 7, 6, 2, 5, 4, 9, -1, -1, -1, -1, -1, -1, -1},
            {9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7, -1, -1, -1, -1},
            {3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0, -1, -1, -1, -1},
            {6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8, -1},
            {9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7, -1, -1, -1, -1},
            {1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4, -1},
            {4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10, -1},
            {7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10, -1, -1, -1, -1},
            {6, 9, 5, 6, 11, 9, 11, 8, 9, -1, -1, -1, -1, -1, -1, -1},
            {3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5, -1, -1, -1, -1},
            {0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11, -1, -1, -1, -1},
            {6, 11, 3, 6, 3, 5, 5, 3, 1, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6, -1, -1, -1, -1},
            {0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10, -1},
            {11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5, -1},
            {6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3, -1, -1, -1, -1},
            {5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2, -1, -1, -1, -1},
            {9, 5, 6, 9, 6, 0, 0, 6, 2, -1, -1, -1, -1, -1, -1, -1},
            {1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8, -1},
            {1, 5, 6, 2, 1, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6, -1},
            {10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0, -1, -1, -1, -1},
            {0, 3, 8, 5, 6, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {10, 5, 6, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {11, 5, 10, 7, 5, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {11, 5, 10, 11, 7, 5, 8, 3, 0, -1, -1, -1, -1, -1, -1, -1},
            {5, 11, 7, 5, 10, 11, 1, 9, 0, -1, -1, -1, -1, -1, -1, -1},
            {10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1, -1, -1, -1, -1},
            {11, 1, 2, 11, 7, 1, 7, 5, 1, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11, -1, -1, -1, -1},
            {9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7, -1, -1, -1, -1},
            {7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2, -1},
            {2, 5, 10, 2, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1},
            {8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5, -1, -1, -1, -1},
            {9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2, -1, -1, -1, -1},
            {9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2, -1},
            {1, 3, 5, 3, 7, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 7, 0, 7, 1, 1, 7, 5, -1, -1, -1, -1, -1, -1, -1},
            {9, 0, 3, 9, 3, 5, 5, 3, 7, -1, -1, -1, -1, -1, -1, -1},
            {9, 8, 7, 5, 9, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {5, 8, 4, 5, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1},
            {5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0, -1, -1, -1, -1},
            {0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5, -1, -1, -1, -1},
            {10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4, -1},
            {2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8, -1, -1, -1, -1},
            {0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11, -1},
            {0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5, -1},
            {9, 4, 5, 2, 11, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4, -1, -1, -1, -1},
            {5, 10, 2, 5, 2, 4, 4, 2, 0, -1, -1, -1, -1, -1, -1, -1},
            {3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9, -1},
            {5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2, -1, -1, -1, -1},
            {8, 4, 5, 8, 5, 3, 3, 5, 1, -1, -1, -1, -1, -1, -1, -1},
            {0, 4, 5, 1, 0, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5, -1, -1, -1, -1},
            {9, 4, 5, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 11, 7, 4, 9, 11, 9, 10, 11, -1, -1, -1, -1, -1, -1, -1},
            {0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11, -1, -1, -1, -1},
            {1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11, -1, -1, -1, -1},
            {3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4, -1},
            {4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2, -1, -1, -1, -1},
            {9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3, -1},
            {11, 7, 4, 11, 4, 2, 2, 4, 0, -1, -1, -1, -1, -1, -1, -1},
            {11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4, -1, -1, -1, -1},
            {2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9, -1, -1, -1, -1},
            {9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7, -1},
            {3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10, -1},
            {1, 10, 2, 8, 7, 4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 9, 1, 4, 1, 7, 7, 1, 3, -1, -1, -1, -1, -1, -1, -1},
            {4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1, -1, -1, -1, -1},
            {4, 0, 3, 7, 4, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {4, 8, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {9, 10, 8, 10, 11, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {3, 0, 9, 3, 9, 11, 11, 9, 10, -1, -1, -1, -1, -1, -1, -1},
            {0, 1, 10, 0, 10, 8, 8, 10, 11, -1, -1, -1, -1, -1, -1, -1},
            {3, 1, 10, 11, 3, 10, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 2, 11, 1, 11, 9, 9, 11, 8, -1, -1, -1, -1, -1, -1, -1},
            {3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9, -1, -1, -1, -1},
            {0, 2, 11, 8, 0, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {3, 2, 11, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {2, 3, 8, 2, 8, 10, 10, 8, 9, -1, -1, -1, -1, -1, -1, -1},
            {9, 10, 2, 0, 9, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8, -1, -1, -1, -1},
            {1, 10, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {1, 3, 8, 9, 1, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 9, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {0, 3, 8, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
            {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };
*/
}
