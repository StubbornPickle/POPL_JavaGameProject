package org.latinschool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.math.MathUtils.ceil;

public class ProceduralTerrain {
    private float outlineWidth;
    private Color[] layers;
    private int[] layerThresholds;
    private float[] layerHealths;
    private Color[] caveLayers;
    private float caveThreshold;
    private float caveScale;
    private long caveSeed;
    private final float baseBlockSize;
    private final float minOreDistance;

    private Block[][] blocks; // [row][col]
    private final List<Vector2> orePositions = new ArrayList<>();
    private int depth = 0;

    public ProceduralTerrain(Vector2 position, int resolution, float outlineWidth, Color[] layers,
                             int[] layerThresholds, float[] layerHealths, Color[] caveLayers,
                             float caveThreshold, float caveScale, long caveSeed) {
        this.outlineWidth = outlineWidth;
        this.layers = layers;
        this.layerThresholds = layerThresholds;
        this.layerHealths = layerHealths;
        this.caveLayers = caveLayers;
        this.caveThreshold = caveThreshold;
        this.caveScale = caveScale;
        this.caveSeed = caveSeed;
        this.baseBlockSize = Main.camera.viewportWidth / resolution;
        this.minOreDistance = baseBlockSize * 10;

        initBlocks(position, resolution);
    }

    private void initBlocks(Vector2 position, int resolution) {
        int rows = ceil(Main.camera.viewportHeight / baseBlockSize) + 1;

        blocks = new Block[rows][resolution];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < resolution; col++) {
                int layerIndex = getLayerIndex(row);
                float x = position.x + (col + 0.5f) * baseBlockSize;
                float y = position.y - (row + 0.5f) * baseBlockSize;
                blocks[row][col] = initBlock(x, y, layerIndex);
            }
        }
    }

    private Block initBlock(float x, float y, int layerIndex) {
        Vector2 position = new Vector2(x, y);
        Color color = calculateColor(new Vector2(x, y), layerIndex);
        float health = layerHealths[layerIndex];

        return new Block(position, color, baseBlockSize, health);
    }

    public void update() {
        if (shouldCycle()) {
            cycleRow();
        }
        System.out.println("Score: " + depth);
    }

    private boolean shouldCycle() {
        float terrainTop = blocks[0][0].getPosition().y + baseBlockSize / 2;
        float viewportTop = Main.camera.position.y + Main.camera.viewportHeight / 2;
        return terrainTop - baseBlockSize >= viewportTop;
    }

    private void cycleRow() {
        depth++;

        float bottomRowY = blocks[blocks.length - 1][0].getPosition().y;
        float newRowY = bottomRowY - baseBlockSize;

        pruneOres(blocks[0][0].getPosition().y);

        for (Block block : blocks[0]) {
            int layerIndex = getLayerIndex(blocks.length + depth);

            block.setPosition(block.getPosition().x, newRowY);
            block.setColor(calculateColor(block.getPosition(), layerIndex));
            block.setBaseHealth(layerHealths[layerIndex]);
        }

        // Shift up
        Block[] topRow = blocks[0];
        System.arraycopy(blocks, 1, blocks, 0, blocks.length - 1);
        blocks[blocks.length - 1] = topRow;
    }

    private void pruneOres(float y) {
        orePositions.removeIf(position -> position.y >= y);
    }


    private Color calculateColor(Vector2 position, int layerIndex) {
        if (isOre(position, layerIndex)) {
            orePositions.add(position);
            return Color.YELLOW; // Mark block as an ore
        }


        return layers[layerIndex]; // Default layer color
    }


    private boolean isOre(Vector2 position, int layerIndex) {
        for (Vector2 orePosition : orePositions) {
            if (orePosition.dst(position) < minOreDistance) {
                return false;
            }
        }

        if (isInCave(position, layers[layerIndex])) {
            return false;
        }

        float oreProbability = 0.25f;
        return Math.random() < oreProbability; // Mark this block as an ore
    }


    private int getLayerIndex(int depth) {
        for (int i = 0; i < layers.length; i++) {
            int threshold = layerThresholds[i];
            int nextThreshold = (i + 1 < layerThresholds.length) ? layerThresholds[i + 1] : Integer.MAX_VALUE;
            if (depth >= threshold && depth < nextThreshold) {
                if (i <= 1) { // No randomness for first 2 layers
                    return i;
                }
                int transitionRange = Math.max(3, Math.min(10, (nextThreshold - threshold) / 2));
                int transitionDepth = depth - threshold;

                float probability = (float) transitionDepth / transitionRange;
                return Math.random() < probability ? i: i - 1;
            }
        }
        return -1;
    }

    public void draw() {
        for (Block[] row : blocks) {
            for (Block block : row) {
                if (shouldRender(block)) {
                    Body body = block.getBody();
                    if (!body.isActive()) {
                        body.setActive(true);
                    }
                    block.draw(outlineWidth);
                } else {
                    Body body = block.getBody();
                    if (body.isActive()) {
                        body.setActive(false);
                    }
                }
            }
        }
    }

    private boolean shouldRender(Block block) {
        if (block.getHealth() > 0.0f && block.getColor() != null && !isInCave(block.getPosition(), block.getColor())) {
            return true;
        }
        return false;
    }

    private boolean isInCave(Vector2 position, Color color) {
        for (Color caveLayer : caveLayers) {
            if (color.equals(caveLayer)) {
                float noiseValue = Noise.noise2(caveSeed, position.x * caveScale, position.y * caveScale);
                return noiseValue > caveThreshold;
            }
        }
        return false;
    }

    public float getOutlineWidth() {
        return outlineWidth;
    }
}
