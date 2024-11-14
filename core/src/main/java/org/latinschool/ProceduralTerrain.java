package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.util.Arrays;

import static com.badlogic.gdx.math.MathUtils.ceil;

public class ProceduralTerrain {
    private final float blockOutlineWidth;
    private final Color[] layers;
    private final float[] layerHealths;
    private final int[] layerThresholds;
    private final Color[] caveLayers;
    private final float caveRenderThreshold;
    private final float caveNoiseScale;
    private final long noiseSeed;
    private final float blockSize;

    private int depth; // Tracks cumulative number of rows down
    private Block[][] blocks;

    public ProceduralTerrain(float startHeight, int blocksPerRow, float blockOutlineWidth,
                             Color[] layers, float[] layerHealths, int[] layerThresholds, Color[] caveLayers,
                             float caveRenderThreshold, float caveNoiseScale, long noiseSeed) {
        this.blockOutlineWidth = blockOutlineWidth;
        this.layers = layers;
        this.layerThresholds = layerThresholds;
        this.caveLayers = caveLayers;
        this.layerHealths = layerHealths;
        this.caveRenderThreshold = caveRenderThreshold;
        this.caveNoiseScale = caveNoiseScale;
        this.noiseSeed = noiseSeed;
        this.blockSize = Main.mainCamera.viewportWidth / blocksPerRow;

        initializeBlocks(startHeight, blocksPerRow);
    }

    private void initializeBlocks(float startHeight, int blocksPerRow) {
        int rows = ceil(Main.mainCamera.viewportHeight / blockSize) + 1; // +1 to allow for cycling

        blocks = new Block[rows][blocksPerRow];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < blocksPerRow; col++) {
                float x = col * blockSize;
                float y = startHeight - (row * blockSize);

                Color color = getDepthColor(row);
                float health;
                if (color.equals(Color.YELLOW)) {
                    health = 50.0f;
                } else {
                    health = layerHealths[Arrays.asList(layers).indexOf(color)];
                }
                Block block = new Block(x, y, blockSize, color, health);
                blocks[row][col] = block;
            }
        }
    }

    private Color getDepthColor(int depth) {
        if (depth > layerThresholds[2]) {
            float maxProbability = 0.025f; // 2.5% max probability
            if (Math.random() < maxProbability) {
                return Color.YELLOW; // Gold block color\
            }
        }

        // Default color selection logic
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

                // Base layer transition logic
                return Math.random() < probability ? layers[i] : layers[i - 1];
            }
        }

        return Color.PURPLE; // Default color if no layer matches
    }


    public void update() {
        if (needsCycling()) {
            cycleRows();
        }
    }

    private boolean needsCycling() {
        float topRowY = blocks[0][0].getPosition().y;
        float viewportTop = Main.mainCamera.position.y + Main.mainCamera.viewportHeight / 2;
        return topRowY - blockSize >= viewportTop;
    }

    private void cycleRows() {
        depth++;

        float lowestY = blocks[blocks.length - 1][0].getPosition().y;
        float newY = lowestY - blockSize;

        for (Block block : blocks[0]) {
            block.setPosition(block.getPosition().x, newY);
            Color color = getDepthColor(blocks.length + depth);
            block.setColor(color);
            float health;
            if (color.equals(Color.YELLOW)) {
                health = 50.0f;
            } else {
                health = layerHealths[Arrays.asList(layers).indexOf(color)];
            }
            block.setBaseHealth(health);
            block.setHealth(health);
        }

        Block[] topRow = blocks[0];
        System.arraycopy(blocks, 1, blocks, 0, blocks.length - 1);
        blocks[blocks.length - 1] = topRow;
    }

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

    private boolean shouldRenderBlock(Block block) {
        if (block.getHealth() <= 0.0f) {
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

    public float getBlockOutlineWidth() {
        return blockOutlineWidth;
    }

    public float getDepth() {
        return depth;
    }

    public Block[][] getBlocks() {
        return blocks;
    }

    public float getBlockSize() {
        return blockSize;
    }
}
