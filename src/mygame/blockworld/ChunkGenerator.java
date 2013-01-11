/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.terrain.noise.basis.ImprovedNoise;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nathan & Ben
 */
public class ChunkGenerator {

    private Random fRandom;
    private Float[][] fChunkMap;
    private HeightMap fHeightMap;
    //private float fRoughness = 10f;
    private float fTerrainScale = 32f;
    private float fTerrainRoughness = 32f;
    private float fNoiseZ = 10f;
    private float fMinLocalRoughness = 4f;

    public ChunkGenerator() {
        fRandom = new Random();
    }

    public void fillChunk(Chunk cnk) {
        fHeightMap = cnk.fWorld.getHeightMap("detail");
        int offset = Chunk.CHUNK_SIZE;
        fChunkMap = new Float[offset + 1][offset + 1];
        

        //check if map for that chunk not yet exsists
        if (fHeightMap.getHeightMap(cnk.fXC, cnk.fZC) == null) {
                    
            Float[][] fChunkTop = fHeightMap.getHeightMap(cnk.fXC, cnk.fZC + offset);
            Float[][] fChunkTopRight = fHeightMap.getHeightMap(cnk.fXC + offset, cnk.fZC + offset);
            Float[][] fChunkRight = fHeightMap.getHeightMap(cnk.fXC + offset, cnk.fZC);
            Float[][] fChunkBottomRight = fHeightMap.getHeightMap(cnk.fXC + offset, cnk.fZC - offset);
            Float[][] fChunkBottom = fHeightMap.getHeightMap(cnk.fXC, cnk.fZC - offset);
            Float[][] fChunkBottomLeft = fHeightMap.getHeightMap(cnk.fXC - offset, cnk.fZC - offset);
            Float[][] fChunkLeft = fHeightMap.getHeightMap(cnk.fXC - offset, cnk.fZC);
            Float[][] fChunkTopLeft = fHeightMap.getHeightMap(cnk.fXC - offset, cnk.fZC + offset);

            // does chunk to the top exsists?
            if (fChunkTop != null) {
                // copy most bottom line to most top line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[i][offset] == null) {
                        fChunkMap[i][offset] = fChunkTop[i][0];
                    } else {
                        if (fChunkMap[i][offset] != fChunkTop[i][0]) { //test if corners that overlap match
                            Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "TOP overlap height value does not match");
                        }
                    }
                }
            }
            // does chunk to the right exsists?
            if (fChunkRight != null) {
                // copy most left line to most right line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[offset][i] == null) {
                        fChunkMap[offset][i] = fChunkRight[0][i];
                    } else {
                        if (fChunkMap[offset][i] != fChunkRight[0][i]) { //test if corners that overlap match
                            Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "RIGHT overlap height value does not match");
                        }
                    }
                }
            }
            // does chunk to the bottom exsists?
            if (fChunkBottom != null) {
                // copy most top line to most bottom line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[i][0] == null) {
                        fChunkMap[i][0] = fChunkBottom[i][offset];
                    } else {
                        if (fChunkMap[i][0] != fChunkBottom[i][offset]) { //test if corners that overlap match
                            Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "BOTTOM overlap height value does not match");
                        }
                    }
                }
            }
            // does chunk to the left exsists?
            if (fChunkLeft != null) {
                // copy most right line to most left line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[0][i] == null) {
                        fChunkMap[0][i] = fChunkLeft[offset][i];
                    } else {
                        if (fChunkMap[0][i] != fChunkLeft[offset][i]) { //test if corners that overlap match
                            Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "LEFT overlap height value does not match");
                        }
                    }
                }
            }

            // Test if corners match with all the surrounding chunks
            // if a corner is still empty fill from the diagonal chunk
            if (fChunkTopLeft != null) {
                if (fChunkMap[0][offset] != null) {
                    if (fChunkMap[0][offset] != fChunkTopLeft[offset][0]) { //test if corner that overlap match
                        Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "TOP LEFT overlap height value does not match");
                    }
                } else {
                    fChunkMap[0][offset] = fChunkTopLeft[offset][0];
                }
            }

            if (fChunkTopRight != null) {
                if (fChunkMap[offset][offset] != null) {
                    if (fChunkMap[offset][offset] != fChunkTopRight[0][0]) { //test if corner that overlap match
                        Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "TOP RIGHT overlap height value does not match");
                    }
                } else {
                    fChunkMap[offset][offset] = fChunkTopRight[0][0];
                }
            }
            if (fChunkBottomRight != null) {
                if (fChunkMap[offset][0] != null) {
                    if (fChunkMap[offset][0] != fChunkBottomRight[0][offset]) { //test if corner that overlap match
                        Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "BOTTOM RIGHT overlap height value does not match");
                    }
                } else {
                    fChunkMap[offset][0] = fChunkBottomRight[0][offset];
                }
            }

            if (fChunkBottomLeft != null) {
                if (fChunkMap[0][0] != null) {
                    if (fChunkMap[0][0] != fChunkBottomLeft[offset][offset]) { //test if corner that overlap match
                        Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "BOTTOM LEFT height value does not match");
                    }
                } else {
                    fChunkMap[0][0] = fChunkBottomLeft[offset][offset];
                }
            }

            Boolean a = false;
            Boolean b = false;
            Boolean c = false;
            Boolean d = false;
            // if still a corner is empty = randomFloat * fRoughness
            fNoiseZ = fRandom.nextFloat();
            if (fChunkMap[0][0] == null) {
                fChunkMap[0][0] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.fXC / offset)) + 0f) / fTerrainScale, (((float) (cnk.fXC / offset)) + 0f) / fTerrainScale, fNoiseZ);
                //fChunkMap[0][0] = fRandom.nextFloat() * (fRoughness*2)-fRoughness;
                a = true;
            }
            if (fChunkMap[offset][0] == null) {
                fChunkMap[offset][0] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.fXC / offset)) + 1f) / fTerrainScale, (((float) (cnk.fXC / offset)) + 0f) / fTerrainScale, fNoiseZ);
                //fChunkMap[offset][0] = fRandom.nextFloat() * (fRoughness*2)-fRoughness;
                b = true;
            }
            if (fChunkMap[offset][offset] == null) {
                fChunkMap[offset][offset] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.fXC / offset)) + 1f) / fTerrainScale, (((float) (cnk.fXC / offset)) + 1f) / fTerrainScale, fNoiseZ);
                //fChunkMap[offset][offset] = fRandom.nextFloat() * (fRoughness*2)-fRoughness;
                c = true;
            }
            if (fChunkMap[0][offset] == null) {
                fChunkMap[0][offset] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.fXC / offset)) + 0f) / fTerrainScale, (((float) (cnk.fXC / offset)) + 1f) / fTerrainScale, fNoiseZ);
                //fChunkMap[0][offset] = fRandom.nextFloat() * (fRoughness*2)-fRoughness;
                d = true;
            }
            if (a && b && c && d) { // all 4 corner where random generated
                Logger.getLogger(BlockWorld.class.getName()).log(Level.SEVERE, "Initial Chunk Corners Randomized");
            }
        } else {
            fChunkMap = fHeightMap.getHeightMap(cnk.fXC, cnk.fZC);
        }
        // Calculate the complete fChunkMap with Diamond-Square algorithm
        float roughnessLocal = Math.max(Math.max(fChunkMap[0][0], fChunkMap[offset][0]), Math.max(fChunkMap[0][offset], fChunkMap[offset][offset]));
        roughnessLocal = roughnessLocal - Math.min(Math.min(fChunkMap[0][0], fChunkMap[offset][0]), Math.min(fChunkMap[0][offset], fChunkMap[offset][offset]));
        diamond_Square(0, 0, offset, offset, Math.max(roughnessLocal, fMinLocalRoughness));
        
        // Loop over fChunkMap to create every block
        for (int x = cnk.fXC; x < cnk.fXC + offset; x++) {
            for (int z = cnk.fZC; z < cnk.fZC + offset; z++) {
                int calculatedHeight = Math.round((fChunkMap[x - cnk.fXC][z - cnk.fZC] + fChunkMap[x - cnk.fXC][z - cnk.fZC + 1] + fChunkMap[x - cnk.fXC + 1][z - cnk.fZC] + fChunkMap[x - cnk.fXC + 1][z - cnk.fZC + 1]) / 4);
                for (int y = cnk.fYC; y < cnk.fYC + offset; y++) {
                    float noise = ImprovedNoise.noise((float)x/8f, (float)y/8f, (float)z/8f);
                    if(noise < .25f) {
                        if (getBlockType(y, calculatedHeight) != null) {
                            cnk.addBlock(getBlockType(y, calculatedHeight), x, y, z);
                        }
                    }
                }
            }
        }
        fHeightMap.setHeightMap(cnk.fXC, cnk.fZC, fChunkMap);
    }

    public void diamond_Step(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        int length = Math.abs(bottomRightX - topLeftX);
        float sum = 0.0f;
        sum += fChunkMap[topLeftX][topLeftY];
        sum += fChunkMap[topLeftX][topLeftY + length];
        sum += fChunkMap[topLeftX + length][topLeftY];
        sum += fChunkMap[topLeftX + length][topLeftY + length];
        fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)] = Math.round(sum / 4) + (fRandom.nextFloat() * ((randomNumberRange * 2) - randomNumberRange));
    }

    public void square_Step(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        float sum;
        int length = Math.abs(bottomRightX - topLeftX);

        //TopMid
        if (fChunkMap[topLeftX + (length / 2)][topLeftY] == null) {
            sum = 0.0f;
            sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
            sum += fChunkMap[topLeftX][topLeftY];
            sum += fChunkMap[topLeftX + length][topLeftY];
            fChunkMap[topLeftX + (length / 2)][topLeftY] = Math.round(sum / 3) + (fRandom.nextFloat() * ((randomNumberRange * 2) - randomNumberRange));
        }

        //LeftMid
        if (fChunkMap[topLeftX][topLeftY + (length / 2)] == null) {
            sum = 0.0f;
            sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
            sum += fChunkMap[topLeftX][topLeftY];
            sum += fChunkMap[topLeftX][topLeftY + length];
            fChunkMap[topLeftX][topLeftY + (length / 2)] = Math.round(sum / 3) + (fRandom.nextFloat() * ((randomNumberRange * 2) - randomNumberRange));
        }

        //RightMid
        if (fChunkMap[topLeftX + length][topLeftY + (length / 2)] == null) {
            sum = 0.0f;
            sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
            sum += fChunkMap[topLeftX + length][topLeftY];
            sum += fChunkMap[topLeftX + length][topLeftY + length];
            fChunkMap[topLeftX + length][topLeftY + (length / 2)] = Math.round(sum / 3) + (fRandom.nextFloat() * ((randomNumberRange * 2) - randomNumberRange));
        }

        //BottomMid
        if (fChunkMap[topLeftX + (length / 2)][topLeftY + length] == null) {
            sum = 0.0f;
            sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
            sum += fChunkMap[topLeftX][topLeftY + length];
            sum += fChunkMap[topLeftX + length][topLeftY + length];
            fChunkMap[topLeftX + (length / 2)][topLeftY + length] = Math.round(sum / 3) + (fRandom.nextFloat() * ((randomNumberRange * 2) - randomNumberRange));
        }
    }

    public void diamond_Square(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        float currentRandomNumberRange = randomNumberRange / 2;
        int length = Math.abs(bottomRightX - topLeftX);
        if (length > 1) {
            diamond_Step(topLeftX, topLeftY, bottomRightX, bottomRightY, currentRandomNumberRange);
            square_Step(topLeftX, topLeftY, bottomRightX, bottomRightY, currentRandomNumberRange);

            diamond_Square(topLeftX, topLeftY, topLeftX + (length / 2), topLeftY + (length / 2), currentRandomNumberRange);
            diamond_Square(topLeftX + (length / 2), topLeftY, topLeftX + length, topLeftY + (length / 2), currentRandomNumberRange);
            diamond_Square(topLeftX, topLeftY + (length / 2), topLeftX + (length / 2), topLeftY + length, currentRandomNumberRange);
            diamond_Square(topLeftX + (length / 2), topLeftY + (length / 2), topLeftX + length, topLeftY + length, currentRandomNumberRange);
        }
    }

    // block type logic
    private Integer getBlockType(int heigth, int topHeight) {
        Integer blockType = null;
        if (heigth == topHeight) {
            blockType = 1;                       // return grass
        }
        if (heigth < topHeight) {
            blockType = 0;                       // return dirt
        }
        if (heigth < (topHeight - 0) - (6* fRandom.nextFloat())) {
            blockType = 3;
        }
        return blockType;                        // return air
    }
}
