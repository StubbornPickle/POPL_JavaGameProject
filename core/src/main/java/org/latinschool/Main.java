package org.latinschool;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Main extends ApplicationAdapter {
    public static Camera mainCamera;
    public static Viewport gameViewport;
    public static World physicsWorld;
    public static ProceduralTerrain terrain;
    public static GlobalContactListener contactListener;
    public static ShapeRenderer shapeRenderer;

    private static final float WORLD_WIDTH = 10.0f;
    private static final float WORLD_HEIGHT = 10.0f;
    private static final Vector2 GRAVITY = new Vector2(0.0f, -9.8f);
    private static final Color BACKGROUND_COLOR = new Color(0.15f, 0.15f, 0.2f, 1f);

    private Player player;
    private Box2DDebugRenderer box2DDebugRenderer;

    private BitmapFont font;
    private SpriteBatch spriteBatch;

    @Override
    public void create() {
        initGlobals();
        initPlayer();

        box2DDebugRenderer = new Box2DDebugRenderer();

        // Initialize font and sprite batch for HUD
        font = new BitmapFont(); // Default font
        font.setColor(Color.WHITE);
        font.getData().setScale(2.0f); // Scale up the font size (2x)
        spriteBatch = new SpriteBatch();
    }

    private void initGlobals() {
        mainCamera = new OrthographicCamera();
        mainCamera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        mainCamera.update();

        gameViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, mainCamera);
        gameViewport.apply();

        physicsWorld = new World(GRAVITY, false);
        contactListener = new GlobalContactListener();
        physicsWorld.setContactListener(contactListener);

        shapeRenderer = new ShapeRenderer();

        initTerrain();
    }

    private void initTerrain() {
        terrain = new ProceduralTerrain(
            WORLD_HEIGHT / 2,
            15,
            0.025f,
            new Color[]{Color.GREEN, Color.BROWN, Color.GRAY, Color.DARK_GRAY},
            new float[]{10.0f, 10.0f, 20.0f, 40.0f},
            new int[]{0, 1, 10, 25},
            new Color[]{Color.GRAY, Color.DARK_GRAY},
            0.5f,
            0.1f,
            new Random().nextLong()
        );
    }

    private void initPlayer() {
        player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT * 0.9f,
            0.225f, 0.91f, 1.5f, 2.0f, 1.0f,
            1.5f, 3.0f, 1.5f, 0.5f,
            true);
    }

    @Override
    public void render() {
        gameViewport.apply();
        physicsWorld.step(Gdx.graphics.getDeltaTime(), 6, 2);

        input();
        logic();
        draw();
        drawHUD();
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

        // box2DDebugRenderer.render(physicsWorld, mainCamera.combined);
    }

    private void drawHUD() {
        spriteBatch.begin(); // Ensure spriteBatch has been started

        CharSequence charSequence = String.valueOf("Depth: " + -Math.round(((WORLD_HEIGHT / 2) - (mainCamera.position.y - player.getHeight() / 2)) / terrain.getBlockSize()));

        font.draw(spriteBatch, charSequence, 10, 480 - 10);

        spriteBatch.end(); // End spriteBatch rendering

    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        physicsWorld.dispose();
        shapeRenderer.dispose();
        font.dispose();
        spriteBatch.dispose();
    }
}
