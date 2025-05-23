package com.example.app.ui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.example.app.model.UserSettings;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.ThemeManager;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SettingsPanel.class.getName());
    
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private JButton activeButton;
    private Dimension originalWindowSize;
    
    // Text fields for profile settings
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    
    // Components for preferences
    private JComboBox<String> currencyComboBox;
    private JTextField currencySymbolField;
    private JRadioButton darkThemeRadio;
    private JRadioButton lightThemeRadio;
    
    // Components for notifications
    private JCheckBox budgetAlertsCheckBox;
    private JCheckBox transactionAlertsCheckBox;
    
    // Components for security
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    
    // Reference to user settings
    private final UserSettings userSettings;

    private String username;

    public SettingsPanel(String username) {
        this.username = username;
        // Get the user settings instance
        userSettings = UserSettings.getInstance();
        LOGGER.log(Level.INFO, "Initializing SettingsPanel with UserSettings");
        
        // Log current settings for debugging
        LOGGER.log(Level.INFO, "Current UserSettings - Name: {0}, Email: {1}, Phone: {2}",
                new Object[]{userSettings.getName(), userSettings.getEmail(), userSettings.getPhone()});
        LOGGER.log(Level.INFO, "Current UserSettings - Currency: {0} ({1}), Dark Theme: {2}",
                new Object[]{userSettings.getCurrencyCode(), userSettings.getCurrencySymbol(), userSettings.isDarkTheme()});
        LOGGER.log(Level.INFO, "Current UserSettings - Budget Alerts: {0}, Transaction Alerts: {1}",
                new Object[]{userSettings.isBudgetAlertsEnabled(), userSettings.isTransactionAlertsEnabled()});
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Title
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        add(Box.createVerticalStrut(10));
        add(titleLabel);

        // Navigation panel
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS));
        navPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        navPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Add buttons
        JButton profileButton = createNavButton("Profile", "PROFILE");
        JButton preferencesButton = createNavButton("Preferences", "PREFERENCES");
        JButton notificationsButton = createNavButton("Notifications", "NOTIFICATIONS");
        JButton securityButton = createNavButton("Security", "SECURITY");
        navPanel.add(profileButton);
        navPanel.add(Box.createHorizontalStrut(10));
        navPanel.add(preferencesButton);
        navPanel.add(Box.createHorizontalStrut(10));
        navPanel.add(notificationsButton);
        navPanel.add(Box.createHorizontalStrut(10));
        navPanel.add(securityButton);

        add(navPanel);
        add(Box.createVerticalStrut(10));

        // Content panel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setPreferredSize(new Dimension(600, 400));
        contentPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Add subpanels
        contentPanel.add(createProfilePanel(), "PROFILE");
        contentPanel.add(createPreferencesPanel(), "PREFERENCES");
        contentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(createSecurityPanel(), "SECURITY");

        add(contentPanel);
        add(Box.createVerticalStrut(10));

        // Set default view
        cardLayout.show(contentPanel, "PROFILE");
        setActiveButton(profileButton);
        
        // Handle window resizing
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        // When panel is shown
                        SwingUtilities.invokeLater(() -> {
                            Window window = SwingUtilities.getWindowAncestor(SettingsPanel.this);
                            if (window != null) {
                                originalWindowSize = window.getSize();
                                window.setSize(700, 550);
                                window.setLocationRelativeTo(null);
                            }
                        });
                    } else {
                        // When panel is hidden
                        SwingUtilities.invokeLater(() -> {
                            Window window = SwingUtilities.getWindowAncestor(SettingsPanel.this);
                            if (window != null && originalWindowSize != null) {
                                window.setSize(originalWindowSize);
                                window.setLocationRelativeTo(null);
                            }
                        });
                    }
                }
            }
        });
    }
    
    private JButton createNavButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        
        button.setForeground(UIManager.getColor("Label.foreground"));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, panelName);
            setActiveButton(button);
        });

        return button;
    }

    private void setActiveButton(JButton button) {
        if (activeButton != null) {
            activeButton.setForeground(UIManager.getColor("Label.foreground"));
            activeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        activeButton = button;
        
        activeButton.setForeground(new Color(70, 130, 180));
        activeButton.setFont(new Font("Arial", Font.BOLD, 14));
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
        // Create and populate text fields with current values
        String name = userSettings.getName();
        String email = userSettings.getEmail();
        String phone = userSettings.getPhone();
        
        LOGGER.log(Level.INFO, "Setting up profile panel with values - Name: {0}, Email: {1}, Phone: {2}", 
                new Object[]{name, email, phone});
        
        nameField = new JTextField(name);
        emailField = new JTextField(email);
        phoneField = new JTextField(phone);
        
        panel.add(createLabeledField("Name:", nameField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Email:", emailField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Phone:", phoneField));
        
        // Add save button with implementation
        panel.add(Box.createVerticalStrut(15));
        JButton submitButton = new JButton("Save Changes");
        submitButton.setAlignmentX(LEFT_ALIGNMENT);
        submitButton.addActionListener(e -> {
            // Save profile data to settings
            userSettings.setName(nameField.getText());
            userSettings.setEmail(emailField.getText());
            userSettings.setPhone(phoneField.getText());
            
            // Save settings to storage
            userSettings.saveSettings();
            
            LOGGER.log(Level.INFO, "Saved profile settings - Name: {0}, Email: {1}, Phone: {2}", 
                    new Object[]{nameField.getText(), emailField.getText(), phoneField.getText()});
            
            JOptionPane.showMessageDialog(
                this, "Profile information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(submitButton);
    
        return panel;
    }

    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Currency components
        currencyComboBox = new JComboBox<>(new String[]{"USD $", "RMB ¥"});
        currencySymbolField = new JTextField(5);
        
        // Set values based on settings
        String storedCurrencyCode = userSettings.getCurrencyCode();
        String storedCurrencySymbol = userSettings.getCurrencySymbol();
        
        LOGGER.log(Level.INFO, "Setting up preferences panel with values - Currency: {0} ({1}), Dark Theme: {2}", 
                new Object[]{storedCurrencyCode, storedCurrencySymbol, userSettings.isDarkTheme()});
        
        if ("USD".equals(storedCurrencyCode)) {
            currencyComboBox.setSelectedItem("USD $");
            currencySymbolField.setText(storedCurrencySymbol);
        } else if ("RMB".equals(storedCurrencyCode)) {
            currencyComboBox.setSelectedItem("RMB ¥");
            currencySymbolField.setText(storedCurrencySymbol);
        } else {
            // Default or other currency
            currencySymbolField.setText(storedCurrencySymbol);
        }
        
        // Listen for currency selection changes
        currencyComboBox.addActionListener(e -> {
            String selected = (String) currencyComboBox.getSelectedItem();
            if ("USD $".equals(selected)) {
                currencySymbolField.setText("$");
            } else if ("RMB ¥".equals(selected)) {
                currencySymbolField.setText("¥");
            }
        });
        
        panel.add(createLabeledField("Default Currency:", currencyComboBox));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Currency Symbol:", currencySymbolField));
        
        // Theme selection
        panel.add(Box.createVerticalStrut(15));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));
        
        JLabel themeLabel = new JLabel("Application Theme");
        themeLabel.setFont(new Font(themeLabel.getFont().getName(), Font.BOLD, 14));
        themeLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(themeLabel);
        panel.add(Box.createVerticalStrut(10));
        
        // Theme radio buttons
        ButtonGroup themeGroup = new ButtonGroup();
        darkThemeRadio = new JRadioButton("Dark Theme");
        lightThemeRadio = new JRadioButton("Light Theme");
        
        // Set selection based on stored setting
        boolean isDarkTheme = userSettings.isDarkTheme();
        darkThemeRadio.setSelected(isDarkTheme);
        lightThemeRadio.setSelected(!isDarkTheme);
        
        themeGroup.add(darkThemeRadio);
        themeGroup.add(lightThemeRadio);
        
        darkThemeRadio.setAlignmentX(LEFT_ALIGNMENT);
        lightThemeRadio.setAlignmentX(LEFT_ALIGNMENT);
        
        panel.add(darkThemeRadio);
        panel.add(Box.createVerticalStrut(5));
        panel.add(lightThemeRadio);
        
        // Theme apply button
        panel.add(Box.createVerticalStrut(15));
        JButton applyThemeButton = new JButton("Apply Theme");
        applyThemeButton.setAlignmentX(LEFT_ALIGNMENT);
        applyThemeButton.addActionListener(e -> {
            boolean selectDark = darkThemeRadio.isSelected();
            
            // Save to settings
            userSettings.setDarkTheme(selectDark);
            userSettings.saveSettings();
            
            LOGGER.log(Level.INFO, "Saved theme setting: Dark theme = {0}", selectDark);
            
            // Apply theme
            applyTheme(selectDark);
        });
        panel.add(applyThemeButton);
        
        // Currency apply button
        panel.add(Box.createVerticalStrut(15));
        JButton applyCurrencyButton = new JButton("Apply Currency");
        applyCurrencyButton.setAlignmentX(LEFT_ALIGNMENT);
        applyCurrencyButton.addActionListener(e -> {
            String selected = (String) currencyComboBox.getSelectedItem();
            String symbol = currencySymbolField.getText();
            String code = "USD";
            
            if ("USD $".equals(selected)) {
                code = "USD";
            } else if ("RMB ¥".equals(selected)) {
                code = "RMB";
            }
            
            // Save to settings
            userSettings.setCurrencyCode(code);
            userSettings.setCurrencySymbol(symbol);
            userSettings.saveSettings();
            
            LOGGER.log(Level.INFO, "Saved currency settings - Code: {0}, Symbol: {1}", 
                    new Object[]{code, symbol});
            
            // Update the currency manager
            CurrencyManager.getInstance().setCurrency(code, symbol);
            
            JOptionPane.showMessageDialog(
                this, "Currency preferences updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(applyCurrencyButton);

        return panel;
    }

    // Apply theme method
    private void applyTheme(boolean darkTheme) {
        try {
            // Save theme setting in ThemeManager
            ThemeManager.getInstance().setTheme(darkTheme);
            
            if (darkTheme) {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarculaLaf());
            } else {
                UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
            }
            
            // Update UI
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                SwingUtilities.updateComponentTreeUI(window);
                JOptionPane.showMessageDialog(
                    this, "Theme applied successfully! Some changes may require restart.", 
                    "Theme Changed", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this, "Failed to change theme: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create checkboxes and set their states from settings
        boolean budgetAlertsEnabled = userSettings.isBudgetAlertsEnabled();
        boolean transactionAlertsEnabled = userSettings.isTransactionAlertsEnabled();
        
        LOGGER.log(Level.INFO, "Setting up notifications panel with values - Budget Alerts: {0}, Transaction Alerts: {1}", 
                new Object[]{budgetAlertsEnabled, transactionAlertsEnabled});
        
        budgetAlertsCheckBox = new JCheckBox("Enable");
        budgetAlertsCheckBox.setSelected(budgetAlertsEnabled);
        
        transactionAlertsCheckBox = new JCheckBox("Enable");
        transactionAlertsCheckBox.setSelected(transactionAlertsEnabled);

        panel.add(createLabeledField("Budget Alerts:", budgetAlertsCheckBox));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Transaction Alerts:", transactionAlertsCheckBox));
        
        // Save button with implementation
        panel.add(Box.createVerticalStrut(15));
        JButton saveButton = new JButton("Save Preferences");
        saveButton.setAlignmentX(LEFT_ALIGNMENT);
        saveButton.addActionListener(e -> {
            // Save notification preferences
            userSettings.setBudgetAlertsEnabled(budgetAlertsCheckBox.isSelected());
            userSettings.setTransactionAlertsEnabled(transactionAlertsCheckBox.isSelected());
            userSettings.saveSettings();
            
            LOGGER.log(Level.INFO, "Saved notification settings - Budget Alerts: {0}, Transaction Alerts: {1}", 
                    new Object[]{budgetAlertsCheckBox.isSelected(), transactionAlertsCheckBox.isSelected()});
            
            JOptionPane.showMessageDialog(
                this, "Notification settings saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(saveButton);

        return panel;
    }

    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Password fields
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        panel.add(createLabeledField("Current Password:", currentPasswordField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("New Password:", newPasswordField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Confirm Password:", confirmPasswordField));
        
        // Change password button with implementation
        panel.add(Box.createVerticalStrut(15));
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setAlignmentX(LEFT_ALIGNMENT);
        changePasswordButton.addActionListener(e -> {
            try {
                // Get password values
                char[] currentPwd = currentPasswordField.getPassword();
                char[] newPwd = newPasswordField.getPassword();
                char[] confirmPwd = confirmPasswordField.getPassword();
                
                // Validate inputs
                if (currentPwd.length == 0) {
                    JOptionPane.showMessageDialog(this, "Please enter your current password", 
                                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (newPwd.length == 0) {
                    JOptionPane.showMessageDialog(this, "Please enter a new password", 
                                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!Arrays.equals(newPwd, confirmPwd)) {
                    JOptionPane.showMessageDialog(this, "New password and confirmation do not match", 
                                                "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Hash current password and verify
                String currentHash = hashPassword(new String(currentPwd));
                String storedHash = userSettings.getPasswordHash();
                
                // For debugging only
                LOGGER.log(Level.INFO, "Current hash: {0}, Stored hash: {1}", 
                        new Object[]{currentHash, storedHash});
                
                // If there's a stored password and it doesn't match
                if (storedHash != null && !storedHash.isEmpty() && !storedHash.equals(currentHash)) {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect", 
                                                "Authentication Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Hash and store the new password
                String newHash = hashPassword(new String(newPwd));
                userSettings.setPasswordHash(newHash);
                userSettings.saveSettings();
                
                LOGGER.log(Level.INFO, "Password changed successfully");
                
                // Clear password fields for security
                currentPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                
                JOptionPane.showMessageDialog(this, "Password changed successfully!", 
                                            "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } finally {
                // Clear password arrays for security
                if (currentPasswordField.getPassword() != null)
                    Arrays.fill(currentPasswordField.getPassword(), '0');
                if (newPasswordField.getPassword() != null)
                    Arrays.fill(newPasswordField.getPassword(), '0');
                if (confirmPasswordField.getPassword() != null)
                    Arrays.fill(confirmPasswordField.getPassword(), '0');
            }
        });
        panel.add(changePasswordButton);

        return panel;
    }
    
    // Simple password hashing method - for demonstration purposes only
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Failed to hash password", e);
            // Fallback to simple encoding if SHA-256 is not available
            return password;
        }
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setAlignmentX(LEFT_ALIGNMENT);
    
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(120, 25));
        panel.add(label);
    
        field.setPreferredSize(new Dimension(200, 25));
        field.setMaximumSize(new Dimension(250, 25));
        panel.add(field);
    
        return panel;
    }
}