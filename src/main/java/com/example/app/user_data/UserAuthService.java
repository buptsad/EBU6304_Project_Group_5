package com.example.app.user_data;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 用户认证和注册服务
 */
public class UserAuthService {
    private static final Logger LOGGER = Logger.getLogger(UserAuthService.class.getName());
    private static final String USER_DATA_BASE_PATH = ".\\user_data";

    /**
     * 验证用户登录
     * @param username 用户名
     * @param password 密码
     * @return 如果验证成功返回true，否则返回false
     */
    public static boolean authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            return false;
        }

        // 构建用户目录路径
        String userDirPath = USER_DATA_BASE_PATH + "\\" + username;
        File userDir = new File(userDirPath);
        
        // 检查用户目录是否存在
        if (!userDir.exists() || !userDir.isDirectory()) {
            LOGGER.log(Level.INFO, "User directory not found: {0}", userDirPath);
            return false;
        }

        // 获取并验证用户设置文件
        File settingsFile = new File(userDir, "user_settings.properties");
        if (!settingsFile.exists() || !settingsFile.isFile()) {
            LOGGER.log(Level.WARNING, "User settings file not found for: {0}", username);
            return false;
        }

        try {
            // 加载用户设置
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            }

            // 验证密码
            String storedHash = properties.getProperty("security.password.hash", "");
            if (storedHash.equals(password)) { // 简单实现，实际应使用更安全的密码哈希算法
                LOGGER.log(Level.INFO, "User {0} authenticated successfully", username);
                return true;
            } else {
                LOGGER.log(Level.INFO, "Authentication failed for user: {0}", username);
                return false;
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading user settings file", e);
            return false;
        }
    }

    /**
     * 注册新用户
     * @param username 用户名
     * @param password 密码
     * @param email 用户邮箱
     * @return 如果注册成功返回true，否则返回false
     */
    public static boolean registerUser(String username, String password, String email) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return false;
        }

        // 构建用户目录路径
        String userDirPath = USER_DATA_BASE_PATH + "\\" + username;
        File userDir = new File(userDirPath);
        
        // 检查用户是否已存在
        if (userDir.exists()) {
            LOGGER.log(Level.INFO, "User already exists: {0}", username);
            return false;
        }

        try {
            // 创建用户目录
            if (!userDir.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Failed to create user directory: {0}", userDirPath);
                return false;
            }
            
            // 创建用户设置文件
            Properties userProperties = new Properties();
            
            // 个人资料设置
            userProperties.setProperty("user.name", username);
            userProperties.setProperty("user.email", email);
            userProperties.setProperty("user.phone", "");
            
            // 首选项默认设置
            userProperties.setProperty("currency.code", "USD");
            userProperties.setProperty("currency.symbol", "$");
            userProperties.setProperty("theme.dark", "false");
            
            // 默认通知设置
            userProperties.setProperty("notifications.budget.enabled", "true");
            userProperties.setProperty("notifications.transaction.enabled", "true");
            
            // 安全设置 - 简单存储密码，实际应使用哈希
            userProperties.setProperty("security.password.hash", password);
            
            // 保存用户设置
            File settingsFile = new File(userDir, "user_settings.properties");
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                userProperties.store(fos, "Financial App User Settings");
            }
            
            // 创建空的账单和预算文件
            createEmptyFile(new File(userDir, "user_bill.csv"), "Date,Description,Category,Amount,Confirmed");
            createEmptyFile(new File(userDir, "user_budgets.csv"), "Category,MonthlyLimit,CurrentSpent,Period");
            
            LOGGER.log(Level.INFO, "User {0} registered successfully", username);
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during user registration", e);
            return false;
        }
    }
    
    /**
     * 创建空文件并写入标题行
     */
    private static void createEmptyFile(File file, String header) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(header);
        }
    }
    
    /**
     * 检查用户名是否可用
     * @param username 要检查的用户名
     * @return 如果用户名可用返回true，否则返回false
     */
    public static boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String userDirPath = USER_DATA_BASE_PATH + "\\" + username;
        File userDir = new File(userDirPath);
        return !userDir.exists();
    }
}