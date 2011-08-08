package solar;

/**
 * An abstract class representing any object in a Solar simulation.  Every SolarBody
 * is characterized by a position, velocity, netForce, and defaultForce.  The
 * defaultForce should typically be set at the start of a simulation and the
 * netForce updated every tick along with position and velocity.
 * @author jardini
 */
public abstract class SolarBody {

    private String name;
    private double mass;
    private double radius;
    private SolarVector netForce;
    private SolarVector position;
    private SolarVector velocity;
    private SolarVector defaultForce;

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
        this.netForce = new SolarVector();
        this.position = new SolarVector();
        this.velocity = new SolarVector();
        this.defaultForce = new SolarVector();
    }

    /**
     * Adds the gravitational force from otherBody to this instance's netForce
     * @param otherBody - A (non-null) SolarBody in the same simulation as
     * the calling object.
     */
    public void addForce(SolarBody otherBody) {
        double distance = SolarPhysics.distance(this, otherBody);
        double force = SolarPhysics.force(this, otherBody);
        if (distance != 0) {
            // forceToAdd = (otherBody.pos - this.pos) * force / distance
            SolarVector forceToAdd = new SolarVector(otherBody.position);
            forceToAdd.subtractFrom(position);
            forceToAdd.scaleBy(force);
            forceToAdd.divideBy(distance);
            netForce.addTo(forceToAdd);
        }
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

    public final SolarVector getPosition() {
        return new SolarVector(position);
    }

    public final SolarVector getVelocity() {
        return new SolarVector(velocity);
    }

    public final SolarVector getDefaultForce() {
        return new SolarVector(defaultForce);
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
    public final void setDefaultForce(SolarVector v) {
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
    public final void setPosition(SolarVector v) {
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
    public final void setVelocity(SolarVector v) {
        this.velocity.copy(v);
    }

    /**
     * Updates the position and velocity of the calling object.
     * @param timestep - The timestep of the simulation.
     */
    public void updatePositionAndVelocity(double timestep) {
        SolarVector oldVelocity = new SolarVector(velocity);
        SolarVector changeInVelocity = new SolarVector(netForce);
        // changeInVelocity = (force / mass) * t
        changeInVelocity.divideBy(mass);
        changeInVelocity.scaleBy(timestep);
        // Updates velocity
        velocity.addTo(changeInVelocity);
        // changeFromAcc = .5*a*t^2
        SolarVector changeFromAcc = new SolarVector(changeInVelocity);
        changeFromAcc.scaleBy(.5);
        changeFromAcc.scaleBy(timestep);
        // changeInPos = v*t + changeFromAcc
        SolarVector changeInPos = new SolarVector(oldVelocity);
        changeInPos.scaleBy(timestep);
        changeInPos.addTo(changeFromAcc);
        // Updates position
        position.addTo(changeInPos);
    }
}
