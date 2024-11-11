package org.latinschool;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Main extends ApplicationAdapter {

    // Global Variables
    public static Camera mainCamera;
    public static Viewport gameViewport;
    public static World physicsWorld;
    public static ShapeRenderer shapeRenderer;

    // Constants
    private static final float WORLD_WIDTH = 10.0f;
    private static final float WORLD_HEIGHT = 10.0f;
    private static final Vector2 GRAVITY = new Vector2(0.0f, -9.8f);
    private static final Color BACKGROUND_COLOR = new Color(0.15f, 0.15f, 0.2f, 1f);

    private ProceduralTerrain terrain;
    private Player player;

    @Override
    public void create() {
        initGlobals();
        initTerrain();
        initPlayer();
    }

    private void initGlobals() {
        mainCamera = new OrthographicCamera();
        mainCamera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        mainCamera.update();

        gameViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, mainCamera);
        gameViewport.apply();

        physicsWorld = new World(GRAVITY, true);
        shapeRenderer = new ShapeRenderer();
    }

    private void initTerrain() {
        terrain = new ProceduralTerrain(
            WORLD_HEIGHT / 2,
            15,
            0.025f,
            new Color[]{Color.GREEN, Color.BROWN, Color.GRAY, Color.DARK_GRAY},
            new int[]{0, 1, 10, 25},
            new Color[]{Color.GRAY, Color.DARK_GRAY},
            0.5f,
            0.1f,
            new Random().nextLong()
        );
    }

    private void initPlayer() {
        player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT * .9f, .225f, .91f, 2.25f);
    }

    @Override
    public void render() {
        gameViewport.apply();
        physicsWorld.step(Gdx.graphics.getDeltaTime(), 6, 2);

        input();
        logic();
        draw();
    }

    private void input() {
        player.input();
    }

    private void logic() {
        terrain.update();
        player.update();
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);

        shapeRenderer.setProjectionMatrix(mainCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Viewport background
        shapeRenderer.setColor(BACKGROUND_COLOR);
        shapeRenderer.rect(0, mainCamera.position.y - mainCamera.viewportHeight / 2, WORLD_WIDTH, WORLD_HEIGHT);

        terrain.draw();
        player.draw();

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        physicsWorld.dispose();
        shapeRenderer.dispose();
    }
}
