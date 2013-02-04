/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.chunkgenerators;

import mygame.blockworld.BasicBlock;
import mygame.blockworld.BlockInfo.BlockType;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;
import mygame.blockworld.Coordinate;

/**
 *
 * @author Nathan
 */
public class FlatTerrainGenerator implements ChunkGenerator {

    public void fillChunk(BlockWorld world, Chunk cnk) {
        Coordinate coordinate = cnk.getCoordinate();
        for (int x = coordinate.x; x < coordinate.x + Chunk.CHUNK_SIZE; x++) {
            for (int z = coordinate.z; z < coordinate.z + Chunk.CHUNK_SIZE; z++) {
                for (int y = coordinate.y; y < coordinate.y + Chunk.CHUNK_SIZE; y++) {
                    if (y < -5) {
                        cnk.addBlock(new BasicBlock(new Coordinate(x, y, z), BlockType.STONE), false);
                    } else if (y < 0) {
                        cnk.addBlock(new BasicBlock(new Coordinate(x, y, z), BlockType.DIRT), false);
                    } else if (y == 0) {
                        cnk.addBlock(new BasicBlock(new Coordinate(x, y, z), BlockType.SNOW), false);
                    }
                    if (x == 0 && y == 0 && z == 00) {
                        for (int i = 0; i < 7; i++) {
                            for (int j = 1; j < 7; j++) {
                                for (int k = 0; k < 7; k++) {
                                    if (j == 6) {
                                        cnk.addBlock(new BasicBlock(new Coordinate(i, j, k), BlockType.STONE), false);
                                    } else {
                                        if (i == 0 || i == 6) {
                                            cnk.addBlock(new BasicBlock(new Coordinate(i, j, k), BlockType.STONE), false);
                                        }
                                        if (k == 0 || k == 6) {
                                            cnk.addBlock(new BasicBlock(new Coordinate(i, j, k), BlockType.STONE), false);
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
