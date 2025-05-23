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
 * ViewModel for SettingsPanel following MVVM pattern.
 * Acts as an intermediary between the SettingsPanel (View) and the UserSettings (Model).
 */
public class SettingsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(SettingsViewModel.class.getName());
    private final UserSettings userSettings;
    private final List<SettingsChangeListener> listeners = new ArrayList<>();
    private String username;

    /**
     * Interface for components that need to be notified of settings changes
     */
    public interface SettingsChangeListener {
        void onSettingsChanged(SettingsChangeType changeType);
    }

    /**
     * Types of settings changes
     */
    public enum SettingsChangeType {
        PROFILE, CURRENCY, THEME, NOTIFICATIONS, SECURITY, ALL
    }

    public SettingsViewModel(String username) {
        this.username = username;
        this.userSettings = UserSettings.getInstance();
        
        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }

    /**
     * Add a listener for settings changes
     */
    public void addSettingsChangeListener(SettingsChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeSettingsChangeListener(SettingsChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify listeners that settings have changed
     */
    private void notifySettingsChanged(SettingsChangeType changeType) {
        for (SettingsChangeListener listener : new ArrayList<>(listeners)) {
            listener.onSettingsChanged(changeType);
        }
    }

    // Profile settings methods
    public String getName() {
        return userSettings.getName();
    }

    public String getEmail() {
        return userSettings.getEmail();
    }

    public String getPhone() {
        return userSettings.getPhone();
    }

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
    public String getCurrencyCode() {
        return userSettings.getCurrencyCode();
    }

    public String getCurrencySymbol() {
        return userSettings.getCurrencySymbol();
    }

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
    public boolean isDarkTheme() {
        return userSettings.isDarkTheme();
    }

    public void updateTheme(boolean darkTheme) {
        userSettings.setDarkTheme(darkTheme);
        userSettings.saveSettings();
        
        LOGGER.log(Level.INFO, "Theme updated - Dark theme: {0}", darkTheme);
        
        // Update ThemeManager
        ThemeManager.getInstance().setTheme(darkTheme);
        
        notifySettingsChanged(SettingsChangeType.THEME);
    }

    // Notification settings methods
    public boolean isBudgetAlertsEnabled() {
        return userSettings.isBudgetAlertsEnabled();
    }

    public boolean isTransactionAlertsEnabled() {
        return userSettings.isTransactionAlertsEnabled();
    }

    public void updateNotifications(boolean budgetAlerts, boolean transactionAlerts) {
        userSettings.setBudgetAlertsEnabled(budgetAlerts);
        userSettings.setTransactionAlertsEnabled(transactionAlerts);
        userSettings.saveSettings();
        
        LOGGER.log(Level.INFO, "Notifications updated - Budget alerts: {0}, Transaction alerts: {1}",
                new Object[]{budgetAlerts, transactionAlerts});
        
        notifySettingsChanged(SettingsChangeType.NOTIFICATIONS);
    }

    // Security settings methods
    public boolean validateCurrentPassword(String currentPassword) {
        String currentHash = hashPassword(currentPassword);
        String storedHash = userSettings.getPasswordHash();
        
        return storedHash == null || storedHash.isEmpty() || storedHash.equals(currentHash);
    }

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

    // Reset settings to defaults
    public void resetToDefaults() {
        userSettings.resetToDefaults();
        userSettings.saveSettings();
        
        notifySettingsChanged(SettingsChangeType.ALL);
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.SETTINGS);
    }

    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.SETTINGS || 
            type == DataRefreshManager.RefreshType.ALL) {
            // Notify listeners about settings changes
            notifySettingsChanged(SettingsChangeType.ALL);
        }
    }

    /**
     * Clean up when no longer needed
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}