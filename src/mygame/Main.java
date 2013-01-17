package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapText;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.texture.Texture;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3tools.optimize.TextureAtlas;
import mygame.blockworld.BlockWorld;
import mygame.blockworld.BlockWorldViewport;
import mygame.blockworld.Chunk;
import mygame.blockworld.Input;
import mygame.blockworld.surfaceextraction.BasicTriangulation;
import mygame.blockworld.surfaceextraction.MeshCreator;

/**
 * test
 *
 * @author Nathan & Ben
 */
public class Main extends SimpleApplication implements ActionListener {

    private static final int PLAYER_GRAVITY = 0;
    private static final int PLAYER_FALLSPEED = 30;
    private static final int PLAYER_JUMPSPEED = 15;
    private static final float PLAYER_WALKSPEED = 0.1f * 4f;
    private static final float PLAYER_STEPHEIGHT = 0.25f * 4f;
    private static final float PLAYER_HITBOX_HEIGHT = 0.75f * 4f;
    private static final float PLAYER_HITBOX_RADIUS = 0.25f * 4f;
    private static final Vector3f PLAYER_START_LOCATION = new Vector3f(0, 25, 0);
    private static final String SAVE_GAME_PATH = "Worlds/world0.dat";  
    
    private Material fBlockMat;
    private BlockWorld fBlockWorld;
    private TextureAtlas fAtlas;
    private BlockWorldViewport fBlockWorldView;
    private BulletAppState bulletAppState;
    private CharacterControl player;
    private AudioNode audio_nature;
    private AudioNode audio_removeBlock;
    private BitmapText hudPosition;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Main app = new Main();
        app.startLogging();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        //fBlockMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        fBlockMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //fBlockMat = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
        //fBlockMat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");

        Texture text = assetManager.loadTexture("Textures/dirt.png");
        text.setMinFilter(Texture.MinFilter.BilinearNoMipMaps);
        text.setMagFilter(Texture.MagFilter.Nearest);
        fBlockMat.setTexture("DiffuseMap", text);
        fBlockMat.setBoolean("VertexLighting", true);
        fBlockMat.setBoolean("HighQuality", true);
        //fBlockMat.setBoolean("UseMaterialColors",true);
        fBlockMat.setBoolean("WardIso", true);
        fBlockMat.setBoolean("SeparateTexCoord", true);

        //fBlockMat.setTexture("ColorMap", assetManager.loadTexture("Textures/dirt.png"));
        //fBlockMat.setColor("Color", ColorRGBA.Green);
        //fBlockMat.getAdditionalRenderState().setWireframe(true);
        //fBlockMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        //fBlockMat.setTexture("ColorMap", assetManager.loadTexture("Textures/grass.png"));
        //fBlockMat.setColor("Color", ColorRGBA.Green);

        /**
         * Set up Physics
         */
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // We re-use the flyby camera for rotation, while positioning is handled by physics
        //viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
        flyCam.setMoveSpeed(10f);
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
        setUpKeys();
        setUpLight();
        initCrossHairs();

        // We set up collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        // The CharacterControl offers extra settings for
        // size, stepheight, jumping, falling, and gravity.
        // We also put the player in its starting position.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(PLAYER_HITBOX_RADIUS, PLAYER_HITBOX_HEIGHT, 1);
        player = new CharacterControl(capsuleShape, PLAYER_STEPHEIGHT);
        player.setJumpSpeed(PLAYER_JUMPSPEED);
        player.setFallSpeed(PLAYER_FALLSPEED);
        player.setGravity(PLAYER_GRAVITY);
        player.setPhysicsLocation(PLAYER_START_LOCATION);

        // We attach the scene and the player to the rootNode and the physics space,
        // to make them appear in the game world.
        bulletAppState.getPhysicsSpace().add(player);
        //cam.setLocation(new Vector3f(0, 30, 0));
        fBlockWorld = new BlockWorld(rootNode, fBlockMat, fAtlas, bulletAppState);
        /*Chunk cnk = fBlockWorld.getChunk(0, 0, 0, true, true);
         cnk.setVisible(true);
         cnk.update();*/
        fBlockWorldView = new BlockWorldViewport(fBlockWorld);
        setUpdAudio();
        setUpHud();
    }

    private void setUpLight() {
        // We add light so we see the scene
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.5f));
        rootNode.addLight(al);

        SkyDome skyDome = new SkyDome(assetManager, cam,
                "Models/Skies/SkyDome.j3o",
                "Textures/Skies/SkyNight_L.png",
                "Textures/Skies/Moon_L.png",
                "Textures/Skies/Sun_L.png",
                "Textures/Skies/Clouds_L.png",
                "Textures/Skies/Fog_Alpha.png");
        Node sky = new Node();
        sky.setQueueBucket(Bucket.Sky);
        sky.addControl(skyDome);
        sky.setCullHint(Spatial.CullHint.Never);

        // Either add a reference to the control for the existing JME fog filter or use the one I posted…
        // But… REMEMBER!  If you use JME’s… the sky dome will have fog rendered over it.
        // Sorta pointless at that point
        FogFilter fog = new FogFilter();
        skyDome.setFogFilter(fog, viewPort);

        // Set some fog colors… or not (defaults are cool)
        //skyDome.setFogColor(fogColor);
        //skyDome.setFogNightColor(fogNightColor);

        // Enable the control to modify the fog filter
        skyDome.setControlFog(true);

        // Add the directional light you use for sun… or not
        DirectionalLight sun = new DirectionalLight();
        sun.setColor(ColorRGBA.White.mult(0.9f));
        sun.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
        rootNode.addLight(sun);
        skyDome.setSun(sun);
        //skyDome.setDayNightTransitionSpeed(1f);
        //skyDome.setMoonSpeed(0.5f);

        // Set some sunlight day/night colors… or not
        //skyDome.setSunDayLight(dayLight);
        //skyDome.setSunNightLight(nightLight);

        // Enable the control to modify your sunlight
        skyDome.setControlSun(true);
        //skyDome.cycleDayToNight();
        // Enable the control
        skyDome.setEnabled(true);

        // Add the skydome to the root… or where ever
        rootNode.attachChild(sky);

    }

    private void setUpdAudio() {
        audio_removeBlock = new AudioNode(assetManager, "Sounds/Effects/RemoveBlock.ogg", false);
        audio_removeBlock.setLooping(false);
        audio_removeBlock.setVolume(.1f);
        rootNode.attachChild(audio_removeBlock);

        audio_nature = new AudioNode(assetManager, "Sounds/Environment/Nature.ogg", false);
        audio_nature.setLooping(true);  // activate continuous playing
        audio_nature.setPositional(true);
        audio_nature.setLocalTranslation(Vector3f.ZERO.clone());
        audio_nature.setVolume(.5f);
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
        hudPosition.setLocalTranslation(0, hudPosition.getLineHeight() + 500, 0);
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
                int x = Math.round(contactPoint.x - contactNormal.x * .5f);
                int y = Math.round(contactPoint.y - contactNormal.y * .5f);
                int z = Math.round(contactPoint.z - contactNormal.z * .5f);
                fBlockWorld.removeBlock(x, y, z);
                /*int sphereSize = 1;
                for (int i = -sphereSize; i < sphereSize + 1; i++) {
                    for (int j = -sphereSize; j < sphereSize + 1; j++) {
                        for (int k = -sphereSize; k < sphereSize + 1; k++) {
                            if (Math.round(Math.sqrt(i * i + j * j + k * k)) <= sphereSize) {
                                fBlockWorld.removeBlock(x + i, y + j, z + k);
                            }
                        }
                    }
                }*/
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
                int x = Math.round(contactPoint.x + contactNormal.x * .5f);
                int y = Math.round(contactPoint.y + contactNormal.y * .5f);
                int z = Math.round(contactPoint.z + contactNormal.z * .5f);
                fBlockWorld.addBlock(2, x, y, z);
                /*int sphereSize = 1;
                for (int i = -sphereSize; i < sphereSize + 1; i++) {
                    for (int j = -sphereSize; j < sphereSize + 1; j++) {
                        for (int k = -sphereSize; k < sphereSize + 1; k++) {
                            if (Math.round(Math.sqrt(i * i + j * j + k * k)) <= sphereSize) {
                                fBlockWorld.addBlock(2, x + i, y + j, z + k);
                            }
                        }
                    }
                }*/
                audio_removeBlock.playInstance();
            }
        } else if (binding.equals("Save") && value) {
            fBlockWorld.saveWorld(SAVE_GAME_PATH);
        } else if (binding.equals("Load") && value) {
            fBlockWorld.loadWorld("Worlds/world0.dat");
        } else if (binding.equals("SwitchRender") && value) {
            MeshCreator old = Chunk.getMeshCreator();
            Chunk.setMeshCreator(fOtherCreator);
            fOtherCreator = old;
        } else if (binding.equals("SwitchCulling") && value) {
            RenderState.FaceCullMode old = fBlockMat.getAdditionalRenderState().getFaceCullMode();
            fBlockMat.getAdditionalRenderState().setFaceCullMode(fOtherCullMode);
            fOtherCullMode = old;
        } else if (binding.equals("SwitchWireFrame") && value) {
            fBlockMat.getAdditionalRenderState().setWireframe(!fBlockMat.getAdditionalRenderState().isWireframe());
        }
    }
    
    private MeshCreator fOtherCreator = new BasicTriangulation();
    private RenderState.FaceCullMode fOtherCullMode = RenderState.FaceCullMode.Off;

    @Override
    public void simpleUpdate(float tpf) {
        Vector3f playerPosition = player.getPhysicsLocation();
        DecimalFormat df = new DecimalFormat("0.000");
        hudPosition.setText("Position:\nx:" + df.format(playerPosition.x) + "\ny:" + df.format(playerPosition.y) + "\nz:" + df.format(playerPosition.z));
        Vector3f camDir = cam.getDirection().clone().multLocal(PLAYER_WALKSPEED);
        Vector3f camLeft = cam.getLeft().clone().multLocal(PLAYER_WALKSPEED * 0.65f);
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
        camPos.y = camPos.y + .75f * 4f * 2f;
        cam.setLocation(camPos);
        listener.setLocation(cam.getLocation());

        //Vector3f camPos = cam.getLocation();
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
