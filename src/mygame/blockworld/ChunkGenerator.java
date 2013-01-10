/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.Random;

/**
 *
 * @author Nathan & Ben
 */
public class ChunkGenerator {
    private Random r;
  
    public ChunkGenerator() {
        r = new Random();
    }
          
    public void fillChunk(Chunk cnk) { 
        Noise noise = new Noise(cnk.CHUNK_SIZE);
        for (int x = cnk.fXC; x < cnk.fXC + cnk.CHUNK_SIZE; x++) {
            for (int z = cnk.fZC; z < cnk.fZC + cnk.CHUNK_SIZE; z++) {
                int calculatedHeight = Math.round(noise.getMap()[x-cnk.fXC][z-cnk.fZC]);
                for (int y = cnk.fYC; y < cnk.fYC + cnk.CHUNK_SIZE; y++) {       
                    if (y < calculatedHeight) {
                        cnk.addBlock(1, x, y, z);
                    }
                }
            }
        }
    }
}
