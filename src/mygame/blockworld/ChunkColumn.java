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
        public void blockAdded(Chunk chunk, Block block) {
            Coordinate blockCoordinate = block.getCoordinate();
            if (fHightestBlockMap[MathUtil.PosMod(blockCoordinate.x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(blockCoordinate.z, Chunk.CHUNK_SIZE)] < blockCoordinate.y) {
                fHightestBlockMap[MathUtil.PosMod(blockCoordinate.x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(blockCoordinate.z, Chunk.CHUNK_SIZE)] = blockCoordinate.y;
            }
        }

        public void blockRemoved(Chunk chunk, Block block) {
            Coordinate blockCoordinate = block.getCoordinate();
            if (fHightestBlockMap[MathUtil.PosMod(blockCoordinate.x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(blockCoordinate.z, Chunk.CHUNK_SIZE)] == blockCoordinate.y) {
                fHightestBlockMap[MathUtil.PosMod(blockCoordinate.y, Chunk.CHUNK_SIZE)][MathUtil.PosMod(blockCoordinate.z, Chunk.CHUNK_SIZE)] = findBlockHeightBelowMe(blockCoordinate);
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
        fHeightMap = map;
    }

    public Chunk get(int y) {
        return fChunks.get((int) Math.floor((double) y / Chunk.CHUNK_SIZE));
    }

    public void put(Chunk chunk) {
        chunk.addChunkListener(fLightMapUpdater);
        fChunks.put((int) Math.floor((double) chunk.getCoordinate().y / Chunk.CHUNK_SIZE), chunk);
        for (int i = 0; i < Chunk.CHUNK_SIZE; i++) {
            for (int j = 0; j < Chunk.CHUNK_SIZE; j++) {
                for (int k = 0; k < Chunk.CHUNK_SIZE; k++) {
                    if (chunk.getBlock(new Coordinate(i, j, k)) != null) {
                        if (fHightestBlockMap[i][k] < (j + chunk.getCoordinate().y)) {
                            fHightestBlockMap[i][k] = (j + chunk.getCoordinate().y);
                        }
                    }
                }
            }
        }
    }

    public Iterable<Chunk> values() { // needed to save data to file
        return fChunks.values();
    }

    private int findBlockHeightBelowMe(Coordinate coordinate) {
        for (int i = coordinate.y - 1; i > Lighting.TOTAL_DARKNESS_HEIGHT; i--) {
            Chunk chunk = get(i);
            if (chunk != null) {
                if (chunk.getBlock(new Coordinate(coordinate.x, i, coordinate.z)) != null) {
                    return i;
                }
            }
        }
        return Lighting.TOTAL_DARKNESS_HEIGHT;
    }

    protected float getDirectSunlight(Coordinate coordinate) {
        // return de waarde van het zonlight op dit punt
        // zonlicht is steeds 1 hoger dan de hoogste block
        float lightValue = Lighting.MIN_LIGHT_VALUE;
        int highestBlock = fHightestBlockMap[MathUtil.PosMod(coordinate.x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(coordinate.z, Chunk.CHUNK_SIZE)];
        if (coordinate.y > highestBlock && coordinate.y >= 0) {
            lightValue = Lighting.MAX_LIGHT_VALUE;
        }
        if (coordinate.y > highestBlock && coordinate.y < 0) {
            lightValue = Math.max(Lighting.MIN_LIGHT_VALUE, (float) Math.pow(Lighting.SUNLIGHT_DEGRADING_CONSTANT, (-coordinate.y)));
        }
        return lightValue;
    }

    public float getSunlightValue(Coordinate coordinate) {
        if(coordinate.y > fHightestBlockMap[MathUtil.PosMod(coordinate.x, Chunk.CHUNK_SIZE)][MathUtil.PosMod(coordinate.z, Chunk.CHUNK_SIZE)]) {
            return getDirectSunlight(coordinate);
        }
        Chunk chunk = get(coordinate.y);
        if (chunk != null) {
            Float value = chunk.getSunlightValue(coordinate);
            if (value != null) {
                return value;
            }
        }
        return Lighting.MIN_LIGHT_VALUE;
    }

}
