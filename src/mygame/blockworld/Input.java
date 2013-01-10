/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

/**
 *
 * @author Fusion
 */
public class Input implements InputListener{
    // 0=up  1=down 2=left 3=right
    public final int[] qwerty = {KeyInput.KEY_W,KeyInput.KEY_S,KeyInput.KEY_A,KeyInput.KEY_D};
    public final int[] azerty = {KeyInput.KEY_Z,KeyInput.KEY_S,KeyInput.KEY_Q,KeyInput.KEY_D};
    
    public Input(){  
    }
    
    public void setUpKeys(InputManager im, ActionListener listener, int[] keyboard) {
        im.addMapping("Left", new KeyTrigger(keyboard[2]));
        im.addMapping("Right", new KeyTrigger(keyboard[3]));
        im.addMapping("Up", new KeyTrigger(keyboard[0]));
        im.addMapping("Down", new KeyTrigger(keyboard[1]));
        im.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        im.addMapping("RemoveBlock", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        im.addMapping("AddBlock", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        im.addMapping("Save", new KeyTrigger(KeyInput.KEY_F9));
        im.addMapping("Load", new KeyTrigger(KeyInput.KEY_F10));
        im.addListener(listener, "Save");
        im.addListener(listener, "Load");
        im.addListener(listener, "RemoveBlock");
        im.addListener(listener, "AddBlock");
        im.addListener(listener, "Left");
        im.addListener(listener, "Right");
        im.addListener(listener, "Up");
        im.addListener(listener, "Down");
        im.addListener(listener, "Jump");
    }
}
