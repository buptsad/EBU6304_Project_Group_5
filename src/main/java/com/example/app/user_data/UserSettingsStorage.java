package com.example.app.user_data;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage handler for user settings.
 * This class manages the physical storage of user settings in the user_data package.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and saves settings to a user-specific properties file</li>
 *   <li>Initializes storage with default settings if needed</li>
 *   <li>Provides methods to get and set the current username and file path</li>
 * </ul>
 
 */
public class UserSettingsStorage {
    /** Private constructor to prevent instantiation */
    private UserSettingsStorage() {
        // Prevent instantiation
    }
    private static final Logger LOGGER = Logger.getLogger(UserSettingsStorage.class.getName());
    private static final String SETTINGS_FILENAME = "user_settings.properties";
    private static File settingsFile;
    private static String username;

    /**
     * Sets the current username and updates the file path.
     * @param username The current user's username
     */
    public static void setUsername(String username) {
        UserSettingsStorage.username = username;
        // Update file path to user-specific path
        String packagePath = ".\\user_data\\" + username;
        settingsFile = new File(packagePath, SETTINGS_FILENAME);

        // Ensure file exists
        initializeStorage();
    }

    /**
     * Initializes the storage directory and file.
     * Creates the directory and file if they do not exist, and writes default settings if needed.
     */
    private static void initializeStorage() {
        File directory = settingsFile.getParentFile();

        // Create directory if it doesn't exist
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.log(Level.INFO, "Created settings directory at: {0}", directory.getAbsolutePath());
            } else {
                LOGGER.log(Level.SEVERE, "Failed to create settings directory at: {0}", directory.getAbsolutePath());
                return;
            }
        }

        // Create default settings file if it doesn't exist
        if (!settingsFile.exists()) {
            try {
                if (settingsFile.createNewFile()) {
                    // Create default settings content
                    Properties defaultProperties = new Properties();

                    // Default profile settings
                    defaultProperties.setProperty("user.name", username != null ? username : "");
                    defaultProperties.setProperty("user.email", "");
                    defaultProperties.setProperty("user.phone", "");

                    // Default preferences
                    defaultProperties.setProperty("currency.code", "USD");
                    defaultProperties.setProperty("currency.symbol", "$");
                    defaultProperties.setProperty("theme.dark", "false");

                    // Default notifications
                    defaultProperties.setProperty("notifications.budget.enabled", "true");
                    defaultProperties.setProperty("notifications.transaction.enabled", "true");

                    // Default security
                    defaultProperties.setProperty("security.password.hash", "");

                    // Save default properties to file
                    try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
                        defaultProperties.store(fos, "Default Financial App User Settings");
                        LOGGER.log(Level.INFO, "Created default settings file at: {0}", settingsFile.getAbsolutePath());
                    }
                } else {
                    LOGGER.log(Level.SEVERE, "Failed to create settings file at: {0}", settingsFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating settings file", e);
            }
        } else {
            LOGGER.log(Level.INFO, "Settings file already exists at: {0}", settingsFile.getAbsolutePath());
        }
    }

    /**
     * Gets the path to the settings file.
     * @return Path to the settings file
     */
    public static String getSettingsFilePath() {
        return settingsFile.getAbsolutePath();
    }

    /**
     * Loads settings from the file.
     * @return Properties object with loaded settings or null if loading failed
     */
    public static Properties loadSettings() {
        Properties properties = new Properties();

        // Confirm file exists
        if (!settingsFile.exists()) {
            LOGGER.log(Level.WARNING, "Settings file does not exist: {0}", settingsFile.getAbsolutePath());
            return null;
        }

        try (FileInputStream fis = new FileInputStream(settingsFile)) {
            properties.load(fis);
            LOGGER.log(Level.INFO, "Successfully loaded settings from: {0}", settingsFile.getAbsolutePath());
            LOGGER.log(Level.INFO, "Loaded {0} settings", properties.size());
            return properties;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading settings from file: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Saves settings to the file.
     * @param properties Properties object with settings to save
     * @return true if successful, false otherwise
     */
    public static boolean saveSettings(Properties properties) {
        try (FileOutputStream fos = new FileOutputStream(settingsFile)) {
            properties.store(fos, "Financial App User Settings");
            LOGGER.log(Level.INFO, "Successfully saved settings to: {0}", settingsFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving settings to file: " + e.getMessage(), e);
            return false;
        }
    }
}