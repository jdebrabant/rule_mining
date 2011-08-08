package solar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of a SolarBodyManager using a simple List to keep track
 * of all SolarBodies in the simulation.  This class also takes care of
 * registering and unregistering SolarBodies with the solarDraw GUI.
 * @author jardini
 */
public class SolarBodyListManager extends SolarBodyManager {

    List<SolarBody> bodies;

    /**
     * Constructor
     * @param s - A non-null SolarDraw for drawing SolarBodies to the screen
     */
    public SolarBodyListManager() {
        bodies = new ArrayList<SolarBody>();
    }

    /**
     * Adds body to the simulation and registers body with solarDraw.
     * @param body - A SolarBody not yet in the simulation.
     */
    public void addBody(SolarBody body) {
        bodies.add(body);
    }

    /**
     * Removes body from the simulation and unregisters the body with solarDraw.
     * @param body - A body currently in the simulation.
     */
    public void removeBody(SolarBody body) {
        bodies.remove(body);
    }

    /**
     * @return An Iterable collection of SolarBodies.
     */
    public Iterable<SolarBody> getBodies() {
        return Collections.unmodifiableCollection(bodies);
    }

    /**
     * Simple n^2 algorithm for processing collisions between all SolarBodies
     * in the simulation.  This method also takes care of unregistering the
     * removed SolarBodies from solarDraw.
     */
    public void processCollisions() {
        List<SolarBody> toRemove = new LinkedList<SolarBody>();
        for (SolarBody body1 : bodies) {
            for (SolarBody body2 : bodies) {
                // Make sure neither body has already been removed
                if (body1 != body2 && !toRemove.contains(body1) && !toRemove.contains(body2)) {
                    double distance = SolarPhysics.distance(body1, body2);
                    // If the bodies collide, process the collision
                    if (distance < SolarConstants.COLLISION_CONSTANT * (body1.getRadius() + body2.getRadius())) {
                        processCollision(body1, body2, toRemove);
                    }
                }
            }
        }
        // Remove and unregister the bodies in toRemove
        for (SolarBody body : toRemove) {
            removeBody(body);
        }
    }

    private String getCombinedBodyName(String collider_name_1, String collider_name_2) {
        return collider_name_1.compareTo(collider_name_2) > 0 ? collider_name_1 : collider_name_2;
    }

    /**
     * Processes a collision based on the collision behavior of the two bodies
     * @param body1 - A SolarBody
     * @param body2 - A SolarBody colliding with body1
     * @param toRemove - A list of all bodies being removed
     */
    private void processCollision(SolarBody body1, SolarBody body2, List<SolarBody> toRemove) {
        SolarConstants.CollisionBehavior behavior1 = body1.getCollisionBehavior();
        SolarConstants.CollisionBehavior behavior2 = body2.getCollisionBehavior();
        if (behavior1 == SolarConstants.CollisionBehavior.Combine) {
            toRemove.add(body2);
            // If both Bodies should combine, combine them as body1
            // and give body1 a new name
            if (behavior2 == SolarConstants.CollisionBehavior.Combine) {
                SolarPhysics.processPlanetCollision(body1, body2);
                body1.setName(getCombinedBodyName(body1.getName(), body2.getName()));
            }
        } else if (behavior1 == SolarConstants.CollisionBehavior.Explode) {
            toRemove.add(body1);
            // If both bodies explode, they are both removed
            if (behavior2 == SolarConstants.CollisionBehavior.Explode) {
                toRemove.add(body2);
            }
        } else {
            throw new IllegalArgumentException("Invalid collision type!");
        }
    }

    /**
     * n^2 algorithm that updates the position and velocities for all
     * SolarBodies in the simulation.  Does so by first calculating the
     * net force on each SolarBody.
     * @param timestep - The timestep of the simulation.
     */
    public void updateSolarBodies(double timestep) {
        for (SolarBody body1 : bodies) {
            body1.resetNetForce();
            // Adds the force from all other SolarBodies
            for (SolarBody body2 : bodies) {
                if (body1 != body2) {
                    body1.addForce(body2);
                }
            }
        }
        for (SolarBody body : bodies) {
            // Updates the position and velocity
            body.updatePositionAndVelocity(timestep);
        }
    }

}