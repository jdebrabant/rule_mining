package galaxy;

/**
 * An abstract class representing any object in a Solar simulation.  Every SolarBody
 * is characterized by a position, velocity, netForce, and defaultForce.  The
 * defaultForce should typically be set at the start of a simulation and the
 * netForce updated every tick along with position and velocity.
 * @author jardini
 */
public abstract class SolarBody implements Object3D {

    private String name;
    private double mass;
    private double radius;
    private Vector3D netForce;
    private Vector3D position;
    private Vector3D velocity;
    private Vector3D defaultForce;

    /**
     *
     * @return SolarDraw.CollisionBehavior
     */
    public abstract SolarConstants.CollisionBehavior getCollisionBehavior();

    /**
     *
     * @return SolarDraw.Shape
     */
    public abstract SolarConstants.Shape getShape();

    /**
     * 
     * @return a String representation of the type of the SolarBody.
     */
    public abstract String getType();

    /**
     * Constructor setting the name, mass and radius to the given values.
     * @param name - The (non-null) name of the constructed planet.
     * @param mass - The mass of the planet.
     * @param radius - The radius of the planet.
     */
    public SolarBody(String name, double mass, double radius) {
        this.name = name;
        this.mass = mass;
        this.radius = radius;
        this.netForce = new Vector3D();
        this.position = new Vector3D();
        this.velocity = new Vector3D();
        this.defaultForce = new Vector3D();
    }

    /**
     * Adds the given force to the netForce vector
     * @param force - A force acting on this SolarBody
     */
    public void addForce(Vector3D force) {
        netForce.addTo(force);
    }

    public final String getName() {
        return name;
    }

    public final double getX() {
        return position.getX();
    }

    public final double getY() {
        return position.getY();
    }

    public final double getZ() {
        return position.getZ();
    }

    public final Vector3D getPosition() {
        return new Vector3D(position);
    }

    public final Vector3D getVelocity() {
        return new Vector3D(velocity);
    }

    public final Vector3D getDefaultForce() {
        return new Vector3D(defaultForce);
    }

    public final double getRadius() {
        return radius;
    }

    public final double getMass() {
        return mass;
    }

    /**
     * Sets the net force equal to defaultForce
     */
    public final void resetNetForce() {
        this.netForce.copy(defaultForce);
    }

    /**
     * @param x - A new x-component of the default force.
     * @param y - A new y-component.
     * @param z - A new z-component.
     */
    public final void setDefaultForce(double x, double y, double z) {
        this.defaultForce.set(x, y, z);
    }

    /**
     * Copies the input vector to set a new default force.
     * @param v - A non-null SolarVector
     */
    public final void setDefaultForce(Vector3D v) {
        this.defaultForce.copy(v);
    }

    /**
     * @param mass - A non-negative double representing the new mass.
     */
    public final void setMass(double mass) {
        this.mass = mass;
    }

    /**
     * @param name - A new name, as a non-null String.
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * @param x - A new x-component of the position.
     * @param y - A new x-component.
     * @param z - A new x-component.
     */
    public final void setPosition(double x, double y, double z) {
        this.position.set(x, y, z);
    }

    /**
     * Copies the input vector to set a new position.
     * @param v - A non-null SolarVector
     */
    public final void setPosition(Vector3D v) {
        this.position.copy(v);
    }

    /**
     * @param radius - A non-negative double representing the new radius.
     */
    public final void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Copies the input vector to set a new velocity.
     * @param x - A new x-component of the velocity.
     * @param y - A new x-component.
     * @param z - A new x-component.
     */
    public final void setVelocity(double x, double y, double z) {
        this.velocity.set(x, y, z);
    }

    /**
     * @param v - A non-null SolarVector.
     */
    public final void setVelocity(Vector3D v) {
        this.velocity.copy(v);
    }

    public void addToNetForce(Vector3D dForce)
    {
        netForce.addTo(dForce);
    }

    /**
     * Updates the position and velocity of the calling object.
     * @param timestep - The timestep of the simulation.
     */
    public void updatePositionAndVelocity(double timestep) {
        Vector3D oldVelocity = new Vector3D(velocity);
        Vector3D changeInVelocity = new Vector3D(netForce);
        // changeInVelocity = (force / mass) * t
        changeInVelocity.divideBy(mass);
        changeInVelocity.scaleBy(timestep);
        // Updates velocity
        velocity.addTo(changeInVelocity);
        // changeFromAcc = .5*a*t^2
        Vector3D changeFromAcc = new Vector3D(changeInVelocity);
        changeFromAcc.scaleBy(.5);
        changeFromAcc.scaleBy(timestep);
        // changeInPos = v*t + changeFromAcc
        Vector3D changeInPos = new Vector3D(oldVelocity);
        changeInPos.scaleBy(timestep);
        changeInPos.addTo(changeFromAcc);
        // Updates position
        position.addTo(changeInPos);
    }
}
