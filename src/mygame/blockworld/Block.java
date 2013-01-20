package mygame.blockworld;

/**
 *
 * @author Fusion
 */
public class Block {
    private Boolean isLightSource;
    private Boolean isTranparent;
    private Boolean isDestructable;
    private int fblockValue;
    private int flightValue;
    private int fXC, fYC, fZC;
    
    public Block(int x, int y, int z, int blockValue){
        setIsLightSource(false);
        setIsTranparent(false);
        setIsDestructable(true);
        setBlockValue(blockValue);
        setX(x);setY(y);setZ(z);
    }

    public Boolean getIsLightSource() {
        return isLightSource;
    }

    public void setIsLightSource(Boolean isLightSource) {
        this.isLightSource = isLightSource;
    }

    public Boolean getIsTranparent() {
        return isTranparent;
    }

    public void setIsTranparent(Boolean isTranparent) {
        this.isTranparent = isTranparent;
    }

    public Boolean getIsDestructable() {
        return isDestructable;
    }

    public void setIsDestructable(Boolean isDestructable) {
        this.isDestructable = isDestructable;
    }

    public int getBlockValue() {
        return fblockValue;
    }

    public void setBlockValue(int fblockValue) {
        this.fblockValue = fblockValue;
    }
    
    public int getLightValue() {
        return flightValue;
    }

    public void setLightValue(int lightValue) {
        this.flightValue = lightValue;
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
