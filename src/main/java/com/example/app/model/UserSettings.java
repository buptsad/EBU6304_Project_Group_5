package com.example.app.model;

import com.example.app.user_data.UserSettingsStorage;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Used to save user settings configured in the settings interface
 * Uses singleton pattern to ensure there is only one user settings instance globally
 */
public class UserSettings {
    private static final Logger LOGGER = Logger.getLogger(UserSettings.class.getName());
    // Use the path from UserSettingsStorage instead of defining it here
    private static final String SETTINGS_FILE_PATH = UserSettingsStorage.getSettingsFilePath();
    private static UserSettings instance;
    private final Properties properties;
    
    // Profile settings
    private static final String NAME = "user.name";
    private static final String EMAIL = "user.email";
    private static final String PHONE = "user.phone";
    
    // Preference settings
    private static final String CURRENCY_CODE = "currency.code";
    private static final String CURRENCY_SYMBOL = "currency.symbol";
    private static final String THEME_DARK = "theme.dark";
    
    // Notification settings
    private static final String BUDGET_ALERTS_ENABLED = "notifications.budget.enabled";
    private static final String TRANSACTION_ALERTS_ENABLED = "notifications.transaction.enabled";
    
    // Security settings
    private static final String PASSWORD_HASH = "security.password.hash";
    
    /**
     * Private constructor, initializes properties and loads settings
     */
    private UserSettings() {
        properties = new Properties();
        loadSettings();
    }
    
    /**
     * Get UserSettings singleton instance
     * @return UserSettings instance
     */
    public static synchronized UserSettings getInstance() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }
    
    /**
     * Load settings from storage
     */
    private void loadSettings() {
        // Try to load settings from the UserSettingsStorage
        Properties loadedProps = UserSettingsStorage.loadSettings();
        
        if (loadedProps != null) {
            // Copy all properties from loaded settings
            properties.clear();
            properties.putAll(loadedProps);
            LOGGER.log(Level.INFO, "Successfully loaded settings from storage");
        } else {
            // If loading fails, initialize with defaults
            LOGGER.log(Level.WARNING, "Failed to load settings, initializing defaults");
            initializeDefaultSettings();
        }
    }
    
    /**
     * Initialize default settings
     */
    private void initializeDefaultSettings() {
        // Profile default values
        properties.setProperty(NAME, "");
        properties.setProperty(EMAIL, "");
        properties.setProperty(PHONE, "");
        
        // Preference default values
        properties.setProperty(CURRENCY_CODE, "USD");
        properties.setProperty(CURRENCY_SYMBOL, "$");
        properties.setProperty(THEME_DARK, "false");
        
        // Notification default values
        properties.setProperty(BUDGET_ALERTS_ENABLED, "true");
        properties.setProperty(TRANSACTION_ALERTS_ENABLED, "true");
        
        // Security default values
        properties.setProperty(PASSWORD_HASH, "");
        
        // Save default settings
        saveSettings();
    }
    
    /**
     * Save current settings
     */
    public void saveSettings() {
        boolean success = UserSettingsStorage.saveSettings(properties);
        if (!success) {
            LOGGER.log(Level.SEVERE, "Failed to save settings");
        }
    }
    
    // Profile settings methods
    
    public String getName() {
        return properties.getProperty(NAME, "");
    }
    
    public void setName(String name) {
        properties.setProperty(NAME, name != null ? name : "");
    }
    
    public String getEmail() {
        return properties.getProperty(EMAIL, "");
    }
    
    public void setEmail(String email) {
        properties.setProperty(EMAIL, email != null ? email : "");
    }
    
    public String getPhone() {
        return properties.getProperty(PHONE, "");
    }
    
    public void setPhone(String phone) {
        properties.setProperty(PHONE, phone != null ? phone : "");
    }
    
    // Preference settings methods
    
    public String getCurrencyCode() {
        return properties.getProperty(CURRENCY_CODE, "USD");
    }
    
    public void setCurrencyCode(String currencyCode) {
        properties.setProperty(CURRENCY_CODE, currencyCode != null ? currencyCode : "USD");
    }
    
    public String getCurrencySymbol() {
        return properties.getProperty(CURRENCY_SYMBOL, "$");
    }
    
    public void setCurrencySymbol(String currencySymbol) {
        properties.setProperty(CURRENCY_SYMBOL, currencySymbol != null ? currencySymbol : "$");
    }
    
    public boolean isDarkTheme() {
        return Boolean.parseBoolean(properties.getProperty(THEME_DARK, "false"));
    }
    
    public void setDarkTheme(boolean darkTheme) {
        properties.setProperty(THEME_DARK, Boolean.toString(darkTheme));
    }
    
    // Notification settings methods
    
    public boolean isBudgetAlertsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(BUDGET_ALERTS_ENABLED, "true"));
    }
    
    public void setBudgetAlertsEnabled(boolean enabled) {
        properties.setProperty(BUDGET_ALERTS_ENABLED, Boolean.toString(enabled));
    }
    
    public boolean isTransactionAlertsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(TRANSACTION_ALERTS_ENABLED, "true"));
    }
    
    public void setTransactionAlertsEnabled(boolean enabled) {
        properties.setProperty(TRANSACTION_ALERTS_ENABLED, Boolean.toString(enabled));
    }
    
    // Security settings methods
    
    public String getPasswordHash() {
        return properties.getProperty(PASSWORD_HASH, "");
    }
    
    public void setPasswordHash(String passwordHash) {
        properties.setProperty(PASSWORD_HASH, passwordHash != null ? passwordHash : "");
    }
    
    /**
     * Reset all settings to default values
     */
    public void resetToDefaults() {
        initializeDefaultSettings();
    }
}