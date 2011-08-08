package solar;

/**
 * An abstract class that manages a collection of SolarBodies and determines
 * the update and collision behavior of the SolarBodies.  Any subclass of
 * this manager should also handle registering and unregistering objects with
 * any external GUI.
 * @author jardini
 */
public abstract class SolarBodyManager {

    protected long m_iTickCount;

    public SolarBodyManager()
    {
        m_iTickCount=0;
    }
    
    /**
     * Adds a body to the simulation.
     * @param body - A SolarBody not in the simulation.
     */
    public abstract void addBody(SolarBody body);

    /**
     * Removes a body from the simulation.
     * @param body - A SolarBody in the simulation.
     */
    public abstract void removeBody(SolarBody body);

    /**
     * @return An iterable collection of SolarBodies.
     */
    public abstract Iterable<SolarBody> getBodies();

    /**
     * Must be implemented to process all collisions between SolarBodies
     * in the simulation at a certain point in time.
     */
    public abstract void processCollisions();

    /**
     * Must be implemented to update the positions and velocities of all
     * SolarBodies in the simulation.
     * @param timestep - The timestep of the simulation.
     */
    public abstract void updateSolarBodies(double timestep);

    /**
     * Factory method for creating Objects that extend type SolarBody.
     * @param name - A name of the newly constructed SolarBody.
     * @param mass - The mass of the new body.
     * @param radius - The radius of the new body.
     * @param type - An all-caps String representing the type of the SolarBody
     * @return A new SolarBody
     */
    public SolarBody createBody(String name, double mass, double radius, String type) {
        if (type.equals("PLANET")) {
            return new Planet(name, mass, radius);
        } else if (type.equals("ROCKET")) {
            return new Rocket(name, mass, radius);
        } else {
            System.err.println("Invalid SolarBody type");
            System.exit(3);
            return null;
        }
    }

    /**
     * Represents one tick of the simulation, updating all bodies
     * then handling all collisions.
     * @param timestep - The timestep of the simulation
     */
    public void tick(double timestep) {
        m_iTickCount++;
        updateSolarBodies(timestep);
        processCollisions();
    }

    public long getTicks() {
        return m_iTickCount;
    }
}
