package galaxy;

/**
 * A representation of Rockets, which are SolarBody instances with a constant thrust.
 * A Rocket will explode upon collision and has the shape of a Cone.
 * @author jardini
 */
public class Rocket extends SolarBody {

    /**
     * Constructor to create a Rocket with the given attributes.
     * @param name - The (non-null) name of the constructed planet.
     * @param mass - The mass of the planet.
     * @param radius - The radius of the planet.
     */
    public Rocket(String name, double mass, double radius) {
        super(name, mass, radius);
    }

    /**
     * Describes the collision behavior of a Rocket.
     * @return CollisionBehavior.Explode
     */
    public SolarConstants.CollisionBehavior getCollisionBehavior() {
        return SolarConstants.CollisionBehavior.Explode;
    }

    /**
     * Describes the Shape of a Rocket, used by solardraw.SolarDraw.
     * @return Shape.Cone
     */
    public SolarConstants.Shape getShape() {
        return SolarConstants.Shape.Cone;
    }

    /**
     * Describes the Shape of a Rocket, used by solardraw.SolarDraw.
     * @return The String "ROCKET"
     */
    @Override
    public String getType() {
        return "ROCKET";
    }
}
