/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import java.util.Set;

/**
 *
 * @author Nathan
 */
public class Coordinate {
    public int x;
    public int y;
    public int z;

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     * Finds the corners connected to the start corner.
     * @param world
     * @param chunk
     * @param start Corner for which to find the connected corners. Corners are identified by the block coordinates who have that corner as the one with the lowest x, y & z values.
     * @param followGroundEdges If the algoritm must follow edges that are touching 4 empty blocks
     * @param followAirEdges If the algoritm must follow edges that are touching 4 filled blocks
     * @param followSurfaceEdges If the algoritm must follow edges that are touching the surface
     * @param recursionDepth Maximum distance from the given coordinate to find connected corners
     * @param connectedCoordinates The set that will be filled with the connected corners
     */
    public static void findConnectedCorners(BlockWorld world, Coordinate start, boolean followGroundEdges, boolean followAirEdges, boolean followSurfaceEdges, int recursionDepth, Set<Coordinate> connectedCoordinates) {
        connectedCoordinates.add(start);
        if(recursionDepth <= 0) {
            return;
        }
        
        int block000 = world.getBlock(start.x-1, start.y-1, start.z-1, false) != null ? 1 : 0;
        int block001 = world.getBlock(start.x-1, start.y-1, start.z, false) != null ? 1 : 0;
        int block010 = world.getBlock(start.x-1, start.y, start.z-1, false) != null ? 1 : 0;
        int block011 = world.getBlock(start.x-1, start.y, start.z, false) != null ? 1 : 0;
        int block100 = world.getBlock(start.x, start.y-1, start.z-1, false) != null ? 1 : 0;
        int block101 = world.getBlock(start.x, start.y-1, start.z, false) != null ? 1 : 0;
        int block110 = world.getBlock(start.x, start.y, start.z-1, false) != null ? 1 : 0;
        int block111 = world.getBlock(start.x, start.y, start.z, false) != null ? 1 : 0;
        
        int edgeXNeg = block000 + block001 + block010 + block011;
        int edgeXPos = block100 + block101 + block110 + block111;
        int edgeYNeg = block000 + block001 + block100 + block101;
        int edgeYPos = block010 + block011 + block110 + block111;
        int edgeZNeg = block000 + block010 + block100 + block110;
        int edgeZPos = block001 + block011 + block101 + block111;
        
        if((followSurfaceEdges && edgeXNeg > 0 && edgeXNeg < 4) || (followGroundEdges && edgeXNeg == 4) || (followAirEdges && edgeXNeg == 0)) {
            findConnectedCorners(world, new Coordinate(start.x-1,start.y,start.z), followGroundEdges, followAirEdges, followSurfaceEdges, recursionDepth-1, connectedCoordinates);
        }
        if((followSurfaceEdges && edgeXPos > 0 && edgeXPos < 4) || (followGroundEdges && edgeXPos == 4) || (followAirEdges && edgeXPos == 0)) {
            findConnectedCorners(world, new Coordinate(start.x+1,start.y,start.z), followGroundEdges, followAirEdges, followSurfaceEdges, recursionDepth-1, connectedCoordinates);
        }
        if((followSurfaceEdges && edgeYNeg > 0 && edgeYNeg < 4) || (followGroundEdges && edgeYNeg == 4) || (followAirEdges && edgeYNeg == 0)) {
            findConnectedCorners(world, new Coordinate(start.x,start.y-1,start.z), followGroundEdges, followAirEdges, followSurfaceEdges, recursionDepth-1, connectedCoordinates);
        }
        if((followSurfaceEdges && edgeYPos > 0 && edgeYPos < 4) || (followGroundEdges && edgeYPos == 4) || (followAirEdges && edgeYPos == 0)) {
            findConnectedCorners(world, new Coordinate(start.x,start.y+1,start.z), followGroundEdges, followAirEdges, followSurfaceEdges, recursionDepth-1, connectedCoordinates);
        }
        if((followSurfaceEdges && edgeZNeg > 0 && edgeZNeg < 4) || (followGroundEdges && edgeZNeg == 4) || (followAirEdges && edgeZNeg == 0)) {
            findConnectedCorners(world, new Coordinate(start.x,start.y,start.z-1), followGroundEdges, followAirEdges, followSurfaceEdges, recursionDepth-1, connectedCoordinates);
        }
        if((followSurfaceEdges && edgeZPos > 0 && edgeZPos < 4) || (followGroundEdges && edgeZPos == 4) || (followAirEdges && edgeZPos == 0)) {
            findConnectedCorners(world, new Coordinate(start.x,start.y,start.z+1), followGroundEdges, followAirEdges, followSurfaceEdges, recursionDepth-1, connectedCoordinates);
        }
    }
}
