package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import static com.badlogic.gdx.math.MathUtils.ceil;

public class ProceduralTerrain {
    // Terrain configuration
    private final float blockOutlineWidth;
    private final Color[] layers;
    private final int[] layerThresholds;
    private final Color[] caveLayers;
    private final float caveRenderThreshold;
    private final float caveNoiseScale;
    private final long noiseSeed;
    private final float blockSize;

    // Terrain state
    private int depth; // Tracks cumulative number of rows down
    private Block[][] blocks;

    /**
     * Initializes a ProceduralTerrain with specified properties.
     * @param startHeight Starting height of the terrain.
     * @param rowResolution Number of blocks per row.
     * @param blockOutlineWidth Outline width applied to the blocks
     * @param layers Array of descending layer colors.
     * @param layerThresholds Thresholds for layers based on depth in rows.
     * @param caveLayers Colors representing layers with caves.
     * @param caveRenderThreshold Noise threshold for caves, between -1 and 1.
     * @param caveNoiseScale Scale of the noise for cave generation.
     * @param noiseSeed Seed for noise generation.
     *
     */
    public ProceduralTerrain(float startHeight, int rowResolution, float blockOutlineWidth,
                             Color[] layers, int[] layerThresholds, Color[] caveLayers,
                             float caveRenderThreshold, float caveNoiseScale, long noiseSeed) {
        this.blockOutlineWidth = blockOutlineWidth;
        this.layers = layers;
        this.layerThresholds = layerThresholds;
        this.caveLayers = caveLayers;
        this.caveRenderThreshold = caveRenderThreshold;
        this.caveNoiseScale = caveNoiseScale;
        this.noiseSeed = noiseSeed;
        this.blockSize = Main.mainCamera.viewportWidth / rowResolution;

        initializeBlocks(startHeight, rowResolution);
    }

    /**
     * Initializes the block grid for the terrain.
     * @param startHeight Starting height of the terrain.
     * @param rowResolution Number of blocks per row.
     */
    private void initializeBlocks(float startHeight, int rowResolution) {
        int rows = ceil(Main.mainCamera.viewportHeight / blockSize) + 1; // +1 to allow for cycling

        blocks = new Block[rows][rowResolution];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < rowResolution; col++) {
                float x = col * blockSize;
                float y = startHeight - (row * blockSize);

                Block block = new Block(x, y, blockSize, getDepthColor(row));
                blocks[row][col] = block;
            }
        }
    }

    /**
     * Determines the color of a block based on its depth.
     * @param depth Depth of the block in rows.
     * @return The color of the block.
     */
    private Color getDepthColor(int depth) {
        for (int i = 0; i < layers.length; i++) {
            int threshold = layerThresholds[i];
            int nextThreshold = (i + 1 < layerThresholds.length) ? layerThresholds[i + 1] : Integer.MAX_VALUE;

            if (depth >= threshold && depth < nextThreshold) {
                if (i <= 1) { // No transition for the first two layers
                    return layers[i];
                }

                int transitionRange = Math.max(3, Math.min(10, (nextThreshold - threshold) / 2));
                int transitionDepth = depth - threshold;
                float probability = (float) transitionDepth / transitionRange;

                return Math.random() < probability ? layers[i] : layers[i - 1];
            }
        }
        return Color.PURPLE;
    }

    /**
     * Updates the terrain, cycling rows if necessary.
     */
    public void update() {
        if (needsCycling()) {
            cycleRows();
        }
    }

    /**
     * Determines if the terrain rows need to be cycled based on viewport position.
     * @return True if cycling is necessary, false otherwise.
     */
    private boolean needsCycling() {
        float topRowY = blocks[0][0].getPosition().y;
        float viewportTop = Main.mainCamera.position.y + Main.mainCamera.viewportHeight / 2;
        return topRowY - blockSize >= viewportTop;
    }

    /**
     * Moves the top row of terrain blocks to the bottom, adjusting colors and positions.
     */
    private void cycleRows() {
        depth++;

        float lowestY = blocks[blocks.length - 1][0].getPosition().y;
        float newY = lowestY - blockSize;

        for (Block block : blocks[0]) {
            block.setPosition(block.getPosition().x, newY);
            block.setColor(getDepthColor(blocks.length + depth));
        }

        Block[] topRow = blocks[0];
        System.arraycopy(blocks, 1, blocks, 0, blocks.length - 1);
        blocks[blocks.length - 1] = topRow;
    }

    /**
     * Renders the terrain blocks, activating or deactivating their physics bodies as needed.
     */
    public void draw() {
        for (Block[] row : blocks) {
            for (Block block : row) {
                Body body = block.getBody();
                if (shouldRenderBlock(block)) {
                    block.draw(blockOutlineWidth);
                    if (!body.isActive()) {
                        body.setActive(true);
                    }
                } else {
                    if (body.isActive()) {
                        body.setActive(false);
                    }
                }
            }
        }
    }

    /**
     * Determines if a block should be rendered based on cave noise and color.
     * @param block The block to check.
     * @return True if the block should render, false if it should be hidden.
     */
    private boolean shouldRenderBlock(Block block) {
        if (block.getColor() == null) {
            return false;
        }

        for (Color caveLayer : caveLayers) {
            if (block.getColor().equals(caveLayer)) {
                Vector2 position = block.getPosition();
                float noiseValue = Noise.noise2(noiseSeed, position.x * caveNoiseScale, position.y * caveNoiseScale);
                return noiseValue < caveRenderThreshold;
            }
        }

        return true;
    }
}
