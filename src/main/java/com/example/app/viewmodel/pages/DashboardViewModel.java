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
 * ViewModel for DashboardPanel following MVVM pattern.
 * Acts as an intermediary between the DashboardPanel (View) and the data models.
 */
public class DashboardViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardViewModel.class.getName());
    private final FinanceData financeData;
    private final String username;
    private final List<DashboardChangeListener> listeners = new ArrayList<>();
    
    /**
     * Interface for components that need to be notified of dashboard data changes
     */
    public interface DashboardChangeListener {
        void onSummaryDataChanged();
        void onActivePanelChanged(String panelName);
    }
    
    // Constants for different dashboard panels
    public static final String OVERVIEW_PANEL = "OVERVIEW";
    public static final String TRANSACTIONS_PANEL = "TRANSACTIONS";
    public static final String BUDGETS_PANEL = "BUDGETS";
    public static final String REPORTS_PANEL = "REPORTS";
    
    private String activePanel = OVERVIEW_PANEL; // Track active panel
    
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
     * Add a listener for dashboard data changes
     */
    public void addChangeListener(DashboardChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     */
    public void removeChangeListener(DashboardChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that summary data has changed
     */
    private void notifySummaryDataChanged() {
        for (DashboardChangeListener listener : new ArrayList<>(listeners)) {
            listener.onSummaryDataChanged();
        }
    }
    
    /**
     * Set the active panel and notify listeners
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
     * Get the currently active panel name
     */
    public String getActivePanel() {
        return activePanel;
    }
    
    /**
     * Load transaction data from user's CSV file
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
     * Get the finance data for summary panels
     */
    public FinanceData getFinanceData() {
        return financeData;
    }
    
    /**
     * Get the user's total balance
     */
    public double getTotalBalance() {
        return financeData.getTotalBalance();
    }
    
    /**
     * Get the user's total income
     */
    public double getTotalIncome() {
        return financeData.getTotalIncome();
    }
    
    /**
     * Get the user's total expenses
     */
    public double getTotalExpenses() {
        return financeData.getTotalExpenses();
    }
    
    /**
     * Get the user's total savings
     */
    public double getTotalSavings() {
        return financeData.getTotalSavings();
    }
    
    // Implement DataRefreshListener method
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
     * Get the username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Clean up when no longer needed
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}