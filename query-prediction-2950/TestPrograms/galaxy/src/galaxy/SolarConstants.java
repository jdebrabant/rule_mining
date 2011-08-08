/********************************************************************************
 * @author wblauroc, npartlan, kdoo, & jfrank
 * File:   SolarConstants.java
 * @date   25 January 2010
 * Asgn:   Solar
 ********************************************************************************/
package galaxy;

/**
 * SolarConstants is a class that can be used to adjust values for Solar.
 * These are NOT merely suggestions. We encourage you to play with them,
 * but make sure to reset them to their original values or defining your own
 * (with new names) before handing in, otherwise it will be much more
 * difficult to grade your project.
 */
public final class SolarConstants {

    public enum Shape {
        Cone, Sphere
    }
    public enum CollisionBehavior {
        Explode, Combine, Shatter
    }

    public static final double MAX_DISTANCE = 2.2794 * Math.pow(10, 11);
    public static final double MIN_DISTANCE = 5.791 * Math.pow(10, 10);
    public static final double MIN_Z = -6.0 * Math.pow(10, 10);
    public static final double MAX_Z = 6.0 * Math.pow(10, 10);


    public static final double SUN_MASS = 1.989 * Math.pow(10, 30);
    public static final double SUN_RADIUS = 6.95 * Math.pow(10, 8);
    public static final double PLANET_MASS = 1.18 * Math.pow(10, 25);
    public static final double PLANET_RADIUS = Math.PI * Math.pow(10, 7);
    public static final int NUMBER_OF_PLANETS = 100;
    
    public static final double ROCKET_MASS = 1.0 * Math.pow(10, 6);
    // really big! but you have to be able to see them
    public static final double ROCKET_RADIUS = 1.0 * Math.pow(10, 8);
    public static final int NUMBER_OF_ROCKETS = 100;
    public static final double MOON_MASS = 7.3477 * Math.pow(10, 22);
    public static final double MOON_RADIUS = 1.7374 * Math.pow(10, 6);
    public static final int NUMBER_OF_MOONS_PER_PLANET = 1;
    // you may not want to change this!
    public static final double GRAVITY_CONSTANT = 6.674 * Math.pow(10, -11);
    // margin of error for finding collisions
    public static final double COLLISION_CONSTANT = 10.0;
    // number of simulated seconds per step of the simulation -- if this is too
    // large, you may miss collisions.
    public static final double DEFAULT_TIME_STEP = 20 * 60;
}
