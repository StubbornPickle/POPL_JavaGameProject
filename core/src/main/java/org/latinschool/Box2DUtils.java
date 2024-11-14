package org.latinschool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Box2DUtils {

    /**
     * Creates a Box2D body with specified parameters and attaches a rectangular fixture.
     *
     * @param world          The Box2D world where the body will be created.
     * @param bodyType       The type of the body (e.g., DynamicBody, StaticBody, KinematicBody).
     * @param bodyPosition   The initial position of the body in world coordinates.
     * @param width          The width of the box shape in meters.
     * @param height         The height of the box shape in meters.
     * @param density        The density of the body for mass calculation.
     * @param friction       The friction coefficient of the body.
     * @param restitution    The restitution (bounciness) of the body.
     * @return               The created Body with an attached rectangular fixture.
     */
    public static Body createBoxBody(World world, BodyType bodyType, Vector2 bodyPosition,
                                  float width, float height, float density,
                                  float friction, float restitution) {

        // Define the body properties (type and initial position)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(bodyPosition);

        // Create the body in the world using the defined properties
        Body body = world.createBody(bodyDef);

        // Define the box shape with half-dimensions (Box2D requires half-width and half-height)
        PolygonShape boxShape = new PolygonShape();
        boxShape.setAsBox(width * .5f, height * .5f);

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

    /**
     * Creates a Box2D body with a capsule shape (rectangle with circular ends).
     *
     * @param world              The Box2D world where the body will be created.
     * @param bodyType           The type of the body (e.g., DynamicBody, StaticBody, KinematicBody).
     * @param bodyPosition       The initial position of the body in world coordinates.
     * @param width              The width of the capsule, which will also be the diameter of the circular ends.
     * @param height             The height of the rectangular part of the capsule.
     * @param rectWidthFactor    The factor applied to the scale of rectangle fixture.
     * @param circleRadiusFactor The factor applied to the scale of the circle fixtures.
     * @param density            The density of the body for mass calculation.
     * @param friction           The friction coefficient of the body.
     * @param restitution        The restitution (bounciness) of the body.
     * @return                   The created Body with a capsule-like collider.
     */
    public static Body createCapsuleBody(World world, BodyType bodyType, Vector2 bodyPosition,
                                         float width, float height, float rectWidthFactor,
                                         float circleRadiusFactor, float density, float friction,
                                         float restitution) {

        // Define the body properties (type and initial position)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(bodyPosition);

        // Create the body in the world using the defined properties
        Body body = world.createBody(bodyDef);

        // Define the rectangle shape for the central part of the capsule
        PolygonShape rectangleShape = new PolygonShape();
        float trueHalfHeight = (height - width) * 0.5f;
        rectangleShape.setAsBox(width * 0.5f * rectWidthFactor, trueHalfHeight);

        // Define fixture properties for the rectangle part
        FixtureDef rectangleFixtureDef = new FixtureDef();
        rectangleFixtureDef.shape = rectangleShape;
        rectangleFixtureDef.density = density;
        rectangleFixtureDef.friction = friction;
        rectangleFixtureDef.restitution = restitution;

        // Attach the rectangle fixture to the body
        body.createFixture(rectangleFixtureDef);

        // Dispose of the rectangle shape to free resources
        rectangleShape.dispose();

        // Define the circle shape for the capsule ends with a radius of half the width
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(width * 0.5f * circleRadiusFactor);

        // Define fixture properties for the circular ends
        FixtureDef circleFixtureDef = new FixtureDef();
        circleFixtureDef.shape = circleShape;
        circleFixtureDef.density = density;
        circleFixtureDef.friction = friction;
        circleFixtureDef.restitution = restitution;

        // Attach the top circular end fixture to the body
        circleShape.setPosition(new Vector2(0, trueHalfHeight));
        body.createFixture(circleFixtureDef);

        // Attach the bottom circular end fixture to the body
        circleShape.setPosition(new Vector2(0, -trueHalfHeight));
        body.createFixture(circleFixtureDef);

        // Dispose of the circle shape to free resources
        circleShape.dispose();

        return body;
    }
}
