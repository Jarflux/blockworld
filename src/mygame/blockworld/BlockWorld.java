/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import mygame.Lighting;
import mygame.blockworld.surfaceextraction.BlockContainer;

/**
 *
 * @author Nathan & Ben
 */
public class BlockWorld implements BlockContainer{
    
    
    private static final Logger logger = Logger.getLogger(BlockWorld.class.getName());
    private List<ChunkListener> fListeners = new LinkedList<ChunkListener>();
    private ChunkListener fGeneralListener = new ChunkListener() {
        public void blockAdded(Chunk chunk, Block block) {
            for (ChunkListener listener : fListeners) {
                listener.blockAdded(chunk, block);
            }
        }

        public void blockRemoved(Chunk chunk, Block block) {
            for (ChunkListener listener : fListeners) {
                listener.blockRemoved(chunk, block);
            }
        }
    };
    private ChunkListener fWorldUpdater = new ChunkListener() {
        public void blockAdded(Chunk chunk, Block block) {
            update(block.getCoordinate());
        }

        public void blockRemoved(Chunk chunk, Block block) {
            update(block.getCoordinate());
        }

        private void update(Coordinate coordinate) {
            Chunk cnk;
            for (int i = (coordinate.x - Chunk.CHUNK_SIZE); i <= (coordinate.x + Chunk.CHUNK_SIZE); i = i + Chunk.CHUNK_SIZE) {
                for (int j = (coordinate.y - Chunk.CHUNK_SIZE); j <= (coordinate.y + Chunk.CHUNK_SIZE); j = j + Chunk.CHUNK_SIZE) {
                    for (int k = (coordinate.z - Chunk.CHUNK_SIZE); k <= (coordinate.z + Chunk.CHUNK_SIZE); k = k + Chunk.CHUNK_SIZE) {
                        cnk = getChunk(new Coordinate(i, j, k), false);
                        if (cnk != null) {
                            cnk.scheduleUpdate();
                        }
                    }
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
        fListeners.add(fWorldUpdater);
    }

    public Material getBlockMat() {
        return fBlockMat;
    }

    public Chunk getChunk(Coordinate coordinate, boolean createChunk) {
        return getChunk(coordinate, createChunk, true);
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

    public Chunk getChunk(Coordinate coordinate, boolean createChunk, boolean generateChunk) {
        int xC = (int) Math.floor((double) coordinate.x / Chunk.CHUNK_SIZE);
        int yC = (int) Math.floor((double) coordinate.y / Chunk.CHUNK_SIZE);
        int zC = (int) Math.floor((double) coordinate.z / Chunk.CHUNK_SIZE);

        ChunkColumn chunkColumn = getChunkColumn(coordinate.x, coordinate.z, createChunk);
        Chunk cnk = null;
        if (chunkColumn != null) {
            cnk = chunkColumn.get(coordinate.y);
        }

        if (cnk == null && createChunk) {              // Chunk met juiste x, y , z bestaat niet
            cnk = new Chunk(this, chunkColumn, fRootNode, fPhysicsState, new Coordinate(xC * Chunk.CHUNK_SIZE, yC * Chunk.CHUNK_SIZE, zC * Chunk.CHUNK_SIZE));
            if (generateChunk) {
                cnk.fillChunk();
            }
            cnk.addChunkListener(fGeneralListener);
            chunkColumn.put(cnk);
        }
        return cnk;
    }

    public Block getBlock(Coordinate coordinate) {
        return getBlock(coordinate, true);
    }

    public Block getBlock(Coordinate coordinate, boolean createChunk) {
        Chunk cnk = getChunk(coordinate, createChunk);
        if (cnk != null) {
            return cnk.getBlock(coordinate);
        }
        return null;
    }

    public void removeBlock(Coordinate coordinate) {
        Chunk cnk = getChunk(coordinate, false);
        if (cnk != null) {
            cnk.removeBlock(coordinate);
        }
    }

    public boolean addBlock(Block block) {
        return getChunk(block.getCoordinate(), true).addBlock(block);
    }

    public void saveWorld(String fileName) {
        /*try {
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
         }*/
    }

    public void loadWorld(String fileName) {
        /*try {
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
         }*/
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

    public float getSunlightValue(Coordinate coordinate) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            return column.getSunlightValue(coordinate);
        }
        return Lighting.MIN_LIGHT_VALUE;
    }

    public void setSunlightValue(Coordinate coordinate, float value) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            Chunk chunk = column.get(coordinate.y);
            if (chunk != null) {
                chunk.setSunlightValue(coordinate, value);
            }
        }
    }

    public Vector3f getConstantLightColor(Coordinate coordinate) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            Chunk chunk = column.get(coordinate.y);
            if (chunk != null) {
                return chunk.getConstantLightColor(coordinate);
            }
        }
        return new Vector3f(0f, 0f, 0f);
    }

    public void setConstantLightColor(Coordinate coordinate, Vector3f color) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            Chunk chunk = column.get(coordinate.y);
            if (chunk != null) {
                chunk.setConstantLightColor(coordinate, color);
            }
        }
    }

    public Vector3f getPulseLightColor(Coordinate coordinate) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            Chunk chunk = column.get(coordinate.y);
            if (chunk != null) {
                return chunk.getPulseLightColor(coordinate);
            }
        }
        return new Vector3f(0f, 0f, 0f);
    }

    public void setPulseLightColor(Coordinate coordinate, Vector3f color) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            Chunk chunk = column.get(coordinate.y);
            if (chunk != null) {
                chunk.setPulseLightColor(coordinate, color);
            }
        }
    }

    public Vector3f getNormal(Coordinate coordinate) {
        ChunkColumn column = getChunkColumn(coordinate.x, coordinate.z, false);
        if (column != null) {
            Chunk chunk = column.get(coordinate.y);
            if (chunk != null) {
                return chunk.getNormal(coordinate);
            }
        }
        return null;
    }
}
