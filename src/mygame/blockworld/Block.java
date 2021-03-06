/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.math.Vector3f;
import java.io.Serializable;

/**
 *
 * @author Nathan
 */
public interface Block extends Serializable {
    boolean isTranparent();
    
    String getBlockName();
    
    boolean isLightSource();
    boolean isConstantLightSource();
    boolean isPulseLightSource();
    
    Vector3f getConstantLightValue();
    Vector3f getPulseLightValue();
    
    int getTextureTop();
    int getTextureBottom();
    int getTextureRight();
    int getTextureLeft();
    int getTextureFront();
    int getTextureBack();
    
    //TODO: Replace by getCoordinate() ?
    Coordinate getCoordinate();
    
}
