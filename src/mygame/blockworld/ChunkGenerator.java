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

    private Random r;
    private BlockWorld bw;
    private float[][] chunkMap;

    public ChunkGenerator() {
        r = new Random();
    }

    public void fillChunk(Chunk cnk) {
        chunkMap = new float[cnk.CHUNK_SIZE+1][cnk.CHUNK_SIZE+1];
        diamond_Square(0, 0,cnk.CHUNK_SIZE, cnk.CHUNK_SIZE, 10f);
        bw = cnk.fWorld;
        for (int x = cnk.fXC; x < cnk.fXC + cnk.CHUNK_SIZE; x++) {
            for (int z = cnk.fZC; z < cnk.fZC + cnk.CHUNK_SIZE; z++) {
                float calculatedHeight = (noise.getMap()[x-cnk.fXC][z-cnk.fZC])* plateau;
                for (int y = cnk.fYC; y < cnk.fYC + cnk.CHUNK_SIZE; y++) {       
                    if (y < Math.round(calculatedHeight)-1) {
                        cnk.addBlock(0, x, y, z);
                    }else if(y == Math.round(calculatedHeight)-1) {
                        cnk.addBlock(1, x, y, z);
                    }
                }
            }
        }
    }

    public void diamond_Step(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        // Calc mid value with 4 corners and randomNumber
        int length = Math.abs(bottomRightX - topLeftX);

        // if integers not found generate random number
        // can be improved by taking the avg of surrounding blocks values       
        float sum = 0.0f;
        if ((0.000 - Math.round(chunkMap[topLeftX][topLeftY])) < 0.0001) {
            chunkMap[topLeftX][topLeftY] = r.nextFloat() * randomNumberRange;
        }
        if ((0.000 - Math.round(chunkMap[topLeftX][topLeftY+ length])) < 0.0001) {
            chunkMap[topLeftX][length] = r.nextFloat() * randomNumberRange;
        }
        if ((0.000 - Math.round(chunkMap[topLeftX+length][topLeftY])) < 0.0001) {
            chunkMap[topLeftX + length][0] = r.nextFloat() * randomNumberRange;
        }
        if ((0.000 - Math.round(chunkMap[topLeftX + length][topLeftY + length])) < 0.0001) {
            chunkMap[topLeftX + length][length] = r.nextFloat() * randomNumberRange;
        }
        sum += chunkMap[topLeftX][topLeftY];
        sum += chunkMap[topLeftX][topLeftY+length];
        sum += chunkMap[topLeftX+ length][topLeftY];
        sum += chunkMap[topLeftX+ length][topLeftY+length];
        chunkMap[topLeftX + (length / 2)][topLeftY+(length / 2)] = Math.round(sum / 4) + (r.nextFloat() * randomNumberRange);
    }

    public void square_Step(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        float sum;
        int length = Math.abs(bottomRightX - topLeftX);

        //TopMid
        sum = 0.0f;
        sum += chunkMap[topLeftX+ (length / 2)][topLeftY+(length / 2)];
        sum += chunkMap[topLeftX][topLeftY];
        sum += chunkMap[topLeftX+ length][topLeftY];
        chunkMap[topLeftX+(length / 2)][topLeftY] = Math.round(sum / 3) + (r.nextFloat() * randomNumberRange);
        //LeftMid
        sum = 0.0f;
        sum += chunkMap[topLeftX + (length / 2)][topLeftY+(length / 2)];
        sum += chunkMap[topLeftX][topLeftY];
        sum += chunkMap[topLeftX][topLeftY+length];
        chunkMap[topLeftX][topLeftY+(length / 2)] = Math.round(sum / 3) + (r.nextFloat() * randomNumberRange);
        //RightMid
        sum = 0.0f;
        sum += chunkMap[topLeftX + (length / 2)][topLeftY+(length / 2)];
        sum += chunkMap[topLeftX + length][topLeftY];
        sum += chunkMap[topLeftX + length][topLeftY+length];
        chunkMap[topLeftX+length][topLeftY+(length / 2)] = Math.round(sum / 3) + (r.nextFloat() * randomNumberRange);
        //BottomMid
        sum = 0.0f;
        sum += chunkMap[topLeftX+(length / 2)][topLeftY+(length / 2)];
        sum += chunkMap[topLeftX][topLeftY+length];
        sum += chunkMap[topLeftX+length][topLeftY+length];
        chunkMap[topLeftX+(length / 2)][topLeftY+length] = Math.round(sum / 3) + (r.nextFloat() * randomNumberRange);
    }

    public void diamond_Square(int topLeftX, int topLeftY, int bottomRightX, int bottomRightY, float randomNumberRange) {
        float currentRandomNumberRange = randomNumberRange / 2;
        int length = Math.abs(bottomRightX - topLeftX);
        if(length > 1) {
            diamond_Step(topLeftX, topLeftY, bottomRightX, bottomRightY, currentRandomNumberRange);
            square_Step(topLeftX, topLeftY, bottomRightX, bottomRightY, currentRandomNumberRange);

            diamond_Square(topLeftX, topLeftY, topLeftX + (length / 2), topLeftY + (length / 2), currentRandomNumberRange);
            diamond_Square(topLeftX + (length / 2), topLeftY, topLeftX + length, topLeftY + (length / 2), currentRandomNumberRange);
            diamond_Square(topLeftX, topLeftY + (length / 2), topLeftX + (length / 2), topLeftY + length, currentRandomNumberRange);
            diamond_Square(topLeftX + (length / 2), topLeftY + (length / 2), topLeftX + length, topLeftY + length, currentRandomNumberRange);
        }
    }
}
