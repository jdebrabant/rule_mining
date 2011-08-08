package galaxy;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Contains unit tests for all basic Vector3D operations
 * @author Justin
 */
public class VectorTest {

    Vector3D v0, v1, v2, v3, v4;

    public VectorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        v0 = new Vector3D();
        v1 = new Vector3D(1, 1, 1);
        v2 = new Vector3D(2, 3, 4);
        v3 = new Vector3D(0, -.001, 99999.8);
        v4 = new Vector3D(-100, 100, .5);
    }

    @After
    public void tearDown() {
    }

    /**
     * Tests addition
     */
    @Test
    public void testAdd() {
        // Expected results
        Vector3D sum0 = new Vector3D(0, 0, 0);
        Vector3D sum1 = new Vector3D(3, 4, 5);
        Vector3D sum2 = new Vector3D(-100, 99.999, 100000.3);
        // Tests
        v0.addTo(v0);
        assertTrue(v0.equals(sum0));
        v1.addTo(v2);
        assertTrue(v1.equals(sum1));
        v3.addTo(v4);
        assertTrue(v3.equals(sum2));
    }

    /**
     * Tests subtraction
     */
    @Test
    public void testSubtract() {
        // Expected
        Vector3D diff0 = new Vector3D(0, 0, 0);
        Vector3D diff1 = new Vector3D(-1, -2, -3);
        Vector3D diff2 = new Vector3D(100, -100.001, 99999.3);
        // Tests
        v0.subtractFrom(v0);
        assertTrue(v0.equals(diff0));
        v1.subtractFrom(v2);
        assertTrue(v1.equals(diff1));
        v3.subtractFrom(v4);
        assertTrue(v3.equals(diff2));
    }

    /**
     * Tests scaling by a constant
     */
    @Test
    public void testScaling() {
        double c0 = 0;
        double c1 = 1.6;
        double c2 = -.4;
        // Expected results
        Vector3D v1Copy = new Vector3D(v1);
        Vector3D v1Scaled0 = new Vector3D(0, 0, 0);
        Vector3D v1Scaled1 = new Vector3D(1.6, 1.6, 1.6);
        Vector3D v1Scaled2 = new Vector3D(-.4, -.4, -.4);
        Vector3D v4Scaled = new Vector3D(100*.4, -100*.4, -.5*.4);
        // Tests
        v1.scaleBy(c0);
        assertTrue(v1.equals(v1Scaled0));
        v1.copy(v1Copy);
        v1.scaleBy(c1);
        assertTrue(v1.equals(v1Scaled1));
        v1.copy(v1Copy);
        v1.scaleBy(c2);
        assertTrue(v1.equals(v1Scaled2));
        v4.scaleBy(c2);
        assertTrue(v4.equals(v4Scaled));
    }
    
    /**
     * Tests dividing by a constant
     */
    @Test
    public void testDivide() {
        double c0 = 0;
        double c1 = 1.6;
        // Expected results
        Vector3D v1Divided = new Vector3D(1 / 1.6, 1 / 1.6, 1 / 1.6);
        double inf = Double.POSITIVE_INFINITY;
        Vector3D divideByZero = new Vector3D(inf, inf, inf);
        Vector3D v2Divided = new Vector3D(2 / 1.6, 3 / 1.6, 4 / 1.6);
        // Tests
        v1.divideBy(c1);
        assertTrue(v1.equals(v1Divided));
        v2.divideBy(c0);
        assertTrue(v2.equals(divideByZero));
        v2.set(2, 3, 4);
        v2.divideBy(c1);
        assertTrue(v2.equals(v2Divided));
    }
    
    /**
     * Tests dot product
     */
    @Test
    public void testDotProduct() {       
        // Expected values
        double dot01 = 0;
        double dot11 = 3;
        double dot12 = 9;
        double dot34 = 49999.8;
        // Tests
        assertEquals(dot01, v0.dotProduct(v1), 0);
        assertEquals(dot11, v1.dotProduct(v1), 0);
        assertEquals(dot12, v1.dotProduct(v2), 0);
        assertEquals(dot12, v2.dotProduct(v1), 0);
        assertEquals(dot34, v3.dotProduct(v4), 0);
    }

    /**
     * Tests copying and setting vectors, along with basic getters
     */
    @Test
    public void testCopying() {
        v2.set(0, 1, 141);
        assertTrue(v2.equals(new Vector3D(0, 1, 141)));
        v3.copy(v1);
        assertTrue(v3.equals(v1));
        Vector3D v5 = new Vector3D(v3);
        assertTrue(v5.equals(v1));
        v5.set(-1, 1, .01);
        assertEquals(v5.getX(), -1, 0);
        assertEquals(v5.getY(), 1, 0);
        assertEquals(v5.getZ(), .01, 0);
    }

}