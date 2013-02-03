/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.surfaceextraction;

import com.jme3.math.Vector3f;
import mygame.blockworld.Block;

/**
 *
 * @author Fusion
 */
public interface BlockContainer {
    Block getBlock(int x, int y, int z);
    Vector3f getNormal(int x, int y, int z);
    Vector3f getConstantLightColor(int x, int y, int z);
    Vector3f getPulseLightColor(int x, int y, int z);
    float getSunlightValue(int x, int y, int z);
}
