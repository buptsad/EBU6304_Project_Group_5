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
 * ViewModel for DashboardTransactionsPanel following the MVVM pattern.
 * Acts as an intermediary between the DashboardTransactionsPanel (View) and the storage classes.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages recent transactions from user storage</li>
 *   <li>Provides recent transaction data for the view</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of transaction change listeners</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class DashboardTransactionsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardTransactionsViewModel.class.getName());
    private final String username;
    private final List<TransactionChangeListener> listeners = new ArrayList<>();
    private static final int MAX_TRANSACTIONS = 20; // Maximum number of transactions to display

    private List<TransactionEntry> recentTransactions = new ArrayList<>();

    /**
     * Listener interface for components that need to be notified of transaction data changes.
     */
    public interface TransactionChangeListener {
        /**
         * Called when the transaction data has changed and the view should be refreshed.
         */
        void onTransactionsChanged();
    }

    /**
     * Data class to represent a transaction entry for display.
     */
    public static class TransactionEntry {
        private final LocalDate date;
        private final String description;
        private final String category;
        private final double amount;

        /**
         * Constructs a TransactionEntry.
         *
         * @param date        the transaction date
         * @param description the transaction description
         * @param category    the transaction category
         * @param amount      the transaction amount
         */
        public TransactionEntry(LocalDate date, String description, String category, double amount) {
            this.date = date;
            this.description = description;
            this.category = category;
            this.amount = amount;
        }

        /**
         * Gets the transaction date.
         *
         * @return the transaction date
         */
        public LocalDate getDate() { return date; }

        /**
         * Gets the transaction description.
         *
         * @return the transaction description
         */
        public String getDescription() { return description; }

        /**
         * Gets the transaction category.
         *
         * @return the transaction category
         */
        public String getCategory() { return category; }

        /**
         * Gets the transaction amount.
         *
         * @return the transaction amount
         */
        public double getAmount() { return amount; }
    }

    /**
     * Constructs a DashboardTransactionsViewModel for the specified user.
     * Initializes storage and loads initial data.
     *
     * @param username the username for which to manage transactions
     */
    public DashboardTransactionsViewModel(String username) {
        this.username = username;
        UserBillStorage.setUsername(username);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Load initial data
        loadTransactionData();
    }

    /**
     * Adds a listener for transaction data changes.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(TransactionChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for transaction data changes.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(TransactionChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the transaction data has changed.
     */
    private void notifyTransactionsChanged() {
        for (TransactionChangeListener listener : new ArrayList<>(listeners)) {
            listener.onTransactionsChanged();
        }
    }

    /**
     * Loads transaction data from UserBillStorage.
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
     * Gets recent transactions for display.
     *
     * @return List of recent transaction entries, sorted by date (newest first)
     */
    public List<TransactionEntry> getRecentTransactions() {
        return new ArrayList<>(recentTransactions);
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
            // Reload data
            loadTransactionData();
            // Notify view model listeners
            notifyTransactionsChanged();
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