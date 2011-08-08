package galaxy;

/**
 * A static class containing physics equations relevant to a solar system simulation.
 * Other than collision-handling, the methods in this class do not
 * mutate their arguments.
 * @author jardini
 */
public final class Physics {

    private Physics() {
    }

    /**
     * Calculates the Euclidean distance between two SolarBody instances.
     * @param body1     An Object3D
     * @param body2     An Object3D
     * @return          The distance between body1 and body2.
     */
    public static double distance(Object3D body1, Object3D body2) {
        double xDiff = body1.getX() - body2.getX();
        double yDiff = body1.getY() - body2.getY();
        double zDiff = body1.getZ() - body2.getZ();
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff));
    }

    public static void getCoordDiff(Object3D body1, Object3D body2, Vector3D coordDiff) {
        coordDiff.copy(body2.getPosition());
        coordDiff.subtractFrom(body1.getPosition());
    }


    /**
     * Calculates the gravitational force between two Object3D instances.
     * Note that if the two bodies are in the exact same position, the force
     * returned is 0, since it has no effect on the position/velocity.
     * @param body1     An Object3D
     * @param body2     An Object3D
     * @return          The force between body1 and body2.
     */
    public static double force(Object3D body1, Object3D body2) {
        double distance = distance(body1, body2);
        if (distance != 0) {
            double force = (SolarConstants.GRAVITY_CONSTANT * body1.getMass() * body2.getMass()) / (distance * distance);
            return force;
        } else return 0;
    }

    /**
     * Determines if body1 and body2 collide, using the collision constant
     * in SolarConstants and and radius of each of the bodies.  Note that
     * this does NOT check that the two objects are distinct.
     * @param body1 An Object3D
     * @param body2 A different Object3D
     * @return      True if and only if body1 will collid with body2
     */
    public static boolean isCollision(Object3D body1, Object3D body2) {
        double dist = distance(body1, body2);
        return (dist < (SolarConstants.COLLISION_CONSTANT * (body1.getRadius() + body2.getRadius())));
    }


    /**
     * Calculates the net gravitational force between two 3D objects, returned
     * as a Vector3D.  Note that if the two objects are in the same position,
     * the method returns 0.
     * @param body1     An Object3D
     * @param body2     An Object3D
     * @param distance  The distance between body1 and body2
     * @return          A 3D vector representing the force between body1 and body2.
     */
    public static Vector3D netForce(Object3D body1, Object3D body2, double distance) {
        if (distance != 0) {
            double distance3 = distance * distance * distance;
            double forceConstant = SolarConstants.GRAVITY_CONSTANT * body1.getMass() * body2.getMass();
            forceConstant /= distance3;
            // forceToAdd = (position2 - position1) * G * mass1 * mass2 / distance^3
            double xForce = forceConstant * (body2.getX() - body1.getX());
            double yForce = forceConstant * (body2.getY() - body1.getY());
            double zForce = forceConstant * (body2.getZ() - body1.getZ());
            return new Vector3D(xForce, yForce, zForce);
        } else return new Vector3D(0, 0, 0);
    }


    public static void addToNetForceToBody1(Object3D body1, Object3D body2, Vector3D coordDiff, double distance) {
        if (distance != 0) {
            double distance3 = distance * distance * distance;
            double forceConstant = SolarConstants.GRAVITY_CONSTANT * body1.getMass() * body2.getMass();
            forceConstant /= distance3;
            // forceToAdd = (position2 - position1) * G * mass1 * mass2 / distance^3
            /*double xForce = forceConstant * (body2.getX() - body1.getX());
            double yForce = forceConstant * (body2.getY() - body1.getY());
            double zForce = forceConstant * (body2.getZ() - body1.getZ());*/
            coordDiff.scaleBy(forceConstant);
            //return new Vector3D(xForce, yForce, zForce);
            body1.addToNetForce(coordDiff);
        }// else return new Vector3D(0, 0, 0);
    }

    /**
     * Calculates and returns the x-component of the velocity required for a
     * Object3D to rotate about another Object3D.
     * @param body  Any Object3D
     * @param star  Another (usually much larger / heavier) Object3D
     * @return      The x-component of the velocity required for body to have
     * a stable orbit around star.
     */
    public static double orbitVelocityX(Object3D body, Object3D star) {
        double force = force(body, star);
        double distance = distance(body, star);
        double v = Math.sqrt((force * distance) / body.getMass());
        return (v * body.getY()) / distance;
    }

    /**
     * Given an Object3D and the x-component of an orbital velocity, calculates
     * the y-component of the orbital velocity.
     * @param body      The body in orbit
     * @param orbitVelocityX    The x-component returned by orbitVelocityX
     * @return      The y-component of the velocity of body.
     */
    public static double orbitVelocityY(Object3D body, double orbitVelocityX) {
        return -(orbitVelocityX * body.getX()) / body.getY();
    }

    /**
     * Given two SolarBodies, combines them into one by mutating body1.
     * body1 gets a new mass, radius, position, and velocity.
     * body2 should be removed from the simulation after calling this method.
     * @param body1     The SolarBody that absorbs the collision
     * @param body2     The SolarBody that is absorbed in the collision
     */
    public static void processPlanetCollision(SolarBody body1, SolarBody body2) {
        // Set new mass
        double mass1 = body1.getMass();
        double mass2 = body2.getMass();
        double newMass = mass1 + mass2;
        body1.setMass(newMass);

        // Set new position
        // newPos = (body1.pos * body1.mass + body2.pos * body2.mass) / newMass
        Vector3D oldPos1 = body1.getPosition();
        oldPos1.scaleBy(mass1);
        Vector3D oldPos2 = body2.getPosition();
        oldPos2.scaleBy(mass2);
        Vector3D newPos = new Vector3D(oldPos1);
        newPos.addTo(oldPos2);
        newPos.divideBy(newMass);
        body1.setPosition(newPos);

        // Set new velocity
        // newVel = (body1.vel * body1.mass + body2.vel * body2.mass) / newMass
        Vector3D oldVel1 = body1.getVelocity();
        oldVel1.scaleBy(mass1);
        Vector3D oldVel2 = body2.getVelocity();
        oldVel2.scaleBy(mass2);
        Vector3D newVel = new Vector3D(oldVel1);
        newVel.addTo(oldVel2);
        newVel.divideBy(newMass);
        body1.setVelocity(newVel);

        // Set new radius
        double rad1 = body1.getRadius();
        double rad2 = body2.getRadius();
        body1.setRadius(Math.cbrt((rad1 * rad1 * rad1) + (rad2 * rad2 * rad2)));
    }
}
