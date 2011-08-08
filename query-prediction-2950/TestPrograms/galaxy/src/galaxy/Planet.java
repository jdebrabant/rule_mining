package galaxy;

/**
 * A representation for a SolarBody exhibiting the behavior of a Planet-like
 * objet, including stars.  A Planet has the attributes of combining upon collision
 * and having the shape Sphere.
 * @author jardini
 */
public class Planet extends SolarBody {

    /**
     * Contructor to create a new planet with the given attributes.
     * @param name - The (non-null) name of the constructed planet.
     * @param mass - The mass of the planet.
     * @param radius - The radius of the planet.
     */
    public Planet(String name, double mass, double radius) {
        super(name, mass, radius);
    }

    /**
     * Describes the collision behavior of a Planet.
     * @return CollisionBehavior.Combine
     */
    public SolarConstants.CollisionBehavior getCollisionBehavior() {
        return SolarConstants.CollisionBehavior.Combine;
    }

    /**
     * Describes the Shape of a Planet, used by solardraw.SolarDraw.
     * @return Shape.Sphere
     */
    public SolarConstants.Shape getShape() {
        return SolarConstants.Shape.Sphere;
    }

    /**
     * Returns a String representing the type of the Object, used by solardraw.SolarDraw.
     * @return The String "PLANET"
     */
    @Override
    public String getType() {
        return "PLANET";
    }
}
