package solar;

/**
 * A static class containing methods used for generating a random simulation.
 * All constants used in this class are contained in solardraw.SolarConstants.
 * @author jardini
 */
public final class SolarRandom {

    private SolarRandom() {
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
     * 
     * @param distance - A distance between two SolarBodies
     * @return A random x-coordinate: -distance <= x < distance
     */
    public static double randomX(double distance) {
        return plusMinus() * (distance * Math.random());
    }

    /**
     * 
     * @param velocity - The velocity of a SolarBody
     * @return velocity +- random() * (velocity/10)
     */
    public static double randomizeVelocity(double velocity) {
        return velocity + (plusMinus() * (Math.random() * (velocity / 10)));
    }

    /**
     * 
     * @param distance - A distance from this SolarBody to a sun at (0, 0, 0)
     * @param x - An x-coordinate for a SolarBody
     * @return A y-coordinate such that the distance between this SolarBody
     * and the sun is correct.
     */
    public static double calculateY(double distance, double x) {
        return plusMinus() * Math.sqrt((distance * distance) - (x * x));
    }

    /**
     * 
     * @return A random double, d: MIN_Z <= d < MAX_Z
     */
    public static double randomZ() {
        return SolarConstants.MIN_Z + ((SolarConstants.MAX_Z - SolarConstants.MIN_Z) * Math.random());
    }
}
