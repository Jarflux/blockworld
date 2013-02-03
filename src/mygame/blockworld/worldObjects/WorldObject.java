/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld.worldObjects;

import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import mygame.blockworld.Block;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.surfaceextraction.BasicTriangulation;
import mygame.blockworld.surfaceextraction.BlockContainer;
import mygame.blockworld.surfaceextraction.MeshCreator;

/**
 *
 * @author Fusion
 */
public class WorldObject implements BlockContainer, Savable {

    private BlockWorld fWorld;
    private Node fRootNode;
    private Geometry fObjectMesh;
    private RigidBodyControl fObjectPhysics;
    private BulletAppState fPhysicsState;
    private static final MeshCreator fMeshCreator = new BasicTriangulation();
    private Map<String, Block> fBlocks = new HashMap<String, Block>();
    private Boolean fNeedsUpdate;
    private Boolean fVisible = true;

    public WorldObject(BlockWorld world, Node rootNode, BulletAppState physicsState, Transform transform) {
        fWorld = world;
        fRootNode = rootNode;
        fPhysicsState = physicsState;
        fObjectMesh = new Geometry();
        fObjectMesh.setLocalTransform(transform);
    }

    public void scheduleUpdate() {
        fNeedsUpdate = true;
    }

    public void update() {
//        updateVisualMesh();
//        updatePhysicsMesh();
    }

    public Boolean isVisible() {
        return fVisible;
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Block getBlock(int x, int y, int z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector3f getNormal(int x, int y, int z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector3f getConstantLightColor(int x, int y, int z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Vector3f getPulseLightColor(int x, int y, int z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSunlightValue(int x, int y, int z) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
