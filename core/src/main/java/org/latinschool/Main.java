package org.latinschool;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;

import java.util.Random;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    // Global variables
    public static World world;
    public static Camera camera;
    public static Viewport viewport;
    public static ShapeRenderer shape;

    // Constants
    private static final float WORLD_WIDTH = 10; // Meters
    private static final float WORLD_HEIGHT = 10; // Meters

    private ProceduralTerrain pTerrain;
    private Player player;
    private Box2DDebugRenderer debugRenderer;


    @Override
    public void create() {
        initGlobals();
        initViewport();
        initTerrain();
        initMisc();
    }

    private void initGlobals() {
        // World
        world = new World(new Vector2(0, -9.8f), true);

        // Camera
        camera = new OrthographicCamera();

        // Shape
        shape = new ShapeRenderer();
    }

    private void initViewport() {
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

    }

    private void initTerrain() {
        pTerrain = new ProceduralTerrain(
            WORLD_HEIGHT / 2,
            15,
            new Color[]{Color.GREEN, Color.BROWN, Color.GRAY, Color.DARK_GRAY}, // Layers
            new float[]{0, 1, 5, 25}, // Layer thresholds
            new Color[]{Color.GRAY, Color.DARK_GRAY}, // Cave layers
            .5f, // Cave threshold
            0.1f, // Cave scale
            new Random().nextLong() // Seed
        );
    }

    private void initMisc() {
        player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT * .9f,.225f, .91f, 2.25f);
        camera.position.set(WORLD_WIDTH / 2,WORLD_HEIGHT * .9f , 0);
        camera.update();
        debugRenderer = new Box2DDebugRenderer();
    }

    // Main loop
    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        player.input(); // Player input
    }

    private void logic() {
        viewport.apply();
        pTerrain.update();
        world.step(1 / 60f, 6, 2);
        player.update();
    }

    private void draw() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        drawBackground();
        pTerrain.draw();
        player.draw();

        shape.end();

        // debugRenderer.render(world, camera.combined);
    }

    private void drawBackground() {
        shape.setColor(0.15f, 0.15f, 0.2f, 1f);
        float backgroundY = camera.position.y - camera.viewportHeight / 2;
        shape.rect(0, backgroundY, camera.viewportWidth, camera.viewportHeight);
    }

    @Override
    public void dispose() {
        shape.dispose();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }
}
