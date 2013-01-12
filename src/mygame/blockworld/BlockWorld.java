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
            for(ChunkListener listener : fListeners) {
                listener.blockAdded(chunk, block, x, y, z);
            }
        }

        public void blockRemoved(Chunk chunk, Integer block, int x, int y, int z) {
            for(ChunkListener listener : fListeners) {
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
            if(x % Chunk.CHUNK_SIZE == 0) {
                getChunk(x-1, y, z, true).sceduleUpdate();
            }
            if(MathUtil.PosMod(x, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                getChunk(x+1, y, z, true).sceduleUpdate();
            }
            if(y % Chunk.CHUNK_SIZE == 0) {
                getChunk(x, y-1, z, true).sceduleUpdate();
            }
            if(MathUtil.PosMod(y, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                getChunk(x, y+1, z, true).sceduleUpdate();
            }
            if(z % Chunk.CHUNK_SIZE == 0) {
                getChunk(x, y, z-1, true).sceduleUpdate();
            }
            if(MathUtil.PosMod(z, Chunk.CHUNK_SIZE) == Chunk.CHUNK_SIZE - 1) {
                getChunk(x, y, z+1, true).sceduleUpdate();
            }
        }       
    };

    public void addChunkListener(ChunkListener listener) {
        fListeners.add(listener);
    }

    public void removeChunkListener(ChunkListener listener) {
        fListeners.remove(listener);
    }
    protected Map<Integer, Map<Integer, Map<Integer, Chunk>>> fChunks = new HashMap<Integer, Map<Integer, Map<Integer, Chunk>>>();
    protected Map<String, HeightMap> fHeightMaps = new HashMap<String, HeightMap>();
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
        this.fHeightMaps.put("detail", new HeightMap());
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
        Map<Integer, Map<Integer, Chunk>> mYZ = null;
        Map<Integer, Chunk> mZ = null;
        mYZ = fChunks.get(xC);  // zoek chunks met juiste X
        if (mYZ != null) {
            mZ = mYZ.get(yC);  // zoek chunks met juiste X en Y 
            if (mZ != null) {
                cnk = mZ.get(zC);  // zoek chunks met juiste Z 
            }
        }
      
        if(cnk == null && createChunk){              // Chunk met juiste x, y , z bestaat niet
           cnk = new Chunk(this, fRootNode, fPhysicsState, xC*Chunk.CHUNK_SIZE, yC*Chunk.CHUNK_SIZE, zC*Chunk.CHUNK_SIZE);
           if(generateChunk) {
               cnk.fillChunk();
           }
           cnk.addChunkListener(fGeneralListener);
           if(mZ == null){                            
               mZ = new HashMap<Integer, Chunk>();
           }
           mZ.put(zC, cnk);
           if(mYZ == null){
               mYZ = new HashMap<Integer, Map<Integer, Chunk>>();         
           }
           mYZ.put(yC, mZ);
           fChunks.put(xC, mYZ);
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
            for(Map<Integer, Map<Integer, Chunk>> mYZ : fChunks.values()) {
                for(Map<Integer, Chunk> mZ : mYZ.values()) {
                    for(Chunk cnk : mZ.values()) {
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
            if(file.exists()) {
                BufferedReader fileReader = new BufferedReader(new FileReader(file));
                while(fileReader.ready()) {
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
    
    public HeightMap getHeightMap(String name){
        return fHeightMaps.get(name);
    }
}
