package mygame.blockworld;

import com.jme3.math.Vector3f;
import mygame.blockworld.BlockInfo.BlockType;

/**
 *
 * @author Fusion
 */
public class BasicBlock implements Block {

    private BlockType fBlockType;
    private int fDirection; //0: Front -> Front, 1: Front -> Right, 2: Front -> Back, 3: Front -> Left (and other faces change accordingly)
    private int fXC, fYC, fZC;

    public BasicBlock(int x, int y, int z, BlockType type) {
        this(x, y, z, type, 0);
    }

    private static int calculateDirection(Vector3f placementNormal) {
        float xAbs = Math.abs(placementNormal.x);
        float zAbs = Math.abs(placementNormal.z);
        if (xAbs > zAbs) {
            if (placementNormal.x > 0) {
                return 1;
            } else {
                return 3;
            }
        } else { //zAbs > xAbs
            if (placementNormal.z > 0) {
                return 0;
            } else {
                return 2;
            }
        }
    }

    public BasicBlock(int x, int y, int z, BlockType type, Vector3f placementNormal) {
        this(x, y, z, type, calculateDirection(placementNormal));
    }

    public BasicBlock(int x, int y, int z, BlockType type, int direction) {
        fBlockType = type;
        fDirection = direction;
        setX(x);
        setY(y);
        setZ(z);
    }

    public boolean isLightSource() {
        return (isConstantLightSource() || isPulseLightSource());
    }

    public boolean isConstantLightSource() {
        return (fBlockType.getBlock().constantLightColor.x > 0f
                || fBlockType.getBlock().constantLightColor.y > 0f
                || fBlockType.getBlock().constantLightColor.z > 0f);
    }

    public boolean isPulseLightSource() {
        return (fBlockType.getBlock().pulseLightColor.x > 0f
                || fBlockType.getBlock().pulseLightColor.y > 0f
                || fBlockType.getBlock().pulseLightColor.z > 0f);
    }

    public boolean isTranparent() {
        return fBlockType.getBlock().isTransparent;
    }

    public boolean isDestructable() {
        return fBlockType.getBlock().isDestructable;
    }

    public String getBlockName() {
        return fBlockType.getBlock().name;
    }

    public Vector3f getConstantLightValue() {
        return fBlockType.getBlock().constantLightColor;
    }

    public Vector3f getPulseLightValue() {
        return fBlockType.getBlock().pulseLightColor;
    }

    public int getTextureTop() {
        return fBlockType.getBlock().textureTop;
    }

    public int getTextureBottom() {
        return fBlockType.getBlock().textureBottom;
    }

    public int getTextureRight() {
        switch (fDirection) {
            case 1:
                return fBlockType.getBlock().textureBack;
            case 2:
                return fBlockType.getBlock().textureLeft;
            case 3:
                return fBlockType.getBlock().textureFront;
            default:
                return fBlockType.getBlock().textureRight;
        }
    }

    public int getTextureLeft() {
        switch (fDirection) {
            case 1:
                return fBlockType.getBlock().textureFront;
            case 2:
                return fBlockType.getBlock().textureRight;
            case 3:
                return fBlockType.getBlock().textureBack;
            default:
                return fBlockType.getBlock().textureLeft;
        }
    }

    public int getTextureFront() {
        switch (fDirection) {
            case 1:
                return fBlockType.getBlock().textureRight;
            case 2:
                return fBlockType.getBlock().textureBack;
            case 3:
                return fBlockType.getBlock().textureLeft;
            default:
                return fBlockType.getBlock().textureFront;
        }
    }

    public int getTextureBack() {
        switch (fDirection) {
            case 1:
                return fBlockType.getBlock().textureLeft;
            case 2:
                return fBlockType.getBlock().textureFront;
            case 3:
                return fBlockType.getBlock().textureRight;
            default:
                return fBlockType.getBlock().textureBack;
        }
    }

    public int getX() {
        return fXC;
    }

    private void setX(int fXC) {
        this.fXC = fXC;
    }

    public int getY() {
        return fYC;
    }

    private void setY(int fYC) {
        this.fYC = fYC;
    }

    public int getZ() {
        return fZC;
    }

    private void setZ(int fZC) {
        this.fZC = fZC;
    }
}
