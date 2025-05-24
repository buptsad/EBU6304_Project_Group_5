package com.example.app.viewmodel.dashboard;

import com.example.app.model.CSVDataImporter;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for OverviewPanel following the MVVM pattern.
 * Acts as an intermediary between the OverviewPanel (View) and the data models.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages transactions and budgets from user storage</li>
 *   <li>Provides access to financial data and advice for the view</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of overview change listeners</li>
 *   <li>Detects and notifies about budget warnings</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class OverviewViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(OverviewViewModel.class.getName());
    private final FinanceData financeData;
    private final FinancialAdvice financialAdvice;
    private final String username;
    private final List<OverviewChangeListener> listeners = new ArrayList<>();

    /** Budget warning threshold (percent) */
    private static final double BUDGET_WARNING_THRESHOLD = 90.0;

    /**
     * Listener interface for components that need to be notified of overview data changes.
     */
    public interface OverviewChangeListener {
        /**
         * Called when financial data has changed and the view should be refreshed.
         */
        void onFinancialDataChanged();

        /**
         * Called when budget warnings are detected.
         * @param warningMessage the warning message to display
         */
        void onBudgetWarningsDetected(String warningMessage);
    }

    /**
     * Constructs an OverviewViewModel for the specified user and data models.
     * Initializes data directories, registers for data refresh events, and loads initial data.
     *
     * @param username the username for which to manage overview data
     * @param financeData the finance data model
     * @param financialAdvice the financial advice model
     */
    public OverviewViewModel(String username, FinanceData financeData, FinancialAdvice financialAdvice) {
        this.username = username;
        this.financeData = financeData;
        this.financialAdvice = financialAdvice;

        // Set data directory and initialize
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);

        // Initialize the financial advice with username
        financialAdvice.initialize(username);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Load initial data
        loadTransactionData();
        financeData.loadBudgets();

        // Check for budget warnings
        checkBudgetWarnings();
    }

    /**
     * Adds a listener for overview changes.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(OverviewChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for overview changes.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(OverviewChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that financial data has changed.
     */
    private void notifyFinancialDataChanged() {
        for (OverviewChangeListener listener : new ArrayList<>(listeners)) {
            listener.onFinancialDataChanged();
        }
    }

    /**
     * Notifies listeners about budget warnings.
     *
     * @param warningMessage the warning message to display
     */
    private void notifyBudgetWarnings(String warningMessage) {
        for (OverviewChangeListener listener : new ArrayList<>(listeners)) {
            listener.onBudgetWarningsDetected(warningMessage);
        }
    }

    /**
     * Loads transaction data from the user's CSV file and imports it into the FinanceData model.
     */
    private void loadTransactionData() {
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);

        if (transactions != null && !transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            LOGGER.log(Level.INFO, "OverviewViewModel: Successfully loaded {0} transactions", transactions.size());
        } else {
            LOGGER.log(Level.WARNING, "OverviewViewModel: No transactions loaded from {0}", csvFilePath);
        }
    }

    /**
     * Checks for budget warnings and notifies listeners if any are found.
     */
    public void checkBudgetWarnings() {
        StringBuilder warningMessage = new StringBuilder("<html><body>");
        boolean hasWarnings = false;

        // Check overall budget
        double overallPercentage = financeData.getOverallBudgetPercentage();
        if (overallPercentage >= BUDGET_WARNING_THRESHOLD) {
            warningMessage.append("<p style='color:#e74c3c'><b>Overall budget:</b> ")
                          .append(String.format("%.1f%%", overallPercentage))
                          .append(" used</p>");
            hasWarnings = true;
        }

        // Check category budgets
        Map<String, Double> categoryBudgets = financeData.getCategoryBudgets();
        for (String category : categoryBudgets.keySet()) {
            double percentage = financeData.getCategoryPercentage(category);
            if (percentage >= BUDGET_WARNING_THRESHOLD) {
                warningMessage.append("<p style='color:#e74c3c'><b>")
                              .append(category)
                              .append(":</b> ")
                              .append(String.format("%.1f%%", percentage))
                              .append(" used</p>");
                hasWarnings = true;
            }
        }

        warningMessage.append("</body></html>");

        // Notify listeners if warnings found
        if (hasWarnings) {
            notifyBudgetWarnings(warningMessage.toString());
        }
    }

    /**
     * Gets the finance data for chart creation.
     *
     * @return the FinanceData instance
     */
    public FinanceData getFinanceData() {
        return financeData;
    }

    /**
     * Gets the financial advice instance.
     *
     * @return the FinancialAdvice instance
     */
    public FinancialAdvice getFinancialAdvice() {
        return financialAdvice;
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
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS) {
                loadTransactionData();
            }

            // Reload budget data if needed
            if (type == DataRefreshManager.RefreshType.BUDGETS) {
                financeData.loadBudgets();
                LOGGER.log(Level.INFO, "OverviewViewModel: Reloaded budget data after budget refresh notification");
            }

            // Notify listeners about data change
            notifyFinancialDataChanged();

            // Check for budget warnings after data refresh
            checkBudgetWarnings();
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