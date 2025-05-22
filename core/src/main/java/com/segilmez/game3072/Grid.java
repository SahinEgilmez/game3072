package com.segilmez.game3072;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    private int size;
    private Tile[][] tiles;
    private float cellSize;
    private float padding;
    private float gridSize;
    private float startX, startY;

    private boolean animating = false;
    private List<Tile> animatingTiles = new ArrayList<>();
    private boolean shouldAddNewTile = false;

    private int[][] grid; // Actual game state values
    private int score = 0;
    private int lastMoveScore = 0;

    public Grid(int size, float gridSize, float x, float y) {
        this.size = size;
        this.gridSize = gridSize;
        this.cellSize = gridSize / size;
        this.padding = cellSize * 0.1f;
        this.startX = x;
        this.startY = y;

        initializeGrid();
        addInitialTiles();
    }

    private void initializeGrid() {
        // Initialize game grid values
        grid = new int[size][size];

        // Initialize visual tiles
        tiles = new Tile[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                float tileX = getTileX(col);
                float tileY = getTileY(row);
                float tileWidth = cellSize - 2 * padding;
                float tileHeight = cellSize - 2 * padding;

                tiles[row][col] = new Tile(0, col, row, tileX, tileY, tileWidth, tileHeight);
            }
        }

        score = 0;
        lastMoveScore = 0;
    }

    private void addInitialTiles() {
        addRandomTile();
        addRandomTile();
    }

    public int getSize() {
        return size;
    }

    public int getTileValue(int row, int col) {
        if (row >= 0 && row < size && col >= 0 && col < size) {
            return grid[row][col];
        }
        return 0;
    }

    public int getScore() {
        return score;
    }

    public int getLastMoveScore() {
        return lastMoveScore;
    }

    public void resetLastMoveScore() {
        lastMoveScore = 0;
    }

    private float getTileX(int col) {
        return startX + col * cellSize + padding;
    }

    private float getTileY(int row) {
        return startY + row * cellSize + padding;
    }

    public void update(float delta) {
        if (!animating) return;

        boolean stillAnimating = false;

        for (Tile tile : animatingTiles) {
            tile.update(delta);
            if (tile.isAnimating()) {
                stillAnimating = true;
            }
        }

        if (!stillAnimating) {
            animatingTiles.clear();
            updateTiles();
            animating = false;

            if (shouldAddNewTile) {
                addRandomTile();
                shouldAddNewTile = false;
            }
        }
    }

    public boolean isAnimating() {
        return animating;
    }

    public void render(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        renderGridBackground(shapeRenderer);
        renderGridCells(shapeRenderer);
        renderTiles(shapeRenderer, batch, font);
    }

    private void renderGridBackground(ShapeRenderer shapeRenderer) {
        float gridCornerRadius = 8f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(GameUtils.hexToColor("#BBADA0")); // Warm gray
        GameUtils.drawRoundedRect(shapeRenderer, startX, startY, gridSize, gridSize, gridCornerRadius);
        shapeRenderer.end();
    }

    private void renderGridCells(ShapeRenderer shapeRenderer) {
        float cellCornerRadius = 5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(GameUtils.hexToColor("#CDC1B4")); // Light warm gray

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                float tileX = getTileX(col);
                float tileY = getTileY(row);
                float tileWidth = cellSize - 2 * padding;
                float tileHeight = cellSize - 2 * padding;

                GameUtils.drawRoundedRect(shapeRenderer, tileX, tileY, tileWidth, tileHeight, cellCornerRadius);
            }
        }
        shapeRenderer.end();
    }

    private void renderTiles(ShapeRenderer shapeRenderer, SpriteBatch batch, BitmapFont font) {
        // Draw non-animating tiles
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (!tiles[row][col].isEmpty()) {
                    tiles[row][col].render(shapeRenderer, batch, font);
                }
            }
        }

        // Draw animating tiles on top
        for (Tile tile : animatingTiles) {
            if (!tile.isEmpty()) {
                tile.render(shapeRenderer, batch, font);
            }
        }
    }

    public void addRandomTile() {
        List<int[]> emptyCells = findEmptyCells();

        if (emptyCells.isEmpty()) {
            return;
        }

        int[] cell = emptyCells.get(MathUtils.random(emptyCells.size() - 1));
        int row = cell[0];
        int col = cell[1];

        int value = MathUtils.randomBoolean(0.9f) ? 2 : 4;
        grid[row][col] = value;
        tiles[row][col].setValue(value);
    }

    private List<int[]> findEmptyCells() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (grid[row][col] == 0) {
                    emptyCells.add(new int[]{row, col});
                }
            }
        }
        return emptyCells;
    }

    public boolean moveUp() {
        return move(Direction.UP);
    }

    public boolean moveDown() {
        return move(Direction.DOWN);
    }

    public boolean moveLeft() {
        return move(Direction.LEFT);
    }

    public boolean moveRight() {
        return move(Direction.RIGHT);
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private boolean move(Direction direction) {
        if (animating) return false;

        boolean moved = false;
        boolean[][] merged = new boolean[size][size];
        lastMoveScore = 0;

        int[][] newGrid = copyGrid();

        switch (direction) {
            case UP:
                moved = processUpMove(newGrid, merged);
                break;
            case DOWN:
                moved = processDownMove(newGrid, merged);
                break;
            case LEFT:
                moved = processLeftMove(newGrid, merged);
                break;
            case RIGHT:
                moved = processRightMove(newGrid, merged);
                break;
        }

        if (moved) {
            grid = newGrid;
            animating = true;
            shouldAddNewTile = true;
        }

        return moved;
    }

    private int[][] copyGrid() {
        int[][] newGrid = new int[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                newGrid[row][col] = grid[row][col];
            }
        }
        return newGrid;
    }

    private boolean processUpMove(int[][] newGrid, boolean[][] merged) {
        boolean moved = false;

        for (int col = 0; col < size; col++) {
            for (int row = size - 2; row >= 0; row--) {
                if (newGrid[row][col] == 0) continue;

                int currentRow = row;
                int value = newGrid[row][col];

                while (currentRow < size - 1) {
                    if (newGrid[currentRow + 1][col] == 0) {
                        // Move to empty cell
                        newGrid[currentRow + 1][col] = value;
                        newGrid[currentRow][col] = 0;
                        currentRow++;
                        moved = true;
                    } else if (newGrid[currentRow + 1][col] == value && !merged[currentRow + 1][col]) {
                        // Merge with matching cell
                        int mergedValue = value * 2;
                        newGrid[currentRow + 1][col] = mergedValue;
                        newGrid[currentRow][col] = 0;
                        merged[currentRow + 1][col] = true;
                        currentRow++;
                        moved = true;

                        // Update score
                        score += mergedValue;
                        lastMoveScore += mergedValue;
                        break;
                    } else {
                        break;
                    }
                }

                if (currentRow != row) {
                    createMoveAnimation(row, col, currentRow, col, value);
                }
            }
        }

        return moved;
    }

    private boolean processDownMove(int[][] newGrid, boolean[][] merged) {
        boolean moved = false;

        for (int col = 0; col < size; col++) {
            for (int row = 1; row < size; row++) {
                if (newGrid[row][col] == 0) continue;

                int currentRow = row;
                int value = newGrid[row][col];

                while (currentRow > 0) {
                    if (newGrid[currentRow - 1][col] == 0) {
                        newGrid[currentRow - 1][col] = value;
                        newGrid[currentRow][col] = 0;
                        currentRow--;
                        moved = true;
                    } else if (newGrid[currentRow - 1][col] == value && !merged[currentRow - 1][col]) {
                        int mergedValue = value * 2;
                        newGrid[currentRow - 1][col] = mergedValue;
                        newGrid[currentRow][col] = 0;
                        merged[currentRow - 1][col] = true;
                        currentRow--;
                        moved = true;

                        score += mergedValue;
                        lastMoveScore += mergedValue;
                        break;
                    } else {
                        break;
                    }
                }

                if (currentRow != row) {
                    createMoveAnimation(row, col, currentRow, col, value);
                }
            }
        }

        return moved;
    }

    private boolean processLeftMove(int[][] newGrid, boolean[][] merged) {
        boolean moved = false;

        for (int row = 0; row < size; row++) {
            for (int col = 1; col < size; col++) {
                if (newGrid[row][col] == 0) continue;

                int currentCol = col;
                int value = newGrid[row][col];

                while (currentCol > 0) {
                    if (newGrid[row][currentCol - 1] == 0) {
                        newGrid[row][currentCol - 1] = value;
                        newGrid[row][currentCol] = 0;
                        currentCol--;
                        moved = true;
                    } else if (newGrid[row][currentCol - 1] == value && !merged[row][currentCol - 1]) {
                        int mergedValue = value * 2;
                        newGrid[row][currentCol - 1] = mergedValue;
                        newGrid[row][currentCol] = 0;
                        merged[row][currentCol - 1] = true;
                        currentCol--;
                        moved = true;

                        score += mergedValue;
                        lastMoveScore += mergedValue;
                        break;
                    } else {
                        break;
                    }
                }

                if (currentCol != col) {
                    createMoveAnimation(row, col, row, currentCol, value);
                }
            }
        }

        return moved;
    }

    private boolean processRightMove(int[][] newGrid, boolean[][] merged) {
        boolean moved = false;

        for (int row = 0; row < size; row++) {
            for (int col = size - 2; col >= 0; col--) {
                if (newGrid[row][col] == 0) continue;

                int currentCol = col;
                int value = newGrid[row][col];

                while (currentCol < size - 1) {
                    if (newGrid[row][currentCol + 1] == 0) {
                        newGrid[row][currentCol + 1] = value;
                        newGrid[row][currentCol] = 0;
                        currentCol++;
                        moved = true;
                    } else if (newGrid[row][currentCol + 1] == value && !merged[row][currentCol + 1]) {
                        int mergedValue = value * 2;
                        newGrid[row][currentCol + 1] = mergedValue;
                        newGrid[row][currentCol] = 0;
                        merged[row][currentCol + 1] = true;
                        currentCol++;
                        moved = true;

                        score += mergedValue;
                        lastMoveScore += mergedValue;
                        break;
                    } else {
                        break;
                    }
                }

                if (currentCol != col) {
                    createMoveAnimation(row, col, row, currentCol, value);
                }
            }
        }

        return moved;
    }

    private void createMoveAnimation(int fromRow, int fromCol, int toRow, int toCol, int value) {
        float startX = getTileX(fromCol);
        float startY = getTileY(fromRow);
        float endX = getTileX(toCol);
        float endY = getTileY(toRow);

        float tileWidth = cellSize - 2 * padding;
        float tileHeight = cellSize - 2 * padding;
        Tile animTile = new Tile(value, fromCol, fromRow, startX, startY, tileWidth, tileHeight);

        animTile.setTargetPosition(endX, endY);
        animatingTiles.add(animTile);
        tiles[fromRow][fromCol].setValue(0);
    }

    private void updateTiles() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                tiles[row][col].setValue(grid[row][col]);
            }
        }
    }

    public boolean isGameOver() {
        if (animating) return false;

        // Check for empty cells
        if (!findEmptyCells().isEmpty()) {
            return false;
        }

        // Check for possible merges
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = grid[row][col];

                // Check right and down for possible merges
                if ((col < size - 1 && grid[row][col + 1] == value) ||
                    (row < size - 1 && grid[row + 1][col] == value)) {
                    return false;
                }
            }
        }

        return true; // No moves possible
    }

    public void dispose() {
        Tile.dispose();
    }
}
