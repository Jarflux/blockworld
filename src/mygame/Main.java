package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
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
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.BlockWorldViewport;
import mygame.blockworld.Input;

/**
 * test
 *
 * @author Nathan & Ben
 */
public class Main extends SimpleApplication implements ActionListener {

    public static void main(String[] args) {
        Main app = new Main();
        app.startLogging();
        app.start();
    }
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private Material fBlockMat;
    private BlockWorld fBlockWorld;
    private BlockWorldViewport fBlockWorldView;
    private BulletAppState bulletAppState;
    private CharacterControl player;
    private AudioNode audio_nature;
    private AudioNode audio_removeBlock;
    private BitmapText hudPosition;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    
    @Override
    public void simpleInitApp() {
        fBlockMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fBlockMat.setTexture("ColorMap", assetManager.loadTexture("Textures/grass.jpg"));
        //fBlockMat.setColor("Color", ColorRGBA.Green);
        
        //fBlockMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //fBlockMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/grass.jpg"));
        //fBlockMat.setBoolean("UseMaterialColors",true);    
        //fBlockMat.setColor("Specular",ColorRGBA.White);
        //fBlockMat.setColor("Diffuse",ColorRGBA.White);
        //fBlockMat.setFloat("Shininess", 5f); // [1,128]    
        
        //fBlockMat.getAdditionalRenderState().setWireframe(true);

        /**
         * Set up Physics
         */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(.01f);
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
        setUpKeys();
        setUpLight();
        initCrossHairs();

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(.25f, .75f, 1);
        player = new CharacterControl(capsuleShape, 0.25f);
        player.setJumpSpeed(8);
        player.setFallSpeed(30);
        player.setGravity(0);
        player.setPhysicsLocation(new Vector3f(0, 5, 0));

        // We attach the scene and the player to the rootNode and the physics space,
        // to make them appear in the game world.
        bulletAppState.getPhysicsSpace().add(player);

        fBlockWorld = new BlockWorld(rootNode, fBlockMat, bulletAppState);
        //fBlockWorld.getChunk(5,0,5, true).showChunk();
        fBlockWorldView = new BlockWorldViewport(fBlockWorld);
        setUpdAudio();
        setUpHud();
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

    private void setUpdAudio(){
        audio_removeBlock = new AudioNode(assetManager, "Sounds/Effects/RemoveBlock.ogg", false);
        audio_removeBlock.setLooping(false);
        audio_removeBlock.setVolume(.1f);
        rootNode.attachChild(audio_removeBlock); 
        
        audio_nature = new AudioNode(assetManager, "Sounds/Environment/Nature.ogg", false);
        audio_nature.setLooping(true);  // activate continuous playing
        audio_nature.setPositional(true);
        audio_nature.setLocalTranslation(Vector3f.ZERO.clone());
        audio_nature.setVolume(3);
        rootNode.attachChild(audio_nature);
        audio_nature.play(); // play continuously!
    }
    /**
     * We over-write some navigational key mappings here, so we can add
     * physics-controlled walking and jumping:
     */
    private void setUpKeys() {
        Input input = new Input();
        input.setUpKeys(inputManager, this, input.qwerty);
    }

    private void setUpHud() {
        hudPosition = new BitmapText(guiFont, false);
        hudPosition.setSize(guiFont.getCharSet().getRenderedSize());      
        hudPosition.setColor(ColorRGBA.White);                          
        hudPosition.setText("Position: ..."); 
        hudPosition.setLocalTranslation(0, hudPosition.getLineHeight()+500, 0);
        guiNode.attachChild(hudPosition);
    }

    /**
     * These are our custom actions triggered by key presses. We do not walk
     * yet, we just keep track of the direction the user pressed.
     */
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
                Vector3f contactPoint = closest.getContactPoint();
                Vector3f contactNormal = closest.getContactNormal();
                fBlockWorld.removeBlock(Math.round(contactPoint.x - contactNormal.x * .5f), Math.round(contactPoint.y - contactNormal.y * .5f), Math.round(contactPoint.z - contactNormal.z * .5f));
                audio_removeBlock.playInstance();
            }
        } else if (binding.equals("AddBlock") && value) {
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
                Vector3f contactPoint = closest.getContactPoint();
                Vector3f contactNormal = closest.getContactNormal();
                fBlockWorld.addBlock(1, Math.round(contactPoint.x + contactNormal.x * .5f), Math.round(contactPoint.y + contactNormal.y * .5f), Math.round(contactPoint.z + contactNormal.z * .5f));
                audio_removeBlock.playInstance();
                /*
                Vector3f punt = results.getClosestCollision().getContactPoint();
                Vector3f center = results.getClosestCollision().getGeometry().getModelBound().getCenter();
                Vector3f dir = punt.subtract(center);
                dir.mult(2);
                logger.log(Level.INFO, "Aimed At Block: x = {0}, y = {1}, z = {2}", new Object[]{center.x, center.y, center.z});
                logger.log(Level.INFO, "Add Block Direction: x = {0}, y = {1}, z = {2}", new Object[]{dir.x, dir.y, dir.z});
                if ((Math.abs(dir.x)) > (Math.abs(dir.y))) {
                    if ((Math.abs(dir.x)) > (Math.abs(dir.z))) {
                        logger.log(Level.INFO, "Biggest x:= {0}, ", new Object[]{dir.x});
                        logger.log(Level.INFO, "New Block x = {0}, y = {1}, z = {2}", new Object[]{Math.round(center.x + (dir.x * 2)), Math.round(center.y), Math.round(center.z)});
                        fBlockWorld.addBlock(1, Math.round(center.x + (dir.x * 2)), Math.round(center.y), Math.round(center.z));
                    } else {
                        logger.log(Level.INFO, "Biggest z:= {0}, ", new Object[]{dir.z});
                        logger.log(Level.INFO, "New Block x = {0}, y = {1}, z = {2}", new Object[]{Math.round(center.x), Math.round(center.y), Math.round(center.z + (dir.z * 2))});
                        fBlockWorld.addBlock(1, Math.round(center.x), Math.round(center.y), Math.round(center.z + (dir.z * 2)));
                    }
                } else {
                    if ((Math.abs(dir.y)) > (Math.abs(dir.z))) {
                        logger.log(Level.INFO, "Biggest y:= {0}, ", new Object[]{dir.y});
                        logger.log(Level.INFO, "New Block x = {0}, y = {1}, z = {2}", new Object[]{Math.round(center.x), Math.round(center.y + (dir.y * 2)), Math.round(center.z)});
                        fBlockWorld.addBlock(1, Math.round(center.x), Math.round(center.y + (dir.y * 2)), Math.round(center.z));
                    } else {
                        logger.log(Level.INFO, "Biggest z:= {0}, ", new Object[]{dir.z});
                        logger.log(Level.INFO, "New Block x = {0}, y = {1}, z = {2}", new Object[]{Math.round(center.x), Math.round(center.y), Math.round(center.z + (dir.z * 2))});
                        fBlockWorld.addBlock(1, Math.round(center.x), Math.round(center.y), Math.round(center.z + (dir.z * 2)));
                    }
                }*/
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f playerPosition = player.getPhysicsLocation();
        DecimalFormat df = new DecimalFormat("0.000");
        hudPosition.setText("Position:\nx:" + df.format(playerPosition.x) + "\ny:" + df.format(playerPosition.y) + "\nz:" + df.format(playerPosition.z));
        Vector3f camDir = cam.getDirection().clone().multLocal(0.1f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.065f);
        camDir.y = 0;
        camLeft.y = 0;
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        Vector3f camPos = player.getPhysicsLocation();
        camPos.y = camPos.y + .75f;
        cam.setLocation(camPos);
        listener.setLocation(cam.getLocation());
        fBlockWorldView.updatePosition(Math.round(camPos.x), Math.round(camPos.y), Math.round(camPos.z));
    }

    /**
     * A centred plus sign to help the player aim.
     */
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

    public void startLogging() {
        Logger.getLogger("").setLevel(Level.SEVERE);
        //Logger.getLogger(Main.class.getName()).setLevel(Level.ALL);
    }
}
