package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;

import java.util.Random;

public class Enemy {
    private final Body body;
    private final float size;

    public Enemy(float x, float y, float size) {
        this.size = size;

        body = Box2DUtils.createBoxBody(Main.physicsWorld, BodyDef.BodyType.DynamicBody, new Vector2(x, y), size, size, 1.0f, 0.2f, 0.0f);

        startRandomMovement();
    }

    private void startRandomMovement() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                float randomX = MathUtils.random(-0.5f, 0.5f);
                float randomY = MathUtils.random(-0.5f, 0.5f);
                body.applyLinearImpulse(randomX, randomY, body.getWorldCenter().x, body.getWorldCenter().y, true);
            }
        }, 0, new Random().nextFloat(3)); // Randomly move every 2 seconds
    }

    public void render() {
        ShapeRenderer shapeRenderer = Main.shapeRenderer;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(
            body.getPosition().x - size / 2,
            body.getPosition().y - size / 2,
            size, size
        );
    }
}
