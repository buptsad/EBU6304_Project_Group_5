package com.example.app.ui;

import com.example.app.user_data.UserAuthService;
import com.example.app.user_data.UserBillStorage;
import com.example.app.user_data.UserSettingsStorage;
import com.example.app.user_data.UserBudgetStorage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LoginFrame provides the login and registration interface for the financial application.
 * It allows users to log in or register a new account, and initializes user data directories and files.
 * <p>
 * Features:
 * <ul>
 *   <li>Login and registration forms with navigation toggle</li>
 *   <li>Creates user data directories and default files on registration</li>
 *   <li>Initializes user storage services on successful login</li>
 *   <li>Styled navigation and error handling</li>
 * </ul>
 */
public class LoginFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());
    /** JTextField for username input */
    private JTextField usernameField;
    /** JPasswordField for password input */
    private JPasswordField passwordField;
    /** JPanel for card layout */
    private JPanel cardPanel;
    /** CardLayout for switching between login and register panels */
    private CardLayout cardLayout;
    /** JPanel for login and register forms */
    private JPanel loginPanel;
    /** JPanel for register form */
    private JPanel registerPanel;
    /** Toggle buttons for login and register */
    private JButton loginToggle;
    /** Toggle button for register */
    private JButton registerToggle;

    // Colors and borders for navigation highlighting (matched from MainFrame)
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);
    private static final Border SELECTED_BORDER = new MatteBorder(0, 0, 2, 0, SELECTED_COLOR);
    private static final Border DEFAULT_BORDER = new EmptyBorder(0, 0, 2, 0);

    /**
     * Constructs the LoginFrame and initializes the UI.
     */
    public LoginFrame() {
        setTitle("Finance Manager - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);

        // Create user_data base directory if not exists
        createUserDataBaseDir();

        // Create main container with card layout
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        // Create toggle switch panel with FlowLayout centered
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        togglePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        // Create toggle buttons
        loginToggle = createStyledButton("Login", "login");
        registerToggle = createStyledButton("Register", "register");

        // Add buttons to the panel
        togglePanel.add(loginToggle);
        togglePanel.add(registerToggle);

        // Create login and register panels
        createLoginPanel();
        createRegisterPanel();

        // Add panels to card layout
        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");

        // Create main layout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(togglePanel, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);

        // Set content pane
        setContentPane(mainPanel);

        // Set active page initially
        setActiveToggle("login");

        setVisible(true);
    }

    /**
     * Creates the base user_data directory if it does not exist.
     */
    private void createUserDataBaseDir() {
        File baseDir = new File(".\\user_data");
        if (!baseDir.exists()) {
            if (baseDir.mkdirs()) {
                LOGGER.log(Level.INFO, "Created base user_data directory");
            } else {
                LOGGER.log(Level.SEVERE, "Failed to create base user_data directory");
            }
        }
    }

    /**
     * Creates a styled navigation button for toggling between login and register panels.
     *
     * @param text the button label
     * @param pageKey the card layout key
     * @return the styled JButton
     */
    private JButton createStyledButton(String text, String pageKey) {
        JButton button = new JButton(text);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(DEFAULT_BORDER);
        button.setToolTipText("Switch to " + text + " page");

        button.addActionListener(e -> {
            cardLayout.show(cardPanel, pageKey);
            setActiveToggle(pageKey);
        });

        return button;
    }

    /**
     * Sets the active toggle button style based on the current page.
     *
     * @param pageKey the card layout key ("login" or "register")
     */
    private void setActiveToggle(String pageKey) {
        // Reset all button styles
        loginToggle.setBorder(DEFAULT_BORDER);
        loginToggle.setForeground(UIManager.getColor("Button.foreground"));
        registerToggle.setBorder(DEFAULT_BORDER);
        registerToggle.setForeground(UIManager.getColor("Button.foreground"));

        // Highlight the active button
        if (pageKey.equals("login")) {
            loginToggle.setBorder(SELECTED_BORDER);
            loginToggle.setForeground(SELECTED_COLOR);
        } else {
            registerToggle.setBorder(SELECTED_BORDER);
            registerToggle.setForeground(SELECTED_COLOR);
        }
    }

    /**
     * Creates the login panel UI and logic.
     */
    private void createLoginPanel() {
        loginPanel = new JPanel();
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
        loginButton.setToolTipText("Click to login to your account");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        // Add action listener to login button
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (UserAuthService.authenticateUser(username, password)) {
                // On successful authentication, initialize storage services
                UserBillStorage.setUsername(username);
                UserSettingsStorage.setUsername(username);
                UserBudgetStorage.setUsername(username);

                // Create and show main window
                dispose();
                EventQueue.invokeLater(() -> new MainFrame(username).setVisible(true));
            } else {
                // Show error message
                JOptionPane.showMessageDialog(this, "Invalid username or password",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Validates the user credentials.
     *
     * @param username the username
     * @param password the password
     * @return true if authenticated, false otherwise
     */
    private boolean validateUser(String username, String password) {
        // Authenticate user
        boolean isAuthenticated = UserAuthService.authenticateUser(username, password);

        if (isAuthenticated) {
            // On successful authentication, initialize storage services
            UserBillStorage.setUsername(username);
            UserSettingsStorage.setUsername(username);
            // Optionally save last login user
            //saveLastLoginUser(username);
        }

        return isAuthenticated;
    }

    /**
     * Creates the registration panel UI and logic.
     */
    private void createRegisterPanel() {
        registerPanel = new JPanel();
        registerPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        registerPanel.add(titleLabel, gbc);

        // Username label and field
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        registerPanel.add(usernameLabel, gbc);

        JTextField regUsernameField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        registerPanel.add(regUsernameField, gbc);

        // Email label and field
        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        registerPanel.add(emailField, gbc);

        // Phone label and field
        JLabel phoneLabel = new JLabel("Phone:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        registerPanel.add(phoneLabel, gbc);

        JTextField phoneField = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 3;
        registerPanel.add(phoneField, gbc);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerPanel.add(passwordLabel, gbc);

        JPasswordField regPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 4;
        registerPanel.add(regPasswordField, gbc);

        // Confirm Password label and field
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        gbc.gridx = 0;
        gbc.gridy = 5;
        registerPanel.add(confirmPasswordLabel, gbc);

        JPasswordField confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        gbc.gridy = 5;
        registerPanel.add(confirmPasswordField, gbc);

        // Register button
        JButton registerButton = new JButton("Register");
        registerButton.setToolTipText("Click to create a new account");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        registerPanel.add(registerButton, gbc);

        // Add action listener to register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = regUsernameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String password = new String(regPasswordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Simple validation
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Please fill in all required fields",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Passwords do not match",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // Check if username already exists
                File userDir = new File(".\\user_data\\" + username);
                if (userDir.exists()) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Username already exists. Please choose a different username.",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                // Create user directory and files
                if (createUserAccount(username, email, phone, password)) {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Registration successful! You can now login.",
                            "Account Created",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Clear registration form
                    regUsernameField.setText("");
                    emailField.setText("");
                    phoneField.setText("");
                    regPasswordField.setText("");
                    confirmPasswordField.setText("");

                    // Switch to login panel
                    setActiveToggle("login");
                    cardLayout.show(cardPanel, "login");
                } else {
                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Error creating user account. Please try again.",
                            "Registration Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }


    /**
     * Creates the user account directory and default files.
     *
     * @param username the username
     * @param email the email address
     * @param phone the phone number
     * @param password the password (plain text, should be hashed in production)
     * @return true if account creation succeeded, false otherwise
     */
    private boolean createUserAccount(String username, String email, String phone, String password) {
        try {
            // Create user directory
            File userDir = new File(".\\user_data\\" + username);
            if (!userDir.exists() && !userDir.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Could not create user directory for: {0}", username);
                return false;
            }

            // Create user_settings.properties file
            File settingsFile = new File(userDir, "user_settings.properties");
            Properties properties = new Properties();

            // Set user properties
            properties.setProperty("user.name", username);
            properties.setProperty("user.email", email);
            properties.setProperty("user.phone", phone);
            properties.setProperty("security.password.hash", password);

            // Set default properties
            properties.setProperty("currency.code", "USD");
            properties.setProperty("currency.symbol", "$");
            properties.setProperty("theme.dark", "false");
            properties.setProperty("notifications.budget.enabled", "true");
            properties.setProperty("notifications.transaction.enabled", "true");

            // Save properties file
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                properties.store(fos, "Financial App User Settings");
            }

            // Create empty user_bill.csv file
            File billsFile = new File(userDir, "user_bill.csv");
            try (FileOutputStream fos = new FileOutputStream(billsFile)) {
                fos.write("Date,Description,Category,Amount,Confirmed\n".getBytes());
            }

            // Create empty user_budgets.csv file
            File budgetsFile = new File(userDir, "user_budgets.csv");
            try (FileOutputStream fos = new FileOutputStream(budgetsFile)) {
                fos.write("Category,Amount,StartDate,EndDate\n".getBytes());
            }

            LOGGER.log(Level.INFO, "Created user account for: {0}", username);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error creating user account", ex);
            return false;
        }
    }

    /**
     * Main method for testing the LoginFrame.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}