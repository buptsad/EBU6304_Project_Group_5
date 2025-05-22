package com.example.app.model;

/**
 * Interface for components that need to be notified of data refresh events.
 */
public interface DataRefreshListener {
    /**
     * Called when data has been refreshed and components need to update.
     * @param type The type of data that was refreshed
     */
    void onDataRefresh(DataRefreshManager.RefreshType type);
}