package com.segilmez.game3072;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;

public class Tile {
    private int value;
    private int gridX, gridY;
    private float x, y;
    private float targetX, targetY;
    private float startX, startY;
    private float width, height;
    private Color backgroundColor;
    private boolean merged;

    private static final float CORNER_RADIUS = 6f;
    private static BitmapFont tileFont;
    private static boolean fontInitialized = false;

    private boolean animating = false;
    private float animationTime = 0f;
    private static final float ANIMATION_DURATION = 0.2f;

    private static final String[] TILE_COLOR_HEX = {
        "#F5F5F5", // 0 or empty
        "#EEE4DA", // 2
        "#EDE0C8", // 4
        "#F2B179", // 8
        "#F59563", // 16
        "#F67C5F", // 32
        "#F65E3B", // 64
        "#EDCF72", // 128
        "#EDCC61", // 256
        "#EDC850", // 512
        "#EDC53F", // 1024
        "#EDC22E", // 2048
        "#3C3A32"  // 4096+
    };

    public Tile(int value, int gridX, int gridY, float x, float y, float width, float height) {
        this.value = value;
        this.gridX = gridX;
        this.gridY = gridY;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.startX = x;
        this.startY = y;
        this.width = width;
        this.height = height;
        this.merged = false;
        updateColor();

        if (!fontInitialized) {
            initializeFont();
        }
    }

    private static void initializeFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("Orbitron/static/Orbitron-Regular.ttf"));

        FreeTypeFontParameter parameter = GameUtils.createFontParameters(
            20,
            Color.WHITE,
            1,
            new Color(0.2f, 0.2f, 0.2f, 0.3f)
        );

        tileFont = generator.generateFont(parameter);
        tileFont.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
        generator.dispose();
        fontInitialized = true;
    }

    public void setValue(int value) {
        this.value = value;
        updateColor();
    }

    public int getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == 0;
    }

    public void setGridPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setTargetPosition(float targetX, float targetY) {
        this.startX = this.x;
        this.startY = this.y;
        this.targetX = targetX;
        this.targetY = targetY;
        this.animating = true;
        this.animationTime = 0f;
    }

    public boolean isAnimating() {
        return animating;
    }

    public void update(float delta) {
        if (animating) {
            animationTime += delta;

            if (animationTime >= ANIMATION_DURATION) {
                x = targetX;
                y = targetY;
                animating = false;
            } else {
                float progress = animationTime / ANIMATION_DURATION;
                float interpolatedProgress = Interpolation.smooth.apply(progress);
                x = startX + (targetX - startX) * interpolatedProgress;
                y = startY + (targetY - startY) * interpolatedProgress;
            }
        }
    }

    private void updateColor() {
        int colorIndex = 0;
        if (value > 0) {
            colorIndex = (int) (Math.log(value) / Math.log(2));
        }
        colorIndex = Math.min(colorIndex, TILE_COLOR_HEX.length - 1);
        backgroundColor = GameUtils.hexToColor(TILE_COLOR_HEX[colorIndex]);

        // For higher numbers (2048+), add a subtle glow effect
        if (value >= 2048) {
            backgroundColor.r = Math.min(1.0f, backgroundColor.r * 1.05f);
            backgroundColor.g = Math.min(1.0f, backgroundColor.g * 1.05f);
            backgroundColor.b = Math.min(1.0f, backgroundColor.b * 1.05f);
        }
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        if (value <= 0) return;

        // Draw background
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(backgroundColor);
        GameUtils.drawRoundedRect(shapeRenderer, x, y, width, height, CORNER_RADIUS);
        shapeRenderer.end();

        // Draw value
        batch.begin();
        String text = String.valueOf(value);

        // Adjust font scale based on value length
        float scale = 1.0f;
        if (text.length() > 3) {
            scale = Math.max(0.5f, 1.0f - (text.length() - 3) * 0.2f);
        }
        tileFont.getData().setScale(scale);

        // Choose text color based on tile value
        if (value <= 4) {
            tileFont.setColor(GameUtils.hexToColor("#776E65")); // Dark for light tiles
        } else {
            tileFont.setColor(GameUtils.hexToColor("#F9F6F2")); // Light for dark tiles
        }

        // Center the text
        GlyphLayout layout = new GlyphLayout(tileFont, text);
        float textX = x + (width - layout.width) / 2;
        float textY = y + (height + layout.height) / 2;

        tileFont.draw(batch, text, textX, textY);
        batch.end();
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public boolean isMerged() {
        return merged;
    }

    public static void dispose() {
        if (tileFont != null) {
            tileFont.dispose();
        }
    }
}
