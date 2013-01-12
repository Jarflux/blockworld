/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.chunkgenerators;

import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Nathan
 */
public interface ChunkGenerator {
    
    void fillChunk(BlockWorld world, Chunk chunk);
    
}
