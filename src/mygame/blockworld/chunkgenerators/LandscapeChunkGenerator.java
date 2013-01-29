/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.chunkgenerators;

import com.jme3.terrain.noise.basis.ImprovedNoise;
import java.util.Random;
import mygame.blockworld.Block;
import mygame.blockworld.BlockInfo.BlockType;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.Chunk;

/**
 *
 * @author Nathan & Ben
 */
public class LandscapeChunkGenerator implements ChunkGenerator {

    private Random fRandom;
    private Float[][] fChunkMap;
    private float fTerrainScale = 32f / 2f;
    private float fTerrainRoughness = 32f;// * 2f;
    private float fNoiseZ = 10f;
    private float fMinLocalRoughness = 2f;

    public LandscapeChunkGenerator() {
        fRandom = new Random();
    }

    public void fillChunk(BlockWorld world, Chunk cnk) {
        int offset = Chunk.CHUNK_SIZE;
        fChunkMap = new Float[offset + 1][offset + 1];

        //check if map for that chunk not yet exsists
        if (world.getHeightMap(cnk.getX(), cnk.getZ()) == null) {

            Float[][] fChunkTop = world.getHeightMap(cnk.getX(), cnk.getZ() + offset);
            Float[][] fChunkTopRight = world.getHeightMap(cnk.getX() + offset, cnk.getZ() + offset);
            Float[][] fChunkRight = world.getHeightMap(cnk.getX() + offset, cnk.getZ());
            Float[][] fChunkBottomRight = world.getHeightMap(cnk.getX() + offset, cnk.getZ() - offset);
            Float[][] fChunkBottom = world.getHeightMap(cnk.getX(), cnk.getZ() - offset);
            Float[][] fChunkBottomLeft = world.getHeightMap(cnk.getX() - offset, cnk.getZ() - offset);
            Float[][] fChunkLeft = world.getHeightMap(cnk.getX() - offset, cnk.getZ());
            Float[][] fChunkTopLeft = world.getHeightMap(cnk.getX() - offset, cnk.getZ() + offset);

            // does chunk to the top exsists?
            if (fChunkTop != null) {
                // copy most bottom line to most top line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[i][offset] == null) {
                        fChunkMap[i][offset] = fChunkTop[i][0];
                    }
                }
            }
            // does chunk to the right exsists?
            if (fChunkRight != null) {
                // copy most left line to most right line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[offset][i] == null) {
                        fChunkMap[offset][i] = fChunkRight[0][i];
                    }
                }
            }
            // does chunk to the bottom exsists?
            if (fChunkBottom != null) {
                // copy most top line to most bottom line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[i][0] == null) {
                        fChunkMap[i][0] = fChunkBottom[i][offset];
                    }
                }
            }
            // does chunk to the left exsists?
            if (fChunkLeft != null) {
                // copy most right line to most left line of my chunk
                for (int i = 0; i < offset + 1; i++) {
                    if (fChunkMap[0][i] == null) {
                        fChunkMap[0][i] = fChunkLeft[offset][i];
                    }
                }
            }

            // Test if corners match with all the surrounding chunks
            // if a corner is still empty fill from the diagonal chunk
            if (fChunkTopLeft != null) {
                if (fChunkMap[0][offset] == null) {
                    fChunkMap[0][offset] = fChunkTopLeft[offset][0];
                }
            }

            if (fChunkTopRight != null) {
                if (fChunkMap[offset][offset] == null) {
                    fChunkMap[offset][offset] = fChunkTopRight[0][0];
                }
            }
            if (fChunkBottomRight != null) {
                if (fChunkMap[offset][0] == null) {
                    fChunkMap[offset][0] = fChunkBottomRight[0][offset];
                }
            }

            if (fChunkBottomLeft != null) {
                if (fChunkMap[0][0] == null) {
                    fChunkMap[0][0] = fChunkBottomLeft[offset][offset];
                }
            }

            // if still a corner is empty = randomFloat * Noise
            fNoiseZ = fRandom.nextFloat();
            if (fChunkMap[0][0] == null) {
                fChunkMap[0][0] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.getX() / offset)) + 0f) / fTerrainScale, (((float) (cnk.getX() / offset)) + 0f) / fTerrainScale, fNoiseZ);
            }
            if (fChunkMap[offset][0] == null) {
                fChunkMap[offset][0] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.getX() / offset)) + 1f) / fTerrainScale, (((float) (cnk.getX() / offset)) + 0f) / fTerrainScale, fNoiseZ);
            }
            if (fChunkMap[offset][offset] == null) {
                fChunkMap[offset][offset] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.getX() / offset)) + 1f) / fTerrainScale, (((float) (cnk.getX() / offset)) + 1f) / fTerrainScale, fNoiseZ);
            }
            if (fChunkMap[0][offset] == null) {
                fChunkMap[0][offset] = fTerrainRoughness * ImprovedNoise.noise((((float) (cnk.getX() / offset)) + 0f) / fTerrainScale, (((float) (cnk.getX() / offset)) + 1f) / fTerrainScale, fNoiseZ);
            }
        } else {
            fChunkMap = world.getHeightMap(cnk.getX(), cnk.getZ());
        }
        // Calculate the complete fChunkMap with Diamond-Square algorithm
        float roughnessLocal = Math.max(Math.max(fChunkMap[0][0], fChunkMap[offset][0]), Math.max(fChunkMap[0][offset], fChunkMap[offset][offset]));
        roughnessLocal = roughnessLocal - Math.min(Math.min(fChunkMap[0][0], fChunkMap[offset][0]), Math.min(fChunkMap[0][offset], fChunkMap[offset][offset]));
        diamond_Square(0, 0, offset, offset, Math.max(roughnessLocal, fMinLocalRoughness));

        // Loop over fChunkMap to create every block
        for (int x = cnk.getX(); x < cnk.getX() + offset; x++) {
            for (int z = cnk.getZ(); z < cnk.getZ() + offset; z++) {
                int calculatedHeight = Math.round((fChunkMap[x - cnk.getX()][z - cnk.getZ()] + fChunkMap[x - cnk.getX()][z - cnk.getZ() + 1] + fChunkMap[x - cnk.getX() + 1][z - cnk.getZ()] + fChunkMap[x - cnk.getX() + 1][z - cnk.getZ() + 1]) / 4);
                for (int y = cnk.getY(); y < cnk.getY() + offset; y++) {
                    if (getBlockType(y, calculatedHeight) != null) {
                        cnk.addBlock(new Block(x, y, z, getBlockType(y, calculatedHeight)));
                    }
                }
            }
        }
        world.setHeightMap(cnk.getX(), cnk.getZ(), fChunkMap);
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

    // block type logic
    private BlockType getBlockType(int heigth, int topHeight) {
        if (heigth == topHeight) {
            return BlockType.SNOW;
        }else if (heigth < (topHeight - fRandom.nextInt(6))) {
            return BlockType.STONE;
        }else if (heigth < topHeight) {
            return BlockType.DIRT;
        }
        return null;
    }
}
