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
 * ViewModel for DashboardReportsPanel following MVVM pattern.
 * Coordinates report data and manages child chart ViewModels.
 */
public class DashboardReportsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardReportsViewModel.class.getName());
    private final FinanceData financeData;
    private final String username;
    private final List<ReportDataChangeListener> listeners = new ArrayList<>();

    /**
     * Interface for components that need to be notified of report data changes
     */
    public interface ReportDataChangeListener {
        void onReportDataChanged();
    }

    public DashboardReportsViewModel(String username) {
        this.username = username;
        this.financeData = new FinanceData();
        
        // Set data directory for loading budgets
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);
        
        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
        
        // Load initial data
        loadTransactionData();
    }

    /**
     * Add a listener for report data changes
     */
    public void addChangeListener(ReportDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeChangeListener(ReportDataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that report data has changed
     */
    private void notifyReportDataChanged() {
        for (ReportDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onReportDataChanged();
        }
    }

    /**
     * Load transaction data from user's CSV file
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
     * Get the finance data model for charts
     */
    public FinanceData getFinanceData() {
        return financeData;
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
            notifyReportDataChanged();
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