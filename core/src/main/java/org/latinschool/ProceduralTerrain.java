package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import static com.badlogic.gdx.math.MathUtils.ceil;

public class ProceduralTerrain {
    private final int resolution; // Blocks per row
    private final Color[] layers; // Descending layer colors
    private final float[] thresholds; // Thresholds for layers
    private final Color[] caveLayers; // Layers given caves
    private final float caveThreshold; // -1 to 1 value
    private final float caveScale; // Cave noise scale
    private final long seed; // Cave noise seed

    private final float blockSize;
    private float depth = 0; // Cumulative number of rows down
    private Block[][] blocks; // [rows][cols]

    public ProceduralTerrain(float height, int resolution, Color[] layers, float[] thresholds, Color[] caveLayers, float caveThreshold, float caveScale, long seed) {
        this.resolution = resolution;
        this.layers = layers;
        this.thresholds = thresholds;
        this.caveLayers = caveLayers;
        this.caveThreshold = caveThreshold;
        this.caveScale = caveScale;
        this.seed = seed;
        blockSize = Main.camera.viewportWidth / resolution;

        init(height);
    }

    private void init(float height) {
        // +1 to account for cycling
        int rows = ceil(Main.camera.viewportHeight / blockSize) + 1;

        blocks = new Block[rows][resolution];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < resolution; col++) {
                float x = col * blockSize;
                float y = height - (row * blockSize);

                Color blockColor = getDepthColor(row);
                float scaleFactor = .9f; // Creates outline effect
                Block block = new Block(x, y, blockSize * scaleFactor, blockColor);
                blocks[row][col] = block;
            }
        }
    }

    private Color getDepthColor(float depth) {
        // Set transition range min/max
        // Convert to attributes?
        int minTransitionRange = 3;
        int maxTransitionRange = 10;

        for (int i = 0; i < layers.length; i++) {
            float threshold = thresholds[i];
            float nextThreshold = (i + 1 < thresholds.length) ? thresholds[i + 1] : Float.MAX_VALUE;

            if (depth >= threshold && depth < nextThreshold) {
                if (i == 0 || i == 1) { // Skip transition for first 2 layers
                    return layers[i];
                }

                // Clamp transition range between minRange and maxRange
                int transitionRange = Math.max(minTransitionRange, Math.min(maxTransitionRange, (int) ((nextThreshold - thresholds[i]) / 2)));

                // Calculate this depths probability
                float transitionDepth = depth - thresholds[i];
                float probability = transitionDepth / transitionRange;

                // Based on probability return either new color or old color
                // Once transition complete probability grows greater than 1 forcing only new color
                return Math.random() < probability ? layers[i] : layers[i - 1];
            }
        }

        // Default to purple
        return Color.PURPLE;
    }

    // Draws and handles physics on each block in the terrain
    public void draw() {
        for (Block[] row: blocks) {
            for (Block block : row) {
                if (shouldRender(block)) {
                    Body blockBody = block.getBody(); // Get physics body
                    // Is it not active b/c it should be as shouldRender passed
                    if (!blockBody.isActive()) { // If not active set to active
                        block.getBody().setActive(true);
                    }
                    block.draw(); // Draw block
                } else {
                    // Dont draw and deactivate physics body
                    block.getBody().setActive(false);
                }
            }
        }
    }

    // Check if a block is within a cave
    private boolean shouldRender(Block block) {
        if (block.getColor().equals(Color.WHITE)) {
            return false;
        }

        Color blockColor = block.getColor();

        // Check if block could be within a cave
        for (int i = 0; i < caveLayers.length; i++) {
            if (blockColor.equals(caveLayers[i])) {
                Vector2 position = block.getPosition();
                float noiseValue = Noise.noise2(seed, position.x * caveScale, position.y * caveScale);

                // -1 to 1 value
                if (noiseValue >= caveThreshold) {
                    return false;
                }

                return true; // Block is outside a cave and should render
            }
        }

        // Should never reach here but if it does say should render
        return true;
    }

    public void update() {
        if (shouldCycle()) {
            cycle();
        }
    }

    private boolean shouldCycle() {
        float terrainHeight = blocks[0][0].getPosition().y; // World space
        float viewportHeight = Main.camera.position.y + Main.camera.viewportHeight / 2; // World space

        // Cycle when top row is FULLY out of view
        return terrainHeight - blockSize >= viewportHeight;
    }

    // Moves top row of terrain to bottom
    private void cycle() {
        depth++; // Increment depth (rows down)

        float terrainDepth = blocks[blocks.length - 1][0].getPosition().y;
        float nextDepth = terrainDepth - blockSize; // World space

        // Generate data for the next row, which will be stored in the top row temporarily
        for (Block block : blocks[0]) {
            // Update the position and color of each block
            float newX = block.getPosition().x;
            float newY = nextDepth;

            block.setPosition(newX, newY); // Update the block's position in world space
            Color blockColor = getDepthColor(blocks.length + depth); // Get color based on depth
            block.setColor(blockColor); // Update the block color
        }

        // Store the top row
        Block[] topRow = blocks[0];
        // Shift all rows in the array up by one, cutting off the top row
        System.arraycopy(blocks, 1, blocks, 0, blocks.length - 1);
        // Set the top row holding the next row's data as the bottom row
        blocks[blocks.length - 1] = topRow;
        // Row cycling complete
    }
}
