package org.latinschool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private final Body body;
    private final float width; // Meters
    private final float height; // Meters
    private final float speed; // Max velocity
    private final float acceleration; // Force applied to accelerate
    private final float jumpForce; // Force applied to jump
    private final float sprintMultiplier; // Speed and quickness multiplier for sprinting
    // private final float sprintDuration; // Max time for sprinting
    //private final float sprintCooldown; // Cooldown time for sprinting
    private final float crouchMultiplier; // Speed and quickness multiplier for crouching
    private final boolean useFollowCam; // Locking to main camera or not

    private float mineSpeed = 10.0f;
    private int blocksMined = 0;
    private float lowestPoint = 1000;

    private Block highlightedBlock;

    public Player(float x, float y, float width, float height,
                  float speed, float acceleration, float jumpForce, float sprintMultiplier,
                  float sprintDuration, float sprintCooldown, float crouchMultiplier, boolean useFollowCam) {

        this.width = width;
        this.height = height;
        this.speed = speed;
        this.acceleration = acceleration;
        this.jumpForce = jumpForce;
        this.sprintMultiplier = sprintMultiplier;
        // this.sprintDuration = sprintDuration;
        // this.sprintCooldown = sprintCooldown;
        this.crouchMultiplier = crouchMultiplier;
        this.useFollowCam = useFollowCam;
        this.body = createPlayerBody(x, y);
    }

    private Body createPlayerBody(float x, float y) {
        Body body = Box2DUtils.createCapsuleBody(
            Main.physicsWorld, BodyDef.BodyType.DynamicBody, new Vector2(x, y),
            width, height, 0.75f, 1.0f,
            1.0f, 0.25f, 0.0f
        );
        createFootSensor(body);
        body.setFixedRotation(true);

        return body;
    }

    private void createFootSensor(Body body) {
        PolygonShape footSensorShape = new PolygonShape();
        footSensorShape.setAsBox(width * 0.4f, 0.05f, new Vector2(0, -height / 2), 0);

        FixtureDef footSensorFixtureDef = new FixtureDef();
        footSensorFixtureDef.shape = footSensorShape;
        footSensorFixtureDef.isSensor = true;

        body.createFixture(footSensorFixtureDef).setUserData("footSensor");
        footSensorShape.dispose();
    }

    public void input() {
        boolean isSprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        boolean isCrouching = Gdx.input.isKeyPressed(Input.Keys.C);
        boolean isGrounded = Main.contactListener.isGrounded();

        float appliedAcceleration = applyMultiplier(acceleration, isSprinting, isCrouching, isGrounded);
        float appliedSpeed = applyMultiplier(speed, isSprinting, isCrouching, isGrounded);

        handleMovement(appliedAcceleration, isGrounded);
        if (isGrounded) {
            capVelocity(appliedSpeed);
        }

        updateHighlightedBlock(1.5f);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && highlightedBlock != null) {
            float by = -mineSpeed * Gdx.graphics.getDeltaTime();
            if (highlightedBlock.getHealth() + by <= 0.0f) {
                blocksMined += 1;
                mineSpeed += 0.05f;
                if (highlightedBlock.getColor().equals(Color.YELLOW)) {
                    mineSpeed += 5.0f;
                }
            }
            highlightedBlock.healthBy(-mineSpeed * Gdx.graphics.getDeltaTime());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            mineSpeed += 1;
        }
    }

    private float applyMultiplier(float value, boolean isSprinting, boolean isCrouching, boolean isGrounded) {
       if (isGrounded) {
           if (isSprinting) {
               return value * sprintMultiplier;
           } else if (isCrouching) {
               return value * crouchMultiplier;
           }
           return value;
       }
       return value * 0.1f;
    }

    private void handleMovement(float quickness, boolean isGrounded) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            body.applyForceToCenter(new Vector2(-quickness, 0), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.applyForceToCenter(new Vector2(quickness, 0), true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)  && isGrounded) {
            body.applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
        }
    }

    private void capVelocity(float speed) {
        Vector2 velocity = body.getLinearVelocity();
        if (Math.abs(velocity.x) > speed) {
            body.setLinearVelocity(speed * Math.signum(velocity.x), velocity.y);
        }
    }

    private void updateHighlightedBlock(float maxLength) {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Main.gameViewport.unproject(mousePos);
        Vector2 mouseWorldPos = new Vector2(mousePos.x, mousePos.y);

        Vector2 playerPos = body.getPosition();
        Vector2 direction = mouseWorldPos.cpy().sub(playerPos).nor();

        float distanceToMouse = playerPos.dst(mouseWorldPos);

        float effectiveLength = Math.min(maxLength, distanceToMouse);

        Vector2 rayEnd = playerPos.cpy().add(direction.scl(effectiveLength));

        final Block[] hitBlock = {null};
        final float[] closestFraction = {1.0f};

        Main.physicsWorld.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getBody() == body) {
                return -1f; // Continue
            }
            Object userData = fixture.getBody().getUserData();
            if (userData instanceof Block) {
                if (fraction < closestFraction[0]) {
                    closestFraction[0] = fraction;
                    hitBlock[0] = (Block) userData;
                }
            }

            return 1f;
        }, playerPos, rayEnd);

        highlightedBlock = hitBlock[0];
    }

    public void update() {
        if (useFollowCam) {
            followCam();
        }
    }

    private void followCam() {
        if (body.getPosition().y < lowestPoint) {
            Main.mainCamera.position.y = body.getPosition().y;
            Main.mainCamera.update();
            lowestPoint = body.getPosition().y;
        }
    }

    public void draw() {
        // Draw player rectangle based on its position and size
        Vector2 bodyPosition = body.getPosition();
        float xa = bodyPosition.x - width / 2;
        float ya = bodyPosition.y - height / 2;

        Main.shapeRenderer.setColor(Color.TEAL);
        Main.shapeRenderer.rect(xa, ya, width, height);

        if (highlightedBlock != null) {
            highlightedBlock.draw(Main.terrain.getBlockOutlineWidth() * .25f);
        }
    }

    public float getHeight() {
        return height;
    }
}
