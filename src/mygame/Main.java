package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.BlockWorldViewport;

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
        
        fBlockWorld = new BlockWorld(rootNode, fBlockMat);
        fBlockWorldView = new BlockWorldViewport(fBlockWorld);
        cam.setLocation(new Vector3f(0, 0, 1));
        
        /** Must add a light to make the lit object visible! */
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        rootNode.addLight(sun);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camPos = cam.getLocation();
        fBlockWorldView.updatePosition(Math.round(camPos.x), Math.round(camPos.z), Math.round(camPos.y));
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
