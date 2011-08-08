package galaxy;

/**
 * A static class containing methods used for generating a random simulation.
 * All constants used in this class are contained in solardraw.SolarConstants.
 * @author jardini
 */
public final class GalaxyRandom {

    private GalaxyRandom() {
    }

    /**
     * Uses Math.random() to randomly make number positive or negative.
     * @return 1 50% of the time, -1 50% of the time.
     */
    public static int plusMinus() {
        if (Math.random() < .5) {
            return 1;
        }
        return -1;
    }

    /**
     * 
     * @return A random double, d: MIN_DISTANCE <= d < MAX_DISTANCE
     */
    public static double randomDistance() {
        return SolarConstants.MIN_DISTANCE + ((SolarConstants.MAX_DISTANCE - SolarConstants.MIN_DISTANCE) * Math.random());
    }

    /**
     * 
     * @return PLANET_MASS +- random() * (PLANET_MASS / 100)
     */
    public static double randomPlanetMass() {
        return SolarConstants.PLANET_MASS + (plusMinus() * (Math.random() * SolarConstants.PLANET_MASS / 100));
    }

    /**
     * Returns a random angle in radians
     * @return - A random angle 0 <= n < 2pi
     */
    public static double randomAngle() {
        return Math.random() * (2 * Math.PI);
    }

    /**
     * 
     * @param distance - A distance between two SolarBodies
     * @param angle - An angle (between 0 and 2 pi) relative to the x-axis
     * @return A random x-coordinate: -distance <= x < distance
     */
    public static double randomX(double distance, double angle) {
        return distance * Math.cos(angle);
    }

        /**
     *
     * @param distance - A distance from this SolarBody to a sun at (0, 0, 0)
     * @param angle - An angle (between 0 and 2 pi) relative to the x-axis
     * @return A y-coordinate such that the distance between this SolarBody
     * and the sun is correct.
     */
    public static double randomY(double distance, double angle) {
        return distance * Math.sin(angle);
    }

    /**
     *
     * @return A random double, d: MIN_Z <= d < MAX_Z
     */
    public static double randomZ() {
        return SolarConstants.MIN_Z + ((SolarConstants.MAX_Z - SolarConstants.MIN_Z) * Math.random());
    }

    /**
     * 
     * @param velocity - The velocity of a SolarBody
     * @return velocity +- random() * (velocity/10)
     */
    public static double randomizeVelocity(double velocity) {
        return velocity + (plusMinus() * (Math.random() * (velocity / 10)));
    }

}
