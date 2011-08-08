package solar;

/**
 * A static class containing physics equations relevant to a solar system simulation.
 * Other than collision-handling, the methods in this class do not
 * mutate their arguments.
 * @author jardini
 */
public final class SolarPhysics {

    private SolarPhysics() {
    }

    /**
     * Calculates the Euclidean distance between two SolarBody instances.
     * @param body1 - A SolarBody
     * @param body2 - A SolarBody
     * @return The distance between body1 and body2.
     */
    public static double distance(SolarBody body1, SolarBody body2) {
        double xDiff = body1.getX() - body2.getX();
        double yDiff = body1.getY() - body2.getY();
        double zDiff = body1.getZ() - body2.getZ();
        return Math.sqrt((xDiff * xDiff) + (yDiff * yDiff) + (zDiff * zDiff));
    }

    /**
     * Calculates the gravitational force between two SolarBody instances.
     * Note that if the two bodies are in the exact same position, the force
     * returned is 0, since it has no effect on the position/velocity.
     * @param body1 - A SolarBody
     * @param body2 - A SolarBody
     * @return The force between body1 and body2.
     */
    public static double force(SolarBody body1, SolarBody body2) {
        double distance = distance(body1, body2);
        if (distance != 0) {
            double force = (SolarConstants.GRAVITY_CONSTANT * body1.getMass() * body2.getMass()) / (distance * distance);
            return force;
        } else return 0;
    }

    /**
     * Calculates and returns the x-component of the velocity required for a
     * SolarBody to rotate about another SolarBody.
     * @param body - Any SolarBody
     * @param star - Another  (usually much larger / heavier) SolarBody
     * @return The x-component of the velocity required for body to have
     * a stable orbit around star.
     */
    public static double orbitVelocityX(SolarBody body, SolarBody star) {
        double force = force(body, star);
        double distance = distance(body, star);
        double v = Math.sqrt((force * distance) / body.getMass());
        return (v * body.getY()) / distance;
    }

    /**
     * Given a SolarBody and the x-component of an orbital velocity, calculates
     * the y-component of the orbital velocity.
     * @param body - The body in orbit
     * @param orbitVelocityX - The x-component returned by orbitVelocityX
     * @return The y-component of the velocity of body.
     */
    public static double orbitVelocityY(SolarBody body, double orbitVelocityX) {
        return -(orbitVelocityX * body.getX()) / body.getY();
    }

    /**
     * Given two SolarBodies, combines them into one by mutating body1.
     * body1 gets a new mass, radius, position, and velocity.
     * body2 should be removed from the simulation after calling this method.
     * @param body1 - The SolarBody that absorbs the collision
     * @param body2 - The SolarBody that is absorbed in the collision
     */
    public static void processPlanetCollision(SolarBody body1, SolarBody body2) {
        // Set new mass
        double mass1 = body1.getMass();
        double mass2 = body2.getMass();
        double newMass = mass1 + mass2;
        body1.setMass(newMass);

        // Set new position
        // newPos = (body1.pos * body1.mass + body2.pos * body2.mass) / newMass
        SolarVector oldPos1 = body1.getPosition();
        oldPos1.scaleBy(mass1);
        SolarVector oldPos2 = body2.getPosition();
        oldPos2.scaleBy(mass2);
        SolarVector newPos = new SolarVector(oldPos1);
        newPos.addTo(oldPos2);
        newPos.divideBy(newMass);
        body1.setPosition(newPos);

        // Set new velocity
        // newVel = (body1.vel * body1.mass + body2.vel * body2.mass) / newMass
        SolarVector oldVel1 = body1.getVelocity();
        oldVel1.scaleBy(mass1);
        SolarVector oldVel2 = body2.getVelocity();
        oldVel2.scaleBy(mass2);
        SolarVector newVel = new SolarVector(oldVel1);
        newVel.addTo(oldVel2);
        newVel.divideBy(newMass);
        body1.setVelocity(newVel);

        // Set new radius
        double rad1 = body1.getRadius();
        double rad2 = body2.getRadius();
        body1.setRadius(Math.cbrt((rad1 * rad1 * rad1) + (rad2 * rad2 * rad2)));
    }
}
