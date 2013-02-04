/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.surfaceextraction;

import com.jme3.math.Vector3f;
import mygame.blockworld.Block;
import mygame.blockworld.Coordinate;

/**
 *
 * @author Fusion
 */
public interface BlockContainer {
    Block getBlock(Coordinate coordinate);
    Vector3f getNormal(Coordinate coordinate);
    Vector3f getConstantLightColor(Coordinate coordinate);
    Vector3f getPulseLightColor(Coordinate coordinate);
    float getSunlightValue(Coordinate coordinate);
}
