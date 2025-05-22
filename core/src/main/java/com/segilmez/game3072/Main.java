package com.segilmez.game3072;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Preferences;

public class Main extends ApplicationAdapter {
    // Game state
    private enum GameState {
        PLAYING,
        GAME_OVER
    }

    private GameState gameState = GameState.PLAYING;

    // Game over elements
    private float gameOverAlpha = 0f;
    private BitmapFont gameOverFont;
    private BitmapFont gameOverSubtitleFont;
    private BitmapFont gameOverButtonFont;
    private Rectangle restartButton;
    private Texture restartButtonTexture;

    // Core rendering elements
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private Grid grid;

    // Screen dimensions
    private float screenWidth;
    private float screenHeight;

    // Scoreboard elements
    private Texture scoreboardTexture;
    private float scoreboardX;
    private float scoreboardY;
    private float scoreboardWidth;
    private float scoreboardHeight;
    // Best score board elements
    private Texture bestScoreboardTexture;
    private float bestScoreboardX;
    private float bestScoreboardY;
    private float bestScoreboardWidth;
    private float bestScoreboardHeight;
    private float bestScoreValue = 0;
    private Preferences preferences;
    private BitmapFont scoreFont;
    private float scoreValue = 0;
    private float targetScore = 0;
    private float scoreAnimationTime = 0;
    private float scoreIncrement = 100; // Points per second during animation

    @Override
    public void create() {
        initializeRenderingObjects();
        initializeGameElements();
        initializeFonts();
        initializePreferences();
        initializeGameState();
    }

    private void initializeRenderingObjects() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
    }

    private void initializeGameElements() {
        float gridSize = Math.min(screenWidth, screenHeight) * 0.8f;
        float gridX = (screenWidth - gridSize) / 2;
        float gridY = (screenHeight - gridSize) / 2;

        grid = new Grid(4, gridSize, gridX, gridY);

        // Scoreboard setup
        scoreboardTexture = new Texture(Gdx.files.internal("score.png"));
        bestScoreboardTexture = new Texture(Gdx.files.internal("best_score.png"));

        scoreboardWidth = gridSize * 0.45f;
        scoreboardHeight = gridSize * 0.45f;
        bestScoreboardWidth = scoreboardWidth;
        bestScoreboardHeight = scoreboardHeight;

        float spacing = scoreboardWidth * 0.1f;
        float totalWidth = scoreboardWidth * 2 + spacing;

        scoreboardX = gridX + (gridSize - totalWidth) / 2f;
        bestScoreboardX = scoreboardX + scoreboardWidth + spacing;
        scoreboardY = gridY + gridSize + gridSize * 0.05f;
        bestScoreboardY = scoreboardY;

        // Restart button
        restartButtonTexture = new Texture(Gdx.files.internal("restart_game.png"));
        float buttonSize = gridSize * 0.2f;
        restartButton = new Rectangle(
            screenWidth / 2 - buttonSize / 2,
            screenHeight * 0.35f,
            buttonSize,
            buttonSize
        );
    }

    private void initializeFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("Orbitron/static/Orbitron-Regular.ttf"));

        // Score and game fonts
        FreeTypeFontParameter parameter = GameUtils.createFontParameters(
            24,
            GameUtils.hexToColor("#776E65"),
            1,
            new Color(0, 0, 0, 0.2f)
        );

        scoreFont = generator.generateFont(parameter);
        font = generator.generateFont(parameter);

        // Game over fonts
        FreeTypeFontParameter gameOverParams = GameUtils.createFontParameters(
            48,
            GameUtils.hexToColor("#776E65"),
            2,
            new Color(0, 0, 0, 0.3f)
        );
        gameOverFont = generator.generateFont(gameOverParams);

        FreeTypeFontParameter subtitleParams = GameUtils.createFontParameters(
            20,
            GameUtils.hexToColor("#776E65"),
            0,
            null
        );
        gameOverSubtitleFont = generator.generateFont(subtitleParams);

        FreeTypeFontParameter buttonParams = GameUtils.createFontParameters(
            16,
            Color.WHITE,
            0,
            null
        );
        gameOverButtonFont = generator.generateFont(buttonParams);

        generator.dispose();
    }

    private void initializePreferences() {
        preferences = Gdx.app.getPreferences("game3072");
        bestScoreValue = preferences.getInteger("bestScore", 0);
    }

    private void initializeGameState() {
        scoreValue = 0;
        targetScore = 0;
        gameState = GameState.PLAYING;
    }

    @Override
    public void render() {
        updateGame();
        renderGame();
        handleInput();
    }

    private void updateGame() {
        float delta = Gdx.graphics.getDeltaTime();
        grid.update(delta);

        // Update score animation
        if (scoreValue < targetScore) {
            scoreAnimationTime += delta;
            scoreValue += scoreIncrement * delta;
            if (scoreValue >= targetScore) {
                scoreValue = targetScore;
            }
        }

        if (scoreValue > bestScoreValue) {
            bestScoreValue = scoreValue;
            preferences.putInteger("bestScore", (int) bestScoreValue);
            preferences.flush();
        }

        // Check for game over
        if (gameState == GameState.PLAYING && grid.isGameOver()) {
            gameState = GameState.GAME_OVER;
            gameOverAlpha = 0f; // Reset fade in effect
        }
    }

    private void renderGame() {
        // Clear screen
        Gdx.gl.glClearColor(
            GameUtils.hexToColor("#FAF8EF").r,
            GameUtils.hexToColor("#FAF8EF").g,
            GameUtils.hexToColor("#FAF8EF").b,
            1
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render game elements
        renderScoreboards();
        grid.render(shapeRenderer, batch, font);

        // Render game over if needed
        if (gameState == GameState.GAME_OVER) {
            renderGameOver(Gdx.graphics.getDeltaTime());
        }
    }

    private void renderGameOver(float delta) {
        // Fade in effect
        if (gameOverAlpha < 0.7f) {
            gameOverAlpha += delta * 0.5f;
            if (gameOverAlpha > 0.7f) gameOverAlpha = 0.7f;
        }

        // Draw semi-transparent overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 1, 1, gameOverAlpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        // Draw "Game Over" text
        GlyphLayout gameOverLayout = new GlyphLayout(gameOverFont, "Game Over");
        gameOverFont.draw(
            batch,
            "Game Over",
            (screenWidth - gameOverLayout.width) / 2,
            screenHeight * 0.65f
        );

        // Draw subtitle
        GlyphLayout subtitleLayout = new GlyphLayout(
            gameOverSubtitleFont,
            "No more moves available!"
        );
        gameOverSubtitleFont.draw(
            batch,
            "No more moves available!",
            (screenWidth - subtitleLayout.width) / 2,
            screenHeight * 0.55f
        );

        // Draw restart button
        batch.draw(
            restartButtonTexture,
            restartButton.x,
            restartButton.y,
            restartButton.width,
            restartButton.height
        );

        batch.end();
    }

    private void renderScoreboards() {
        if (batch == null || scoreboardTexture == null || scoreFont == null) {
            return;
        }

        batch.begin();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        batch.draw(
            scoreboardTexture,
            scoreboardX,
            scoreboardY,
            scoreboardWidth,
            scoreboardHeight
        );

        String scoreText = String.valueOf((int) scoreValue);
        GlyphLayout valueLayout = new GlyphLayout(scoreFont, scoreText);

        // Scale font if needed
        float maxWidth = scoreboardWidth * 0.7f;
        if (valueLayout.width > maxWidth) {
            float scale = maxWidth / valueLayout.width;
            scoreFont.getData().setScale(scale);
            valueLayout = new GlyphLayout(scoreFont, scoreText);
        } else {
            scoreFont.getData().setScale(1.0f);
        }

        // Center text
        float valueX = scoreboardX + (scoreboardWidth - valueLayout.width) * 0.5f;
        float valueY = scoreboardY + (scoreboardHeight + valueLayout.height) * 0.4f;
        scoreFont.draw(batch, scoreText, valueX, valueY);

        // Render best score board
        batch.draw(
            bestScoreboardTexture,
            bestScoreboardX,
            bestScoreboardY,
            bestScoreboardWidth,
            bestScoreboardHeight
        );

        String bestText = String.valueOf((int) bestScoreValue);
        GlyphLayout bestLayout = new GlyphLayout(scoreFont, bestText);

        float bestMaxWidth = bestScoreboardWidth * 0.7f;
        if (bestLayout.width > bestMaxWidth) {
            float scale = bestMaxWidth / bestLayout.width;
            scoreFont.getData().setScale(scale);
            bestLayout = new GlyphLayout(scoreFont, bestText);
        } else {
            scoreFont.getData().setScale(1.0f);
        }

        float bestX = bestScoreboardX + (bestScoreboardWidth - bestLayout.width) * 0.5f;
        float bestY = bestScoreboardY + (bestScoreboardHeight + bestLayout.height) * 0.4f;
        scoreFont.draw(batch, bestText, bestX, bestY);

        batch.end();
    }

    private void handleInput() {
        if (grid.isAnimating()) return;

        if (gameState == GameState.GAME_OVER) {
            handleGameOverInput();
        } else {
            handleGameplayInput();
        }
    }

    private void handleGameOverInput() {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            touchPos.y = Gdx.graphics.getHeight() - touchPos.y;

            if (restartButton.contains(touchPos.x, touchPos.y)) {
                resetGame();
            }
        }
    }

    private void handleGameplayInput() {
        boolean moved = false;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            moved = grid.moveUp();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            moved = grid.moveDown();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            moved = grid.moveLeft();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            moved = grid.moveRight();
        }

        if (moved) {
            updateScoreAfterMove();
        }
    }

    private void updateScoreAfterMove() {
        int moveScore = grid.getLastMoveScore();
        if (moveScore > 0) {
            targetScore += moveScore;
            if (targetScore > bestScoreValue) {
                bestScoreValue = scoreValue; // will follow score animation
            }
            scoreIncrement = moveScore * 3;
            scoreAnimationTime = 0;
        }
    }

    private void resetGame() {
        bestScoreValue = Math.max(bestScoreValue, targetScore);
        preferences.putInteger("bestScore", (int) bestScoreValue);
        preferences.flush();

        float gridSize = Math.min(screenWidth, screenHeight) * 0.8f;
        float startX = (screenWidth - gridSize) / 2;
        float startY = (screenHeight - gridSize) / 2;
        grid = new Grid(4, gridSize, startX, startY);
        gameState = GameState.PLAYING;
        gameOverAlpha = 0f;
        scoreValue = 0;
        targetScore = 0;
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;

        float gridSize = Math.min(screenWidth, screenHeight) * 0.8f;
        float gridX = (screenWidth - gridSize) / 2;
        float gridY = (screenHeight - gridSize) / 2;

        grid = new Grid(4, gridSize, gridX, gridY);

        scoreboardWidth = gridSize * 0.45f;
        scoreboardHeight = gridSize * 0.45f;
        bestScoreboardWidth = scoreboardWidth;
        bestScoreboardHeight = scoreboardHeight;

        float spacing = scoreboardWidth * 0.1f;
        float totalWidth = scoreboardWidth * 2 + spacing;

        scoreboardX = gridX + (gridSize - totalWidth) / 2f;
        bestScoreboardX = scoreboardX + scoreboardWidth + spacing;
        scoreboardY = gridY + gridSize + gridSize * 0.05f;
        bestScoreboardY = scoreboardY;

        float buttonSize = gridSize * 0.2f;
        restartButton = new Rectangle(
            screenWidth / 2 - buttonSize / 2,
            screenHeight * 0.35f,
            buttonSize,
            buttonSize
        );
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        scoreboardTexture.dispose();
        bestScoreboardTexture.dispose();
        scoreFont.dispose();
        gameOverFont.dispose();
        gameOverSubtitleFont.dispose();
        gameOverButtonFont.dispose();
        restartButtonTexture.dispose();
        grid.dispose();
    }
}
