package com.example.app.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class that manages data refresh notifications across the application
 * using the Observer pattern.
 * <p>
 * This class allows components to register as listeners for data changes,
 * and coordinates refresh events to ensure data consistency throughout the application.
 
 */
public class DataRefreshManager {
    private static final Logger LOGGER = Logger.getLogger(DataRefreshManager.class.getName());
    private static DataRefreshManager instance;
    private final List<DataRefreshListener> listeners;
    
    /**
     * Flag used to prevent recursive refresh calls
     */
    private volatile boolean refreshInProgress = false;
    
    /**
     * Enum defining different types of data that could be refreshed
     */
    public enum RefreshType {
        /**
         * Indicates that transaction data has been updated
         */
        TRANSACTIONS,
        
        /**
         * Indicates that budget data has been updated
         */
        BUDGETS,
        
        /**
         * Indicates that currency settings have been updated
         */
        CURRENCY,
        
        /**
         * Indicates that application settings have been updated
         */
        SETTINGS,
        
        /**
         * Indicates that financial advice has been updated
         */
        ADVICE,
        
        /**
         * Indicates that all data types have been updated
         */
        ALL
    }
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private DataRefreshManager() {
        listeners = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Returns the singleton instance of the DataRefreshManager
     *
     * @return the singleton instance of this class
     */
    public static synchronized DataRefreshManager getInstance() {
        if (instance == null) {
            instance = new DataRefreshManager();
        }
        return instance;
    }
    
    /**
     * Adds a listener to receive data refresh notifications
     *
     * @param listener the DataRefreshListener to be added
     */
    public void addListener(DataRefreshListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
            LOGGER.log(Level.FINE, "Added listener: {0}", listener.getClass().getName());
        }
    }
    
    /**
     * Removes a listener from receiving data refresh notifications
     *
     * @param listener the DataRefreshListener to be removed
     */
    public void removeListener(DataRefreshListener listener) {
        listeners.remove(listener);
        LOGGER.log(Level.FINE, "Removed listener: {0}", listener.getClass().getName());
    }
    
    /**
     * Notifies all registered listeners that data of the specified type has been refreshed
     *
     * @param type the type of data that has been refreshed
     */
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
    
    /**
     * Convenience method to notify listeners that transaction data has been refreshed
     */
    public void refreshTransactions() {
        notifyRefresh(RefreshType.TRANSACTIONS);
    }
    
    /**
     * Convenience method to notify listeners that budget data has been refreshed
     */
    public void refreshBudgets() {
        notifyRefresh(RefreshType.BUDGETS);
    }
    
    /**
     * Convenience method to notify listeners that all data types have been refreshed
     */
    public void refreshAll() {
        notifyRefresh(RefreshType.ALL);
    }
    
    /**
     * Resets the singleton instance of DataRefreshManager.
     * <p>
     * <b>For testing purposes only.</b> This method allows tests to reset the singleton
     * instance so that a fresh instance can be created. It should not be used in production code.
     */
    static void _resetForTests() {
        instance = null;
    }
}