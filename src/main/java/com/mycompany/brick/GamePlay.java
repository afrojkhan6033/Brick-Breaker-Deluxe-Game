package com.mycompany.brick;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.GradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class GamePlay extends JPanel implements KeyListener, ActionListener {
    
    // Game states
    private static final int MENU = 0;
    private static final int PLAYING = 1;
    private static final int PAUSED = 2;
    private static final int GAME_OVER = 3;
    private static final int LEVEL_COMPLETE = 4;
    
    // Current game state
    private int gameState = MENU;
    
    // Menu options
    private String[] menuOptions = {"Start Game", "High Scores", "Exit"};
    private int selectedOption = 0;
    
    // Game variables
    private boolean play = false;
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int highScore = 0;
    private int totalbricks;
    private Timer timer;
    private int delay = 8;
    private int playerX = 310;
    private int paddleWidth = 100;
    private int ballposX = 350;
    private int ballposY = 350;
    private double ballXdir = -2;
    private double ballYdir = -3;
    private double ballSpeed = 1.0;
    private MapGenerator map;
    private Random random = new Random();
    
    // Background effects
    private int[] stars = new int[100];
    private int[] starsY = new int[100];
    private int[] starSpeed = new int[100];
    
    // Power-ups
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    
    // Particle effects
    private ArrayList<Particle> particles = new ArrayList<>();
    
    // Double buffering
    private BufferedImage offScreenImage;
    private Graphics2D offScreenGraphics;

    public GamePlay() {
        // Initialize the star background
        for (int i = 0; i < stars.length; i++) {
            stars[i] = random.nextInt(690);
            starsY[i] = random.nextInt(590);
            starSpeed[i] = 1 + random.nextInt(3);
        }
        
        // Load high score
        loadHighScore();
        
        // Initialize level
        initLevel();
        
        // Set up panel
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        
        // Initialize timer
        timer = new Timer(delay, this);
        timer.start();
        
        // Create off-screen buffer
        offScreenImage = new BufferedImage(700, 600, BufferedImage.TYPE_INT_ARGB);
        offScreenGraphics = offScreenImage.createGraphics();
        offScreenGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    private void initLevel() {
        // Create map based on level
        switch (level) {
            case 1:
                map = new MapGenerator(3, 7, level);
                totalbricks = 21;
                break;
            case 2:
                map = new MapGenerator(4, 8, level);
                totalbricks = 32;
                break;
            case 3:
                map = new MapGenerator(5, 9, level);
                totalbricks = 45;
                break;
            default:
                map = new MapGenerator(3 + (level - 3) / 2, 7 + (level - 3) / 2, level);
                totalbricks = (3 + (level - 3) / 2) * (7 + (level - 3) / 2);
        }
        
        // Increase ball speed with level
        ballSpeed = 1.0 + (level * 0.1);
    }
    
    private void loadHighScore() {
        try {
            File file = new File("highscore.dat");
            if (file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                highScore = (Integer) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            System.out.println("Error loading high score: " + e.getMessage());
        }
    }
    
    private void saveHighScore() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("highscore.dat"));
            oos.writeObject(highScore);
            oos.close();
        } catch (Exception e) {
            System.out.println("Error saving high score: " + e.getMessage());
        }
    }
    
    private void resetGame() {
        level = 1;
        score = 0;
        lives = 3;
        ballSpeed = 1.0;
        paddleWidth = 100;
        playerX = 310;
        resetBall();
        initLevel();
        powerUps.clear();
        particles.clear();
        gameState = PLAYING;
        play = true;
    }
    
    private void resetBall() {
        ballposX = playerX + paddleWidth / 2 - 10;
        ballposY = 530;
        ballXdir = (random.nextBoolean() ? 2 : -2) * ballSpeed;
        ballYdir = -3 * ballSpeed;
    }
    
    private void nextLevel() {
        level++;
        resetBall();
        initLevel();
        powerUps.clear();
        particles.clear();
        play = true;
        gameState = PLAYING;
    }
    
    @Override
    public void paint(Graphics g) {
        // Draw using double buffer
        update(g);
    }
    
    @Override
    public void update(Graphics g) {
        // Clear the off-screen buffer
        offScreenGraphics.setColor(new Color(10, 10, 40));
        offScreenGraphics.fillRect(0, 0, 700, 600);
        
        // Draw stars in background
        drawStars();
        
        // Draw based on game state
        switch (gameState) {
            case MENU:
                drawMenu();
                break;
            case PLAYING:
            case PAUSED:
                drawGame();
                if (gameState == PAUSED) {
                    drawPauseScreen();
                }
                break;
            case GAME_OVER:
                drawGame();
                drawGameOver();
                break;
            case LEVEL_COMPLETE:
                drawGame();
                drawLevelComplete();
                break;
        }
        
        // Draw the off-screen buffer to the screen
        g.drawImage(offScreenImage, 0, 0, this);
    }
    
    private void drawStars() {
        // Draw animated star background
        for (int i = 0; i < stars.length; i++) {
            int brightness = 100 + random.nextInt(155);
            offScreenGraphics.setColor(new Color(brightness, brightness, brightness));
            offScreenGraphics.fillRect(stars[i], starsY[i], 1 + starSpeed[i]/2, 1 + starSpeed[i]/2);
        }
    }
    
    private void drawMenu() {
        // Draw title
        GradientPaint titleGradient = new GradientPaint(
            0, 100, new Color(255, 50, 50),
            700, 200, new Color(255, 200, 50)
        );
        offScreenGraphics.setPaint(titleGradient);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 50));
        offScreenGraphics.drawString("BRICK BREAKER", 140, 150);
        
        // Draw menu options
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 30));
        for (int i = 0; i < menuOptions.length; i++) {
            if (i == selectedOption) {
                offScreenGraphics.setColor(new Color(255, 200, 0));
                offScreenGraphics.fillRoundRect(250, 250 + i * 60, 200, 40, 15, 15);
                offScreenGraphics.setColor(Color.BLACK);
            } else {
                offScreenGraphics.setColor(new Color(200, 200, 200));
            }
            offScreenGraphics.drawString(menuOptions[i], 270, 280 + i * 60);
        }
        
        // Draw instructions
        offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 16));
        offScreenGraphics.setColor(new Color(200, 200, 200));
        offScreenGraphics.drawString("Use Arrow Keys to Navigate and Enter to Select", 180, 500);
        offScreenGraphics.drawString("During Game: Left/Right to Move, P to Pause", 180, 530);
    }
    
    private void drawGame() {
        // Draw border
        GradientPaint borderGradient = new GradientPaint(
            0, 0, new Color(100, 100, 255),
            0, 600, new Color(50, 50, 150)
        );
        offScreenGraphics.setPaint(borderGradient);
        offScreenGraphics.fillRect(0, 0, 3, 592);
        offScreenGraphics.fillRect(0, 0, 692, 3);
        offScreenGraphics.fillRect(691, 0, 3, 592);
        
        // Draw map
        map.draw(offScreenGraphics);
        
        // Draw HUD
        drawHUD();
        
        // Draw paddle
        drawPaddle();
        
        // Draw ball
        drawBall();
        
        // Draw power-ups
        drawPowerUps();
        
        // Draw particles
        drawParticles();
    }
    
    private void drawPauseScreen() {
        // Semi-transparent overlay
        offScreenGraphics.setColor(new Color(0, 0, 0, 150));
        offScreenGraphics.fillRect(0, 0, 700, 600);
        
        // Pause text
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 40));
        offScreenGraphics.drawString("GAME PAUSED", 220, 250);
        
        offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
        offScreenGraphics.drawString("Press P to Continue", 250, 300);
        offScreenGraphics.drawString("Press ESC to Return to Menu", 220, 330);
    }
    
    private void drawGameOver() {
        // Semi-transparent overlay
        offScreenGraphics.setColor(new Color(0, 0, 0, 150));
        offScreenGraphics.fillRect(0, 0, 700, 600);
        
        // Game over text
        GradientPaint gameOverGradient = new GradientPaint(
            0, 250, new Color(255, 50, 50),
            700, 300, new Color(255, 100, 50)
        );
        offScreenGraphics.setPaint(gameOverGradient);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 40));
        offScreenGraphics.drawString("GAME OVER", 240, 250);
        
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 25));
        offScreenGraphics.drawString("Final Score: " + score, 270, 300);
        
        if (score > highScore) {
            offScreenGraphics.setColor(new Color(255, 255, 0));
            offScreenGraphics.drawString("NEW HIGH SCORE!", 240, 340);
        }
        
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
        offScreenGraphics.drawString("Press Enter to Play Again", 230, 390);
        offScreenGraphics.drawString("Press ESC to Return to Menu", 220, 420);
    }
    
    private void drawLevelComplete() {
        // Semi-transparent overlay
        offScreenGraphics.setColor(new Color(0, 0, 0, 150));
        offScreenGraphics.fillRect(0, 0, 700, 600);
        
        // Level complete text
        GradientPaint levelCompleteGradient = new GradientPaint(
            0, 250, new Color(50, 255, 50),
            700, 300, new Color(50, 200, 255)
        );
        offScreenGraphics.setPaint(levelCompleteGradient);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 40));
        offScreenGraphics.drawString("LEVEL " + level + " COMPLETE!", 180, 250);
        
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.setFont(new Font("Arial", Font.PLAIN, 20));
        offScreenGraphics.drawString("Press Enter to Continue to Level " + (level + 1), 180, 320);
        offScreenGraphics.drawString("Press ESC to Return to Menu", 220, 350);
    }
    
    private void drawHUD() {
        // Score and level display
        offScreenGraphics.setColor(Color.WHITE);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 20));
        offScreenGraphics.drawString("Score: " + score, 580, 30);
        offScreenGraphics.drawString("Level: " + level, 20, 30);
        
        // Draw lives
        offScreenGraphics.setColor(Color.RED);
        offScreenGraphics.setFont(new Font("Arial", Font.BOLD, 20));
        offScreenGraphics.drawString("Lives: ", 280, 30);
        
        for (int i = 0; i < lives; i++) {
            offScreenGraphics.setColor(Color.RED);
            offScreenGraphics.fillOval(350 + i * 30, 15, 15, 15);
        }
    }
    
    private void drawPaddle() {
        // Create gradient for paddle
        GradientPaint paddleGradient = new GradientPaint(
            playerX, 550, new Color(255, 255, 100),
            playerX, 558, new Color(255, 150, 0)
        );
        
        offScreenGraphics.setPaint(paddleGradient);
        RoundRectangle2D paddle = new RoundRectangle2D.Float(playerX, 550, paddleWidth, 8, 10, 10);
        offScreenGraphics.fill(paddle);
        
        // Add glowing effect
        offScreenGraphics.setColor(new Color(255, 255, 150, 100));
        offScreenGraphics.fillRoundRect(playerX - 2, 548, paddleWidth + 4, 12, 12, 12);
    }
    
    private void drawBall() {
        // Create radial gradient for ball
        Point2D center = new Point2D.Float((float)ballposX + 10, (float)ballposY + 10);
        float radius = 10f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {Color.WHITE, new Color(100, 255, 100)};
        RadialGradientPaint ballGradient = new RadialGradientPaint(center, radius, dist, colors, CycleMethod.NO_CYCLE);
        
        offScreenGraphics.setPaint(ballGradient);
        offScreenGraphics.fillOval(ballposX, ballposY, 20, 20);
        
        // Add glowing effect
        offScreenGraphics.setColor(new Color(100, 255, 100, 100));
        offScreenGraphics.fillOval(ballposX - 5, ballposY - 5, 30, 30);
    }
    
    private void drawPowerUps() {
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(offScreenGraphics);
        }
    }
    
    private void drawParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            p.draw(offScreenGraphics);
            
            if (p.life <= 0) {
                particles.remove(i);
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        timer.start();
        
        // Update star positions
        for (int i = 0; i < stars.length; i++) {
            starsY[i] += starSpeed[i];
            if (starsY[i] > 600) {
                starsY[i] = 0;
                stars[i] = random.nextInt(690);
            }
        }
        
        if (gameState == PLAYING) {
            if (play) {
                // Check paddle collision
                if (new Rectangle(ballposX, ballposY, 20, 20).intersects(new Rectangle(playerX, 550, paddleWidth, 8))) {
                    // Calculate new ball direction based on where it hit the paddle
                    double paddleCenter = playerX + paddleWidth / 2.0;
                    double hitPoint = ballposX + 10.0;
                    double offset = (hitPoint - paddleCenter) / (paddleWidth / 2.0);
                    
                    // Change direction based on hit point
                    ballXdir = offset * 3.0 * ballSpeed;
                    ballYdir = -Math.abs(ballYdir);
                    
                    // Add particles
                    createParticles(ballposX + 10, 550, 10, new Color(255, 255, 150));
                }
                
                // Check brick collisions
                checkBrickCollision();
                
                // Update ball position
                ballposX += ballXdir;
                ballposY += ballYdir;
                
                // Check wall collisions
                if (ballposX < 5) {
                    ballXdir = Math.abs(ballXdir);
                }
                if (ballposY < 5) {
                    ballYdir = Math.abs(ballYdir);
                }
                if (ballposX > 670) {
                    ballXdir = -Math.abs(ballXdir);
                }
                
                // Check if ball fell
                if (ballposY > 570) {
                    lives--;
                    if (lives <= 0) {
                        gameOver();
                    } else {
                        resetBall();
                    }
                }
                
                // Check if level complete
                if (totalbricks <= 0) {
                    levelComplete();
                }
                
                // Update power-ups
                updatePowerUps();
            }
        }
        
        repaint();
    }
    
    private void checkBrickCollision() {
        A: for (int i = 0; i < map.map.length; i++) {
            for (int j = 0; j < map.map[0].length; j++) {
                if (map.map[i][j] > 0) {
                    int brickX = j * map.bricksWidth + 80;
                    int brickY = i * map.bricksHeight + 50;
                    int bricksWidth = map.bricksWidth;
                    int bricksHeight = map.bricksHeight;

                    Rectangle brickrect = new Rectangle(brickX, brickY, bricksWidth, bricksHeight);
                    Rectangle ballrect = new Rectangle(ballposX, ballposY, 20, 20);

                    if (ballrect.intersects(brickrect)) {
                        // Reduce brick health or break it
                        map.map[i][j]--;
                        
                        // If brick is completely broken
                        if (map.map[i][j] <= 0) {
                            totalbricks--;
                            score += 5 * level;
                            
                            // Create particle effect
                            createParticles(brickX + bricksWidth/2, brickY + bricksHeight/2, 20, map.getBrickColor(i, j));
                            
                            // Chance to spawn power-up
                            if (random.nextInt(100) < 20) {
                                spawnPowerUp(brickX + bricksWidth/2, brickY + bricksHeight/2);
                            }
                        }

                        // Change ball direction based on collision side
                        if (ballposX + 19 <= brickrect.x || ballposX + 1 >= brickrect.x + bricksWidth) {
                            ballXdir = -ballXdir;
                        } else {
                            ballYdir = -ballYdir;
                        }
                        
                        break A;
                    }
                }
            }
        }
    }
    
    private void updatePowerUps() {
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update();
            
            // Check if power-up is caught
            if (new Rectangle(powerUp.x, powerUp.y, powerUp.size, powerUp.size)
                    .intersects(new Rectangle(playerX, 550, paddleWidth, 8))) {
                
                // Apply power-up effect
                applyPowerUp(powerUp.type);
                
                // Remove power-up
                powerUps.remove(i);
                
                // Create particles
                createParticles(powerUp.x + powerUp.size/2, powerUp.y + powerUp.size/2, 15, powerUp.getColor());
            } 
            // Remove if out of screen
            else if (powerUp.y > 600) {
                powerUps.remove(i);
            }
        }
    }
    
    private void spawnPowerUp(int x, int y) {
        int type = random.nextInt(PowerUp.TOTAL_TYPES);
        powerUps.add(new PowerUp(x, y, type));
    }
    
    private void applyPowerUp(int type) {
        switch (type) {
            case PowerUp.EXPAND_PADDLE:
                paddleWidth = Math.min(paddleWidth + 20, 150);
                break;
            case PowerUp.SHRINK_PADDLE:
                paddleWidth = Math.max(paddleWidth - 20, 60);
                break;
            case PowerUp.SLOW_BALL:
                ballSpeed = Math.max(ballSpeed * 0.8, 0.5);
                ballXdir *= 0.8;
                ballYdir *= 0.8;
                break;
            case PowerUp.FAST_BALL:
                ballSpeed *= 1.2;
                ballXdir *= 1.2;
                ballYdir *= 1.2;
                break;
            case PowerUp.EXTRA_LIFE:
                lives = Math.min(lives + 1, 5);
                break;
            case PowerUp.MULTI_BALL:
                // Not implemented in this version
                break;
        }
    }
    
    private void createParticles(int x, int y, int count, Color color) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }
    
    private void gameOver() {
        play = false;
        gameState = GAME_OVER;
        
        // Update high score
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }
    
    private void levelComplete() {
        play = false;
        gameState = LEVEL_COMPLETE;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (gameState) {
            case MENU:
                handleMenuKeys(e);
                break;
            case PLAYING:
                handleGameKeys(e);
                break;
            case PAUSED:
                handlePausedKeys(e);
                break;
            case GAME_OVER:
                handleGameOverKeys(e);
                break;
            case LEVEL_COMPLETE:
                handleLevelCompleteKeys(e);
                break;
        }
    }
    
    private void handleMenuKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                break;
            case KeyEvent.VK_DOWN:
                selectedOption = (selectedOption + 1) % menuOptions.length;
                break;
            case KeyEvent.VK_ENTER:
                executeMenuOption();
                break;
        }
    }
    
    private void executeMenuOption() {
        switch (selectedOption) {
            case 0: // Start Game
                resetGame();
                break;
            case 1: // High Scores
                // Not implemented in this version
                break;
            case 2: // Exit
                System.exit(0);
                break;
        }
    }
    
    private void handleGameKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                if (playerX + paddleWidth >= 690) {
                    playerX = 690 - paddleWidth;
                } else {
                    moveRight();
                }
                break;
            case KeyEvent.VK_LEFT:
                if (playerX <= 5) {
                    playerX = 5;
                } else {
                    moveLeft();
                }
                break;
            case KeyEvent.VK_P:
                gameState = PAUSED;
                break;
            case KeyEvent.VK_ESCAPE:
                gameState = MENU;
                break;
        }
    }
    
    private void handlePausedKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_P:
                gameState = PLAYING;
                break;
            case KeyEvent.VK_ESCAPE:
                gameState = MENU;
                break;
        }
    }
    
    private void handleGameOverKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                resetGame();
                break;
            case KeyEvent.VK_ESCAPE:
                gameState = MENU;
                break;
        }
    }
    
    private void handleLevelCompleteKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                nextLevel();
                break;
            case KeyEvent.VK_ESCAPE:
                gameState = MENU;
                break;
        }
    }

    public void moveRight() {
        play = true;
        playerX += 20;
    }
    
    public void moveLeft() {
        play = true;
        playerX -= 20;
    }
    
// Inner class for power-ups
private class PowerUp {
    public static final int EXPAND_PADDLE = 0;
    public static final int SHRINK_PADDLE = 1;
    public static final int SLOW_BALL = 2;
    public static final int FAST_BALL = 3;
    public static final int EXTRA_LIFE = 4;
    public static final int MULTI_BALL = 5;
    public static final int TOTAL_TYPES = 6;
    
    private int x, y;
    private int type;
    private int size;
    private double speed;
    
    public PowerUp(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = 25;
        this.speed = 2 + random.nextDouble();
    }
    
    public void update() {
        y += speed;
    }
    
    public void draw(Graphics2D g) {
        // Draw power-up background
        g.setColor(getColor());
        g.fillOval(x, y, size, size);
        
        // Draw power-up icon
        g.setColor(Color.WHITE);
        switch (type) {
            case EXPAND_PADDLE:
                g.fillRect(x + 5, y + size/2 - 2, size - 10, 4);
                break;
            case SHRINK_PADDLE:
                g.fillRect(x + 10, y + size/2 - 2, size - 20, 4);
                break;
            case SLOW_BALL:
                g.drawOval(x + 5, y + 5, size - 10, size - 10);
                g.drawString("S", x + size/2 - 4, y + size/2 + 5);
                break;
            case FAST_BALL:
                g.drawOval(x + 5, y + 5, size - 10, size - 10);
                g.drawString("F", x + size/2 - 4, y + size/2 + 5);
                break;
            case EXTRA_LIFE:
                g.fillOval(x + 5, y + 5, size - 10, size - 10);
                g.setColor(Color.RED);
                g.fillOval(x + 8, y + 8, size - 16, size - 16);
                break;
            case MULTI_BALL:
                g.fillOval(x + 5, y + 5, 7, 7);
                g.fillOval(x + size - 12, y + 5, 7, 7);
                g.fillOval(x + size/2 - 3, y + size - 12, 7, 7);
                break;
        }
        
        // Add glow effect
        g.setColor(new Color(getColor().getRed(), getColor().getGreen(), getColor().getBlue(), 100));
        g.fillOval(x - 5, y - 5, size + 10, size + 10);
    }
    
    public Color getColor() {
        switch (type) {
            case EXPAND_PADDLE:
                return new Color(50, 200, 50);
            case SHRINK_PADDLE:
                return new Color(200, 50, 50);
            case SLOW_BALL:
                return new Color(50, 100, 200);
            case FAST_BALL:
                return new Color(200, 150, 50);
            case EXTRA_LIFE:
                return new Color(200, 50, 150);
            case MULTI_BALL:
                return new Color(150, 150, 250);
            default:
                return Color.WHITE;
        }
    }
}

// Inner class for particle effects
private class Particle {
    private double x, y;
    private double xVel, yVel;
    private double size;
    private Color color;
    private int life;
    private final int MAX_LIFE = 30;
    
    public Particle(int x, int y, Color baseColor) {
        this.x = x;
        this.y = y;
        
        // Random velocity
        this.xVel = -2 + random.nextDouble() * 4;
        this.yVel = -2 + random.nextDouble() * 4;
        
        // Random size
        this.size = 2 + random.nextDouble() * 4;
        
        // Slightly randomize color
        int r = Math.min(255, Math.max(0, baseColor.getRed() - 30 + random.nextInt(60)));
        int g = Math.min(255, Math.max(0, baseColor.getGreen() - 30 + random.nextInt(60)));
        int b = Math.min(255, Math.max(0, baseColor.getBlue() - 30 + random.nextInt(60)));
        this.color = new Color(r, g, b);
        
        // Set life
        this.life = MAX_LIFE;
    }
    
    public void update() {
        // Update position
        x += xVel;
        y += yVel;
        
        // Apply gravity
        yVel += 0.1;
        
        // Reduce life
        life--;
    }
    
    public void draw(Graphics2D g) {
        // Calculate alpha based on remaining life
        int alpha = (int)(255 * ((double)life / MAX_LIFE));
        Color fadeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        
        g.setColor(fadeColor);
        g.fillOval((int)x, (int)y, (int)size, (int)size);
    }
}
}
    