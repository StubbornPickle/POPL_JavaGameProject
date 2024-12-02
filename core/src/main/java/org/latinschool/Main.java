package org.latinschool;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Main extends ApplicationAdapter {
    public static Camera camera;
    public static Viewport viewport;
    public static ProceduralTerrain terrain;
    public static ShapeRenderer shapeRenderer;
    public static World physicsWorld;
    public static Player player;

    private Box2DDebugRenderer box2DDebugRenderer;
    private Body bounds;

    @Override
    public void create() {
        initGlobals();
        createBounds();
    }

    private void initGlobals() {
        initRendering(10.0f, 10.0f);
        initPhysics();
        initTerrain();
        initPlayer();
    }

    private void initRendering(float worldWidth, float worldHeight) {
        camera = new OrthographicCamera();
        camera.position.set(worldWidth / 2, worldHeight / 2, 0);
        camera.update();

        viewport = new FitViewport(worldWidth, worldHeight, camera);
        viewport.apply();

        shapeRenderer = new ShapeRenderer();
    }

    private void initPhysics() {
        physicsWorld = new World(new Vector2(0.0f, -9.8f), true);
        box2DDebugRenderer = new Box2DDebugRenderer();
    }

    private void createBounds() {
        BodyDef bodyDef = Box2DUtils.createBodyDef(new Vector2(camera.position.x, camera.position.y), BodyDef.BodyType.StaticBody);
        bounds = physicsWorld.createBody(bodyDef);

        float halfWidth = camera.viewportWidth / 2;

        createBoundFixture(-halfWidth - 10.0f, 0.0f, 20.0f, camera.viewportHeight * 2); // Left
        createBoundFixture(halfWidth + 10.0f, 0.0f, 20.0f, camera.viewportHeight * 2); // Right
        createBoundFixture(0.0f, -halfWidth - 10.0f, camera.viewportWidth * 2, 20.0f); // Bottom
        createBoundFixture(0.0f, halfWidth + 10.0f, camera.viewportWidth * 2, 20.0f); // Top
    }

    private void createBoundFixture(float x, float y, float width, float height) {
        PolygonShape shape = Box2DUtils.createRectangleShape(width, height, new Vector2(x, y), 0.0f);
        FixtureDef fixtureDef = Box2DUtils.createFixtureDef(shape, 0.0f, 0.0f, 0.0f);
        bounds.createFixture(fixtureDef);

        shape.dispose();
    }

    private void initTerrain() {
        terrain = new ProceduralTerrain(
            new Vector2(0, camera.position.y),
            15,
            0.025f,
            new Color[]{Color.GREEN, Color.BROWN, Color.GRAY, Color.DARK_GRAY}, // Layers
            new int[]{0, 1, 5, 20}, // Layer thresholds
            new float[]{10.0f, 10.0f, 25.0f, 50.0f}, // Layer healths
            new Color[]{Color.GRAY, Color.DARK_GRAY}, // Cave layers
            0.5f,
            0.1f,
            new Random().nextLong()
        );
    }

    private void initPlayer() {
        player = new Player(
            new Vector2(camera.viewportWidth / 2, camera.viewportHeight * 0.9f),
            0.225f, 0.91f, 1.5f, 2.0f, 1.0f,
            1.5f, 0.5f,
            true, 10.0f, 10.0f
        );
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        player.input();
    }

    private void logic() {
        viewport.apply();
        physicsWorld.step(1f / 60, 6, 2);
        terrain.update();
        player.update();
        bounds.setTransform(bounds.getPosition().x, camera.position.y, bounds.getAngle());
    }

    private void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        clearViewport(new Color(0.15f, 0.15f, 0.2f, 1f));
        terrain.draw();
        player.draw();
        shapeRenderer.end();
    }


    private void clearViewport(Color color) {
        float halfWidth = camera.viewportWidth / 2;
        float halfHeight = camera.viewportHeight / 2;
        float x = camera.position.x - halfWidth;
        float y = camera.position.y - halfHeight;
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, camera.viewportWidth, camera.viewportHeight);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        physicsWorld.dispose();
    }
}
