package org.latinschool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Box2DUtils {

    /**
     * Creates a Box2D body with specified parameters and attaches a rectangular fixture.
     *
     * @param world          The Box2D world where the body will be created.
     * @param bodyType       The type of the body (e.g., DynamicBody, StaticBody, KinematicBody).
     * @param bodyPosition   The initial position of the body in world coordinates.
     * @param shapeWidth     The width of the box shape in meters.
     * @param shapeHeight    The height of the box shape in meters.
     * @param density        The density of the body for mass calculation.
     * @param friction       The friction coefficient of the body.
     * @param restitution    The restitution (bounciness) of the body.
     * @return               The created Body with an attached rectangular fixture.
     */
    public static Body createBody(World world, BodyType bodyType, Vector2 bodyPosition,
                                  float shapeWidth, float shapeHeight, float density,
                                  float friction, float restitution) {

        // Define the body properties (type and initial position)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(bodyPosition);

        // Create the body in the world using the defined properties
        Body body = world.createBody(bodyDef);

        // Define the box shape with half-dimensions (Box2D requires half-width and half-height)
        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(shapeWidth * .5f, shapeHeight * .5f);

        // Define fixture properties, including shape, density, friction, and restitution
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = boxShape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;

        // Attach the fixture to the body
        body.createFixture(fixtureDef);

        // Dispose of the shape to free resources
        boxShape.dispose();

        return body;
    }
}
