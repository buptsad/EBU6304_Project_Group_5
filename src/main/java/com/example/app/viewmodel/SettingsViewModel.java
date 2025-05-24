package com.example.app.viewmodel;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.UserSettings;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.ThemeManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for SettingsPanel following the MVVM pattern.
 * Acts as an intermediary between the SettingsPanel (View) and the UserSettings (Model).
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages user settings</li>
 *   <li>Provides access to profile, currency, theme, notification, and security settings</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of settings change listeners</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class SettingsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(SettingsViewModel.class.getName());
    private final UserSettings userSettings;
    private final List<SettingsChangeListener> listeners = new ArrayList<>();
    private String username;

    /**
     * Interface for components that need to be notified of settings changes.
     */
    public interface SettingsChangeListener {
        /**
         * Called when settings have changed.
         * @param changeType the type of settings change
         */
        void onSettingsChanged(SettingsChangeType changeType);
    }

    /**
     * Types of settings changes.
     */
    public enum SettingsChangeType {
        /** Profile settings changed */
        PROFILE,
        /** Currency settings changed */
        CURRENCY,
        /** Theme settings changed */
        THEME,
        /** Notification settings changed */
        NOTIFICATIONS,
        /** Security settings changed */
        SECURITY,
        /** All settings changed */
        ALL
    }

    /**
     * Constructs a SettingsViewModel for the specified user.
     * Registers for data refresh events.
     *
     * @param username the username for which to manage settings
     */
    public SettingsViewModel(String username) {
        this.username = username;
        this.userSettings = UserSettings.getInstance();

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }

    /**
     * Adds a listener for settings changes.
     *
     * @param listener the listener to add
     */
    public void addSettingsChangeListener(SettingsChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for settings changes.
     *
     * @param listener the listener to remove
     */
    public void removeSettingsChangeListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies listeners that settings have changed.
     *
     * @param changeType the type of settings change
     */
    private void notifySettingsChanged(SettingsChangeType changeType) {
        for (SettingsChangeListener listener : new ArrayList<>(listeners)) {
            listener.onSettingsChanged(changeType);
        }
    }

    // Profile settings methods

    /**
     * Gets the user's name.
     * @return the user's name
     */
    public String getName() {
        return userSettings.getName();
    }

    /**
     * Gets the user's email.
     * @return the user's email
     */
    public String getEmail() {
        return userSettings.getEmail();
    }

    /**
     * Gets the user's phone number.
     * @return the user's phone number
     */
    public String getPhone() {
        return userSettings.getPhone();
    }

    /**
     * Updates the user's profile information and saves settings.
     * @param name the new name
     * @param email the new email
     * @param phone the new phone number
     */
    public void updateProfile(String name, String email, String phone) {
        userSettings.setName(name);
        userSettings.setEmail(email);
        userSettings.setPhone(phone);
        userSettings.saveSettings();

        LOGGER.log(Level.INFO, "Profile updated - Name: {0}, Email: {1}, Phone: {2}",
                new Object[]{name, email, phone});

        notifySettingsChanged(SettingsChangeType.PROFILE);
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.SETTINGS);
    }

    // Currency settings methods

    /**
     * Gets the user's currency code.
     * @return the currency code
     */
    public String getCurrencyCode() {
        return userSettings.getCurrencyCode();
    }

    /**
     * Gets the user's currency symbol.
     * @return the currency symbol
     */
    public String getCurrencySymbol() {
        return userSettings.getCurrencySymbol();
    }

    /**
     * Updates the user's currency settings and saves settings.
     * @param currencyCode the new currency code
     * @param currencySymbol the new currency symbol
     */
    public void updateCurrency(String currencyCode, String currencySymbol) {
        userSettings.setCurrencyCode(currencyCode);
        userSettings.setCurrencySymbol(currencySymbol);
        userSettings.saveSettings();

        LOGGER.log(Level.INFO, "Currency updated - Code: {0}, Symbol: {1}",
                new Object[]{currencyCode, currencySymbol});

        // Update CurrencyManager
        CurrencyManager.getInstance().setCurrency(currencyCode, currencySymbol);

        notifySettingsChanged(SettingsChangeType.CURRENCY);
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.CURRENCY);
    }

    // Theme settings methods

    /**
     * Checks if dark theme is enabled.
     * @return true if dark theme is enabled, false otherwise
     */
    public boolean isDarkTheme() {
        return userSettings.isDarkTheme();
    }

    /**
     * Updates the theme setting and saves settings.
     * @param darkTheme true to enable dark theme, false for light theme
     */
    public void updateTheme(boolean darkTheme) {
        userSettings.setDarkTheme(darkTheme);
        userSettings.saveSettings();

        LOGGER.log(Level.INFO, "Theme updated - Dark theme: {0}", darkTheme);

        // Update ThemeManager
        ThemeManager.getInstance().setTheme(darkTheme);

        notifySettingsChanged(SettingsChangeType.THEME);
    }

    // Notification settings methods

    /**
     * Checks if budget alerts are enabled.
     * @return true if budget alerts are enabled, false otherwise
     */
    public boolean isBudgetAlertsEnabled() {
        return userSettings.isBudgetAlertsEnabled();
    }

    /**
     * Checks if transaction alerts are enabled.
     * @return true if transaction alerts are enabled, false otherwise
     */
    public boolean isTransactionAlertsEnabled() {
        return userSettings.isTransactionAlertsEnabled();
    }

    /**
     * Updates notification settings and saves settings.
     * @param budgetAlerts true to enable budget alerts
     * @param transactionAlerts true to enable transaction alerts
     */
    public void updateNotifications(boolean budgetAlerts, boolean transactionAlerts) {
        userSettings.setBudgetAlertsEnabled(budgetAlerts);
        userSettings.setTransactionAlertsEnabled(transactionAlerts);
        userSettings.saveSettings();

        LOGGER.log(Level.INFO, "Notifications updated - Budget alerts: {0}, Transaction alerts: {1}",
                new Object[]{budgetAlerts, transactionAlerts});

        notifySettingsChanged(SettingsChangeType.NOTIFICATIONS);
    }

    // Security settings methods

    /**
     * Validates the current password against the stored hash.
     * @param currentPassword the current password to validate
     * @return true if the password is valid, false otherwise
     */
    public boolean validateCurrentPassword(String currentPassword) {
        String currentHash = hashPassword(currentPassword);
        String storedHash = userSettings.getPasswordHash();

        return storedHash == null || storedHash.isEmpty() || storedHash.equals(currentHash);
    }

    /**
     * Updates the user's password if the current password is valid and the new passwords match.
     * @param currentPassword the current password
     * @param newPassword the new password
     * @param confirmPassword the confirmation of the new password
     * @return true if the password was updated successfully, false otherwise
     */
    public boolean updatePassword(String currentPassword, String newPassword, String confirmPassword) {
        // Validate inputs
        if (currentPassword == null || currentPassword.isEmpty()) {
            LOGGER.log(Level.WARNING, "Current password is empty");
            return false;
        }

        if (newPassword == null || newPassword.isEmpty()) {
            LOGGER.log(Level.WARNING, "New password is empty");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            LOGGER.log(Level.WARNING, "New password and confirmation do not match");
            return false;
        }

        // Validate current password
        if (!validateCurrentPassword(currentPassword)) {
            LOGGER.log(Level.WARNING, "Current password is incorrect");
            return false;
        }

        // Update password
        String newHash = hashPassword(newPassword);
        userSettings.setPasswordHash(newHash);
        userSettings.saveSettings();

        LOGGER.log(Level.INFO, "Password updated successfully");

        notifySettingsChanged(SettingsChangeType.SECURITY);
        return true;
    }

    /**
     * Hashes a password using SHA-256.
     * @param password the password to hash
     * @return the hashed password as a hex string
     */
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

    /**
     * Resets all settings to their default values and saves settings.
     */
    public void resetToDefaults() {
        userSettings.resetToDefaults();
        userSettings.saveSettings();

        notifySettingsChanged(SettingsChangeType.ALL);
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.SETTINGS);
    }

    /**
     * Handles data refresh events from the DataRefreshManager.
     * Notifies listeners if relevant data has changed.
     *
     * @param type the type of data refresh event
     */
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.SETTINGS ||
            type == DataRefreshManager.RefreshType.ALL) {
            // Notify listeners about settings changes
            notifySettingsChanged(SettingsChangeType.ALL);
        }
    }

    /**
     * Cleans up listeners and unregisters from the DataRefreshManager.
     * Should be called when this ViewModel is no longer needed.
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}