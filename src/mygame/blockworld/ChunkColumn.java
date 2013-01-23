/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.HashMap;
import java.util.Map;
import mygame.Lighting;
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
            System.out.println("Block added at position X:" + x +" Y:"+ y + " Z:"+ z);
            }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            if (fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)] == y) {
                fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)] = findBlockHeightBelowMe(x, y, z);
            }
            System.out.println("Block removed from position X:" + x +" Y:"+ y + " Z:"+ z);
            System.out.println("Light at position X:" + x +" Y:"+ y + " Z:"+ z + " is now "+ chunk.getSunlightValue(x, y, z));  
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
        fHeightMap = map;
    }

    public Chunk get(int y) {
        return fChunks.get((int) Math.floor((double) y / Chunk.CHUNK_SIZE));
    }

    public void put(Chunk chunk) {
        chunk.addChunkListener(fLightMapUpdater);
        fChunks.put((int) Math.floor((double) chunk.getY() / Chunk.CHUNK_SIZE), chunk);
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

    private int findBlockHeightBelowMe(int x, int y, int z) {
        for (int i = y - 1; i > Lighting.TOTAL_DARKNESS_HEIGHT; i--) {
            Chunk chunk = get((i) / Chunk.CHUNK_SIZE);
            if (chunk != null) {
                if (chunk.get(x, i, z) != null) {
                    return i;
                }
            }
        }
        return Lighting.TOTAL_DARKNESS_HEIGHT;
    }

    public float getDirectSunlight(int x, int y, int z) {
        // return de waarde van het zonlight op dit punt
        // zonlicht is steeds 1 hoger dan de hoogste block
        float lightValue = Lighting.MIN_LIGHT_VALUE;
        int highestBlock = fHightestBlockMap[MathUtil.PosMod(x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(z, Chunk.CHUNK_SIZE)];
        if (y > highestBlock && y >= 0) {
            lightValue = Lighting.MAX_LIGHT_VALUE;
        }
        if (y > highestBlock && y < 0) {
            lightValue = Math.max(Lighting.MIN_LIGHT_VALUE, (float) Math.pow(Lighting.SUNLIGHT_DEGRADING_CONSTANT, (-y)));
        }
        return lightValue;
    }
}
