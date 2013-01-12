/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.surfaceextraction;

import com.jme3.scene.Mesh;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Nathan
 */
public interface MeshCreator {
    
    Mesh calculateMesh(BlockWorld world, Chunk chunk);
    
}
