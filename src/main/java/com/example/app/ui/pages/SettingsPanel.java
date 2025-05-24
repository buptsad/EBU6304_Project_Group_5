package com.example.app.ui.pages;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.example.app.viewmodel.SettingsViewModel;
import com.example.app.viewmodel.SettingsViewModel.SettingsChangeListener;
import com.example.app.viewmodel.SettingsViewModel.SettingsChangeType;
import com.example.app.ui.ThemeManager;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The SettingsPanel provides a user interface for managing application and user preferences.
 * It allows users to update their profile, preferences (currency, theme), notification settings,
 * and security options such as password changes. The panel uses a CardLayout to switch between
 * different settings categories and synchronizes with a SettingsViewModel.
 * <p>
 * Features:
 * <ul>
 *   <li>Edit profile information (name, email, phone)</li>
 *   <li>Change default currency and application theme</li>
 *   <li>Enable or disable budget and transaction notifications</li>
 *   <li>Change password and reset all settings to defaults</li>
 *   <li>Responsive to ViewModel changes</li>
 * </ul>
 
 */
public class SettingsPanel extends JPanel implements SettingsChangeListener {
    private static final Logger LOGGER = Logger.getLogger(SettingsPanel.class.getName());

    /** Reference to the ViewModel */
    private final SettingsViewModel viewModel;

    /** CardLayout for switching between settings categories */
    private final CardLayout cardLayout;
    /** Panel containing the settings content */
    private final JPanel contentPanel;
    /** Currently active navigation button */
    private JButton activeButton;
    /** Stores the original window size for resizing logic */
    private Dimension originalWindowSize;

    // Profile fields
    /** Text field for editing the user's name */
    private JTextField nameField;
    /** Text field for editing the user's email */
    private JTextField emailField;
    /** Text field for editing the user's phone number */
    private JTextField phoneField;

    // Preferences fields
    /** Combo box for selecting the default currency */
    private JComboBox<String> currencyComboBox;
    /** Text field for editing the currency symbol */
    private JTextField currencySymbolField;
    /** Radio button for selecting dark theme */
    private JRadioButton darkThemeRadio;
    /** Radio button for selecting light theme */
    private JRadioButton lightThemeRadio;

    // Notification fields
    /** Checkbox for enabling/disabling budget alerts */
    private JCheckBox budgetAlertsCheckBox;
    /** Checkbox for enabling/disabling transaction alerts */
    private JCheckBox transactionAlertsCheckBox;

    // Security fields
    /** Password field for entering the current password */
    private JPasswordField currentPasswordField;
    /** Password field for entering the new password */
    private JPasswordField newPasswordField;
    /** Password field for confirming the new password */
    private JPasswordField confirmPasswordField;

    /**
     * Constructs a new SettingsPanel for the specified user.
     *
     * @param username the username of the current user
     */
    public SettingsPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new SettingsViewModel(username);
        this.viewModel.addSettingsChangeListener(this);

        LOGGER.log(Level.INFO, "Initializing SettingsPanel with ViewModel for user: {0}", username);

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

        // Add navigation buttons
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

        // Content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setPreferredSize(new Dimension(600, 400));
        contentPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Add subpanels for each settings category
        contentPanel.add(createProfilePanel(), "PROFILE");
        contentPanel.add(createPreferencesPanel(), "PREFERENCES");
        contentPanel.add(createNotificationsPanel(), "NOTIFICATIONS");
        contentPanel.add(createSecurityPanel(), "SECURITY");

        add(contentPanel);
        add(Box.createVerticalStrut(10));

        // Set default view
        cardLayout.show(contentPanel, "PROFILE");
        setActiveButton(profileButton);

        // Handle window resizing for better UX
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        // When panel is shown, enlarge window
                        SwingUtilities.invokeLater(() -> {
                            Window window = SwingUtilities.getWindowAncestor(SettingsPanel.this);
                            if (window != null) {
                                originalWindowSize = window.getSize();
                                window.setSize(700, 550);
                                window.setLocationRelativeTo(null);
                            }
                        });
                    } else {
                        // When panel is hidden, restore window size
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

    /**
     * Creates a navigation button for switching settings categories.
     *
     * @param text the button label
     * @param panelName the card name to show
     * @return the navigation button
     */
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

    /**
     * Sets the currently active navigation button for highlighting.
     *
     * @param button the button to set as active
     */
    private void setActiveButton(JButton button) {
        if (activeButton != null) {
            activeButton.setForeground(UIManager.getColor("Label.foreground"));
            activeButton.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        activeButton = button;

        activeButton.setForeground(new Color(70, 130, 180));
        activeButton.setFont(new Font("Arial", Font.BOLD, 14));
    }

    /**
     * Creates the profile settings panel.
     *
     * @return the profile panel
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create and populate text fields with values from ViewModel
        nameField = new JTextField(viewModel.getName());
        emailField = new JTextField(viewModel.getEmail());
        phoneField = new JTextField(viewModel.getPhone());

        LOGGER.log(Level.INFO, "Setting up profile panel with values - Name: {0}, Email: {1}, Phone: {2}",
                new Object[]{nameField.getText(), emailField.getText(), phoneField.getText()});

        panel.add(createLabeledField("Name:", nameField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Email:", emailField));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Phone:", phoneField));

        // Add save button
        panel.add(Box.createVerticalStrut(15));
        JButton submitButton = new JButton("Save Changes");
        submitButton.setAlignmentX(LEFT_ALIGNMENT);
        submitButton.addActionListener(e -> {
            // Save profile data via ViewModel
            viewModel.updateProfile(nameField.getText(), emailField.getText(), phoneField.getText());

            JOptionPane.showMessageDialog(
                this, "Profile information updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(submitButton);

        return panel;
    }

    /**
     * Creates the preferences settings panel (currency, theme).
     *
     * @return the preferences panel
     */
    private JPanel createPreferencesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Currency components
        currencyComboBox = new JComboBox<>(new String[]{"USD $", "RMB ¥"});
        currencySymbolField = new JTextField(5);

        // Set values based on ViewModel
        String storedCurrencyCode = viewModel.getCurrencyCode();
        String storedCurrencySymbol = viewModel.getCurrencySymbol();

        LOGGER.log(Level.INFO, "Setting up preferences panel with values - Currency: {0} ({1}), Dark Theme: {2}",
                new Object[]{storedCurrencyCode, storedCurrencySymbol, viewModel.isDarkTheme()});

        if ("USD".equals(storedCurrencyCode)) {
            currencyComboBox.setSelectedItem("USD $");
            currencySymbolField.setText(storedCurrencySymbol);
        } else if ("RMB".equals(storedCurrencyCode)) {
            currencyComboBox.setSelectedItem("RMB ¥");
            currencySymbolField.setText(storedCurrencySymbol);
        } else {
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

        // Set selection based on ViewModel
        boolean isDarkTheme = viewModel.isDarkTheme();
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

            // Update theme via ViewModel
            viewModel.updateTheme(selectDark);

            // Apply theme to UI
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

            // Update currency via ViewModel
            viewModel.updateCurrency(code, symbol);

            JOptionPane.showMessageDialog(
                this, "Currency preferences updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(applyCurrencyButton);

        return panel;
    }

    /**
     * Applies the selected theme to the application UI.
     *
     * @param darkTheme true for dark theme, false for light theme
     */
    private void applyTheme(boolean darkTheme) {
        try {
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

    /**
     * Creates the notifications settings panel.
     *
     * @return the notifications panel
     */
    private JPanel createNotificationsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create checkboxes and set their states from ViewModel
        boolean budgetAlertsEnabled = viewModel.isBudgetAlertsEnabled();
        boolean transactionAlertsEnabled = viewModel.isTransactionAlertsEnabled();

        LOGGER.log(Level.INFO, "Setting up notifications panel with values - Budget Alerts: {0}, Transaction Alerts: {1}",
                new Object[]{budgetAlertsEnabled, transactionAlertsEnabled});

        budgetAlertsCheckBox = new JCheckBox("Enable");
        budgetAlertsCheckBox.setSelected(budgetAlertsEnabled);

        transactionAlertsCheckBox = new JCheckBox("Enable");
        transactionAlertsCheckBox.setSelected(transactionAlertsEnabled);

        panel.add(createLabeledField("Budget Alerts:", budgetAlertsCheckBox));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createLabeledField("Transaction Alerts:", transactionAlertsCheckBox));

        // Save button
        panel.add(Box.createVerticalStrut(15));
        JButton saveButton = new JButton("Save Preferences");
        saveButton.setAlignmentX(LEFT_ALIGNMENT);
        saveButton.addActionListener(e -> {
            // Save notification preferences via ViewModel
            viewModel.updateNotifications(
                budgetAlertsCheckBox.isSelected(),
                transactionAlertsCheckBox.isSelected()
            );

            JOptionPane.showMessageDialog(
                this, "Notification settings saved!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(saveButton);

        return panel;
    }

    /**
     * Creates the security settings panel (password change, reset).
     *
     * @return the security panel
     */
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

        // Change password button
        panel.add(Box.createVerticalStrut(15));
        JButton changePasswordButton = new JButton("Change Password");
        changePasswordButton.setAlignmentX(LEFT_ALIGNMENT);
        changePasswordButton.addActionListener(e -> {
            try {
                // Get password values
                char[] currentPwd = currentPasswordField.getPassword();
                char[] newPwd = newPasswordField.getPassword();
                char[] confirmPwd = confirmPasswordField.getPassword();

                // Validation
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

                // Update password via ViewModel
                boolean success = viewModel.updatePassword(
                    new String(currentPwd),
                    new String(newPwd),
                    new String(confirmPwd)
                );

                if (success) {
                    // Clear password fields for security
                    currentPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");

                    JOptionPane.showMessageDialog(this, "Password changed successfully!",
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to change password. Current password may be incorrect.",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }

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

        // Add reset to defaults button
        panel.add(Box.createVerticalStrut(20));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(20));

        JLabel resetLabel = new JLabel("Reset All Settings");
        resetLabel.setFont(new Font(resetLabel.getFont().getName(), Font.BOLD, 14));
        resetLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(resetLabel);
        panel.add(Box.createVerticalStrut(10));

        JButton resetButton = new JButton("Reset to Default Values");
        resetButton.setAlignmentX(LEFT_ALIGNMENT);
        resetButton.setForeground(Color.RED);
        resetButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                this,
                "This will reset all your settings to default values. Are you sure?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                // Reset settings via ViewModel
                viewModel.resetToDefaults();

                JOptionPane.showMessageDialog(
                    this,
                    "All settings have been reset to defaults.",
                    "Settings Reset",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Update UI with default values
                updateUIFromViewModel();
            }
        });
        panel.add(resetButton);

        return panel;
    }

    /**
     * Creates a labeled field for use in settings panels.
     *
     * @param labelText the label text
     * @param field the input component
     * @return a panel containing the label and field
     */
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

    /**
     * Updates all UI components from the ViewModel.
     */
    private void updateUIFromViewModel() {
        // Profile
        nameField.setText(viewModel.getName());
        emailField.setText(viewModel.getEmail());
        phoneField.setText(viewModel.getPhone());

        // Currency
        String currencyCode = viewModel.getCurrencyCode();
        if ("USD".equals(currencyCode)) {
            currencyComboBox.setSelectedItem("USD $");
        } else if ("RMB".equals(currencyCode)) {
            currencyComboBox.setSelectedItem("RMB ¥");
        }
        currencySymbolField.setText(viewModel.getCurrencySymbol());

        // Theme
        boolean isDarkTheme = viewModel.isDarkTheme();
        darkThemeRadio.setSelected(isDarkTheme);
        lightThemeRadio.setSelected(!isDarkTheme);

        // Notifications
        budgetAlertsCheckBox.setSelected(viewModel.isBudgetAlertsEnabled());
        transactionAlertsCheckBox.setSelected(viewModel.isTransactionAlertsEnabled());

        // Security
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    /**
     * Called when settings change in the ViewModel.
     * Updates the UI to reflect the new settings.
     *
     * @param changeType the type of settings change
     */
    @Override
    public void onSettingsChanged(SettingsChangeType changeType) {
        SwingUtilities.invokeLater(() -> {
            if (changeType == SettingsChangeType.ALL) {
                updateUIFromViewModel();
            } else {
                switch (changeType) {
                    case PROFILE:
                        nameField.setText(viewModel.getName());
                        emailField.setText(viewModel.getEmail());
                        phoneField.setText(viewModel.getPhone());
                        break;
                    case CURRENCY:
                        String currencyCode = viewModel.getCurrencyCode();
                        if ("USD".equals(currencyCode)) {
                            currencyComboBox.setSelectedItem("USD $");
                        } else if ("RMB".equals(currencyCode)) {
                            currencyComboBox.setSelectedItem("RMB ¥");
                        }
                        currencySymbolField.setText(viewModel.getCurrencySymbol());
                        break;
                    case THEME:
                        boolean isDarkTheme = viewModel.isDarkTheme();
                        darkThemeRadio.setSelected(isDarkTheme);
                        lightThemeRadio.setSelected(!isDarkTheme);
                        break;
                    case NOTIFICATIONS:
                        budgetAlertsCheckBox.setSelected(viewModel.isBudgetAlertsEnabled());
                        transactionAlertsCheckBox.setSelected(viewModel.isTransactionAlertsEnabled());
                        break;
                    case SECURITY:
                        // No UI update needed for security
                        break;
                }
            }
        });
    }

    /**
     * Called when this panel is removed from its container.
     * Cleans up listeners and resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        viewModel.removeSettingsChangeListener(this);
        viewModel.cleanup();
    }
}