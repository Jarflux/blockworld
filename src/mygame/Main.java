package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.BlockWorldViewport;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    private Material fBlockMat;
    private Material fTestMat;
    private BlockWorld fBlockWorld;
    private BlockWorldViewport fBlockWorldView;
    private BulletAppState bulletAppState;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;

    @Override
    public void simpleInitApp() {
        fBlockMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fBlockMat.setTexture("ColorMap", assetManager.loadTexture("Textures/grass.jpg"));
        fBlockMat.setColor("Color", ColorRGBA.Green);
        //fBlockMat.getAdditionalRenderState().setWireframe(true);
        
        fTestMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //fBlockMat.setTexture("ColorMap", assetManager.loadTexture("Textures/grass.jpg"));
        fTestMat.setColor("Color", ColorRGBA.Red);
        
        /** Set up Physics */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(.01f);
        setUpKeys();
        setUpLight();
        initCrossHairs();

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(.5f, 1.7f, 1);
        player = new CharacterControl(capsuleShape, 1.05f);
        player.setJumpSpeed(10);
        player.setFallSpeed(30);
        player.setGravity(30);
        player.setPhysicsLocation(new Vector3f(0, 3, 0));

        // We attach the scene and the player to the rootNode and the physics space,
        // to make them appear in the game world.
        bulletAppState.getPhysicsSpace().add(player);
        
        fBlockWorld = new BlockWorld(rootNode, fBlockMat, bulletAppState);
        fBlockWorldView = new BlockWorldViewport(fBlockWorld);
        
        /** Must add a light to make the lit object visible! */
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
        
      }


      private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.3f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setColor(ColorRGBA.White);
        dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
        rootNode.addLight(dl);
      }

      /** We over-write some navigational key mappings here, so we can
       * add physics-controlled walking and jumping: */
      private void setUpKeys() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("RemoveBlock", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(this, "RemoveBlock");
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
      }

      /** These are our custom actions triggered by key presses.
       * We do not walk yet, we just keep track of the direction the user pressed. */
      public void onAction(String binding, boolean value, float tpf) {
        if (binding.equals("Left")) {
          left = value;
        } else if (binding.equals("Right")) {
          right = value;
        } else if (binding.equals("Up")) {
          up = value;
        } else if (binding.equals("Down")) {
          down = value;
        } else if (binding.equals("Jump")) {
          player.jump();
        } else if (binding.equals("RemoveBlock") && value) {
            // 1. Reset results list.
            CollisionResults results = new CollisionResults();
            // 2. Aim the ray from cam loc to cam direction.
            Ray ray = new Ray(cam.getLocation(), cam.getDirection());
            // 3. Collect intersections between Ray and Shootables in results list.
            rootNode.collideWith(ray, results);
            // 5. Use the results (we mark the hit object)
            if (results.size() > 0) {
              // The closest collision point is what was truly hit:
              CollisionResult closest = results.getClosestCollision();
              closest.getGeometry().setMaterial(fTestMat);
              Vector3f contact = closest.getGeometry().getModelBound().getCenter();
              System.out.println("Vector: x = " + contact.x + ", y = " + contact.y + ", z = " + contact.z);
              if(fBlockWorld.get(Math.round(contact.x), Math.round(contact.y), Math.round(contact.z)) == null) {
                  System.out.println("Not found");
              }else{
                  System.out.println("Found");
              }
              fBlockWorld.removeBlock(Math.round(contact.x), Math.round(contact.y), Math.round(contact.z));
            }
        }
      }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left)  { walkDirection.addLocal(camLeft); }
        if (right) { walkDirection.addLocal(camLeft.negate()); }
        if (up)    { walkDirection.addLocal(camDir); }
        if (down)  { walkDirection.addLocal(camDir.negate()); }
        player.setWalkDirection(walkDirection);
        Vector3f camPos = player.getPhysicsLocation();
        cam.setLocation(camPos);
        fBlockWorldView.updatePosition(Math.round(camPos.x), Math.round(camPos.y), Math.round(camPos.z));
    }
    
    /** A centred plus sign to help the player aim. */
    protected void initCrossHairs() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
          settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
          settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
      }
  
    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
