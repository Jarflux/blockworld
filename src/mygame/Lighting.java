
package mygame;

import mygame.blockworld.BlockWorld;

/**
 *
 * @author Fusion
 */
public class Lighting {

    public static final float MIN_LIGHT_VALUE = 0.08f;
    public static final float MAX_LIGHT_VALUE = 1.0f;
    public static final float SUNLIGHT_DEGRADING_CONSTANT = 26f / 27f;//26/27;
    public static final int TOTAL_DARKNESS_HEIGHT = -128;//-128;
    
    private float[][][] fLightValues;
    private boolean[][][] fIsAlreadyAdjusted;
    private BlockWorld fWorld;
    private int fXOffset, fYOffset, fZOffset;
    private static float DIVIDER = 1.5f;
    public static int RECURSION_DEPTH = 6;

    public static float[][][] calculateDiffuseMap(BlockWorld world, int xAbs, int yAbs, int zAbs, float lightValue) {
        Lighting li = new Lighting(world, xAbs, yAbs, zAbs, lightValue);
        //System.out.println("x:" + xAbs + " y:" + yAbs + " z:" + zAbs + " Light:"+ lightValue );
        //System.out.println(world.get(xAbs, yAbs, zAbs));
        //li.print(4);
        return li.fLightValues;
    }

    public Lighting(BlockWorld world, int xAbs, int yAbs, int zAbs, float lightValue) {
        int arraySize = (RECURSION_DEPTH * 2) + 1;
        int middle = arraySize / 2;
        fLightValues = new float[arraySize][arraySize][arraySize];
        fIsAlreadyAdjusted = new boolean[arraySize][arraySize][arraySize];
        fWorld = world;
        fXOffset = xAbs - RECURSION_DEPTH;
        fYOffset = yAbs - RECURSION_DEPTH;
        fZOffset = zAbs - RECURSION_DEPTH;
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
    
    // uses relative coordinates for the light array
    private void calcNeighboursLight(int x, int y, int z, int recursionDepth) {
        recursionDepth--;
        if(recursionDepth > 0) {
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
    }
    
    // uses relative coordinates for the light array
    private void setLight(int x, int y, int z, float value, int recursionDepth) {
        if((value > MIN_LIGHT_VALUE) && (fWorld.get(x + fXOffset, y + fYOffset, z + fZOffset) == null) ) {
            if (fLightValues[x][y][z] < value) {
                fLightValues[x][y][z] = value;
                fIsAlreadyAdjusted[x][y][z] = false;
            } else if (Math.abs(fLightValues[x][y][z] - value) < 0.0001f) {
                if (!fIsAlreadyAdjusted[x][y][z]) {
                    fLightValues[x][y][z] += value * (DIVIDER/4f);
                    fIsAlreadyAdjusted[x][y][z] = true;
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
