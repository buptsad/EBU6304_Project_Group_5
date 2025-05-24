package com.example.app.viewmodel.pages;

import com.example.app.model.CSVDataImporter;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for ReportsPanel following the MVVM pattern.
 * Handles data loading and notifies listeners on data changes.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages report data for the reports panel</li>
 *   <li>Notifies listeners when report data changes</li>
 *   <li>Handles data refresh events and reloads data as needed</li>
 *   <li>Provides access to the FinanceData model for charts and reports</li>
 *   <li>Supports registration and removal of report data change listeners</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class ReportsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(ReportsViewModel.class.getName());
    private final FinanceData financeData;
    private final String username;
    private final List<ReportsChangeListener> listeners = new ArrayList<>();

    /**
     * Listener interface for components that need to be notified of report data changes.
     */
    public interface ReportsChangeListener {
        /**
         * Called when the report data has changed and the view should be refreshed.
         */
        void onReportsDataChanged();
    }

    /**
     * Constructs a ReportsViewModel for the specified user.
     * Initializes the FinanceData model and loads initial data.
     *
     * @param username the username for which to manage reports
     */
    public ReportsViewModel(String username) {
        this.username = username;
        this.financeData = new FinanceData();
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);

        DataRefreshManager.getInstance().addListener(this);
        loadTransactionData();
    }

    /**
     * Adds a listener for report data changes.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(ReportsChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Removes a listener for report data changes.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(ReportsChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the report data has changed.
     */
    private void notifyReportsDataChanged() {
        for (ReportsChangeListener listener : new ArrayList<>(listeners)) {
            listener.onReportsDataChanged();
        }
    }

    /**
     * Loads transaction data from the user's CSV file and imports it into the FinanceData model.
     */
    public void loadTransactionData() {
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);
        if (!transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            LOGGER.log(Level.INFO, "Loaded {0} transactions", transactions.size());
        } else {
            LOGGER.log(Level.WARNING, "No transactions loaded from {0}", csvFilePath);
        }
    }

    /**
     * Gets the FinanceData model for use in charts and reports.
     *
     * @return the FinanceData instance
     */
    public FinanceData getFinanceData() {
        return financeData;
    }

    /**
     * Handles data refresh events from the DataRefreshManager.
     * Reloads data and notifies listeners if relevant data has changed.
     *
     * @param type the type of data refresh event
     */
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.ALL) {
            loadTransactionData();
            notifyReportsDataChanged();
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