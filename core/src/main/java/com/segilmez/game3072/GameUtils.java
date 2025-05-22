package com.segilmez.game3072;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GameUtils {

    /**
     * Draws a rounded rectangle using ShapeRenderer
     */
    public static void drawRoundedRect(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        // Limit the radius to half the smallest dimension
        radius = Math.min(radius, Math.min(width / 2, height / 2));

        // Draw the main rectangle
        renderer.rect(x + radius, y, width - 2 * radius, height);
        renderer.rect(x, y + radius, width, height - 2 * radius);

        // Draw the four corner circles
        float segmentCount = 20; // More segments = smoother circles

        // Top-left corner
        renderer.arc(x + radius, y + height - radius, radius, 90, 90, (int) segmentCount);

        // Top-right corner
        renderer.arc(x + width - radius, y + height - radius, radius, 0, 90, (int) segmentCount);

        // Bottom-right corner
        renderer.arc(x + width - radius, y + radius, radius, 270, 90, (int) segmentCount);

        // Bottom-left corner
        renderer.arc(x + radius, y + radius, radius, 180, 90, (int) segmentCount);
    }

    /**
     * Creates font parameters with standard game styling
     */
    public static FreeTypeFontParameter createFontParameters(int size, Color color, float borderWidth, Color borderColor) {
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.borderWidth = borderWidth;
        parameter.borderColor = borderColor;
        return parameter;
    }

    /**
     * Converts hex color to libGDX Color
     */
    public static Color hexToColor(String hex) {
        return Color.valueOf(hex);
    }

}
