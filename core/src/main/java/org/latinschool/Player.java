package org.latinschool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;

public class Player {
    private final Body body;
    private final float width;
    private final float height;
    private final float speed;
    private final float acceleration;
    private final float jumpForce;
    private final float sprintMultiplier;
    private final float crouchMultiplier;
    private final boolean useFollowCam;
    private float baseHealth;
    private float health;

    private float mineSpeed;
    private float lastYVelocity = 0.0f;
    private boolean isGrounded = false;
    private Block targetedBlock;

    public Player(Vector2 position, float width, float height, float speed, float acceleration,
                  float jumpForce, float sprintMultiplier, float crouchMultiplier, boolean useFollowCam,
                  float mineSpeed, float baseHealth) {
        this.width = width;
        this.height = height;
        this.speed = speed;
        this.acceleration = acceleration;
        this.jumpForce = jumpForce;
        this.sprintMultiplier = sprintMultiplier;
        this.crouchMultiplier = crouchMultiplier;
        this.useFollowCam = useFollowCam;
        this.mineSpeed = mineSpeed;
        this.baseHealth = baseHealth;
        this.health = baseHealth;
        this.body = createPlayerBody(position);
    }

    private Body createPlayerBody(Vector2 position) {
        Body body = Box2DUtils.createCapsuleBody(Main.physicsWorld, BodyDef.BodyType.DynamicBody, position, width, height,
            0.75f, 1.0f, 1.0f, 0.25f, 0.0f);
        body.setUserData(this);
        body.setFixedRotation(true);
        body.setSleepingAllowed(false);

        return body;
    }

    public void input() {
        boolean isSprinting = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        boolean isCrouching = Gdx.input.isKeyPressed(Input.Keys.C);

        float appliedAcceleration = applyMultiplier(acceleration, isSprinting, isCrouching, isGrounded);
        float appliedSpeed = applyMultiplier(speed, isSprinting, isCrouching, isGrounded);

        handleMovement(appliedAcceleration, isGrounded);
        if (isGrounded) {
            capVelocity(appliedSpeed);
        }

        if (health <= 0.0f) {
            targetedBlock = null;
        } else {
            targetedBlock = getTargetedBlock(1.5f);
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && targetedBlock != null) {
            targetedBlock.healthBy(-mineSpeed * Gdx.graphics.getDeltaTime());
            if (targetedBlock.getHealth() <= 0.0f) {
                if (targetedBlock.getColor().equals(Color.YELLOW)) {
                    mineSpeed += 5.0f;
                } else {
                    mineSpeed += targetedBlock.getBaseHealth() / 10.0f;
                }
            }
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

    private void handleMovement(float acceleration, boolean isGrounded) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            body.applyForceToCenter(new Vector2(-acceleration, 0), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            body.applyForceToCenter(new Vector2(acceleration, 0), true);
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

    private Block getTargetedBlock(float maxDistance) {
        Vector2 playerPos = body.getPosition();
        Vector2 mouseWorldPos = getMouseWorldPosition();
        return performRayCast(playerPos, calculateRayEnd(playerPos, mouseWorldPos, maxDistance));
    }

    private Vector2 getMouseWorldPosition() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        Main.viewport.unproject(mousePos);
        return new Vector2(mousePos.x, mousePos.y);
    }

    private Vector2 calculateRayEnd(Vector2 p1, Vector2 p2, float maxDistance) {
        Vector2 direction = p2.cpy().sub(p1).nor();
        float effectiveLength = Math.min(maxDistance, p1.dst(p2));
        return p1.cpy().add(direction.scl(effectiveLength));
    }

    private Block performRayCast(Vector2 start, Vector2 end) {
        final Block[] hitBlock = {null};
        final float[] closestFraction = {1.0f};

        Main.physicsWorld.rayCast((fixture, point, normal, fraction) -> {
            if (fixture.getBody() == body) {
                return -1.0f;
            }
            Object userData = fixture.getBody().getUserData();
            if (userData instanceof Block) {
                if (fraction < closestFraction[0]) {
                    closestFraction[0] = fraction;
                    hitBlock[0] = (Block) userData;
                }
            }
            return 1.0f;
        }, start, end);

        return hitBlock[0];
    }

    public void update() {
        if (health <= 0.0f) {
            body.setActive(false);
        }
        if (useFollowCam) {
            followCam();
        }

        float curYVelocity = body.getLinearVelocity().y;
        isGrounded = false;
        if (Math.abs(curYVelocity) < 0.01f && lastYVelocity <= 0) {
            isGrounded = true;
            if (lastYVelocity < -10.0f) {
                health += lastYVelocity;
            }
        }
        lastYVelocity = curYVelocity;
    }


    private void followCam() {
        if (Main.camera.position.y > body.getPosition().y) {
            Main.camera.position.y = body.getPosition().y;
            Main.camera.update();
        }
    }

    public void draw() {
        drawPlayer();
        if (targetedBlock != null && targetedBlock.getColor() != null) {
            outlineTargetedBlock();
        }
    }

    private void drawPlayer() {
        Vector2 position = body.getPosition();
        float x = position.x - width / 2;
        float y = position.y - height / 2;

        Main.shapeRenderer.setColor(getAdjustedColor(Color.TEAL, 20));
        Main.shapeRenderer.rect(x, y, width, height);
    }

    private Color getAdjustedColor(Color originalColor, int steps) {
        float stepSize = 1.0f / steps;
        float darknessFactor = Math.max(0, Math.min(1, Math.round(health / baseHealth / stepSize) * stepSize));
        return new Color(originalColor.r * darknessFactor, originalColor.g * darknessFactor, originalColor.b * darknessFactor, originalColor.a);
    }

    private void outlineTargetedBlock() {
        float outlineWidth = Main.terrain.getOutlineWidth();
        targetedBlock.draw(outlineWidth, Color.WHITE, 0);
        targetedBlock.draw(outlineWidth * 2.0f);
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public float getHeath() {
        return health;
    }
}
