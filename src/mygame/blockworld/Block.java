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
    private float flightValue;
    private int fNaturalLightValue;
    private int fXC, fYC, fZC;
    
    public Block(int x, int y, int z, int blockValue, boolean isLightSource){
        setIsLightSource(isLightSource);
        if(isLightSource()){
            setLightValue(1.0f);
        }
        setIsTranparent(false);
        setIsDestructable(true);
        setBlockValue(blockValue);
        setX(x);setY(y);setZ(z);
    }
    
    public Block(int x, int y, int z, int blockValue){
        this(x, y, z, blockValue, false);
    }

    public Boolean isLightSource() {
        return isLightSource;
    }

    public void setIsLightSource(Boolean isLightSource) {
        this.isLightSource = isLightSource;
    }

    public Boolean isTranparent() {
        return isTranparent;
    }

    public void setIsTranparent(Boolean isTranparent) {
        this.isTranparent = isTranparent;
    }

    public Boolean isDestructable() {
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
    
    public float getLightValue() {
        return flightValue;
    }

    public void setLightValue(float lightValue) {
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
