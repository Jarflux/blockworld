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
public class FlatTerrainGenerator implements ChunkGenerator {

    public void fillChunk(BlockWorld world, Chunk cnk) {
        for (int x = cnk.getX(); x < cnk.getX() + Chunk.CHUNK_SIZE; x++) {
            for (int z = cnk.getZ(); z < cnk.getZ() + Chunk.CHUNK_SIZE; z++) {
                for (int y = cnk.getY(); y < cnk.getY() + Chunk.CHUNK_SIZE; y++) {
                    if (y < -5) {
                        cnk.addBlock(3, x, y, z);
                    } else if (y < 0) {
                        cnk.addBlock(0, x, y, z);
                    } else if (y == 0) {
                        cnk.addBlock(1, x, y, z);
                    }
                }
            }
        }
    }
}
