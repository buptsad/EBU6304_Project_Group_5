package com.example.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class that manages data refresh notifications across the application
 * using the Observer pattern.
 */
public class DataRefreshManager {
    private static final Logger LOGGER = Logger.getLogger(DataRefreshManager.class.getName());
    private static DataRefreshManager instance;
    private final List<DataRefreshListener> listeners;
    
    // Add this field to prevent recursive refresh calls
    private volatile boolean refreshInProgress = false;
    
    // Enum for different types of data that could be refreshed
    public enum RefreshType {
        TRANSACTIONS,
        BUDGETS,
        CURRENCY,
        SETTINGS,
        ALL
    }
    
    private DataRefreshManager() {
        listeners = new ArrayList<>();
    }
    
    public static synchronized DataRefreshManager getInstance() {
        if (instance == null) {
            instance = new DataRefreshManager();
        }
        return instance;
    }
    
    public void addListener(DataRefreshListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            LOGGER.log(Level.FINE, "Added listener: {0}", listener.getClass().getName());
        }
    }
    
    public void removeListener(DataRefreshListener listener) {
        listeners.remove(listener);
        LOGGER.log(Level.FINE, "Removed listener: {0}", listener.getClass().getName());
    }
    
    public void notifyRefresh(RefreshType type) {
        // Prevent recursive refresh calls
        if (refreshInProgress) {
            LOGGER.log(Level.WARNING, "Recursive refresh call detected for {0}, skipping", type);
            return;
        }
        
        try {
            refreshInProgress = true;
            LOGGER.log(Level.INFO, "Notifying {0} listeners of {1} data refresh", 
                new Object[]{listeners.size(), type});
            
            for (DataRefreshListener listener : new ArrayList<>(listeners)) {
                try {
                    listener.onDataRefresh(type);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error notifying listener: " + listener.getClass().getName(), e);
                }
            }
        } finally {
            refreshInProgress = false;
        }
    }
    
    // Convenience methods for specific refresh types
    public void refreshTransactions() {
        notifyRefresh(RefreshType.TRANSACTIONS);
    }
    
    public void refreshBudgets() {
        notifyRefresh(RefreshType.BUDGETS);
    }
    
    public void refreshAll() {
        notifyRefresh(RefreshType.ALL);
    }
}