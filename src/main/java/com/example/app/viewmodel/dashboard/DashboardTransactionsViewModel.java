package com.example.app.viewmodel.dashboard;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.user_data.UserBillStorage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * ViewModel for DashboardTransactionsPanel following MVVM pattern.
 * Acts as an intermediary between the DashboardTransactionsPanel (View) and the storage classes.
 */
public class DashboardTransactionsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardTransactionsViewModel.class.getName());
    private final String username;
    private final List<TransactionChangeListener> listeners = new ArrayList<>();
    private static final int MAX_TRANSACTIONS = 20; // Maximum number of transactions to display

    private List<TransactionEntry> recentTransactions = new ArrayList<>();

    /**
     * Interface for components that need to be notified of transaction data changes
     */
    public interface TransactionChangeListener {
        void onTransactionsChanged();
    }

    /**
     * Data class to represent a transaction entry for display
     */
    public static class TransactionEntry {
        private final LocalDate date;
        private final String description;
        private final String category;
        private final double amount;

        public TransactionEntry(LocalDate date, String description, String category, double amount) {
            this.date = date;
            this.description = description;
            this.category = category;
            this.amount = amount;
        }

        public LocalDate getDate() { return date; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }

    public DashboardTransactionsViewModel(String username) {
        this.username = username;
        UserBillStorage.setUsername(username);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Load initial data
        loadTransactionData();
    }

    /**
     * Add a listener for transaction data changes
     */
    public void addChangeListener(TransactionChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeChangeListener(TransactionChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that transaction data has changed
     */
    private void notifyTransactionsChanged() {
        for (TransactionChangeListener listener : new ArrayList<>(listeners)) {
            listener.onTransactionsChanged();
        }
    }

    /**
     * Load transaction data from UserBillStorage
     */
    private void loadTransactionData() {
        List<Object[]> transactions = UserBillStorage.loadTransactions();
        List<TransactionEntry> entries = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Object[] transaction : transactions) {
            try {
                String dateStr = transaction[0].toString();
                String description = transaction[1].toString();
                String category = transaction[2].toString();
                double amount = (transaction[3] instanceof Double)
                        ? (Double) transaction[3]
                        : Double.parseDouble(transaction[3].toString());
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10)); // Support "yyyy-MM-dd" or "yyyy-MM-dd HH:mm"
                entries.add(new TransactionEntry(date, description, category, amount));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to parse transaction: " + Arrays.toString(transaction), e);
            }
        }

        // Sort by date descending and limit
        this.recentTransactions = entries.stream()
                .sorted(Comparator.comparing(TransactionEntry::getDate).reversed())
                .limit(MAX_TRANSACTIONS)
                .collect(Collectors.toList());
    }

    /**
     * Get recent transactions for display
     * @return List of recent transaction entries, sorted by date (newest first)
     */
    public List<TransactionEntry> getRecentTransactions() {
        return new ArrayList<>(recentTransactions);
    }

    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.ALL) {
            // Reload data
            loadTransactionData();
            // Notify view model listeners
            notifyTransactionsChanged();
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