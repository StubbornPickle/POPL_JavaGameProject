package org.latinschool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private final Body body;
    private final float width; // Meters
    private final float height; // Meters
    private final float speed; // In meters
    private final float maxVelocity = 5f;
    private boolean isGrounded = true; // To track if the player is on the ground

    public Player(float x, float y, float width, float height, float speed) {
        this.width = width;
        this.height = height;
        this.speed = speed;

        // Create the player body
        body = Box2DUtils.createBody(Main.physicsWorld, BodyDef.BodyType.DynamicBody, new Vector2(x, y), width, height, 1.0f, .5f, 0.0f);
        body.setFixedRotation(true); // Prevents the body from rotating
    }

    public void input() {
        // Update grounded status
        // isGrounded = checkIfGrounded();

        // Move player if grounded and within max speed limits
        handleMovement();

        // Handle mining action (mouse click)
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mineBlock();
        }
    }

    private void handleMovement() {
        if (isGrounded) {
            // Move left or right within the speed limits
            float velocityX = body.getLinearVelocity().x;
            if (Gdx.input.isKeyPressed(Input.Keys.A) && velocityX > -maxVelocity) {
                body.setLinearVelocity(new Vector2(-speed, body.getLinearVelocity().y));
            } else if (Gdx.input.isKeyPressed(Input.Keys.D) && velocityX < maxVelocity) {
                body.setLinearVelocity(new Vector2(speed, body.getLinearVelocity().y));
            }
        }
    }

    private boolean checkIfGrounded() {
        return true;
        /**
        float rayLength = 0.1f; // Small ray distance to check if grounded
        Vector2 rayStart = body.getPosition();
        Vector2 rayEnd = new Vector2(rayStart.x, rayStart.y - height / 2 - rayLength);

        final boolean[] grounded = {false};

        // Perform the raycast to check for ground
        Main.physicsWorld.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getBody() != body) { // Ignore the player's body
                grounded[0] = true; // Hit something below, the player is grounded
                return 0; // Terminate raycast
            }
            return 1; // Continue raycast
        }, rayStart, rayEnd);

        return grounded[0];
         **/
    }

    private void mineBlock() {
        // Convert the mouse position to world coordinates
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Main.gameViewport.unproject(mousePos);
        Vector2 clickedWorldPos = new Vector2(mousePos.x, mousePos.y);

        // Create a ray from player to the clicked position with a max distance of 1.5f
        Vector2 playerPos = body.getPosition();
        Vector2 direction = clickedWorldPos.cpy().sub(playerPos).nor().scl(1.5f); // Limit to 1.5 meters
        Vector2 rayEnd = playerPos.cpy().add(direction);

        final float[] closestFraction = {1.0f}; // Max fraction to track closest block
        final Block[] firstHitBlock = {null}; // Block hit by the ray

        // Perform raycast to find the closest block
        Main.physicsWorld.rayCast((fixture, point, normal, fraction) -> {
            Object userData = fixture.getBody().getUserData();

            if (userData instanceof Block && fraction < closestFraction[0]) {
                closestFraction[0] = fraction; // Update closest hit fraction
                firstHitBlock[0] = (Block) userData; // Capture the block hit
            }
            return 1; // Continue raycast
        }, playerPos, rayEnd);

        // If a block was hit, mark it for destruction (by changing its color)
        if (firstHitBlock[0] != null) {
            firstHitBlock[0].setColor(null); // "Destroy" the block by clearing its color
        }
    }

    public void update() {
        followCam();
    }

    private void followCam() {
        // Keep the camera centered on the player's y-position
        Main.mainCamera.position.y = body.getPosition().y;
        Main.mainCamera.update();
    }

    public void draw() {
        // Draw player rectangle based on its position and size
        Vector2 bodyPosition = body.getPosition();
        float x = bodyPosition.x - width / 2;
        float y = bodyPosition.y - height / 2;

        Main.shapeRenderer.setColor(Color.TEAL);
        Main.shapeRenderer.rect(x, y, width, height);
    }
}
