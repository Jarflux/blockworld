/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.EventListener;

/**
 *
 * @author Nathan
 */
public interface ChunkListener extends EventListener {
    
    void blockAdded(Chunk chunk, Integer block, int x, int y, int z);
    void blockRemoved(Chunk chunk, Integer block, int x, int y, int z);
    
}
