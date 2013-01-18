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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.TextureAtlas;
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
                getChunk(x - 1, y, z, true).scheduleUpdate();
            }
            if (MathUtil.PosMod(x, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                getChunk(x + 1, y, z, true).scheduleUpdate();
            }
            if (y % Chunk.CHUNK_SIZE == 0) {
                getChunk(x, y - 1, z, true).scheduleUpdate();
            }
            if (MathUtil.PosMod(y, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                getChunk(x, y + 1, z, true).scheduleUpdate();
            }
            if (z % Chunk.CHUNK_SIZE == 0) {
                getChunk(x, y, z - 1, true).scheduleUpdate();
            }
            if (MathUtil.PosMod(z, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                getChunk(x, y, z + 1, true).scheduleUpdate();
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
    //protected Map<String, HeightMap> fHeightMaps = new HashMap<String, HeightMap>();
    protected Node fRootNode;
    protected Material fBlockMat;
    protected TextureAtlas fAtlas;

    public TextureAtlas getAtlas() {
        return fAtlas;
    }

    public Material getBlockMat() {
        return fBlockMat;
    }
    protected BulletAppState fPhysicsState;

    public BlockWorld(Node rootNode, Material blockMat, TextureAtlas atlas, BulletAppState physicsState) {
        this.fRootNode = rootNode;
        this.fBlockMat = blockMat;
        this.fAtlas = atlas;
        this.fPhysicsState = physicsState;
        //this.fHeightMaps.put("detail", new HeightMap());
        fListeners.add(fPhysicsUpdater);
    }

    public Chunk getChunk(int x, int y, int z, boolean createChunk) {
        return getChunk(x, y, z, createChunk, true);
    }

    public Chunk getChunk(int x, int y, int z, boolean createChunk, boolean generateChunk) {
        double fx = x;
        double fy = y;
        double fz = z;
        int xC = (int) Math.floor(fx / Chunk.CHUNK_SIZE);
        int yC = (int) Math.floor(fy / Chunk.CHUNK_SIZE);
        int zC = (int) Math.floor(fz / Chunk.CHUNK_SIZE);

        Chunk cnk = null;
        Map<Integer, ChunkColumn> mX = null;
        ChunkColumn chunkColumn = null;
        mX = fChunkColumns.get(xC);
        if (mX != null) {
            chunkColumn = mX.get(zC);
            if (chunkColumn != null) {
                cnk = chunkColumn.get(yC);
            }
        }

        if (cnk == null && createChunk) {              // Chunk met juiste x, y , z bestaat niet
            cnk = new Chunk(this, fRootNode, fPhysicsState, xC * Chunk.CHUNK_SIZE, yC * Chunk.CHUNK_SIZE, zC * Chunk.CHUNK_SIZE);
            if (generateChunk) {
                cnk.fillChunk();
            }
            cnk.addChunkListener(fGeneralListener);
            if (chunkColumn == null) {
                chunkColumn = new ChunkColumn();
            }
            chunkColumn.put(cnk);
            if (mX == null) {
                mX = new HashMap<Integer, ChunkColumn>();
            }
            mX.put(zC, chunkColumn);
            fChunkColumns.put(xC, mX);
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
        if (fChunkColumns.get(x) != null) {
            if (fChunkColumns.get(x).get(z) != null) {
                return (Float[][]) fChunkColumns.get(x).get(z).getHeigthMap();
            }
        }
        return null;
    }

    public void setHeightMap(int x, int z, Float[][] map) {
        if (fChunkColumns.get(x) != null) {
            if (fChunkColumns.get(x).get(z) != null) {
                fChunkColumns.get(x).get(z).setHeigthMap(map);
                
            }
        }
        
        if(fChunkColumns.get(x) == null) { 
            Map<Integer, ChunkColumn> mX = new HashMap<Integer, ChunkColumn>();
            ChunkColumn chunkColumn = new ChunkColumn();
            chunkColumn.setHeigthMap(map);
            mX.put(z, chunkColumn);
            fChunkColumns.put(x, mX);
        }else{
            ChunkColumn chunkColumn = new ChunkColumn();
            chunkColumn.setHeigthMap(map);
            fChunkColumns.get(x).put(z, chunkColumn); 
        }
       
    }
}
