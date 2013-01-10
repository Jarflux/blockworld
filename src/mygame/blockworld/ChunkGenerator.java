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
        float plateau = r.nextFloat()*4.5f;
        Noise noise = new Noise(cnk.CHUNK_SIZE);
        for (int x = cnk.fXC; x < cnk.fXC + cnk.CHUNK_SIZE; x++) {
            for (int z = cnk.fZC; z < cnk.fZC + cnk.CHUNK_SIZE; z++) {
                float calculatedHeight = (noise.getMap()[x-cnk.fXC][z-cnk.fZC])* plateau;
                for (int y = cnk.fYC; y < cnk.fYC + cnk.CHUNK_SIZE; y++) {       
                    if (y < calculatedHeight) {
                        cnk.addBlock(1, x, y, z);
                    }
                }
            }
        }
    }
}
