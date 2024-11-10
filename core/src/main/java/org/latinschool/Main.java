package org.latinschool;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
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
    public static final World world = new World(new Vector2(0, -9.8f), true);
    public static final Camera camera = new OrthographicCamera();

    private static final float WORLD_WIDTH = 10; // Meters
    private static final float WORLD_HEIGHT = 10; // Meters

    private Viewport viewport;
    private ShapeRenderer shape;
    private ProceduralTerrain pTerrain;
    private Box2DDebugRenderer debugRenderer;


    @Override
    public void create() {
        initCamera();
        initViewport();
        initTerrain();
        initMisc();
    }

    private void initCamera() {
        camera.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT / 2, 0);
        camera.update();
    }

    private void initViewport() {
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();

    }

    private void initTerrain() {
        pTerrain = new ProceduralTerrain(
            WORLD_HEIGHT / 2,
            15,
            new Color[]{Color.GREEN, Color.BROWN, Color.GRAY, Color.DARK_GRAY},
            new float[]{0, 1, 5, 25},
            new Color[]{Color.GRAY, Color.DARK_GRAY},
            .5f,
            0.1f,
            new Random().nextLong()
        );
    }

    private void initMisc() {
        shape = new ShapeRenderer();
        debugRenderer = new Box2DDebugRenderer();
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input() {
        // TEMP INPUT
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            float deltaTime = Gdx.graphics.getDeltaTime();
            camera.position.add(0, -10.0f * deltaTime, 0);
            camera.update();
        }
    }

    private void logic() {
        viewport.apply();
        world.step(1 / 60f, 6, 2);
        pTerrain.update();
    }

    private void draw() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        drawBackground();
        pTerrain.draw(shape);

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
