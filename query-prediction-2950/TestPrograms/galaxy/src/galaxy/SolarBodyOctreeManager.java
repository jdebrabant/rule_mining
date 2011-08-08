package galaxy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
/**
 * A class for managing a simulation through the use of an octree.  All
 * SolarBodies are stored in a collection.  Each time the bodies are updated,
 * an octree is built for efficient updating of positions/velocities and for
 * collision detection.
 * @author jardini
 */
public class SolarBodyOctreeManager extends SolarBodyManager {

    Octree<SolarBody> octree;
    Collection<SolarBody> bodies;
    //SolarDraw solarDraw;

    /**
     * @param s     A non-null SolarDraw for drawing SolarBodies to the screen
     */
    public SolarBodyOctreeManager() {
        bodies = new LinkedList<SolarBody>();
        //solarDraw = s;
    }

    /**
     * Adds body to the simulation and registers body with solarDraw.
     * @param body  A SolarBody not yet in the simulation.
     */
    public void addBody(SolarBody body) {
        //solarDraw.registerObject(body);
        bodies.add(body);
    }

    /**
     * Removes body from the simulation and unregisters the body with solarDraw.
     * @param body  A body currently in the simulation.
     */
    public void removeBody(SolarBody body) {
        //solarDraw.unregisterObject(body);
        bodies.remove(body);
    }

    /**
     * @return An Iterable collection of SolarBodies.
     */
    public Collection<SolarBody> getBodies() {
        return Collections.unmodifiableCollection(bodies);
    }

    /**
     * Processes all collisions occuring within a tick using the octree.
     * All bodies are collided with the root of the octree
     */
    public void processCollisions() {
        Collection<SolarBody> toRemove = new LinkedList<SolarBody>();
        // Loop through all bodies
        for (SolarBody body : bodies) {
            if (!toRemove.contains(body)) {
                // If object in system, check for collisions
                if (octree.intersects(body)) {
                    SolarBody collider = octree.collide(body, toRemove);
                    if (collider != null) {
                        processCollision(body, collider, toRemove);
                    }
                } else {
                   // If object has escaped the system, remove it
                    toRemove.add(body);
                }
            }
        }
        // Removes and unregisters bodies
        for (SolarBody body : toRemove) {
            removeBody(body);
        }
    }

    private String getCombinedBodyName(String collider_name_1, String collider_name_2) {
        return collider_name_1.compareTo(collider_name_2) > 0 ? collider_name_1 : collider_name_2;
    }

    /**
     * Processes a collision based on the collision behavior of the two bodies
     * @param body1     A SolarBody
     * @param body2     A SolarBody colliding with body1
     * @param toRemove  A list of all bodies being removed
     */
    private void processCollision(SolarBody body1, SolarBody body2, Collection<SolarBody> toRemove) {
        System.out.println(body1+" collided with "+body2);
        SolarConstants.CollisionBehavior behavior1 = body1.getCollisionBehavior();
        SolarConstants.CollisionBehavior behavior2 = body2.getCollisionBehavior();
        if (behavior1 == SolarConstants.CollisionBehavior.Combine) {
            toRemove.add(body2);
            // If both Bodies should combine, combine them as body1
            // and give body1 a new name
            if (behavior2 == SolarConstants.CollisionBehavior.Combine) {
                Physics.processPlanetCollision(body1, body2);
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
     * Updates the position and velocities of all bodies in the simulation
     * by building an octree
     * @param timestep  Timestep for the simulation
     */
    public void updateSolarBodies(double timestep) {
        octree = new Octree<SolarBody>(OctreeConstants.CENTER, OctreeConstants.INITIAL_SIDE_LENGTH / 2);
        // Reset the net force of all bodies
        for (SolarBody body : bodies) {
            body.resetNetForce();
        }
        // Build a new octree from all bodies
        octree.build(bodies);
        // Calculate forces through the octree
        Vector3D coordDiff = new Vector3D();
        for (SolarBody body : bodies) {
            coordDiff.set(0, 0, 0);
            octree.addTreeForce(body, coordDiff);
            //Vector3D toAdd = octree.treeForce(body);
            //body.addForce(toAdd);
        }
        // Update the position and velocity of each body
        for (SolarBody body : bodies) {
            body.updatePositionAndVelocity(timestep);
        }
    }
}

