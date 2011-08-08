package galaxy;

/**
 * Stores constants used for creating and using methods for an Octree
 * @author Justin
 */
public final class OctreeConstants {

    private OctreeConstants() {}

    public static final Vector3D CENTER = new Vector3D(0, 0, 0);
    public static final double INITIAL_SIDE_LENGTH = 2.8585 * Math.pow(10, 12);
    public static final int ITEMS_PER_NODE = 1;
    public static final int MAX_DEPTH = 20;
    public static final double THETA = .5;
}
