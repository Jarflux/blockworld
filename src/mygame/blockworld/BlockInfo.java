/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

/**
 *
 * @author Nathan
 */
public class BlockInfo {
    
    public final int textureTop;
    public final int textureBottom;
    public final int textureRight;
    public final int textureLeft;
    public final int textureFront;
    public final int textureBack;
    public final String name;
    public final float fireLightValue;
    public final float magicLightValue;
    public final boolean isDestructable;
    public final boolean isTransparent;
    
    private BlockInfo(String name, float fireLightValue, float magicLightValue,
            boolean isDestructable, boolean isTransparent, 
            int textureTop, int textureBottom, int textureRight, 
            int textureLeft, int textureFront, int textureBack) {
        this.textureTop = textureTop;
        this.textureBottom = textureBottom;
        this.textureRight = textureRight;
        this.textureLeft = textureLeft;
        this.textureFront = textureFront;
        this.textureBack = textureBack;
        this.name = name;
        this.fireLightValue = fireLightValue;
        this.magicLightValue = magicLightValue;
        this.isDestructable = isDestructable;
        this.isTransparent = isTransparent;
    }
    
    public static enum BlockType {
        DIRT(new BlockInfo("Dirt", 0f, 0f, true, false, 242, 242, 242, 242, 242, 242)),
        STONE(new BlockInfo("Stone", 0f, 0f, true, false, 241, 241, 241, 241, 241, 241)),
        SNOW(new BlockInfo("Snow", 0f, 0f, true, false, 178, 242, 180, 180, 180, 180)),
        PUMPKIN(new BlockInfo("Pumpkin", 1f, 0f, true, false, 150, 150, 134, 134, 136, 134)),
        LAPUS(new BlockInfo("Lapus Lazuli", 0f, 1f, true, false, 80, 80, 80, 80, 80, 80)),
        ;
        
        private final BlockInfo fBlock;
        private BlockType(BlockInfo block) {
            fBlock = block;
        }
        public final BlockInfo getBlock() {
            return fBlock;
        }
    }
    
}
