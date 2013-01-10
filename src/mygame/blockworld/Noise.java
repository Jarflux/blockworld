/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.math.Vector3f;
import java.util.Random;

/**
 *
 * @author Ben
 */
public class Noise {

    Vector3f[][] vectors;
    float[][] map;
    private Random r;
    private Vector3f v1, v2, v3, v4;

    public Noise(int chunkSize) {
        r = new Random();
        // assign random direction vector to each field in the matrix
        vectors = new Vector3f[chunkSize][chunkSize];
        map = new float[chunkSize][chunkSize];
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                vectors[i][j] = new Vector3f((r.nextInt(10) - 5), (r.nextInt(10) - 5), (r.nextInt(10) - 5)).normalize();
            }
        }
        // for each field get 3 other neighbour vectors
        for (int i = 0; i < chunkSize; i++) {
            for (int j = 0; j < chunkSize; j++) {
                int iSign = 1;
                int jSign = 1;
                if (i > chunkSize - 2) {
                    iSign = -1;
                }
                if (j > chunkSize - 2) {
                    jSign = -1;
                }
                v1 = vectors[i][j];
                v2 = vectors[i + (1 * iSign)][j];
                v3 = vectors[i][j + (1 * jSign)];
                v4 = vectors[i + (1 * iSign)][j + (1 * jSign)];
                
                float diffX = 0.3f;
                float diffY = 0.3f;       
                float fadedX = (6 * (float)Math.pow(diffX,5)) - (15 * (float)Math.pow(diffX,4)) + (10 * (float)Math.pow(diffX,3));
                float fadedY = (6 * (float)Math.pow(diffY,5)) - (15 * (float)Math.pow(diffY,4)) + (10 * (float)Math.pow(diffY,3));
                float interpolated1 = interpolate(v1.dot(v1), v1.dot(v3) , fadedX);
                float interpolated2 = interpolate(v1.dot(v2), v1.dot(v4) , fadedX);
                
                map[i][j] = interpolate(interpolated1, interpolated2, fadedY);
            }
        }
    }
    
    public float[][] getMap()
    {
           return map;
    }
    
    private float interpolate(float flt1, float flt2, float fadedY) {
        return ((flt2 - flt1)*fadedY)+flt1;
    }
    

}
