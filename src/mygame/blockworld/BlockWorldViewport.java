/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Nathan & Ben
 */
public class BlockWorldViewport {
    private static final Logger logger = Logger.getLogger(BlockWorldViewport.class.getName());
    protected BlockWorld fWorld;
    protected List<Chunk> fShown = new LinkedList<Chunk>();
    int fX, fY, fZ;
    
    public static final int VIEW_WIDTH = 2;
    public static final int VIEW_LENGTH = 1;
    public static final int VIEW_HEIGHT = 2;
    
    public BlockWorldViewport(BlockWorld world) {
        fWorld = world;
        fX = 9999999;
        fY = 9999999;
        fZ = 9999999;
    }
    
    public void updatePosition(int x, int y, int z) {
        if( (x / Chunk.CHUNK_SIZE != fX / Chunk.CHUNK_SIZE) ||
                (y / Chunk.CHUNK_SIZE != fY / Chunk.CHUNK_SIZE) ||
                (z / Chunk.CHUNK_SIZE != fZ / Chunk.CHUNK_SIZE) ) {
            for(Chunk cnk : fShown) {
                cnk.hideChunk();
            }
            fShown.clear();
            int xC = x / Chunk.CHUNK_SIZE;
            int yC = y / Chunk.CHUNK_SIZE;
            int zC = z / Chunk.CHUNK_SIZE;
            for(int i = xC-VIEW_WIDTH; i <= xC+VIEW_WIDTH; i++) {
                for(int j = yC-VIEW_LENGTH; j <= yC+VIEW_LENGTH; j++) {
                    for(int k = zC-VIEW_HEIGHT; k <= zC+VIEW_HEIGHT; k++) {
                        Chunk cnk = fWorld.getChunk(i*Chunk.CHUNK_SIZE, j*Chunk.CHUNK_SIZE, k*Chunk.CHUNK_SIZE, true);
                        if(cnk != null) {
                            cnk.showChunk();
                            fShown.add(cnk);
                        }
                    }
                }
            }
        }
        fX = x;
        fY = y;
        fZ = z;
    }
    
}
