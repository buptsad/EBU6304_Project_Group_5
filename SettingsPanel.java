package com.example.app.ui.pages;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    public SettingsPanel() {
        setLayout(new BorderLayout());

        // Title
        JLabel titleLabel = new JLabel("Settings", JLabel.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Navigation bar
        JPanel navPanel = new JPanel(new GridLayout(1, 4));
        navPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JButton profileButton = createNavButton("Profile", "PROFILE");
        JButton preferencesButton = createNavButton("Preferences", "PREFERENCES");
        JButton notificationsButton = createNavButton("Notifications", "NOTIFICATIONS");
        JButton securityButton = createNavButton("Security", "SECURITY");
        navPanel.add(profileButton);
        navPanel.add(preferencesButton);
        navPanel.add(notificationsButton);
        navPanel.add(securityButton);
        add(navPanel, BorderLayout.CENTER);

        // Content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add sub-panels
        contentPanel.add(createProfilePanel(), "PROFILE");
        contentPanel.add(createPreferencesPanel(), "PREFERENCES");
        contentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(createSecurityPanel(), "SECURITY");

        add(contentPanel, BorderLayout.SOUTH);

        // Set default view
        cardLayout.show(contentPanel, "PROFILE");
    }

    private JButton createNavButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.addActionListener(e -> cardLayout.show(contentPanel, panelName));
    
        // 设置按钮样式
        button.setFont(new Font("Arial", Font.PLAIN, 5)); // 设置字体
        button.setPreferredSize(new Dimension(20, 5)); // 设置按钮大小
        button.setFocusPainted(false); // 移除焦点边框
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // 设置边框
        button.setBackground(Color.LIGHT_GRAY); // 设置背景颜色
        button.setForeground(Color.BLACK); // 设置字体颜色
    
        return button;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Phone:"));
        JTextField phoneField = new JTextField();
        panel.add(phoneField);

        return panel;
    }

    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Default Currency:"));
        JComboBox<String> currencyComboBox = new JComboBox<>(new String[]{"USD $", "RMB ¥"});
        panel.add(currencyComboBox);

        panel.add(new JLabel("Currency Symbol:"));
        JTextField currencySymbolField = new JTextField();
        panel.add(currencySymbolField);

        return panel;
    }

    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Budget Alerts:"));
        JCheckBox budgetAlertsCheckBox = new JCheckBox("Enable");
        panel.add(budgetAlertsCheckBox);
        panel.add(new JLabel()); // Placeholder

        panel.add(new JLabel("Transaction Alerts:"));
        JCheckBox transactionAlertsCheckBox = new JCheckBox("Enable");
        panel.add(transactionAlertsCheckBox);
        panel.add(new JLabel()); // Placeholder

        return panel;
    }

    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("Current Password:"));
        JPasswordField currentPasswordField = new JPasswordField();
        panel.add(currentPasswordField);

        panel.add(new JLabel("New Password:"));
        JPasswordField newPasswordField = new JPasswordField();
        panel.add(newPasswordField);

        panel.add(new JLabel("Confirm New Password:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);

        return panel;
    }
}