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
 * ViewModel for OverviewPanel following MVVM pattern.
 * Acts as an intermediary between the OverviewPanel (View) and the data models.
 */
public class OverviewViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(OverviewViewModel.class.getName());
    private final FinanceData financeData;
    private final FinancialAdvice financialAdvice;
    private final String username;
    private final List<OverviewChangeListener> listeners = new ArrayList<>();
    
    // Budget warning threshold (percent)
    private static final double BUDGET_WARNING_THRESHOLD = 90.0;
    
    /**
     * Interface for components that need to be notified of overview data changes
     */
    public interface OverviewChangeListener {
        void onFinancialDataChanged();
        void onBudgetWarningsDetected(String warningMessage);
    }
    
    public OverviewViewModel(String username, FinanceData financeData, FinancialAdvice financialAdvice) {
        this.username = username;
        this.financeData = financeData;
        this.financialAdvice = financialAdvice;
        
        // Set data directory and initialize
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);
        
        // Initialize the financial advice with username
        financialAdvice.initialize(username);
        financialAdvice.setFinanceData(financeData);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
        
        // Load initial data
        loadTransactionData();
        financeData.loadBudgets();
        
        // Check for budget warnings
        checkBudgetWarnings();
    }
    
    /**
     * Add a listener for overview changes
     */
    public void addChangeListener(OverviewChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     */
    public void removeChangeListener(OverviewChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that financial data has changed
     */
    private void notifyFinancialDataChanged() {
        for (OverviewChangeListener listener : new ArrayList<>(listeners)) {
            listener.onFinancialDataChanged();
        }
    }
    
    /**
     * Notify listeners about budget warnings
     */
    private void notifyBudgetWarnings(String warningMessage) {
        for (OverviewChangeListener listener : new ArrayList<>(listeners)) {
            listener.onBudgetWarningsDetected(warningMessage);
        }
    }
    
    /**
     * Load transaction data from user's CSV file
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
     * Check for budget warnings and notify listeners if found
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
     * Get the finance data for chart creation
     */
    public FinanceData getFinanceData() {
        return financeData;
    }
    
    /**
     * Get the financial advice instance
     */
    public FinancialAdvice getFinancialAdvice() {
        return financialAdvice;
    }
    
    // Implement DataRefreshListener method
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
     * Clean up when no longer needed
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}