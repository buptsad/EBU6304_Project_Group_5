package com.example.app.ui;

import com.example.app.user_data.UserAuthService;
import com.example.app.user_data.UserBillStorage;
import com.example.app.user_data.UserSettingsStorage;


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

public class LoginFrame extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private JPanel loginPanel;
    private JPanel registerPanel;
    private JButton loginToggle;
    private JButton registerToggle;
    
    // Colors and borders for navigation highlighting (matched from MainFrame)
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);
    private static final Border SELECTED_BORDER = new MatteBorder(0, 0, 2, 0, SELECTED_COLOR);
    private static final Border DEFAULT_BORDER = new EmptyBorder(0, 0, 2, 0);
    
    public LoginFrame() {
        setTitle("Finance Manager - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        
        // 创建 user_data 基础目录
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
        
        // Create login panel
        createLoginPanel();
        
        // Create register panel
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
                // 成功认证后，初始化存储服务
                UserBillStorage.setUsername(username);
                UserSettingsStorage.setUsername(username);
                
                // 创建并显示主窗口
                dispose();
                EventQueue.invokeLater(() -> new MainFrame(username).setVisible(true));
            } else {
                // 显示错误消息
                JOptionPane.showMessageDialog(this, "Invalid username or password", 
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private boolean validateUser(String username, String password) {
        // 验证用户身份
        boolean isAuthenticated = UserAuthService.authenticateUser(username, password);
        
        if (isAuthenticated) {
            // 成功认证后，初始化存储服务
            UserBillStorage.setUsername(username);
            UserSettingsStorage.setUsername(username);
            
            // 保存最后一次登录的用户名
            //saveLastLoginUser(username);
        }
        
        return isAuthenticated;
    }
    
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
                
                // 简单验证
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
                
                // 检查用户名是否已存在
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
                
                // 创建用户目录和文件
                if (createUserAccount(username, email, phone, password)) {
                    JOptionPane.showMessageDialog(
                        LoginFrame.this,
                        "Registration successful! You can now login.",
                        "Account Created",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // 清空注册表单
                    regUsernameField.setText("");
                    emailField.setText("");
                    phoneField.setText("");
                    regPasswordField.setText("");
                    confirmPasswordField.setText("");
                    
                    // 切换到登录面板
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
    
    private boolean createUserAccount(String username, String email, String phone, String password) {
        try {
            // 创建用户目录
            File userDir = new File(".\\user_data\\" + username);
            if (!userDir.exists() && !userDir.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Could not create user directory for: {0}", username);
                return false;
            }
            
            // 创建 user_settings.properties 文件
            File settingsFile = new File(userDir, "user_settings.properties");
            Properties properties = new Properties();
            
            // 设置用户属性
            properties.setProperty("user.name", username);
            properties.setProperty("user.email", email);
            properties.setProperty("user.phone", phone);
            properties.setProperty("security.password.hash", password);
            
            // 设置默认属性
            properties.setProperty("currency.code", "USD");
            properties.setProperty("currency.symbol", "$");
            properties.setProperty("theme.dark", "false");
            properties.setProperty("notifications.budget.enabled", "true");
            properties.setProperty("notifications.transaction.enabled", "true");
            
            // 保存属性文件
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                properties.store(fos, "Financial App User Settings");
            }
            
            // 创建空的 user_bill.csv 文件
            File billsFile = new File(userDir, "user_bill.csv");
            try (FileOutputStream fos = new FileOutputStream(billsFile)) {
                fos.write("Date,Description,Category,Amount,Confirmed\n".getBytes());
            }
            
            // 创建空的 user_budgets.csv 文件
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
    
    // Main method for testing
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