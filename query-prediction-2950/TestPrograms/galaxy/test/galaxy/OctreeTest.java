package galaxy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import solardraw.*;
import java.util.*;

/**
 * Contains unit tests for Octree operations
 * @author Justin
 */
public class OctreeTest {

    SolarBody sun;
    SolarBody earth;

    public OctreeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        sun = new Planet("Sun", 1.989E30, 6.95E8);
        earth = new Planet("Earth", 5.974E24, 6.371E6);
        earth.setPosition(0, 1.496E11, 0);
    }

    @After
    public void tearDown() {
    }

    /**
     * Tests inserting items into the Octree through build and insert.
     * Only tests that items are inserted into the tree, not that they are
     * in the correct child node.
     */
    @Test
    public void testInsert() {
        Octree<SolarBody> test1 = new Octree<SolarBody>(0, 0, 0, 128);
        Octree<SolarBody> test2 = new Octree<SolarBody>(-64, 6.4, 64, 10000);
        ArrayList<SolarBody> allPlanets = new ArrayList<SolarBody>();
        // Takes 20 planets with random distances/sizes from Solar handout
        for (int i = 1; i <= 20; i++) {
            String newName = "Planet" + i;
            SolarBody newBody = SolarBodyManager.createBody(newName,
GalaxyRandom.randomPlanetMass(), SolarConstants.PLANET_RADIUS, "PLANET");

            double randDist = GalaxyRandom.randomDistance();
            double angle = GalaxyRandom.randomAngle();
            double randX = GalaxyRandom.randomX(randDist, angle);
            double randY = GalaxyRandom.randomY(randDist, angle);
            double randZ = GalaxyRandom.randomZ();
            newBody.setPosition(randX, randY, randZ);
            double velX = Physics.orbitVelocityX(newBody, sun);
            velX = GalaxyRandom.randomizeVelocity(velX);
            double velY = Physics.orbitVelocityY(newBody, velX);
            velY = GalaxyRandom.randomizeVelocity(velY);
            newBody.setVelocity(velX, velY, 0);

            allPlanets.add(newBody);
            test1.insert(newBody);
        }
        // Tests that all 20 planets inserted
        assertEquals(test1.getItems().size(), 20);
        assertTrue(test1.getItems().containsAll(allPlanets));
        // Tests that all 20 planets inserted through build()
        test2.build(test1.getItems());
        assertEquals(test2.getItems().size(), 20);
        assertTrue(test2.getItems().containsAll(allPlanets));

        SolarBody notContained = SolarBodyManager.createBody("Thing",
                500, 153, "ROCKET");
        assertTrue(!test1.getItems().contains(notContained));
        assertTrue(!test2.getItems().contains(notContained));
        test1.insert(notContained);
        assertTrue(test1.getItems().contains(notContained));
    }
    
    /**
     * Tests collision checking in an octree
     */
    @Test
    public void testCollide() {
        Octree<SolarBody> test1 = new Octree<SolarBody>(0, 0, 0, 1.4E12);
        Octree<SolarBody> test2 = new Octree<SolarBody>(-64, 6.4, 64, 1E12);
        ArrayList<SolarBody> allPlanets = new ArrayList<SolarBody>();
        // Takes 30 planets with random distances/sizes from Solar handout
        for (int i = 0; i < 30; i++) {
            String newName = "Planet" + i;
            SolarBody newBody = SolarBodyManager.createBody(newName,
                    GalaxyRandom.randomPlanetMass(), SolarConstants.PLANET_RADIUS, "PLANET");
            double randDist = GalaxyRandom.randomDistance();
            double angle = GalaxyRandom.randomAngle();
            double randX = GalaxyRandom.randomX(randDist, angle);
            double randY = GalaxyRandom.randomY(randDist, angle);
            double randZ = GalaxyRandom.randomZ();
            newBody.setPosition(randX, randY, randZ);

            allPlanets.add(newBody);
            test1.insert(newBody);
        }

        SolarBody notColliding = SolarBodyManager.createBody("Thing",
                50, 100, "PLANET");
        notColliding.setPosition(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        Collection<SolarBody> toRemove = new LinkedList<SolarBody>();
        assertTrue(test1.collide(notColliding, toRemove) == null);
        assertTrue(test2.collide(notColliding, toRemove) == null);
        // Makes sure objects don't get returned if they collide with themself
        test1.insert(notColliding);
        assertTrue(test1.collide(notColliding, toRemove) == null);
        
        // Ensures collisions are correctly identified for all contained planets
        // Even with positions varying by small amounts
        for (int i = 0; i < 30; i++) {
            SolarBody curr = allPlanets.get(i);
            SolarBody currCopy = SolarBodyManager.createBody("collide", curr.getMass(), curr.getRadius(), "PLANET");
            Vector3D toAdd = new Vector3D(Math.random() * 5000, Math.random() * 5000, Math.random() * 5000);
            Vector3D sum = curr.getPosition();
            sum.addTo(toAdd);
            currCopy.setPosition(sum);
            assertTrue(test2.collide(currCopy, toRemove) == null);
            assertEquals(test1.collide(currCopy, toRemove), curr);
            // Make sure all nodes containing objects are leaves
            assertTrue(test1.containingNode(curr).isLeaf());
        }
    }
    
    /**
     * Tests computation for center of child nodes
     */
    @Test
    public void testComputeCenter() {
        Octree<SolarBody> tree1 = new Octree<SolarBody>(0, 0, 0, 1000);
        Octree<SolarBody> tree2 = new Octree<SolarBody>(100, -100, 1000, 4500);
        // Expected values
        Vector3D c0 = new Vector3D(500, 500, 500);
        Vector3D c1 = new Vector3D(-500, 500, 500);
        Vector3D c2 = new Vector3D(500, -500, 500);
        Vector3D c3 = new Vector3D(-500, -500, 500);
        Vector3D c4 = new Vector3D(500, 500, -500);
        Vector3D c5 = new Vector3D(-500, 500, -500);
        Vector3D c6 = new Vector3D(500, -500, -500);
        Vector3D c7 = new Vector3D(-500, -500, -500);
        Vector3D c02 = new Vector3D(2350, 2150, 3250);
        Vector3D c82 = new Vector3D(-2150, -2350, -1250);
        // Tests
        assertEquals(c0, tree1.computeCenter(0));
        assertEquals(c1, tree1.computeCenter(1));
        assertEquals(c2, tree1.computeCenter(2));
        assertEquals(c3, tree1.computeCenter(3));
        assertEquals(c4, tree1.computeCenter(4));
        assertEquals(c5, tree1.computeCenter(5));
        assertEquals(c6, tree1.computeCenter(6));
        assertEquals(c7, tree1.computeCenter(7));
        assertEquals(c02, tree2.computeCenter(0));
        assertEquals(c82, tree2.computeCenter(8));
    }

    /**
     * Tests getting the correct octant for an object
     */
    @Test
    public void testGetOctant() {
        Octree<SolarBody> tree1 = new Octree<SolarBody>(0, 0, 0, 10000);
        Octree<SolarBody> tree2 = new Octree<SolarBody>(100, -100, -50.5, 50000);

        SolarBody body1 = new Planet("one", 500, 5000);
        SolarBody body2 = new Planet("two", 300, 100.5);
        SolarBody body3 = new Rocket("three", 50.5, 10);
        SolarBody body4 = new Rocket("four", 100, 100);
        body2.setPosition(100, 0, -50.49);
        body3.setPosition(-10, -500, -30);
        body4.setPosition(150, -140, -50.5);
        // Tests
        assertEquals(tree1.getOctant(body1), 0);
        assertEquals(tree1.getOctant(body2), 4);
        assertEquals(tree1.getOctant(body3), 7);
        assertEquals(tree1.getOctant(body4), 6);
        assertEquals(tree2.getOctant(body1), 1);
        assertEquals(tree2.getOctant(body2), 0);
        assertEquals(tree2.getOctant(body3), 3);
        assertEquals(tree2.getOctant(body4), 2);
    }

    /**
     * Tests intersection algorithm between 3D objects and octrees
     */
    @Test
    public void testIntersect() {
        Octree<SolarBody> tree1 = new Octree<SolarBody>(0, 0, 0, 10000);
        Octree<SolarBody> tree2 = new Octree<SolarBody>(100, -100, -50.5, 50);
        Octree<SolarBody> tree3 = new Octree<SolarBody>(-50, 134, 23, 15.5);

        SolarBody body1 = new Planet("one", 500, 5000);
        SolarBody body2 = new Planet("two", 300, 100.5);
        SolarBody body3 = new Rocket("three", 50.5, 10);
        SolarBody body4 = new Rocket("four", 100, 100);
        body3.setPosition(-10, -500, -30);
        body4.setPosition(-50, 40, 100);
        tree1.insert(body1);
        tree1.insert(body2);
        tree2.insert(body2);
        tree2.insert(body3);
        tree2.insert(body4);

        // Tests
        assertTrue(tree1.intersects(body1));
        assertTrue(tree1.intersects(body2));
        assertTrue(tree1.intersects(body3));
        assertTrue(tree1.intersects(body4));
        
        assertTrue(tree2.intersects(body1));
        assertTrue(tree2.intersects(body2));
        assertTrue(!tree2.intersects(body3));
        assertTrue(tree2.intersects(body4));

        assertTrue(tree3.intersects(body1));
        assertTrue(tree3.intersects(body2));
        assertTrue(!tree3.intersects(body3));
        assertTrue(tree3.intersects(body4));
    }

    /**
     * Tests computing mass and center of mass.  Also inserts items in identical
     * position to ensure no infinite loop occurs.
     */
    @Test
    public void testComputeMassAndCM() {
        Octree<SolarBody> leaf = new Octree<SolarBody>(0, 0, 0, 5000);
        Octree<SolarBody> tree1 = new Octree<SolarBody>(0, 0, 0, 10000);
        Octree<SolarBody> tree2 = new Octree<SolarBody>(0, 0, 0, 10000);
        
        SolarBody body1 = new Planet("one", 500, 5000);
        SolarBody body2 = new Planet("two", 300, 100.5);
        SolarBody body3 = new Rocket("three", 50.5, 10);
        SolarBody body4 = new Rocket("four", 100, 100);
        body3.setPosition(-10, -500, -30);
        body4.setPosition(-50, 40, 100);
        // Expected values
        double mass1 = 800;
        Vector3D cm1 = new Vector3D(0, 0, 0);
        double mass2 = 450.5;
        Vector3D cm2 = new Vector3D(-12.2198, -47.1698, 18.8346);
        // Tests
        leaf.insert(body1);
        tree1.insert(body1);
        tree1.insert(body2);
        tree2.insert(body2);
        tree2.insert(body3);
        tree2.insert(body4);
        // Tests computation for mass and center of mass for 3 trees
        leaf.computeMassAndCenterOfMass();
        tree1.computeMassAndCenterOfMass();
        tree2.computeMassAndCenterOfMass();
        // Values for leaf equal to the values for the one planet
        assertEquals(leaf.getItems().size(), 1);
        assertEquals(leaf.getMass(), body1.getMass(), 0);
        assertEquals(leaf.getX(), body1.getX(), 0);
        assertEquals(leaf.getY(), body1.getY(), 0);
        assertEquals(leaf.getZ(), body1.getZ(), 0);

        assertEquals(tree1.getItems().size(), 2);
        assertEquals(tree1.getMass(), mass1, 0);
        assertEquals(tree1.getX(), cm1.getX(), 0);
        assertEquals(tree1.getY(), cm1.getY(), 0);
        assertEquals(tree1.getZ(), cm1.getZ(), 0);

        assertEquals(tree2.getItems().size(), 3);
        assertEquals(tree2.getMass(), mass2, 0);
        assertEquals(tree2.getX(), cm2.getX(), .0001);
        assertEquals(tree2.getY(), cm2.getY(), .0001);
        assertEquals(tree2.getZ(), cm2.getZ(), .0001);
    }

    /**
     * Tests algorithm for finding forces from octnodes.  Includes test where
     * Object being tested is included in the node.
     */
    @Test
    public void testTreeForce() {
        Octree<SolarBody> leaf = new Octree<SolarBody>(0, 0, 0, 5000);
        Octree<SolarBody> tree1 = new Octree<SolarBody>(0, 0, 0, 10000);
        Octree<SolarBody> tree2 = new Octree<SolarBody>(0, 0, 0, 10000);

        SolarBody body1 = new Planet("one", 5E7, 5000);
        SolarBody body2 = new Planet("two", 3E8, 100.5);
        SolarBody body3 = new Rocket("three", 3E7, 10);
        SolarBody body4 = new Rocket("four", 1E7, 100);
        body3.setPosition(-10, -5, -30);
        body4.setPosition(-5, 40, 10);
        // Expected values
        Vector3D force0 = new Vector3D(0, 0, 0);
        Vector3D force1 = new Vector3D(213.5451, 106.7726, 640.6354);
        Vector3D force2 = new Vector3D(16.3019, -130.4159, -32.6040);
        Vector3D force3 = new Vector3D(-32.8353, 3.3776, -86.8616);
        // Tests
        leaf.insert(body1);
        tree1.insert(body1);
        tree1.insert(body2);
        tree2.insert(body1);
        tree2.insert(body2);
        tree2.insert(body3);
        tree2.insert(body4);
        // Tests computation for mass and center of mass for 3 trees
        leaf.computeMassAndCenterOfMass();
        tree1.computeMassAndCenterOfMass();
        tree2.computeMassAndCenterOfMass();

        SolarBody testBody = new Planet("test", 1E7, 100);
        testBody.setPosition(-50, 100, -50);
        double dist = Physics.distance(testBody, body1);
        Vector3D res1 = tree1.treeForce(body3);
        Vector3D res2 = tree1.treeForce(body4);
        Vector3D res3 = tree2.treeForce(body1);
        assertEquals(Physics.netForce(testBody, body1, dist), leaf.treeForce(testBody));
        assertEquals(force0, leaf.treeForce(body1));
        assertEquals(force1.getX(), res1.getX(), .0001);
        assertEquals(force1.getY(), res1.getY(), .0001);
        assertEquals(force1.getZ(), res1.getZ(), .0001);
        assertEquals(force2.getX(), res2.getX(), .0001);
        assertEquals(force2.getY(), res2.getY(), .0001);
        assertEquals(force2.getZ(), res2.getZ(), .0001);
        assertEquals(force3.getX(), res3.getX(), .0001);
        assertEquals(force3.getY(), res3.getY(), .0001);
        assertEquals(force3.getZ(), res3.getZ(), .0001);
    }

    /**
     * Tests Octree bounding boxes, making sure
     */
    @Test
    public void testOctreeBoundingBoxes() {
        Octree<SolarBody> test = new Octree<SolarBody>(0, 0, 512, 512);
        SolarBody body1 = SolarBodyManager.createBody("body1", 10, 10, "ROCKET");
        SolarBody body2 = SolarBodyManager.createBody("thing", 50, 50, "PLANET");
        SolarBody body3 = SolarBodyManager.createBody("body3", 1000, 100, "PLANET");
        body1.setPosition(-1, -1, 0);
        body2.setPosition(1, 1, 1);
        body3.setPosition(-1, -1, 1);
        test.insert(body1);
        test.insert(body2);
        test.insert(body3);
        // Tests bounding box size and position
        Octree leaf1 = test.containingNode(body2);
        Octree leaf2 = test.containingNode(body3);
        assertEquals(leaf1.getCenter(), new Vector3D(256, 256, 256));
        assertEquals(leaf2.getCenter(), new Vector3D(-0.5, -0.5, 1.5));
        assertEquals(leaf1.getRadius(), 256, 0);
        assertEquals(leaf2.getRadius(), 0.5, 0);
        assertTrue(leaf2 != test.containingNode(body1));
    }

}