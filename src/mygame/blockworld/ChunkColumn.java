/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.HashMap;
import java.util.Map;
import mygame.MathUtil;

/**
 *
 * @author Fusion
 */
public class ChunkColumn {

    private ChunkListener fLightMapUpdater = new ChunkListener() {
        public void blockAdded(Chunk chunk, Integer block, int x, int y, int z) {
            if (fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)] < y) {
                fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)] = y;
            }
        }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            if (fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)] == y) {
                fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)]--; // lower block must be found
            }
        }
    };
    private Map<Integer, Chunk> fChunks = new HashMap<Integer, Chunk>();
    private Float[][] fHeightMap = null;
    private int[][] fHightestBlockMap = new int[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];

    public ChunkColumn() {
        for (int i = 0; i < Chunk.CHUNK_SIZE; i++) {
            for (int j = 0; j < Chunk.CHUNK_SIZE; j++) {
                fHightestBlockMap[i][j] = Integer.MIN_VALUE;
            }
        }
    }

    public Float[][] getHeightMap() {
        return fHeightMap;
    }

    public int[][] getHighestBlockMap() {
        return fHightestBlockMap;
    }

    public void setHeightMap(Float[][] map) {
        fHeightMap = map.clone();
    }

    public Chunk get(int y) {
        return fChunks.get(y);
    }

    public void put(Chunk chunk) {
        chunk.addChunkListener(fLightMapUpdater);
        fChunks.put(chunk.getY()/Chunk.CHUNK_SIZE, chunk);            
        for (int i = 0; i < Chunk.CHUNK_SIZE; i++) {
            for (int j = 0; j < Chunk.CHUNK_SIZE; j++) {
                for (int k = 0; k < Chunk.CHUNK_SIZE; k++) {
                    if (chunk.get(i, j, k) != null) {                 
                        if (fHightestBlockMap[i][k] < (j + chunk.getY())) {
                            fHightestBlockMap[i][k] = (j + chunk.getY());
                        }
                    }
                }
            }
        }
    }

    public Iterable<Chunk> values() { // needed to save data to file
        return fChunks.values();
    }
}
