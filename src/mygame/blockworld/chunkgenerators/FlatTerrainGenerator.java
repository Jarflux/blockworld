/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.chunkgenerators;

import mygame.blockworld.Block;
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
                        cnk.addBlock(new Block(x, y, z, 3));
                    } else if (y < 0) {
                        cnk.addBlock(new Block(x, y, z, 0));
                    } else if (y == 0) {
                        cnk.addBlock(new Block(x, y, z, 1));
                    }
                }
            }
        }
    }
}
