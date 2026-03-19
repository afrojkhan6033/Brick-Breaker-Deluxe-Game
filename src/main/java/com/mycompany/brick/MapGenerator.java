package com.mycompany.brick;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;

public class MapGenerator {
    public int[][] map;
    public int bricksWidth;
    public int bricksHeight;
    private int level;
    private Random random = new Random();
    
    // Brick patterns
    private static final int PATTERN_REGULAR = 0;
    private static final int PATTERN_CHECKERBOARD = 1;
    private static final int PATTERN_PYRAMID = 2;
    private static final int PATTERN_RANDOM = 3;
    
    public MapGenerator(int row, int col, int level) {
        this.level = level;
        map = new int[row][col];
        
        // Use different patterns based on level
        int pattern = (level - 1) % 4;
        generatePattern(pattern, row, col);
        
        bricksWidth = 540/col;
        bricksHeight = 150/row;
    }
    
    private void generatePattern(int pattern, int row, int col) {
        switch(pattern) {
            case PATTERN_REGULAR:
                generateRegularPattern(row, col);
                break;
            case PATTERN_CHECKERBOARD:
                generateCheckerboardPattern(row, col);
                break;
            case PATTERN_PYRAMID:
                generatePyramidPattern(row, col);
                break;
            case PATTERN_RANDOM:
                generateRandomPattern(row, col);
                break;
        }
    }
    
    private void generateRegularPattern(int row, int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                // Increase brick strength with row
                map[i][j] = level > 3 ? i + 1 : 1;
            }
        }
    }
    
    private void generateCheckerboardPattern(int row, int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if ((i + j) % 2 == 0) {
                    map[i][j] = Math.min(level, 3);
                } else {
                    map[i][j] = 1;
                }
            }
        }
    }
    
    private void generatePyramidPattern(int row, int col) {
        int mid = col / 2;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int distance = Math.abs(j - mid);
                if (distance <= i) {
                    map[i][j] = row - i;
                } else {
                    map[i][j] = 0;
                }
            }
        }
    }
    
    private void generateRandomPattern(int row, int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                // 75% chance of having a brick
                if (random.nextInt(100) < 75) {
                    map[i][j] = random.nextInt(level) + 1;
                } else {
                    map[i][j] = 0;
                }
            }
        }
        
        // Ensure at least 5 bricks exist
        int count = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (map[i][j] > 0) count++;
            }
        }
        
        if (count < 5) {
            generateRegularPattern(row, col);
        }
    }
    
    public void draw(Graphics2D g) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] > 0) {
                    // Get brick color based on strength
                    Color brickColor = getBrickColor(i, j);
                    Color gradientColor = darken(brickColor, 0.3f);
                    
                    // Create gradient for brick
                    GradientPaint brickGradient = new GradientPaint(
                        j * bricksWidth + 80, i * bricksHeight + 50, brickColor,
                        j * bricksWidth + 80, i * bricksHeight + 50 + bricksHeight, gradientColor
                    );
                    
                    // Draw brick with rounded corners
                    g.setPaint(brickGradient);
                    RoundRectangle2D brick = new RoundRectangle2D.Float(
                        j * bricksWidth + 80, i * bricksHeight + 50, 
                        bricksWidth - 2, bricksHeight - 2, 
                        6, 6
                    );
                    g.fill(brick);
                    
                    // Add highlight to make it look 3D
                    g.setColor(lighten(brickColor, 0.3f));
                    g.setStroke(new BasicStroke(2));
                    g.drawLine(
                        j * bricksWidth + 80, i * bricksHeight + 50,
                        j * bricksWidth + 80 + bricksWidth - 2, i * bricksHeight + 50
                    );
                    g.drawLine(
                        j * bricksWidth + 80, i * bricksHeight + 50,
                        j * bricksWidth + 80, i * bricksHeight + 50 + bricksHeight - 2
                    );
                    
                    // Add shadow to make it look 3D
                    g.setColor(darken(brickColor, 0.5f));
                    g.drawLine(
                        j * bricksWidth + 80 + bricksWidth - 2, i * bricksHeight + 50,
                        j * bricksWidth + 80 + bricksWidth - 2, i * bricksHeight + 50 + bricksHeight - 2
                    );
                    g.drawLine(
                        j * bricksWidth + 80, i * bricksHeight + 50 + bricksHeight - 2,
                        j * bricksWidth + 80 + bricksWidth - 2, i * bricksHeight + 50 + bricksHeight - 2
                    );
                    
                    // Draw brick strength
                    if (map[i][j] > 1) {
                        g.setColor(Color.WHITE);
                        g.setFont(g.getFont().deriveFont(12f));
                        g.drawString(String.valueOf(map[i][j]), 
                            j * bricksWidth + 80 + bricksWidth/2 - 4, 
                            i * bricksHeight + 50 + bricksHeight/2 + 4);
                    }
                }
            }
        }
    }
    
    public Color getBrickColor(int row, int col) {
        switch(map[row][col]) {
            case 1:
                return new Color(200, 50, 50); // Red
            case 2:
                return new Color(50, 150, 200); // Blue
            case 3:
                return new Color(50, 200, 50); // Green
            case 4:
                return new Color(200, 150, 50); // Gold
            case 5:
                return new Color(150, 50, 200); // Purple
            default:
                return new Color(200 - map[row][col]*10, 200 - map[row][col]*15, 200); // Scale from light to dark
        }
    }
    
    private Color darken(Color color, float factor) {
        return new Color(
            Math.max((int)(color.getRed() * (1 - factor)), 0),
            Math.max((int)(color.getGreen() * (1 - factor)), 0),
            Math.max((int)(color.getBlue() * (1 - factor)), 0)
        );
    }
    
    private Color lighten(Color color, float factor) {
        return new Color(
            Math.min((int)(color.getRed() * (1 + factor)), 255),
            Math.min((int)(color.getGreen() * (1 + factor)), 255),
            Math.min((int)(color.getBlue() * (1 + factor)), 255)
        );
    }
}