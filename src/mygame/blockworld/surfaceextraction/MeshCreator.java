/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.surfaceextraction;

import com.jme3.scene.Mesh;
import mygame.LightingCalculator;

/**
 *
 * @author Nathan
 */
public interface MeshCreator {

    // Max values are exlusive: min <= iterator < max
    Mesh calculateMesh(BlockContainer blockContainer, LightingCalculator lightingCalculator, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax);
}
