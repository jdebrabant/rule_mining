package solar;

/**
 * Representation of immutable 3D vectors.
 * This simplifies physics calculations necessary for the simulation.
 * @author jardini
 */
public final class SolarVector {

    private double x;
    private double y;
    private double z;

    /**
     * Default constructor initializes all components to 0
     */
    public SolarVector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Copy constructor
     * @param v - Another (non-null) SolarVector
     */
    public SolarVector(SolarVector v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    /**
     * Constructor to directly set fields.
     * @param x - Any double
     * @param y - Any double
     * @param z - Any double
     */
    public SolarVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Copies the given SolarVector
     * @param v - A non-null SolarVector
     */
    public final void copy(SolarVector v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    /**
     * @param x - Any double
     * @param y - Any double
     * @param z - Any double
     */
    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    /**
     * Vector addition
     * @param v - A non-null SolarVector
     */
    public void addTo(SolarVector v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    /**
     * Vector subtraction
     * @param v - A non-null SolarVector
     */
    public void subtractFrom(SolarVector v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    /**
     * Scales each component by c.
     * @param c - A constant
     */
    public void scaleBy(double c) {
        this.x *= c;
        this.y *= c;
        this.z *= c;
    }

    /**
     * Divides each component by c.
     * @param c - A non-zero constant
     */
    public void divideBy(double c) {
        this.x /= c;
        this.y /= c;
        this.z /= c;
    }

    /**
     * @return true iff o is a SolarVector x, y, and z fields that are equal
     * to the calling SolarVector's x, y, and z fields.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SolarVector v = (SolarVector) o;
        return (this.x == v.x && this.y == v.y && this.z == v.z);
    }
}
