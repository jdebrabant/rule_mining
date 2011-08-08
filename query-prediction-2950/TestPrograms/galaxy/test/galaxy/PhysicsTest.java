package galaxy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import solardraw.SolarConstants;
import static org.junit.Assert.*;

/**
 * Contains unit tests for physics equations (distance, force) as well as
 * tests for collisions between Planets
 * @author Justin
 */
public class PhysicsTest {

    SolarBody sun, earth;
    SolarBody planet1, planet2, rocket1, rocket2;
    SolarBody zero1, zero2;

    public PhysicsTest() {
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
        planet1 = new Planet("planet1", 50, 10);
        planet2 = new Planet("planet2", 10, 20);
        rocket1 = new Rocket("rocket1", 5, 5);
        rocket2 = new Rocket("rocket2", 5E4, .5);
        zero1 = new Planet("zeroOne", 50, 10);
        zero2 = new Planet("zeroTwo", 50, 10);

    }

    @After
    public void tearDown() {
    }

    /**
     * Tests distance formula
     */
    @Test
    public void testDistance() {
        planet2.setPosition(3, 4, 0);
        rocket1.setPosition(2, 3, 6);
        rocket1.setVelocity(3, 10, 101.2);

        double distance1 = 5;
        double distance2 = 7;
        double distanceEarth = 1.496E11;

        assertEquals(Physics.distance(zero1, zero2), 0, 0);
        assertEquals(Physics.distance(planet1, planet2), distance1, .0000001);
        assertEquals(Physics.distance(planet1, rocket1), distance2, .0000001);
        assertEquals(Physics.distance(rocket1, rocket1), 0, 0);
        assertEquals(Physics.distance(earth, sun), distanceEarth, .0000001);
    }

    /**
     * Tests force magnitude formula
     */
    @Test
    public void testForceMagnitude() {
        planet2.setPosition(3, 4, 0);
        rocket1.setPosition(2, 3, 6);
        rocket1.setVelocity(3, 10, 101.2);
        rocket2.setPosition(5, 10, 9);
        rocket2.setDefaultForce(1, -12, 41412);

        double force1 = SolarConstants.GRAVITY_CONSTANT * 500 / 25;
        double force2 = SolarConstants.GRAVITY_CONSTANT * 250 / 49;
        double force3 = SolarConstants.GRAVITY_CONSTANT * 5E5 / 100;
        // Tests
        assertEquals(Physics.force(zero1, zero2), 0, 0);
        assertEquals(Physics.force(planet1, planet2), force1, .0000001);
        assertEquals(Physics.force(planet1, rocket1), force2, .0000001);
        assertEquals(Physics.force(rocket1, rocket2), force3, .0000001);
    }

    /**
     * Test force vector equation (relies on distance formula working!)
     */
    @Test
    public void testForceVector() {
        planet2.setPosition(3, 4, 0);
        rocket1.setPosition(2, 0, 3);
        rocket1.setVelocity(3, 10, 101.2);
        rocket2.setPosition(2, 0, 4);
        rocket2.setDefaultForce(1, -12, 41412);
        // Expected results
        Vector3D force1 = new Vector3D(8.009E-10, 1.0678E-9, 0);
        Vector3D force2 = new Vector3D(7.1194E-10, 0, 1.0679E-9);
        Vector3D force3 = new Vector3D(0, 0, 1.6685E-5);
        // Tests
        double dist1 = Physics.distance(planet1, planet2);
        double dist2 = Physics.distance(planet1, rocket1);
        double dist3 = Physics.distance(rocket1, rocket2);
        
        assertEquals(Physics.netForce(zero1, zero2, 0), new Vector3D());
        Vector3D res1 = Physics.netForce(planet1, planet2, dist1);
        assertEquals(res1.getX(), force1.getX(), .000001);
        assertEquals(res1.getY(), force1.getY(), .000001);
        assertEquals(res1.getZ(), force1.getZ(), .000001);
        Vector3D res2 = Physics.netForce(planet1, rocket1, dist2);
        assertEquals(res2.getX(), force2.getX(), .000001);
        assertEquals(res2.getY(), force2.getY(), .000001);
        assertEquals(res2.getZ(), force2.getZ(), .000001);
        Vector3D res3 = Physics.netForce(rocket1, rocket2, dist3);
        assertEquals(res3.getX(), force3.getX(), .000001);
        assertEquals(res3.getY(), force3.getY(), .000001);
        assertEquals(res3.getZ(), force3.getZ(), .000001);
    }

    /**
     * Tests isCollision method
     */
    @Test
    public void testIsCollision() {
        planet1.setPosition(1, 0, 0);
        planet2.setPosition(3, 40, 10);
        rocket1.setPosition(2, 3, 6);
        rocket1.setVelocity(3, 10, 101.2);
        rocket2.setPosition(500.01, 10000, 900);
        rocket2.setDefaultForce(1, -12, 41412);
        // Tests
        assertTrue(Physics.isCollision(zero1, zero1));
        assertTrue(Physics.isCollision(planet1, planet2));
        assertTrue(Physics.isCollision(planet1, rocket1));
        
        assertTrue(!Physics.isCollision(planet1, rocket2));
        assertTrue(!Physics.isCollision(rocket2, planet1));
        assertTrue(!Physics.isCollision(rocket1, rocket2));
    }

    /**
     * Tests orbital velocity equations
     */
    @Test
    public void testOrbitalVelocity() {
        planet2.setPosition(3, 4, 0);
        rocket1.setPosition(2E10, 3E9, 6E11);
        rocket1.setVelocity(10000, 234, 101.2);
        // Expected values
        double rocketOrbitX = 74.30790;
        double rocketOrbitY = -198588.19886;
        double earthOrbitalVelocity = 2.978E4;
        double orbitX = Physics.orbitVelocityX(earth, sun);
        // Tests
        assertEquals(Physics.orbitVelocityX(rocket1, sun), rocketOrbitX, .00001);
        assertEquals(Physics.orbitVelocityY(rocket1, orbitX), rocketOrbitY, .00001);
        assertEquals(earthOrbitalVelocity, orbitX, 10);
        assertEquals(Physics.orbitVelocityY(earth, orbitX), 0, 0);

    }

    
    /**
     * Tests collisions between planets
     */
    @Test
    public void testPlanetCollisions() {
        planet1 = new Planet("one", 15, 10);
        planet2 = new Planet("two", 25, 5);
        SolarBody planet3 = new Planet("twoCopy", 12.5, 15.9);
        planet2.setPosition(16, 9, 8);
        planet2.setVelocity(36, 25, 100);
        planet3.setPosition(16, 9, 8);
        planet3.setVelocity(36, 25, 100.5);
        // Expected results
        SolarBody result1 = new Planet("three", 40, Math.cbrt(1125));
        result1.setPosition(25. * 16 / 40, 25. * 9 / 40, 25. * 8 / 40);
        result1.setVelocity(25. * 36 / 40, 25. * 25 / 40, 25. * 100 / 40);
        SolarBody result2 = new Planet("res2", 37.5, Math.cbrt(5*5*5+15.9*15.9*15.9));
        result2.setPosition(16, 9, 8);
        result2.setVelocity(36, 25, 100.16666);
        // Tests
        Physics.processPlanetCollision(planet1, planet2);
        Physics.processPlanetCollision(planet2, planet3);
        planet1.setName(result1.getName());

        assertEquals(planet1.getMass(), result1.getMass(), 0);
        assertEquals(planet1.getRadius(), result1.getRadius(), 0);
        assertTrue(planet1.getPosition().equals(result1.getPosition()));
        assertTrue(planet1.getVelocity().equals(result1.getVelocity()));
        assertTrue(planet1.getName().equals(result1.getName()));

        assertEquals(planet2.getMass(), result2.getMass(), 0);
        assertEquals(planet2.getRadius(), result2.getRadius(), .000001);
        Vector3D pos = planet2.getPosition();
        assertEquals(pos, result2.getPosition());
        Vector3D vel = planet2.getVelocity();
        assertEquals(vel.getX(), result2.getVelocity().getX(), .00001);
        assertEquals(vel.getY(), result2.getVelocity().getY(), .00001);
        assertEquals(vel.getZ(), result2.getVelocity().getZ(), .00001);
    }
}