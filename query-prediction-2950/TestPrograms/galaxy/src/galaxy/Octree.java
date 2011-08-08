package galaxy;

import java.util.ArrayList;
import java.util.Collection;
/**
 * A representation of an Octree, which can hold any Object3D (any object
 * that can be characterized by a position, a radius and a mass)
 * @author jardini
 */
public class Octree<E extends Object3D> implements Object3D {

    private Collection<E> items;
    private Octree<E>[] children;
    private Vector3D center;
    private Vector3D centerOfMass;
    private Vector3D netForce;
    private double radius;
    private double radius2;
    private double mass;
    private int level;
    private boolean isLeaf;

    /**
     * Public constructor taking coordinates of center and halfWidth
     * @param x     x-coordinate of the center
     * @param y     y-coordinate of the center
     * @param z     z-coordinate of the center
     * @param radius     Half of the length of a side of the root node
     */
    public Octree(double x, double y, double z, double radius) {
        items = new ArrayList<E>();
        this.centerOfMass = new Vector3D();
        this.center = new Vector3D(x, y, z);
        this.netForce = new Vector3D(0, 0, 0);
        this.mass = 0;
        this.radius = radius;
        this.radius2 = radius*2;
        children = (Octree<E>[]) new Octree[8];
        isLeaf = true;
        level = 1;
    }

    /**
     * Alternate constructor taking in a Vector representing the center
     * of the Octree
     * 
     * @param center       The coordinates of the root node's center
     * @param radius       Half of the length of a side of the root node
     */
    public Octree(Vector3D center, double radius) {
        this(center.getX(), center.getY(), center.getZ(), radius);
    }

    /**
     * Private constructor for subtrees created by calling insert
     *
     * @param center    The center of the root node
     * @param radius    Half of the length of a side of the root node
     * @param level     The depth of the node
     */
    private Octree(Vector3D center, double radius, int level) {
        items = new ArrayList<E>();
        this.mass = 0;
        this.centerOfMass = new Vector3D();
        this.center = new Vector3D(center);
        this.netForce = new Vector3D(0, 0, 0);
        this.radius = radius;
        this.radius2 = radius*2;
        children = (Octree<E>[]) new Octree[8];
        isLeaf = true;
        this.level = level;
    }

    /**
     * Builds an octree from a collection of objects by inserting the items
     * then computing the mass and center of mass.
     * @param   objects     A collection of 3D objects
     */
    public void build(Collection<E> objects) {
        for (E object : objects) {
            insert(object);
        }
        computeMassAndCenterOfMass();
    }

    /**
     * Collides this Octree with the given item.  Returns a body colliding
     * with item, else null if there is no collision.
     * @param collider  An item within the same simulation as this Octree
     * @param toRemove   A list of items that will be removed this tick
     */
    public E collide(E collider, Collection<E> toRemove) {
        // If a leaf, check for collision with each item individually
        if (isLeaf) {
            for (E item : items) {
                if (collider != item && !toRemove.contains(item)) {
                    if (Physics.isCollision(collider, item)) {
                        return item;
                    }
                }
            }
        } else {
            // If a parent, check if collider intersects the node, then recur
            for (Octree<E> child : children) {
                if (child != null) {
                    if (child.intersects(collider)) {
                        E result = child.collide(collider, toRemove);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param octant  An int 0 <= octant < 8 representing the octant of one
     * of this node's children
     * @return        The center of the child node in the given octant.
     */
    Vector3D computeCenter(int octant) {
        double newX, newY, newZ;
        if (octant == 0 || octant == 2 || octant == 4 || octant == 6) {
            newX = center.getX() + (radius / 2);
        } else {
            newX = center.getX() - (radius / 2);
        }
        if (octant == 0 || octant == 1 || octant == 4 || octant == 5) {
            newY = center.getY() + (radius / 2);
        } else {
            newY = center.getY() - (radius / 2);
        }
        if (octant == 0 || octant == 1 || octant == 2 || octant == 3) {
            newZ = center.getZ() + (radius / 2);
        } else {
            newZ = center.getZ() - (radius / 2);
        }
        return new Vector3D(newX, newY, newZ);
    }

    /**
     * Computes the mass and center of mass for this node, must be called
     * before calling treeForce to get accurate results
     */
    public void computeMassAndCenterOfMass() {
        // Make sure mass and center of mass are reset before computing
        // If a parent
        if (!isLeaf) {
            // Add results from each child
            for (Octree<E> child : children) {
                if (child != null) {
                    child.computeMassAndCenterOfMass();
                    mass += child.mass;
                    Vector3D cm = new Vector3D(child.centerOfMass);
                    cm.scaleBy(child.mass);
                    centerOfMass.addTo(cm);
                }
            }
            centerOfMass.divideBy(mass);
        } else {
            // If a leaf, iterate through items and calculate mass & cm
            for (E item : items) {
                mass += item.getMass();
                Vector3D cm = new Vector3D(item.getX(), item.getY(), item.getZ());
                cm.scaleBy(item.getMass());
                centerOfMass.addTo(cm);
            }
            centerOfMass.divideBy(mass);
        }
    }

   /**
    * For testing purposes only: returns the smallest octree containing
    * this item
    * 
    * @param item   Any Object3D
    * @return       The smallest Octree containing this item
    */
    Octree containingNode(E item) {
        // If a leaf, check all items contained in it
        if (isLeaf) {
            for (E item2 : items) {
                if (item == item2) {
                    return this;
                }
            }
            return null;
        } else {
            // Else check all the children nodes
            Octree<E> toReturn = null;
            for (Octree<E> child : children) {
                if (child != null) {
                    toReturn = child.containingNode(item);
                    if (toReturn != null) {
                        return toReturn;
                    }
                }
            }
            return toReturn;
        }
    }

    /**
     * For testing only
     * @return  The center position of this Octnode
     */
    Vector3D getCenter() {
        return center;
    }

    /**
     * @return  The total mass of all objects in this Octree
     */
    public double getMass() {
        return mass;
    }

    /**
     * For testing, traverses the tree, putting all items in an iterable collection
     * @return  An iterable collection of items in the tree, in no
     * guaranteed order.
     */
    Collection<E> getItems() {
        Collection<E> toReturn = new ArrayList<E>();
        if (isLeaf) {
            toReturn.addAll(items);
        } else {
            for (Octree<E> child : children) {
                if (child != null) {
                  toReturn.addAll(child.getItems());
                }
            }
        }
        return toReturn;
    }

    /**
     * Private helper for determining in which child node item belongs
     *
     * @param item  An item being inserted into the Octree
     * @return      The index of the child node to place this item
     */
    int getOctant(E item) {
        int x = item.getX() < center.getX() ? 1 : 0;
        int y = item.getY() < center.getY() ? 2 : 0;
        int z = item.getZ() < center.getZ() ? 4 : 0;
        return x + y + z;
    }

    /**
     * @return The "radius" (half the length of one side) of the bounding box
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @return  The x-coordinate of the center of mass of objects in this node.
     */
    public double getX() {
        return centerOfMass.getX();
    }

    /**
     * @return  The y-coordinate of the center of mass of objects in this node.
     */
    public double getY() {
        return centerOfMass.getY();
    }

    /**
     * @return  The z-coordinate of the center of mass of objects in this node.
     */
    public double getZ() {
        return centerOfMass.getZ();
    }

    /**
     * Inserts the given item into the octree.  Octrees do not check for
     * duplicates and will insert items outside of the tree's bounding box
     * at an edge node at the max depth.
     * 
     * @param   item    A non-null item to be inserted
     */
    public void insert(E item) {
        // If a leaf, place in node if possible
        if (isLeaf) {
            // If a leaf that's not yet filled, insert item into leaf
            if (this.items.size() < OctreeConstants.ITEMS_PER_NODE) {
                this.items.add(item);
            } else {
                // Make new leaves if not yet at max depth
                if (level < OctreeConstants.MAX_DEPTH) {
                    // Else create children and place in appropriate child
                    int octant = getOctant(item);
                    children[octant] = new Octree<E>(computeCenter(octant), radius / 2, level + 1);
                    children[octant].insert(item);
                    // And move all items from this node to the children
                    for (E item2 : items) {
                        octant = getOctant(item2);
                        if (children[octant] == null) {
                            children[octant] = new Octree<E>(computeCenter(octant), radius / 2, level + 1);
                        }
                        children[octant].insert(item2);
                    }
                    items.clear();
                    isLeaf = false;
                } else {
                    this.items.add(item);
                }
            }
        } else {
            // If a parent, insert into appropriate child
            int octant = getOctant(item);
            if (children[octant] == null) {
                children[octant] = new Octree<E>(computeCenter(octant), radius / 2, level + 1);
            }
            children[octant].insert(item);
        }
    }

    /**
     * Private helper to check for an intersection between an item and
     * this octnode
     *
     * @param item  Any Object3D
     * @return      True if item's center is within this octnode's bounding box
     */
    boolean intersects(E item) {
        double newRadius = radius + (SolarConstants.COLLISION_CONSTANT * item.getRadius());
        if ((center.getX() - newRadius) > item.getX()) {
            return false;
        }
        if ((center.getX() + newRadius) <= item.getX()) {
            return false;
        }
        if ((center.getY() - newRadius) > item.getY()) {
            return false;
        }
        if ((center.getY() + newRadius) <= item.getY()) {
            return false;
        }
        if ((center.getZ() - newRadius) > item.getZ()) {
            return false;
        }
        if ((center.getZ() + newRadius) <= item.getZ()) {
            return false;
        }
        return true;
    }

    /**
     * For testing only
     * @return  True if and only if this octnode is a leaf
     */
    boolean isLeaf() {
        return isLeaf;
    }

    /**
     * Computes the net force on item from all objects in this octnode.
     * @param item  Any Object3D
     * @return      A force vector on item due to this octnode.
     */
/*
    public Vector3D treeForce(E item) {
        // If a leaf, go through each item in the node and compute the force
        if (isLeaf) {
            //Vector3D netForce = new Vector3D();
            
            netForce.set(0, 0, 0);
            for (E item2 : items) {
                if (item != item2) {
                    double distance = Physics.distance(item, item2);
                    //netForce.addTo(Physics.netForce(item, item2, distance));
                    Physics.addToNetForceToBody1(item, item2, distance);
                }
            }
            return netForce;
        } else {
            // If a parent, approximate or add force from each child node
            double distance = Physics.distance(item, this);
            //if ((radius * 2) < (OctreeConstants.THETA * distance)) {
            if (radius2 < (OctreeConstants.THETA * distance)) {
                //return Physics.netForce(item, this, distance);
                netForce.set(0, 0, 0);
                Physics.addToNetForceToBody1(item, this, distance);
                return netForce;
            } else {
                //Vector3D netForce = new Vector3D();
                netForce.set(0, 0, 0);
                for (Octree<E> child : children) {
                    if (child != null) {
                        Vector3D toAdd = child.treeForce(item);
                        netForce.addTo(toAdd);
                    }
                }
                return netForce;
            }
        } 
    }
*/
    public void addTreeForce(E item, Vector3D coordDiff) {
        // If a leaf, go through each item in the node and compute the force
        if (isLeaf) {
            for (E item2 : items) {
                if (item != item2) {
                    Physics.getCoordDiff(item, item2, coordDiff);
                    //double distance = Physics.distance(item, item2);
                    double distance = coordDiff.length();
                    Physics.addToNetForceToBody1(item, item2, coordDiff, distance);
                }
            }
        } else {
            // If a parent, approximate or add force from each child node
            Physics.getCoordDiff(item, this, coordDiff);
            //double distance = Physics.distance(item, this);
            double distance = coordDiff.length();
            if (radius2 < (OctreeConstants.THETA * distance)) {
                //Physics.addToNetForceToBody1(item, this, distance);
                Physics.addToNetForceToBody1(item, this, coordDiff, distance);
            } else {
                for (Octree<E> child : children) {
                    if (child != null) {
                        child.addTreeForce(item, coordDiff);
                    }
                }
            }
        }
    }

    public void addToNetForce(Vector3D dForce)
    {
        throw new RuntimeException("Not implemented");
    }

    public Vector3D getPosition()
    {
        return this.centerOfMass;
    }
}