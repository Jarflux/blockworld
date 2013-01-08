package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.BlockWorldViewport;
import mygame.blockworld.Chunk;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    private Material fBlockMat;
    private BlockWorld fBlockWorld;
    private BlockWorldViewport fBlockWorldView;
    
    @Override
    public void simpleInitApp() {
        fBlockMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fBlockMat.setColor("Color", ColorRGBA.Blue);
        //fBlockMat.getAdditionalRenderState().setWireframe(true);
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        
        fBlockWorld = new BlockWorld(rootNode, fBlockMat);
        fBlockWorldView = new BlockWorldViewport(fBlockWorld);
        //cam.setLocation(new Vector3f(0, 0, 3));
        //cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_X);
        //fBlockWorld.getChunk(0, 0, -3, true).showChunk();
        
        /** Must add a light to make the lit object visible! */
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camPos = cam.getLocation();
        fBlockWorldView.updatePosition(Math.round(camPos.x), Math.round(camPos.y), Math.round(camPos.z));
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
