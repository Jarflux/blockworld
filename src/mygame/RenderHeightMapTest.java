/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.terrain.noise.basis.ImprovedNoise;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Nathan & Ben
 */
public class RenderHeightMapTest {
    
    public static void main(String[] args) {
        JFrame frm = new JFrame("Test");
        frm.add(new JPanel() {
            private int k = 0;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                for(int i = 0; i < 256; i++) {
                    for(int j = 0; j < 256; j++) {
                        //float f = noise.value((float)i/32f, (float)j/32f, (float)k);
                        float f = ImprovedNoise.noise((float)i/32f, (float)j/32f, (float)k);
                        g.setColor(new Color(1f-(f+1f)/2f, 1f-(f+1f)/2f, 1f-(f+1f)/2f));
                        g.drawLine(i, j, i, j);
                    }
                }
                System.out.println(k);
                k = k+1;
            }

        });
        frm.setVisible(true);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setSize(300, 300);
    }
}
