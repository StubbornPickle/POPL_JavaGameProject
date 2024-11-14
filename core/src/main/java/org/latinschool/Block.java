package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Block {
    private final Body body;    // Physics body of the block
    private final float size;   // Block size in meters
    private Color color;        // Block color
    private float health;

    private float currentHealth;

    public Block(float x, float y, float size, Color color, float health) {
        this.size = size;
        this.color = color;
        this.body = createBlockBody(x, y, size);
        this.health = health;
        this.body.setUserData(this); // Attach this instance to the body’s user data for reference.
        this.currentHealth = health;
    }

    private Body createBlockBody(float x, float y, float size) {
        return Box2DUtils.createBoxBody(
            Main.physicsWorld, BodyDef.BodyType.StaticBody,
            new Vector2(x + size / 2, y - size / 2),
            size, size, 0.0f, 0.25f, 0.0f
        );
    }

    public void draw(float outlineWidth) {
        // Dynamically determine the number of steps based on health, with a minimum of 2 steps
        int steps = Math.max(2, (int) Math.ceil(health / 5.0)); // Divide health by 5 and round up, with a minimum of 2 steps
        float stepSize = 1.0f / steps; // Step size for darkness factor

        // Calculate the discrete darkness factor
        float rawFactor = Math.max(0, Math.min(1, Math.round(currentHealth / health / stepSize) * stepSize)); // Clamp between 0 and 1, snapping to stepSize
        float darknessFactor = 0.5f + rawFactor * 0.5f; // Remap [0, 1] to [0.5, 1]

        // Adjust color based on darknessFactor
        Color adjustedColor = new Color(
            color.r * darknessFactor,
            color.g * darknessFactor,
            color.b * darknessFactor,
            color.a
        );

        Main.shapeRenderer.setColor(adjustedColor);

        // Draw the rectangle
        Vector2 position = getPosition();
        float x = position.x + outlineWidth;
        float y = position.y - outlineWidth;

        Main.shapeRenderer.rect(x, y, size - outlineWidth * 2, -size + outlineWidth * 2);
    }



    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition().add(-size / 2, size / 2);
    }

    public void setPosition(float x, float y) {
        body.setTransform(x + size / 2, y - size / 2, body.getAngle());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void healthBy(float value) {
        currentHealth += value;
    }

    public float getHealth() {
        return currentHealth;
    }

    public void setHealth(float health) {
        currentHealth = health;
    }

    public void setBaseHealth(float health) {
        this.health = health;
    }

}
