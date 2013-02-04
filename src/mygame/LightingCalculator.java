/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.math.Vector4f;
import mygame.blockworld.Coordinate;
import mygame.blockworld.surfaceextraction.BlockContainer;

/**
 *
 * @author Fusion
 */
public interface LightingCalculator {
   Vector4f calculateLight(BlockContainer container, Coordinate coordinate); 
}
