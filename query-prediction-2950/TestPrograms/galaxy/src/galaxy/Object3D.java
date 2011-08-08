package galaxy;

/**
 * An interface for any object in 3D space: Anything extending Object3D can
 * be placed into an Octree.  An Object3D consists of x, y, and z coordinates,
 * as well as a mass and a radius.
 * @author jardini
 */
public interface Object3D {

    double getX();
    double getY();
    double getZ();
    double getMass();
    double getRadius();

    Vector3D getPosition();
    void addToNetForce(Vector3D dForce);
    //double getDistance(Object3D distFrom);
    
}
