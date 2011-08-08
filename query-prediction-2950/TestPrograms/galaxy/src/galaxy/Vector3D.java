package galaxy;

/**
 * Representation of mutable 3D vectors.
 * This simplifies physics calculations necessary for the simulation.
 * @author jardini
 */
public final class Vector3D {

    private double x;
    private double y;
    private double z;

    /**
     * Default constructor initializes all components to 0
     */
    public Vector3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Copy constructor
     * @param v - Another (non-null) SolarVector
     */
    public Vector3D(Vector3D v) {
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
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Copies the given SolarVector
     * @param v - A non-null SolarVector
     */
    public final void copy(Vector3D v) {
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
    public void addTo(Vector3D v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
    }

    public void addTo(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }


    /**
     * Vector subtraction
     * @param v - A non-null SolarVector
     */
    public void subtractFrom(Vector3D v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
    }

    public double dotProduct(Vector3D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
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

    public double length() {
        return Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z);
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
        Vector3D v = (Vector3D) o;
        return (this.x == v.x && this.y == v.y && this.z == v.z);
    }

    @Override
    public String toString() {
        return "[" + x + " " + y + " " + z + "]";
    }
}
