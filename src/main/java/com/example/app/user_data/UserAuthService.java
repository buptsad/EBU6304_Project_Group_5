package com.example.app.user_data;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides user authentication and registration services.
 * <p>
 * This class handles user login verification, registration, and username availability checks.
 * User data is stored in the user_data directory, with each user having a separate folder and settings file.
 
 */
public class UserAuthService {

    /** Private constructor to prevent instantiation */
    private UserAuthService() {
        // Prevent instantiation
    }
    private static final Logger LOGGER = Logger.getLogger(UserAuthService.class.getName());
    private static final String USER_DATA_BASE_PATH = ".\\user_data";

    /**
     * Authenticates a user by verifying the username and password.
     *
     * @param username the username
     * @param password the password
     * @return true if authentication is successful, false otherwise
     */
    public static boolean authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null) {
            return false;
        }

        // Build user directory path
        String userDirPath = USER_DATA_BASE_PATH + "\\" + username;
        File userDir = new File(userDirPath);

        // Check if user directory exists
        if (!userDir.exists() || !userDir.isDirectory()) {
            LOGGER.log(Level.INFO, "User directory not found: {0}", userDirPath);
            return false;
        }

        // Get and verify user settings file
        File settingsFile = new File(userDir, "user_settings.properties");
        if (!settingsFile.exists() || !settingsFile.isFile()) {
            LOGGER.log(Level.WARNING, "User settings file not found for: {0}", username);
            return false;
        }

        try {
            // Load user settings
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            }

            // Verify password (simple implementation, should use secure hash in production)
            String storedHash = properties.getProperty("security.password.hash", "");
            if (storedHash.equals(password)) {
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
     * Registers a new user with the given username, password, and email.
     *
     * @param username the username
     * @param password the password
     * @param email    the user's email
     * @return true if registration is successful, false otherwise
     */
    public static boolean registerUser(String username, String password, String email) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return false;
        }

        // Build user directory path
        String userDirPath = USER_DATA_BASE_PATH + "\\" + username;
        File userDir = new File(userDirPath);

        // Check if user already exists
        if (userDir.exists()) {
            LOGGER.log(Level.INFO, "User already exists: {0}", username);
            return false;
        }

        try {
            // Create user directory
            if (!userDir.mkdirs()) {
                LOGGER.log(Level.SEVERE, "Failed to create user directory: {0}", userDirPath);
                return false;
            }

            // Create user settings file
            Properties userProperties = new Properties();

            // Profile settings
            userProperties.setProperty("user.name", username);
            userProperties.setProperty("user.email", email);
            userProperties.setProperty("user.phone", "");

            // Default preferences
            userProperties.setProperty("currency.code", "USD");
            userProperties.setProperty("currency.symbol", "$");
            userProperties.setProperty("theme.dark", "false");

            // Default notification settings
            userProperties.setProperty("notifications.budget.enabled", "true");
            userProperties.setProperty("notifications.transaction.enabled", "true");

            // Security settings - simple password storage (should use hash in production)
            userProperties.setProperty("security.password.hash", password);

            // Save user settings
            File settingsFile = new File(userDir, "user_settings.properties");
            try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                userProperties.store(fos, "Financial App User Settings");
            }

            // Create empty bill and budget files
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
     * Creates an empty file and writes the header line.
     *
     * @param file   the file to create
     * @param header the header line to write
     * @throws IOException if an I/O error occurs
     */
    private static void createEmptyFile(File file, String header) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println(header);
        }
    }

    /**
     * Checks if a username is available for registration.
     *
     * @param username the username to check
     * @return true if the username is available, false otherwise
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