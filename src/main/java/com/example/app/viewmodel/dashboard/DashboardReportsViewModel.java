package com.example.app.viewmodel.dashboard;

import com.example.app.model.CSVDataImporter;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for DashboardReportsPanel following the MVVM pattern.
 * Coordinates report data and manages child chart ViewModels.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages report data for the dashboard</li>
 *   <li>Notifies listeners when report data changes</li>
 *   <li>Handles data refresh events and reloads data as needed</li>
 *   <li>Provides access to the FinanceData model for charts</li>
 * </ul>
 
 */
public class DashboardReportsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardReportsViewModel.class.getName());
    private final FinanceData financeData;
    private final String username;
    private final List<ReportDataChangeListener> listeners = new ArrayList<>();

    /**
     * Listener interface for components that need to be notified of report data changes.
     */
    public interface ReportDataChangeListener {
        /**
         * Called when the report data has changed and the view should be refreshed.
         */
        void onReportDataChanged();
    }

    /**
     * Constructs a DashboardReportsViewModel for the specified user.
     * Initializes the FinanceData model and loads initial data.
     *
     * @param username the username for which to manage reports
     */
    public DashboardReportsViewModel(String username) {
        this.username = username;
        this.financeData = new FinanceData();

        // Set data directory for loading budgets and transactions
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Load initial data
        loadTransactionData();
    }

    /**
     * Adds a listener for report data changes.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(ReportDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for report data changes.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(ReportDataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the report data has changed.
     */
    private void notifyReportDataChanged() {
        for (ReportDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onReportDataChanged();
        }
    }

    /**
     * Loads transaction data from the user's CSV file and imports it into the FinanceData model.
     */
    private void loadTransactionData() {
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);

        if (!transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            LOGGER.log(Level.INFO, "Successfully loaded {0} transactions", transactions.size());
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
            type == DataRefreshManager.RefreshType.BUDGETS ||
            type == DataRefreshManager.RefreshType.ALL) {

            // Reload transaction data if needed
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
                type == DataRefreshManager.RefreshType.ALL) {
                loadTransactionData();
            }

            // Notify listeners about data change
            notifyReportDataChanged();
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