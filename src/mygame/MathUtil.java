/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

/**
 *
 * @author Nathan & Ben
 */
public class MathUtil {
    
    public static int PosMod(int a, int b) {
        int r = a % b;
        return r < 0 ? b + r : r;
    }
    
    public static float RelativeAdd(float a, float b) {
        return (a + b)/(1 + (a*b));
    }
    
    public static float RelativeAdd(float a, float b, float max) {
        return (a + b)/(1 + ((a*b)/(max*max)));
    }
    
/*
    public static int PosDiv(int a, int b) {
        return a < 0 ? (a / b) - 1 : a / b;
    }
*/
}
