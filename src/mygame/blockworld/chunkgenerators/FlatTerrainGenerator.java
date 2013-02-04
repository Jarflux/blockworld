/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.chunkgenerators;

import mygame.blockworld.BasicBlock;
import mygame.blockworld.BlockInfo.BlockType;
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
                        cnk.addBlock(new BasicBlock(x, y, z, BlockType.STONE), false);
                    } else if (y < 0) {
                        cnk.addBlock(new BasicBlock(x, y, z, BlockType.DIRT), false);
                    } else if (y == 0) {
                        cnk.addBlock(new BasicBlock(x, y, z, BlockType.SNOW), false);
                    }
                    if (x == 0 && y == 0 && z == 00) {
                        for (int i = 0; i < 7; i++) {
                            for (int j = 1; j < 7; j++) {
                                for (int k = 0; k < 7; k++) {
                                    if (j == 6) {
                                        cnk.addBlock(new BasicBlock(i, j, k, BlockType.STONE), false);
                                    } else {
                                        if (i == 0 || i == 6) {
                                            cnk.addBlock(new BasicBlock(i, j, k, BlockType.STONE), false);
                                        }
                                        if (k == 0 || k == 6) {
                                            cnk.addBlock(new BasicBlock(i, j, k, BlockType.STONE), false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
