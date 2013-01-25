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
    void blockAdded(Chunk chunk, Block block);
    void blockRemoved(Chunk chunk, Block block);  
}
