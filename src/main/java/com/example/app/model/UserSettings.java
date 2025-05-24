package com.example.app.model;

import com.example.app.user_data.UserSettingsStorage;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages user settings configured in the settings interface.
 * Implements the singleton pattern to ensure there is only one user settings instance globally.
 * <p>
 * This class handles loading, saving, and accessing all user preferences including
 * profile information, UI preferences, notification settings, and security settings.
 
 */
public class UserSettings {
    private static final Logger LOGGER = Logger.getLogger(UserSettings.class.getName());
    // Use the path from UserSettingsStorage instead of defining it here
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
     * Private constructor that initializes properties and loads settings.
     * Part of the singleton pattern implementation.
     */
    private UserSettings() {
        properties = new Properties();
        loadSettings();
    }
    
    /**
     * Returns the singleton instance of the UserSettings class.
     * If the instance doesn't exist yet, it will be created.
     *
     * @return the singleton instance of UserSettings
     */
    public static synchronized UserSettings getInstance() {
        if (instance == null) {
            instance = new UserSettings();
        }
        return instance;
    }
    
    /**
     * Loads settings from persistent storage.
     * If loading fails, initializes with default settings.
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
     * Initializes all settings with default values.
     * Called when settings cannot be loaded from storage.
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
     * Saves current settings to persistent storage.
     * Logs an error message if saving fails.
     */
    public void saveSettings() {
        boolean success = UserSettingsStorage.saveSettings(properties);
        if (!success) {
            LOGGER.log(Level.SEVERE, "Failed to save settings");
        }
    }
    
    // Profile settings methods
    
    /**
     * Gets the user's name.
     *
     * @return the user's name or empty string if not set
     */
    public String getName() {
        return properties.getProperty(NAME, "");
    }
    
    /**
     * Sets the user's name.
     *
     * @param name the name to set, null values are converted to empty string
     */
    public void setName(String name) {
        properties.setProperty(NAME, name != null ? name : "");
    }
    
    /**
     * Gets the user's email address.
     *
     * @return the user's email or empty string if not set
     */
    public String getEmail() {
        return properties.getProperty(EMAIL, "");
    }
    
    /**
     * Sets the user's email address.
     *
     * @param email the email to set, null values are converted to empty string
     */
    public void setEmail(String email) {
        properties.setProperty(EMAIL, email != null ? email : "");
    }
    
    /**
     * Gets the user's phone number.
     *
     * @return the user's phone number or empty string if not set
     */
    public String getPhone() {
        return properties.getProperty(PHONE, "");
    }
    
    /**
     * Sets the user's phone number.
     *
     * @param phone the phone number to set, null values are converted to empty string
     */
    public void setPhone(String phone) {
        properties.setProperty(PHONE, phone != null ? phone : "");
    }
    
    // Preference settings methods
    
    /**
     * Gets the currency code for the application.
     *
     * @return the currency code, defaults to "USD" if not set
     */
    public String getCurrencyCode() {
        return properties.getProperty(CURRENCY_CODE, "USD");
    }
    
    /**
     * Sets the currency code for the application.
     *
     * @param currencyCode the currency code to set, null values are converted to "USD"
     */
    public void setCurrencyCode(String currencyCode) {
        properties.setProperty(CURRENCY_CODE, currencyCode != null ? currencyCode : "USD");
    }
    
    /**
     * Gets the currency symbol for display in the application.
     *
     * @return the currency symbol, defaults to "$" if not set
     */
    public String getCurrencySymbol() {
        return properties.getProperty(CURRENCY_SYMBOL, "$");
    }
    
    /**
     * Sets the currency symbol for display in the application.
     *
     * @param currencySymbol the currency symbol to set, null values are converted to "$"
     */
    public void setCurrencySymbol(String currencySymbol) {
        properties.setProperty(CURRENCY_SYMBOL, currencySymbol != null ? currencySymbol : "$");
    }
    
    /**
     * Checks if the dark theme is enabled.
     *
     * @return true if dark theme is enabled, false otherwise
     */
    public boolean isDarkTheme() {
        return Boolean.parseBoolean(properties.getProperty(THEME_DARK, "false"));
    }
    
    /**
     * Sets whether the dark theme should be enabled.
     *
     * @param darkTheme true to enable dark theme, false to disable
     */
    public void setDarkTheme(boolean darkTheme) {
        properties.setProperty(THEME_DARK, Boolean.toString(darkTheme));
    }
    
    // Notification settings methods
    
    /**
     * Checks if budget alerts are enabled.
     *
     * @return true if budget alerts are enabled, false otherwise
     */
    public boolean isBudgetAlertsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(BUDGET_ALERTS_ENABLED, "true"));
    }
    
    /**
     * Sets whether budget alerts should be enabled.
     *
     * @param enabled true to enable budget alerts, false to disable
     */
    public void setBudgetAlertsEnabled(boolean enabled) {
        properties.setProperty(BUDGET_ALERTS_ENABLED, Boolean.toString(enabled));
    }
    
    /**
     * Checks if transaction alerts are enabled.
     *
     * @return true if transaction alerts are enabled, false otherwise
     */
    public boolean isTransactionAlertsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(TRANSACTION_ALERTS_ENABLED, "true"));
    }
    
    /**
     * Sets whether transaction alerts should be enabled.
     *
     * @param enabled true to enable transaction alerts, false to disable
     */
    public void setTransactionAlertsEnabled(boolean enabled) {
        properties.setProperty(TRANSACTION_ALERTS_ENABLED, Boolean.toString(enabled));
    }
    
    // Security settings methods
    
    /**
     * Gets the user's password hash.
     *
     * @return the password hash or empty string if not set
     */
    public String getPasswordHash() {
        return properties.getProperty(PASSWORD_HASH, "");
    }
    
    /**
     * Sets the user's password hash.
     *
     * @param passwordHash the password hash to set, null values are converted to empty string
     */
    public void setPasswordHash(String passwordHash) {
        properties.setProperty(PASSWORD_HASH, passwordHash != null ? passwordHash : "");
    }
    
    /**
     * Resets all settings to their default values.
     */
    public void resetToDefaults() {
        initializeDefaultSettings();
    }
}