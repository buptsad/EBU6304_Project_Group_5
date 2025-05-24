package com.example.app.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * Manages the application's theme settings (dark or light mode).
 * <p>
 * This singleton class loads and saves the theme preference to a configuration file,
 * and provides methods to get or set the current theme.
 
 */
public class ThemeManager {
    /** The configuration file name for storing theme settings */
    private static final String CONFIG_FILE = "theme.properties";
    /** The key used in the properties file for the theme */
    private static final String THEME_KEY = "theme";
    /** The value representing dark theme */
    private static final String DARK_THEME = "dark";
    /** The value representing light theme */
    private static final String LIGHT_THEME = "light";

    /** Singleton instance */
    private static ThemeManager instance;
    /** The current theme value ("dark" or "light") */
    private String currentTheme;

    /**
     * Private constructor. Loads the theme from the configuration file.
     */
    private ThemeManager() {
        // Load theme setting from configuration file
        loadTheme();
    }

    /**
     * Gets the singleton instance of ThemeManager.
     *
     * @return the ThemeManager instance
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Checks if the current theme is dark.
     *
     * @return true if dark theme, false if light theme
     */
    public boolean isDarkTheme() {
        return DARK_THEME.equals(currentTheme);
    }

    /**
     * Sets the theme and saves it to the configuration file.
     *
     * @param isDark true for dark theme, false for light theme
     */
    public void setTheme(boolean isDark) {
        currentTheme = isDark ? DARK_THEME : LIGHT_THEME;
        saveTheme();
    }

    /**
     * Loads the theme setting from the configuration file.
     */
    private void loadTheme() {
        Properties properties = new Properties();
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                currentTheme = properties.getProperty(THEME_KEY, DARK_THEME);
            } catch (Exception e) {
                System.err.println("Error loading theme configuration: " + e.getMessage());
                currentTheme = DARK_THEME; // Default to dark theme
            }
        } else {
            currentTheme = DARK_THEME; // Default to dark theme
        }
    }

    /**
     * Saves the current theme setting to the configuration file.
     */
    private void saveTheme() {
        Properties properties = new Properties();
        properties.setProperty(THEME_KEY, currentTheme);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Theme Configuration");
        } catch (Exception e) {
            System.err.println("Error saving theme configuration: " + e.getMessage());
        }
    }
}