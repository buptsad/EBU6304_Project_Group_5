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
 * ViewModel for DashboardPanel following the MVVM pattern.
 * Acts as an intermediary between the DashboardPanel (View) and the data models.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages transactions and budgets from user storage</li>
 *   <li>Provides access to financial data for summary panels</li>
 *   <li>Tracks and notifies about the active dashboard panel</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of dashboard change listeners</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class DashboardViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardViewModel.class.getName());
    private final FinanceData financeData;
    private final String username;
    private final List<DashboardChangeListener> listeners = new ArrayList<>();

    /**
     * Listener interface for components that need to be notified of dashboard data changes.
     */
    public interface DashboardChangeListener {
        /**
         * Called when summary data has changed and the view should be refreshed.
         */
        void onSummaryDataChanged();

        /**
         * Called when the active dashboard panel has changed.
         * @param panelName the new active panel name
         */
        void onActivePanelChanged(String panelName);
    }

    /** Panel name constants */
    public static final String OVERVIEW_PANEL = "OVERVIEW";
    /** Panel name constants */
    public static final String TRANSACTIONS_PANEL = "TRANSACTIONS";
    /** Panel name constants */
    public static final String BUDGETS_PANEL = "BUDGETS";
    /** Panel name constants */
    public static final String REPORTS_PANEL = "REPORTS";

    /** Tracks the currently active dashboard panel */
    private String activePanel = OVERVIEW_PANEL;

    /**
     * Constructs a DashboardViewModel for the specified user.
     * Initializes data directories, registers for data refresh events, and loads initial data.
     *
     * @param username the username for which to manage dashboard data
     */
    public DashboardViewModel(String username) {
        this.username = username;
        this.financeData = new FinanceData();

        // Set data directory
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Load initial data
        loadTransactionData();
        financeData.loadBudgets();
    }

    /**
     * Adds a listener for dashboard data changes.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(DashboardChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for dashboard data changes.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(DashboardChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that summary data has changed.
     */
    private void notifySummaryDataChanged() {
        for (DashboardChangeListener listener : new ArrayList<>(listeners)) {
            listener.onSummaryDataChanged();
        }
    }

    /**
     * Sets the active dashboard panel and notifies listeners if it changed.
     *
     * @param panelName the new active panel name
     */
    public void setActivePanel(String panelName) {
        if (!this.activePanel.equals(panelName)) {
            this.activePanel = panelName;

            for (DashboardChangeListener listener : new ArrayList<>(listeners)) {
                listener.onActivePanelChanged(panelName);
            }
        }
    }

    /**
     * Gets the currently active dashboard panel name.
     *
     * @return the active panel name
     */
    public String getActivePanel() {
        return activePanel;
    }

    /**
     * Loads transaction data from the user's CSV file and imports it into the FinanceData model.
     */
    private void loadTransactionData() {
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);

        if (transactions != null && !transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            LOGGER.log(Level.INFO, "DashboardViewModel: Successfully loaded {0} transactions", transactions.size());
        } else {
            LOGGER.log(Level.WARNING, "DashboardViewModel: No transactions loaded from {0}", csvFilePath);
        }
    }

    /**
     * Gets the finance data for summary panels.
     *
     * @return the FinanceData instance
     */
    public FinanceData getFinanceData() {
        return financeData;
    }

    /**
     * Gets the user's total balance.
     *
     * @return the total balance
     */
    public double getTotalBalance() {
        return financeData.getTotalBalance();
    }

    /**
     * Gets the user's total income.
     *
     * @return the total income
     */
    public double getTotalIncome() {
        return financeData.getTotalIncome();
    }

    /**
     * Gets the user's total expenses.
     *
     * @return the total expenses
     */
    public double getTotalExpenses() {
        return financeData.getTotalExpenses();
    }

    /**
     * Gets the user's total savings.
     *
     * @return the total savings
     */
    public double getTotalSavings() {
        return financeData.getTotalSavings();
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
            notifySummaryDataChanged();
        }
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
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