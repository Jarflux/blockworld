
package mygame;

import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Fusion
 */
public class Lighting {

    public static final float MIN_LIGHT_VALUE = 0.08f;
    public static final float MAX_LIGHT_VALUE = 1.0f;
    public static final float SUNLIGHT_DEGRADING_CONSTANT = 3f / 4f;//26/27;
    public static final int TOTAL_DARKNESS_HEIGHT = -17;//-128;
    
    private float[][][] fLightValues;
    private boolean[][][] fisAlreadyAdjusted;
    private BlockWorld fWorld;
    private int fxOffset, fyOffset, fzOffset;
    private static float DIVIDER = 2f;
    private static int RECURSION_DEPTH = 4;

    public static float[][][] calculateDiffuseMap(BlockWorld world, int x, int y, int z, float lightValue) {
        Lighting li = new Lighting(world, x, y, z, lightValue);
        //System.out.println("x:" + x + " y:" + y + " z:" + z + " Light:"+ lightValue );
        return li.fLightValues;
    }

    public Lighting(BlockWorld world, int x, int y, int z, float lightValue) {
        int arraySize = (RECURSION_DEPTH * 2) + 1;
        int middle = arraySize / 2;
        fLightValues = new float[arraySize][arraySize][arraySize];
        fisAlreadyAdjusted = new boolean[arraySize][arraySize][arraySize];
        fWorld = world;
        fxOffset = x - RECURSION_DEPTH;
        fyOffset = y - RECURSION_DEPTH;
        fzOffset = z - RECURSION_DEPTH;
        setLight(middle, middle, middle, lightValue, RECURSION_DEPTH);
    }

    private void print(int y) {
        System.out.println("----------------------------------------------------------------");
        for (int i = 0; i < fLightValues.length; i++) {
            for (int j = 0; j < fLightValues.length; j++) {
                System.out.printf("| %3.2f ", fLightValues[j][y][i]);
            }
            System.out.printf("|\n");
        }
        System.out.println("----------------------------------------------------------------");
    }

    private void calcNeighboursLight(int x, int y, int z, int recursionDepth) {
        recursionDepth--;
        float newValue = fLightValues[x][y][z] / DIVIDER;
        //top
        setLight(x, y, z - 1, newValue, recursionDepth);
        //right
        setLight(x + 1, y, z, newValue, recursionDepth);
        //bottom
        setLight(x, y, z + 1, newValue, recursionDepth);
        //left
        setLight(x - 1, y, z, newValue, recursionDepth);
        //front
        setLight(x, y + 1, z, newValue, recursionDepth);
        //back
        setLight(x, y - 1, z, newValue, recursionDepth);
    }

    private void setLight(int x, int y, int z, float value, int recursionDepth) {
        if(value > MIN_LIGHT_VALUE && !(fWorld.get(x + fxOffset, y + fyOffset, z + fzOffset) == null)) {
            if (fLightValues[x][y][z] < value) {
                fLightValues[x][y][z] = value;
                fisAlreadyAdjusted[x][y][z] = false;
            } else if (Math.abs(fLightValues[x][y][z] - value) < 0.0001f) {
                if (!fisAlreadyAdjusted[x][y][z]) {
                    fLightValues[x][y][z] += value * (DIVIDER/4f);
                    fisAlreadyAdjusted[x][y][z] = true;
                } else {
                    return;
                }
            } else {
                return;
            }
            calcNeighboursLight(x, y, z, recursionDepth);
        }
    }
}
