package galaxy;

import java.security.Permission;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import solardraw.SolarDraw;
import static org.junit.Assert.*;

/**
 * Contains tests for XML input/output, basic SolarBody operations, and
 * system tests.
 * @author jardini
 */
public class GalaxyTest {
    /**
     * Dummy class for testing Octree manager
     */
    private static class SolarDrawDummy implements SolarDraw {

        public void registerObject(Body sdo) {}

        public void unregisterObject(Body sdo) {}

        public void tick() {}

        public long getTicks() {
            return 1;
        }

        public void begin() {}

        public String getCombinedBodyName(String collider_name_1, String collider_name_2) {
            return "a";
        }
        
    }

    /**
     * Exit Exception used for handling system tests involving exiting
     */
    private static class ExitException extends SecurityException {

        public final int status;

        public ExitException(int status) {
            super("There is no escape!");
            this.status = status;
        }
    }

    /**
     * Security manager to handle system tests involving exiting
     */
    private static class NoExitSecurityManager extends SecurityManager {

        @Override
        public void checkPermission(Permission perm) {
            // allow anything.
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow anything.
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }
    SolarBody sun;
    SolarBody earth;
    SolarBody rocket;

    public GalaxyTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        System.setSecurityManager(new NoExitSecurityManager());
        sun = new Planet("Sun", 1.989E30, 6.95E8);
        earth = new Planet("Earth", 5.974E24, 6.371E6);
        earth.setPosition(0, 1.496E11, 0);
    }

    @After
    public void tearDown() {
        System.setSecurityManager(null);
    }

    /**
     * Tests SolarBodyOctreeManager, making sure
     * bodies are correctly added to/removed from the octree,
     * and collisions occur properly
     */
    @Test
    public void testOctreeManager() {
        //GalaxyMain main = new GalaxyMain("galaxies/testOut.xml", 100, 0, 0);
        SolarDraw solarDraw = new SolarDrawDummy();
        SolarBodyManager octreeManager = new SolarBodyOctreeManager(solarDraw);
        SolarBody newBody = null;
        
        for (int i = 1; i <= 9; i++) {
            String newName = "Planet" + i;
            newBody = SolarBodyManager.createBody(newName,
2E10, 20, "PLANET");
            newBody.setPosition(i*3, i*4, i*5);
            octreeManager.addBody(newBody);
        }
        assertEquals(octreeManager.getBodies().size(), 9);
        octreeManager.removeBody(newBody);
        assertEquals(octreeManager.getBodies().size(), 8);
        int i = 1;
        
        // Makes sure positions changed correctly
        for (SolarBody b : octreeManager.getBodies()) {
            assertTrue(b.getPosition().equals(new Vector3D(i*3, i*4, i*5)));
            ++i;
        }
        i = 1;
        octreeManager.updateSolarBodies(40);
        // Assures all bodies position's and velocities were updated
        for (SolarBody b : octreeManager.getBodies()) {
            assertTrue(!b.getPosition().equals(new Vector3D(i*3, i*4, i*5)));
            assertTrue(!b.getVelocity().equals(new Vector3D()));
            ++i;
        }
        octreeManager.processCollisions();
        // Only last body should remain
        assertEquals(octreeManager.getBodies().size(), 1);
    }

    /**
     * Tests that the fields of SolarBodies are properly set/returned
     * Makes sure the private fields can't be mutated through getters
     * Also tests basic setters/getters for SolarBodies
     */
    @Test
    public void testSolarBodyIntegrity() {
        SolarBody testBody = new Planet("Test", 1, 2);
        SolarBody test2 = new Rocket("na>me", .001, 9151.2);
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(4, 5, 6);
        testBody.setPosition(v1);
        testBody.setVelocity(v2);
        testBody.setDefaultForce(7, 8, 9);
        test2.setVelocity(.1, 0, 0);
        test2.setName("new>name");
        test2.setRadius(1151.2);
        test2.setDefaultForce(.1, 0, 151);
        test2.addForce(Physics.netForce(test2, testBody, Physics.distance(test2, testBody)));
        test2.resetNetForce();

        assertTrue(testBody.getPosition().equals(v1));
        assertTrue(testBody.getPosition() != v1);

        testBody.getVelocity().addTo(v1);
        assertTrue(testBody.getVelocity().equals(v2));
        assertTrue(testBody.getDefaultForce().equals(new Vector3D(7, 8, 9)));
        testBody.setDefaultForce(1, 2, 3);
        assertTrue(testBody.getDefaultForce().equals(testBody.getPosition()));
        assertEquals(testBody.getType(), "PLANET");
        assertEquals(testBody.getCollisionBehavior(), SolarDraw.CollisionBehavior.Combine);
        assertEquals(testBody.getShape(), SolarDraw.Shape.Sphere);

        assertTrue(test2.getName().equals("new>name"));
        assertEquals(test2.getMass(), .001, 0);
        assertEquals(test2.getRadius(), 1151.2, 0);
        assertEquals(test2.getType(), "ROCKET");
        assertEquals(test2.getCollisionBehavior(), SolarDraw.CollisionBehavior.Explode);
        assertEquals(test2.getShape(), SolarDraw.Shape.Cone);
        assertEquals(test2.getDefaultForce(), new Vector3D(.1, 0, 151));
    }

    /**
     * Tests for invalid inputs
     */
    @Test
    public void testSystemFailures() {
        GalaxyMain main;
        try {
            main = new GalaxyMain("galaxies/negative_mass.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Missing/Invalid OBJECT attribute
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/negative_radius.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Missing/Invalid OBJECT attribute
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/unknown_type.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Illegal SolarBody type
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/missing_thrust.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Works fine, thrust default is 0
            assertEquals("Exit status", 0, e.status);
        }
                try {
            main = new GalaxyMain("galaxies/missing_position.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Works fine, position default is 0
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/invalid_tag.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Invalid formatting in XML tag
            assertEquals("Exit status", 0, e.status);
        }
        // For any non-existent file, prints "File not found" and exists
    }

    /**
     * System test results documented here
     */
    @Test
    public void testSystem() {
        GalaxyMain main;
        try {
            main = new GalaxyMain("galaxies/empty.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Runs successfully, nothing shown because empty file,
            // Output file correctly has no planets, just the time info
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/OrbitTest.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Successfully has 1 planet orbit around the sun, slower than demo though
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/manysuns.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Successfully has all suns collide to 1 HUGE sun immediately
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/smallplanetring.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Explodes instantly, matches demo behavior
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/smallrocketring.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Explodes after pulling in for half a second, matches demo
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/testRocket.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: 6 rockets correctly absorbed into sun all at the same time
            // UPS matches demo almost exactly
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/rocketring.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Rockets all sucked in and dissapear
            // In mine, 4 escape at the end at high speed in compass directions
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/planetring.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: All planets absorbed to form middle sun, matches Solar demo
            // Galaxy Demo incorrect?  Sun dissapears at end in demo
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/PlanetRocketCollision.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: 5/6 rockets absorbed, matches demo almost exactly
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/TwoColliding.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Two suns eventually get close enough to merge
            // In demo, the suns slingshot away: Did the demo not use the collision constant?
            // Mine: 60-80 UPS, Demo: 50ish UPS
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("galaxies/fourgalaxies.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Four galaxies: 2000 planets...runs at reasonable rate
            // Correctly merges the 4 suns after a while
            // Mine: 9ish UPS, Demo: 4.5ish UPS
            assertEquals("Exit status", 0, e.status);
        }
        try {
            main = new GalaxyMain("thrusttest.xml", "galaxies/testout.xml");
            main.runSimulation();
        } catch (ExitException e) {
            // Result: Rockets correctly incorporate thrust force, matches demo
            // Mine: 600ish UPS, Demo: 400ish UPS
            assertEquals("Exit status", 0, e.status);
        }
    }


    /**
     * Tests the XML reader and writer
     * testIn makes sure the reader works correctly with arguments in any order,
     * with planets and rockets, and makes sure the internal representation
     * is correct.
     *
     */
    @Test
    public void testXml() {
        TimeInfo timeInfo = new TimeInfo();
        // testIn contains strange characters in names
        String inFile = "galaxies/testIn.xml";
        String outFile = "galaxies/testOut.xml";
        SolarDraw solarDraw = new SolarDrawDummy();
        SolarBodyManager bodyManager = new SolarBodyOctreeManager(solarDraw);
        try {
            GalaxyXML.parseInput(inFile, timeInfo, bodyManager);
        } catch (XMLInputException e) {
            System.err.print(e.getMessage());
        }
        // Ensures time values read in correctly
        assertEquals(timeInfo.getInitialTime(), 0, 0);
        assertEquals(timeInfo.getTimestep(), 20.001, 0);
        assertEquals(timeInfo.getTimestepsToRun(), 1000000, 0);

        // Ensures all SolarBodies read in properly
        Collection<SolarBody> solarBodies = (Collection<SolarBody>) bodyManager.getBodies();
        assertEquals(solarBodies.size(), 7);
        SolarBody[] bodies = solarBodies.toArray(new SolarBody[10]);
        assertEquals(bodies[0].getName(), "Star");
        bodies[0].setName("Bro\"ke>n");

        // Ensures that writing output and re-reading works properly
        GalaxyXML.writeOutput(outFile, timeInfo, bodyManager, 0);
        try {
            GalaxyXML.parseInput(inFile, timeInfo, bodyManager);
        } catch (XMLInputException e) {
            System.err.print(e.getMessage());
        }
        assertEquals(timeInfo.getInitialTime(), 0, 0);
        assertEquals(timeInfo.getTimestep(), 20.001, 0);
        assertEquals(timeInfo.getTimestepsToRun(), 1000000, 0);
        assertEquals(solarBodies.size(), 14);
        bodies = solarBodies.toArray(new SolarBody[14]);
        assertEquals(bodies[0].getName(), "Bro\"ke>n");
        assertEquals(bodies[7].getName(), "Star");
        assertEquals(bodies[13].getName(), "Earth3");
        GalaxyXML.writeOutput(outFile, timeInfo, bodyManager, .5);
        // Manually check that the output file testOut.xml contains
        // exactly 14 objects: with 7 pairs of identical objects
        // and that that time = .5, timestep = 20.001, and timestepsToRun = 1000000
    }

}