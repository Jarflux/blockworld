/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.scene.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.TextureAtlas;
import mygame.Lighting;
import mygame.MathUtil;

/**
 *
 * @author Nathan & Ben
 */
public class BlockWorld {

    private static final Logger logger = Logger.getLogger(BlockWorld.class.getName());
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    private ChunkListener fGeneralListener = new ChunkListener() {
        public void blockAdded(Chunk chunk, Integer block, int x, int y, int z) {
            for (ChunkListener listener : fListeners) {
                listener.blockAdded(chunk, block, x, y, z);
            }
        }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            for (ChunkListener listener : fListeners) {
                listener.blockRemoved(chunk, block, x, y, z);
            }
        }
    };
    private ChunkListener fPhysicsUpdater = new ChunkListener() {
        public void blockAdded(Chunk chunk, Integer block, int x, int y, int z) {
            update(x, y, z);
        }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            update(x, y, z);
        }

        private void update(int x, int y, int z) {
            if (x % Chunk.CHUNK_SIZE == 0) {
                Chunk cnk = getChunk(x - 1, y, z, false);
                if (cnk != null) {
                    cnk.scheduleUpdate();
                }
            }
            if (MathUtil.PosMod(x, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                Chunk cnk = getChunk(x + 1, y, z, false);
                if (cnk != null) {
                    cnk.scheduleUpdate();
                }
            }
            if (y % Chunk.CHUNK_SIZE == 0) {
                Chunk cnk = getChunk(x, y - 1, z, false);
                if (cnk != null) {
                    cnk.scheduleUpdate();
                }
            }
            if (MathUtil.PosMod(y, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                Chunk cnk = getChunk(x, y + 1, z, false);
                if (cnk != null) {
                    cnk.scheduleUpdate();
                }
            }
            if (z % Chunk.CHUNK_SIZE == 0) {
                Chunk cnk = getChunk(x, y, z - 1, false);
                if (cnk != null) {
                    cnk.scheduleUpdate();
                }
            }
            if (MathUtil.PosMod(z, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                Chunk cnk = getChunk(x, y, z + 1, false);
                if (cnk != null) {
                    cnk.scheduleUpdate();
                }
            }
        }
    };

    public void addChunkListener(ChunkListener listener) {
        fListeners.add(listener);
    }

    public void removeChunkListener(ChunkListener listener) {
        fListeners.remove(listener);
    }
    protected Map<Integer, Map<Integer, ChunkColumn>> fChunkColumns = new HashMap<Integer, Map<Integer, ChunkColumn>>();
    protected Node fRootNode;
    protected Material fBlockMat;
    protected BulletAppState fPhysicsState;

    public BlockWorld(Node rootNode, Material blockMat, BulletAppState physicsState) {
        this.fRootNode = rootNode;
        this.fBlockMat = blockMat;
        this.fPhysicsState = physicsState;
        //this.fHeightMaps.put("detail", new HeightMap());
        fListeners.add(fPhysicsUpdater);
    }

    public Material getBlockMat() {
        return fBlockMat;
    }

    public Chunk getChunk(int x, int y, int z, boolean createChunk) {
        return getChunk(x, y, z, createChunk, true);
    }

    public ChunkColumn getChunkColumn(int x, int z, boolean createChunkColumn) {
        int xC = (int) Math.floor((double) x / Chunk.CHUNK_SIZE);
        int zC = (int) Math.floor((double) z / Chunk.CHUNK_SIZE);

        Map<Integer, ChunkColumn> mX = fChunkColumns.get(xC);
        ChunkColumn cnkColumn = null;
        if (mX != null) {
            cnkColumn = mX.get(zC);
            if (cnkColumn == null) {
                cnkColumn = new ChunkColumn();
                mX.put(zC, cnkColumn);
            }
        } else if (createChunkColumn) {
            mX = new HashMap<Integer, ChunkColumn>();
            fChunkColumns.put(xC, mX);
            cnkColumn = new ChunkColumn();
            mX.put(zC, cnkColumn);
        }
        return cnkColumn;
    }

    public Chunk getChunk(int x, int y, int z, boolean createChunk, boolean generateChunk) {
        int xC = (int) Math.floor((double) x / Chunk.CHUNK_SIZE);
        int yC = (int) Math.floor((double) y / Chunk.CHUNK_SIZE);
        int zC = (int) Math.floor((double) z / Chunk.CHUNK_SIZE);

        ChunkColumn chunkColumn = getChunkColumn(x, z, createChunk);
        Chunk cnk = null;
        if(chunkColumn != null) {
            cnk = chunkColumn.get(y);
        }
        
        if (cnk == null && createChunk) {              // Chunk met juiste x, y , z bestaat niet
            cnk = new Chunk(this, chunkColumn, fRootNode, fPhysicsState, xC * Chunk.CHUNK_SIZE, yC * Chunk.CHUNK_SIZE, zC * Chunk.CHUNK_SIZE);
            if (generateChunk) {
                cnk.fillChunk();
            }
            cnk.addChunkListener(fGeneralListener);
            if (chunkColumn == null) {
                chunkColumn = new ChunkColumn();
            }
            chunkColumn.put(cnk);
        }
        return cnk;
    }

    public Integer get(int x, int y, int z) {
        return get(x, y, z, false);
    }

    public Integer get(int x, int y, int z, boolean createChunk) {
        Chunk cnk = getChunk(x, y, z, createChunk);
        if (cnk != null) {
            return cnk.get(x, y, z);
        }
        return null;
    }

    public void removeBlock(int x, int y, int z) {
        Chunk cnk = getChunk(x, y, z, false);
        if (cnk != null) {
            cnk.removeBlock(x, y, z);
        }
    }

    public boolean addBlock(Integer block, int x, int y, int z) {
        return getChunk(x, y, z, true).addBlock(block, x, y, z);
    }

    public void saveWorld(String fileName) {
        try {
            File file = new File(fileName);
            file.createNewFile();
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
            for (Map<Integer, ChunkColumn> mX : fChunkColumns.values()) {
                for (ChunkColumn chunkColumn : mX.values()) {
                    for (Chunk cnk : chunkColumn.values()) {
                        cnk.save(fileWriter);
                    }
                }
            }
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadWorld(String fileName) {
        try {
            File file = new File(fileName);
            if (file.exists()) {
                BufferedReader fileReader = new BufferedReader(new FileReader(file));
                while (fileReader.ready()) {
                    String line = fileReader.readLine();
                    String[] coords = line.split(":");
                    int xC = Integer.valueOf(coords[0]);
                    int yC = Integer.valueOf(coords[1]);
                    int zC = Integer.valueOf(coords[2]);
                    getChunk(xC, yC, zC, true, false).load(fileReader);
                }
                fileReader.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Float[][] getHeightMap(int x, int z) {
        ChunkColumn column = getChunkColumn(x, z, false);
        if (column != null) {
            return column.getHeightMap();
        }
        return null;
    }

    public void setHeightMap(int x, int z, Float[][] map) {
        ChunkColumn column = getChunkColumn(x, z, false);
        if (column != null) {
            column.setHeightMap(map);
        }
    }

    public int[][] getHighestBlockMap(int x, int z) {
        ChunkColumn column = getChunkColumn(x, z, false);
        if (column != null) {
            return column.getHighestBlockMap();
        }
        return null;
    }

    public float getSunlightValue(int x, int y, int z) {
        ChunkColumn column = getChunkColumn(x, z, false);
        if (column != null) {
            Chunk chunk = column.get(y);
            if (chunk != null) {
                Float value = chunk.getSunlightValue(x, y, z);
                if (value != null) {
                    return value;
                }  
            }
            return column.getDirectSunlight(x, y, z);
        }
        return Lighting.MIN_LIGHT_VALUE;

    }

    public void setSunlightValue(int x, int y, int z, float value) {
        ChunkColumn column = getChunkColumn(x, z, false);
        if (column != null) {
            Chunk chunk = column.get(y);
            if (chunk != null) {
                chunk.setSunlightValue(x, y, z, value);
            }
        }
    }
}
