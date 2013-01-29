/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.io.Serializable;

/**
 *
 * @author Nathan
 */
public interface Block extends Serializable {
    boolean isTranparent();
    
    String getBlockName();
    
    boolean isFireLightSource();
    float getFireLightValue();
    
    boolean isMagicLightSource();
    float getMagicLightValue();
    
    int getTextureTop();
    int getTextureBottom();
    int getTextureRight();
    int getTextureLeft();
    int getTextureFront();
    int getTextureBack();
    
    int getX();
    int getY();
    int getZ();
    
}
