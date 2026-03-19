package com.mycompany.brick;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.io.File;

public class MyApp {
    public static void main(String[] args) {
        // Use system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error setting look and feel: " + e.getMessage());
        }
        
        // Create game on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Create game window
            JFrame frame = new JFrame("Brick Breaker Deluxe");
            
            // Set icon if available
            try {
                File iconFile = new File("images/icon.png");
                if (iconFile.exists()) {
                    Image icon = new ImageIcon("images/icon.png").getImage();
                    frame.setIconImage(icon);
                }
            } catch (Exception e) {
                System.out.println("Error loading icon: " + e.getMessage());
            }
            
            // Set up frame
            frame.setBounds(10, 10, 700, 600);
            frame.setTitle("Brick Breaker Deluxe");
            frame.setResizable(false);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Center the window on screen
            frame.setLocationRelativeTo(null);
            
            // Create and add game panel
            GamePlay gamePlay = new GamePlay();
            frame.add(gamePlay);
            
            // Request focus for keyboard input
            gamePlay.requestFocus();
        });
    }
}