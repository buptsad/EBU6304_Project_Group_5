package com.example.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.example.app.ui.LoginFrame;
import com.example.app.ui.CurrencyManager;
import javax.swing.*;

/**
 * Main entry point for the financial application.
 * <p>
 * Initializes the look and feel, currency manager, and launches the login page.
 
 */
public class Main {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * This class only contains static methods and should not be instantiated.
     */
    private Main() {
        // Prevent instantiation
    }

    /**
     * Application entry point.
     * Sets up the look and feel, initializes managers, and starts the login UI.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Set up FlatDarculaLaf (dark theme)
        CurrencyManager.getInstance();
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatDarculaLaf");
        }
        
        // Ensure we're running on the EDT (Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            // Start with login page
            new LoginFrame();
        });
    }
}