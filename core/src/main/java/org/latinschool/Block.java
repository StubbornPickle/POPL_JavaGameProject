package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Block {
    private final Vector2 position;
    private float size;
    private Color color;
    private Body body;

    public Block(float x, float y, float size, Color color) {
        this.position = new Vector2(x, y);
        this.size = size;
        this.color = color;

        createCollider();
    }

    private void createCollider() {
        // Define a body definition for a static body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position.x + size / 2, position.y - size / 2); // Center the body at the block's position
        bodyDef.type = BodyDef.BodyType.StaticBody;

        // Create the body in the Box2D world
        body = Main.world.createBody(bodyDef);

        // Define a shape for the fixture
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(size / 2, size / 2); // Set the size of the box shape

        // Create a fixture definition and attach it to the body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0f; // Density is irrelevant for static bodies
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.2f; // Slight bounce, if needed

        body.createFixture(fixtureDef); // Ignore warning

        // Dispose of the shape to free resources
        shape.dispose();
    }

    public void draw(ShapeRenderer shape) {
        shape.setColor(color);
        // Draws from top left corner
        shape.rect(position.x, position.y, size, -size);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        body.setTransform(position.x + size / 2, position.y - size / 2, body.getAngle());
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Body getBody() {
        return body;
    }
}
