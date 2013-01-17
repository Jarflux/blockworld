/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame.blockworld;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author Fusion
 */
public class RayTracing {

    public void doRayTracing(Node rootNode, Vector3f sourceLocation, Vector3f sourceDirection, int bounces) {
        // 1. Reset results list.
        CollisionResults results = new CollisionResults();
        // 2. Aim the ray from cam loc to cam direction.
        Ray ray = new Ray(sourceLocation, sourceDirection);
        // 3. Collect intersections between Ray and Shootables in results list.
        rootNode.collideWith(ray, results);
        // 5. Use the results (we mark the hit object)
        if (results.size() > 0) {
            CollisionResult closest = results.getClosestCollision();
            Vector3f contactPoint = closest.getContactPoint();
            Vector3f contactNormal = closest.getContactNormal();
            if( bounces - 1 > 0 ){
                doRayTracing(rootNode, contactPoint, contactNormal, bounces - 1);
            }
        }
    }
}
