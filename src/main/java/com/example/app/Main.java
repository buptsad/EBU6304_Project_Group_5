package com.example.app;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.example.app.ui.LoginFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set up FlatDarculaLaf (dark theme)
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