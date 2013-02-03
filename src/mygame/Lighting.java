package mygame;

import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import mygame.blockworld.Block;
import mygame.blockworld.surfaceextraction.BlockContainer;

/**
 *
 * @author Fusion
 */
public class Lighting implements LightingCalculator {

    public static final float MIN_LIGHT_VALUE = 0f;
    public static final float MAX_LIGHT_VALUE = 1.0f;
    public static final float SUNLIGHT_DEGRADING_CONSTANT = 26f / 27f;//26/27;
    public static final int TOTAL_DARKNESS_HEIGHT = -128;//-128;
    private static final float DIFFUSION_DIVIDER = 1.5f;
    private static final int MAX_RECURSION_DEPTH = 8;

    public static float[][][] calculateDiffuseMap(BlockContainer blockContainer, int xAbs, int yAbs, int zAbs, float lightValue) {
        // Lighting li = new Lighting(world, xAbs, yAbs, zAbs, lightValue);
        int arraySize = (MAX_RECURSION_DEPTH * 2) + 1;
        int middle = arraySize / 2;
        float[][][] lightValues = new float[arraySize][arraySize][arraySize];
        boolean[][][] isAlreadyAdjusted = new boolean[arraySize][arraySize][arraySize];
        int fXOffset = xAbs - MAX_RECURSION_DEPTH;
        int fYOffset = yAbs - MAX_RECURSION_DEPTH;
        int fZOffset = zAbs - MAX_RECURSION_DEPTH;
        Lighting.setLight(blockContainer, lightValues, isAlreadyAdjusted, MAX_RECURSION_DEPTH, middle, middle, middle, lightValue, fXOffset, fYOffset, fZOffset);

        //System.out.println("x:" + xAbs + " y:" + yAbs + " z:" + zAbs + " Light:"+ lightValue );
        //System.out.println(world.getBlock(xAbs, yAbs, zAbs));
        //li.print(lightValues, 4);
        return lightValues;
    }

    private void print(float[][][] lightValues, int y) {
        System.out.println("----------------------------------------------------------------");
        for (int i = 0; i < lightValues.length; i++) {
            for (int j = 0; j < lightValues.length; j++) {
                System.out.printf("| %3.2f ", lightValues[j][y][i]);
            }
            System.out.printf("|\n");
        }
        System.out.println("----------------------------------------------------------------");
    }

    // uses relative coordinates for the light array
    private static void calcNeighboursLight(BlockContainer blockContainer, float[][][] lightValues, boolean[][][] isAlreadyAdjusted,
            int recursionDepth, int x, int y, int z, int fXOffset, int fYOffset, int fZOffset) {
        //if(recursionDepth > 0) {
        float newValue = lightValues[x][y][z] / DIFFUSION_DIVIDER;
        //top
        setLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x, y, z - 1, newValue, fXOffset, fYOffset, fZOffset);
        //right
        setLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x + 1, y, z, newValue, fXOffset, fYOffset, fZOffset);
        //bottom
        setLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x, y, z + 1, newValue, fXOffset, fYOffset, fZOffset);
        //left
        setLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x - 1, y, z, newValue, fXOffset, fYOffset, fZOffset);
        //front
        setLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x, y + 1, z, newValue, fXOffset, fYOffset, fZOffset);
        //back
        setLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x, y - 1, z, newValue, fXOffset, fYOffset, fZOffset);
        // }
    }

    // uses relative coordinates for the light array
    private static void setLight(BlockContainer blockContainer, float[][][] lightValues, boolean[][][] isAlreadyAdjusted, int recursionDepth, int x, int y, int z,
            float value, int fXOffset, int fYOffset, int fZOffset) {
        if ((value > MIN_LIGHT_VALUE) && (blockContainer.getBlock(x + fXOffset, y + fYOffset, z + fZOffset) == null)
            || (recursionDepth == MAX_RECURSION_DEPTH)) {
            if (lightValues[x][y][z] < value) {
                lightValues[x][y][z] = value;
                isAlreadyAdjusted[x][y][z] = false;
            } else if (Math.abs(lightValues[x][y][z] - value) < 0.0001f) {
                if (!isAlreadyAdjusted[x][y][z]) {
                    lightValues[x][y][z] += value * (DIFFUSION_DIVIDER/4f);
                    isAlreadyAdjusted[x][y][z] = true;
                } else {
                    return;
                }
            } else {
                return;
            }
            recursionDepth--;
            if (recursionDepth > 0) {
                calcNeighboursLight(blockContainer, lightValues, isAlreadyAdjusted, recursionDepth, x, y, z, fXOffset, fYOffset, fZOffset);
            }
        }
    }

    public Vector4f calculateLight(BlockContainer blockContainer, int x, int y, int z) {
        return getAvgLight(blockContainer, x, y, z);
    }

    public static Vector4f getAvgLight(BlockContainer blockContainer, int x, int y, int z) {
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
                    b = blockContainer.getBlock(i, j, k);
                    if (b == null) {
                        Vector3f constantLightColor = blockContainer.getConstantLightColor(i, j, k);
                        if (constantLightColor.x > 0.0f || constantLightColor.y > 0.0f || constantLightColor.z > 0.0f) {
                            constantLightColorRed += constantLightColor.x;
                            constantLightColorGreen += constantLightColor.y;
                            constantLightColorBlue += constantLightColor.z;
                            constantSamples++;
                        }
                        Vector3f pulseLightColor = blockContainer.getPulseLightColor(i, j, k);
                        if (pulseLightColor.x > 0.0f || pulseLightColor.y > 0.0f || pulseLightColor.z > 0.0f) {
                            pulseLightColorRed += pulseLightColor.x;
                            pulseLightColorGreen += pulseLightColor.y;
                            pulseLightColorBlue += pulseLightColor.z;
                            pulseSamples++;
                        }
                        sunlight = sunlight + blockContainer.getSunlightValue(i, j, k);
                        sunSamples++;
                    }
                }
            }
        }
        float ambientOcclusion = 1f;
        if (sunSamples < 3) {
            ambientOcclusion = 0.8f;
        }
        if (sunSamples < 2) {
            ambientOcclusion = 0.8f;
        }
        if (sunSamples == 0) {
            return new Vector4f(0f, 0f, 0f, 0f);
        }
        float constantRed = constantLightColorRed / constantSamples;
        float constantGreen = constantLightColorGreen / constantSamples;
        float constantBlue = constantLightColorBlue / constantSamples;

        float pulseRed = pulseLightColorRed / pulseSamples;
        float pulseGreen = pulseLightColorGreen / pulseSamples;
        float pulseBlue = pulseLightColorBlue / pulseSamples;

        return new Vector4f(MathUtil.packColorIntoFloat(constantRed, constantGreen, constantBlue), MathUtil.packColorIntoFloat(pulseRed, pulseGreen, pulseBlue), 0f, sunlight / sunSamples * ambientOcclusion);
    }
}
