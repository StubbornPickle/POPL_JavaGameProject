package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;

public class Block {
    // Block data
    private final Body body;    // Physics body of the block
    private final float size;   // Block size in meters
    private Color color;        // Block color

    /**
     * Constructs a Block at the specified position, with a given size and color.
     * @param posX X-coordinate of the top-left corner of the block.
     * @param posY Y-coordinate of the top-left corner of the block.
     * @param size Size of the block in meters.
     * @param color Color of the block.
     */
    public Block(float posX, float posY, float size, Color color) {
        this.size = size;
        this.color = color;
        this.body = createBlockBody(posX, posY, size);
        this.body.setUserData(this); // Attach this instance to the body’s user data for reference.
    }

    /**
     * Draws the block with an optional outline width.
     * @param outlineWidth Width of the outline around the block.
     */
    public void draw(float outlineWidth) {
        // Set color for block
        Main.shapeRenderer.setColor(color);

        // Calculate position with outline
        Vector2 position = getPosition();
        float x = position.x + outlineWidth;
        float y = position.y + outlineWidth;

        // Draw block rectangle with specified outline
        Main.shapeRenderer.rect(x, y, size - outlineWidth * 2, -size + outlineWidth * 2);
    }

    /**
     * Retrieves the physics body of the block.
     * @return The physics body.
     */
    public Body getBody() {
        return body;
    }

    /**
     * Returns the position of the block's top-left corner in world coordinates.
     * @return The top-left position of the block.
     */
    public Vector2 getPosition() {
        return body.getPosition().add(-size / 2, size / 2);
    }

    /**
     * Sets the position of the block's top-left corner in world coordinates.
     * @param posX X-coordinate of the top-left corner.
     * @param posY Y-coordinate of the top-left corner.
     */
    public void setPosition(float posX, float posY) {
        body.setTransform(posX + size / 2, posY - size / 2, body.getAngle());
    }

    /**
     * Retrieves the color of the block.
     * @return The current color of the block.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of the block.
     * @param color New color for the block.
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Creates a static body for the block in the physics world.
     * @param posX X-coordinate of the top-left corner.
     * @param posY Y-coordinate of the top-left corner.
     * @param size Size of the block in meters.
     * @return The created physics body.
     */
    private Body createBlockBody(float posX, float posY, float size) {
        return Box2DUtils.createBody(
            Main.physicsWorld, BodyDef.BodyType.StaticBody,
            new Vector2(posX + size / 2, posY - size / 2),
            size, size, 0.0f, 1.0f, 0.2f
        );
    }
}
