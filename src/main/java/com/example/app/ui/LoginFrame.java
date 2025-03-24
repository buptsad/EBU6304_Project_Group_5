package com.example.app.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    
    public LoginFrame() {
        setTitle("Finance Manager - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        
        // Create login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title label
        JLabel titleLabel = new JLabel("Finance Manager");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginPanel.add(titleLabel, gbc);
        
        // Username label and field
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        loginPanel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(usernameField, gbc);
        
        // Password label and field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(passwordField, gbc);
        
        // Login button
        JButton loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);
        
        // Add action listener to login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // For demo purposes, any login is successful
                // In a real app, you would validate credentials here
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                dispose(); // Close the login window
            }
        });
        
        add(loginPanel);
        setVisible(true);
    }
}