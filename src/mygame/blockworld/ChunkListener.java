/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.scene.Geometry;
import java.util.EventListener;

/**
 *
 * @author Nathan
 */
public interface ChunkListener extends EventListener {
    
    void blockAdded(Geometry block, int x, int y, int z);
    void blockRemoved(Geometry block, int x, int y, int z);
    
}
