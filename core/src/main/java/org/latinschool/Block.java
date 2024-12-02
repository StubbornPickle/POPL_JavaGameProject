package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Block {
    private final Body body;
    private Color color;
    private float size;
    private float baseHealth;
    private float health;

    public Block(Vector2 position, Color color, float size, float health) {
        this.body = Box2DUtils.createBoxBody(Main.physicsWorld, position, size, size,
            BodyDef.BodyType.StaticBody, 0.0f, 0.25f, 0.0f);
        this.body.setUserData(this);
        this.color = color;
        this.size = size;
        this.baseHealth = health;
        this.health = health;
    }


    public void draw(float outlineWidth) {
        draw(outlineWidth, color, 4);
    }

    public void draw(float outlineWidth, Color color, int steps) {
        float halfSize = size / 2;
        Vector2 position = body.getPosition();
        float x = position.x - halfSize + outlineWidth;
        float y = position.y + halfSize - outlineWidth;

        Main.shapeRenderer.setColor(getAdjustedColor(color, steps));
        Main.shapeRenderer.rect(x, y, size - outlineWidth * 2, -size + outlineWidth * 2);
    }

    private Color getAdjustedColor(Color originalColor, int steps) {
        if (steps == 0) { return originalColor; }
        float stepSize = 1.0f / steps;
        float rawFactor = Math.max(0, Math.min(1, Math.round(health / baseHealth / stepSize) * stepSize));
        float darknessFactor = 0.5f + rawFactor * 0.5f;
        return new Color(originalColor.r * darknessFactor, originalColor.g * darknessFactor, originalColor.b * darknessFactor, originalColor.a);
    }

    public Body getBody() {
        return body;
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void setPosition(float x, float y) {
        body.setTransform(x, y, body.getAngle());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getBaseHealth() {
        return baseHealth;
    }

    public void setBaseHealth(float baseHealth) {
        this.baseHealth = baseHealth;
        this.health = baseHealth;
    }

    public float getHealth() {
        return health;
    }

    public void healthBy(float by) {
        health += by;
    }

    public float getSize() {
        return size;
    }
}
