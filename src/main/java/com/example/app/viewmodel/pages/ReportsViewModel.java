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
 * ViewModel for ReportsPanel following MVVM pattern.
 * Handles data loading and notifies listeners on data changes.
 */
public class ReportsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(ReportsViewModel.class.getName());
    private final FinanceData financeData;
    private final String username;
    private final List<ReportsChangeListener> listeners = new ArrayList<>();

    public interface ReportsChangeListener {
        void onReportsDataChanged();
    }

    public ReportsViewModel(String username) {
        this.username = username;
        this.financeData = new FinanceData();
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);

        DataRefreshManager.getInstance().addListener(this);
        loadTransactionData();
    }

    public void addChangeListener(ReportsChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeChangeListener(ReportsChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyReportsDataChanged() {
        for (ReportsChangeListener listener : new ArrayList<>(listeners)) {
            listener.onReportsDataChanged();
        }
    }

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

    public FinanceData getFinanceData() {
        return financeData;
    }

    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.ALL) {
            loadTransactionData();
            notifyReportsDataChanged();
        }
    }

    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}