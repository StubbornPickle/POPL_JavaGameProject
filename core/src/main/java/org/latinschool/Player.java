package org.latinschool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private Body body;
    private final float width;
    private final float height;
    private final float speed;// Set movement speed

    public Player(float x, float y, float width, float height, float speed) {
        this.width = width;
        this.height = height;
        this.speed = speed;

        createCollider(x, y);
    }

    private void createCollider(float x, float y) {
        // Define a body definition for a dynamic body
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(x, y);
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        // Create the body in the Box2D world
        body = Main.world.createBody(bodyDef);

        // Define a shape for the fixture
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2, height / 2); // Set the size of the box shape

        // Create a fixture definition and attach it to the body
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f; // Density affects dynamic bodies
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.2f; // Slight bounce, if needed

        body.createFixture(fixtureDef);
        body.setFixedRotation(true); // Prevents the body from rotating

        shape.dispose();
    }

    public void input() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            body.setLinearVelocity(-speed, body.getLinearVelocity().y);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.setLinearVelocity(speed, body.getLinearVelocity().y);
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mineBlock();
        }
    }

    private void mineBlock() {
        // Step 1: Convert the mouse position to world coordinates using the viewport to handle scaling
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Main.viewport.unproject(mousePos);  // Use the viewport's unproject method instead of camera's
        Vector2 clickedWorldPos = new Vector2(mousePos.x, mousePos.y);

        // Step 2: Define the ray from the player’s position to the clicked position with a maximum length of 1.5f
        Vector2 playerPos = body.getPosition();
        Vector2 direction = clickedWorldPos.cpy().sub(playerPos).nor().scl(1.5f); // Limit length to 1.5 meters
        Vector2 rayEnd = playerPos.cpy().add(direction);

        // Step 3: Initialize a variable to track the closest fraction
        final float[] closestFraction = {1.0f}; // Start with the max fraction (1.0f represents full length)
        final Block[] firstHitBlock = {null}; // Variable to capture the first block hit

        // Step 4: Perform the raycast and find the closest block
        Main.world.rayCast((fixture, point, normal, fraction) -> {
            Object userData = fixture.getBody().getUserData();

            // Check if the fixture belongs to a block and is closer than any previously hit block
            if (userData instanceof Block && fraction < closestFraction[0]) {
                closestFraction[0] = fraction; // Update to the closest fraction
                firstHitBlock[0] = (Block) userData; // Capture the block as the first hit
            }
            return 1; // Continue raycast to check if there is a closer fixture
        }, playerPos, rayEnd);

        // Step 5: If a block was found as the first hit, destroy or mark it
        if (firstHitBlock[0] != null) {
            firstHitBlock[0].setColor(Color.WHITE); // Mark it for "destruction" visually
        }
    }










    public void update() {
        followCam();
    }

    private void followCam() {
        Main.camera.position.y = body.getPosition().y;
        Main.camera.update();
    }

    public void draw() {
        Vector2 bodyPosition = body.getPosition();

        float x = bodyPosition.x - width / 2;
        float y = bodyPosition.y - height / 2;

        Main.shape.setColor(Color.TEAL);
        Main.shape.rect(x, y, width, height);
    }
}
