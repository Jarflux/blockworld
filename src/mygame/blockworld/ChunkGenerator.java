/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.Random;

/**
 *
 * @author Nathan & Ben
 */
public class ChunkGenerator {

    private Random fRandom;
    private BlockWorld fBlockWorld;
    private float[][] fChunkMap;
    private float[][] fTmpChunkMap;
    private float fRoughness = 5f;

    public ChunkGenerator() {
        fRandom = new Random();
    }

    public void fillChunk(Chunk cnk) {
        fBlockWorld = cnk.fWorld;
        fChunkMap = new float[cnk.CHUNK_SIZE + 1][cnk.CHUNK_SIZE + 1];
        fTmpChunkMap = new float[cnk.CHUNK_SIZE + 1][cnk.CHUNK_SIZE + 1];
        
        if (fBlockWorld.getChunk(cnk.fXC - 1, cnk.fYC, cnk.fZC -1, false) != null) {
            fChunkMap[0][0] = ((float[][])fBlockWorld.getChunk(cnk.fXC -1, cnk.fYC, cnk.fZC -1, false).getGeneratorData())[cnk.CHUNK_SIZE-1][cnk.CHUNK_SIZE-1];
        } else {
            fChunkMap[0][0] = fRandom.nextFloat() * fRoughness;
        }
        
        if (fBlockWorld.getChunk(cnk.fXC + cnk.CHUNK_SIZE, cnk.fYC, cnk.fZC -1, false) != null) {
            fChunkMap[0][cnk.CHUNK_SIZE] = ((float[][])fBlockWorld.getChunk(cnk.fXC + cnk.CHUNK_SIZE, cnk.fYC, cnk.fZC - 1, false).getGeneratorData())[cnk.CHUNK_SIZE-1][cnk.CHUNK_SIZE-1];
        } else {
            fChunkMap[0][cnk.CHUNK_SIZE] = fRandom.nextFloat() * fRoughness;
        }
        
        if (fBlockWorld.getChunk(cnk.fXC - 1, cnk.fYC, cnk.fZC + cnk.CHUNK_SIZE, false) != null) {
            fChunkMap[cnk.CHUNK_SIZE][0] = ((float[][])fBlockWorld.getChunk(cnk.fXC -1, cnk.fYC, cnk.fZC + cnk.CHUNK_SIZE, false).getGeneratorData())[cnk.CHUNK_SIZE-1][cnk.CHUNK_SIZE-1];
        } else {
            fChunkMap[cnk.CHUNK_SIZE][0] = fRandom.nextFloat() * fRoughness;
        }
        
        if (fBlockWorld.getChunk(cnk.fXC +  cnk.CHUNK_SIZE +1, cnk.fYC, cnk.fZC + cnk.CHUNK_SIZE + 1, false) != null) {
            fChunkMap[cnk.CHUNK_SIZE][cnk.CHUNK_SIZE] = ((float[][])fBlockWorld.getChunk(cnk.fXC +  cnk.CHUNK_SIZE +1, cnk.fYC, cnk.fZC + cnk.CHUNK_SIZE + 1, false).getGeneratorData())[cnk.CHUNK_SIZE-1][cnk.CHUNK_SIZE-1];
        } else {
            fChunkMap[cnk.CHUNK_SIZE][cnk.CHUNK_SIZE] = fRandom.nextFloat() * fRoughness;
        }
        diamond_Square(0, 0, cnk.CHUNK_SIZE, cnk.CHUNK_SIZE, fRoughness);  
        
        for (int x = cnk.fXC; x < cnk.fXC + cnk.CHUNK_SIZE; x++) {
            for (int z = cnk.fZC; z < cnk.fZC + cnk.CHUNK_SIZE; z++) {
                float calculatedHeight = fChunkMap[x - cnk.fXC + 1][z - cnk.fZC + 1];
                for (int y = cnk.fYC; y < cnk.fYC + cnk.CHUNK_SIZE; y++) {
                    fTmpChunkMap[x - cnk.fXC][z - cnk.fZC] = calculatedHeight - 1 ; 
                    if (y < Math.round(calculatedHeight) - 1) {
                        cnk.addBlock(0, x, y, z);
                    } else if (y == Math.round(calculatedHeight) - 1) {
                        cnk.addBlock(1, x, y, z);
                    }
                }
            }
        }
        cnk.setGeneratorData(fTmpChunkMap);
    }

    public void diamond_Step(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        // Calc mid value with 4 corners and randomNumber
        int length = Math.abs(bottomRightX - topLeftX);

        // if integers not found generate random number
        // can be improved by taking the avg of surrounding blocks values       
        float sum = 0.0f;
        sum += fChunkMap[topLeftX][topLeftY];
        sum += fChunkMap[topLeftX][topLeftY + length];
        sum += fChunkMap[topLeftX + length][topLeftY];
        sum += fChunkMap[topLeftX + length][topLeftY + length];
        fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)] = Math.round(sum / 4) + (fRandom.nextFloat() * randomNumberRange);
    }

    public void square_Step(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        float sum;
        int length = Math.abs(bottomRightX - topLeftX);

        //TopMid
        sum = 0.0f;
        sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
        sum += fChunkMap[topLeftX][topLeftY];
        sum += fChunkMap[topLeftX + length][topLeftY];
        fChunkMap[topLeftX + (length / 2)][topLeftY] = Math.round(sum / 3) + (fRandom.nextFloat() * randomNumberRange);

        //LeftMid
        sum = 0.0f;
        sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
        sum += fChunkMap[topLeftX][topLeftY];
        sum += fChunkMap[topLeftX][topLeftY + length];
        fChunkMap[topLeftX][topLeftY + (length / 2)] = Math.round(sum / 3) + (fRandom.nextFloat() * randomNumberRange);

        //RightMid
        sum = 0.0f;
        sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
        sum += fChunkMap[topLeftX + length][topLeftY];
        sum += fChunkMap[topLeftX + length][topLeftY + length];
        fChunkMap[topLeftX + length][topLeftY + (length / 2)] = Math.round(sum / 3) + (fRandom.nextFloat() * randomNumberRange);

        //BottomMid
        sum = 0.0f;
        sum += fChunkMap[topLeftX + (length / 2)][topLeftY + (length / 2)];
        sum += fChunkMap[topLeftX][topLeftY + length];
        sum += fChunkMap[topLeftX + length][topLeftY + length];
        fChunkMap[topLeftX + (length / 2)][topLeftY + length] = Math.round(sum / 3) + (fRandom.nextFloat() * randomNumberRange);
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
}
