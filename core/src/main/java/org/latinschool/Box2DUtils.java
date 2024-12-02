package org.latinschool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Box2DUtils {

    public static Body createBoxBody(World world, Vector2 position, float width, float height,
                                     BodyDef.BodyType bodyType, float density, float friction, float restitution) {
        BodyDef bodyDef = createBodyDef(position, bodyType);
        Body body = world.createBody(bodyDef);
        PolygonShape shape = createRectangleShape(width, height);
        FixtureDef fixtureDef = createFixtureDef(shape, density, friction, restitution);

        body.createFixture(fixtureDef);
        shape.dispose();

        return body;
    }

    public static Body createCapsuleBody(World world, BodyDef.BodyType bodyType, Vector2 bodyPosition,
                                         float width, float height, float rectWidthFactor, float circleRadiusFactor,
                                         float density, float friction, float restitution) {
        BodyDef bodyDef = createBodyDef(bodyPosition, bodyType);
        Body body = world.createBody(bodyDef);

        float trueHalfHeight = (height - width) * 0.5f;

        createCapsuleRect(body, width, trueHalfHeight, rectWidthFactor, density, friction, restitution);
        createCapsuleEnds(body, width, trueHalfHeight, circleRadiusFactor, density, friction, restitution);

        return body;
    }

    private static void createCapsuleRect(Body body, float width, float trueHalfHeight, float rectWidthFactor,
                                          float density, float friction, float restitution) {
        PolygonShape rectangleShape = createRectangleShape(width * rectWidthFactor, trueHalfHeight * 2);
        FixtureDef rectangleFixtureDef = createFixtureDef(rectangleShape, density, friction, restitution);

        body.createFixture(rectangleFixtureDef);
        rectangleShape.dispose();
    }

    private static void createCapsuleEnds(Body body, float width, float trueHalfHeight, float circleRadiusFactor,
                                          float density, float friction, float restitution) {
        CircleShape circleShape = createCircleShape(width * 0.5f * circleRadiusFactor);
        FixtureDef circleFixtureDef = createFixtureDef(circleShape, density, friction, restitution);

        circleShape.setPosition(new Vector2(0, trueHalfHeight)); // Top circle
        body.createFixture(circleFixtureDef);

        circleShape.setPosition(new Vector2(0, -trueHalfHeight)); // Bottom circle
        body.createFixture(circleFixtureDef);

        circleShape.dispose();
    }

    public static BodyDef createBodyDef(Vector2 position, BodyDef.BodyType bodyType) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        return bodyDef;
    }

    public static PolygonShape createRectangleShape(float width, float height) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2, Vector2.Zero, 0.0f);
        return shape;
    }

    public static PolygonShape createRectangleShape(float width, float height, Vector2 center, float angle) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2, center, angle);
        return shape;
    }

    public static CircleShape createCircleShape(float radius) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        return shape;
    }

    public static FixtureDef createFixtureDef(Shape shape, float density, float friction, float restitution) {
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.friction = friction;
        fixtureDef.restitution = restitution;
        return fixtureDef;
    }
}
