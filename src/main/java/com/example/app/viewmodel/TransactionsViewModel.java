package com.example.app.viewmodel;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.user_data.UserBillStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for TransactionsPanel following the MVVM pattern.
 * Acts as an intermediary between the TransactionsPanel (View) and the storage classes.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and manages transactions from user storage</li>
 *   <li>Provides access to transaction and category data for the view</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of transaction change listeners</li>
 *   <li>Handles filtering, adding, deleting, and saving transactions</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class TransactionsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(TransactionsViewModel.class.getName());
    private final String username;
    private final List<TransactionChangeListener> listeners = new ArrayList<>();
    private List<Object[]> transactions = new ArrayList<>();
    private Set<String> categories = new HashSet<>();

    /**
     * Interface for components that need to be notified of transaction changes.
     */
    public interface TransactionChangeListener {
        /**
         * Called when transaction data has changed and the view should be refreshed.
         */
        void onTransactionsChanged();
    }

    /**
     * Constructs a TransactionsViewModel for the specified user.
     * Registers for data refresh events and loads initial data.
     *
     * @param username the username for which to manage transactions
     */
    public TransactionsViewModel(String username) {
        this.username = username;

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Initialize storage with username
        UserBillStorage.setUsername(username);

        // Load initial data
        loadTransactions();
    }

    /**
     * Adds a listener for transaction changes.
     *
     * @param listener the listener to add
     */
    public void addTransactionChangeListener(TransactionChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for transaction changes.
     *
     * @param listener the listener to remove
     */
    public void removeTransactionChangeListener(TransactionChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all listeners that transaction data has changed.
     */
    private void notifyTransactionsChanged() {
        for (TransactionChangeListener listener : new ArrayList<>(listeners)) {
            listener.onTransactionsChanged();
        }
    }

    /**
     * Loads transactions from storage and updates categories.
     */
    public void loadTransactions() {
        transactions = UserBillStorage.loadTransactions();
        updateCategorySet();
        notifyTransactionsChanged();
        LOGGER.log(Level.INFO, "Loaded {0} transactions", transactions.size());
    }

    /**
     * Updates the set of categories from current transactions.
     */
    private void updateCategorySet() {
        categories.clear();
        for (Object[] transaction : transactions) {
            if (transaction.length >= 3 && transaction[2] != null) {
                String category = (String) transaction[2];
                if (!category.isEmpty()) {
                    categories.add(category);
                }
            }
        }
    }

    /**
     * Gets all loaded transactions.
     *
     * @return a list of all transactions
     */
    public List<Object[]> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Gets all unique categories from transactions.
     *
     * @return a set of unique category names
     */
    public Set<String> getCategories() {
        return new HashSet<>(categories);
    }

    /**
     * Saves transactions to storage.
     *
     * @param transactions the list of transactions to save
     * @return true if saved successfully, false otherwise
     */
    public boolean saveTransactions(List<Object[]> transactions) {
        UserBillStorage.setUsername(username);
        boolean success = UserBillStorage.saveTransactions(transactions);

        if (success) {
            this.transactions = new ArrayList<>(transactions);
            updateCategorySet();

            // Notify system-wide refresh
            DataRefreshManager.getInstance().refreshTransactions();

            LOGGER.log(Level.INFO, "Saved {0} transactions", transactions.size());
        } else {
            LOGGER.log(Level.SEVERE, "Failed to save transactions");
        }

        return success;
    }

    /**
     * Adds new transactions to existing ones and saves.
     *
     * @param newTransactions the list of new transactions to add
     * @return true if added and saved successfully, false otherwise
     */
    public boolean addTransactions(List<Object[]> newTransactions) {
        if (newTransactions == null || newTransactions.isEmpty()) {
            return false;
        }

        List<Object[]> updatedTransactions = new ArrayList<>(transactions);
        updatedTransactions.addAll(newTransactions);

        return saveTransactions(updatedTransactions);
    }

    /**
     * Adds a single transaction and saves.
     *
     * @param transaction the transaction to add
     * @return true if added and saved successfully, false otherwise
     */
    public boolean addTransaction(Object[] transaction) {
        List<Object[]> singleTransaction = new ArrayList<>();
        singleTransaction.add(transaction);
        return addTransactions(singleTransaction);
    }

    /**
     * Deletes transactions by their indices and saves.
     *
     * @param indices the list of indices to delete
     * @return true if deleted and saved successfully, false otherwise
     */
    public boolean deleteTransactions(List<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            return false;
        }

        List<Object[]> updatedTransactions = new ArrayList<>(transactions);

        // Sort indices in descending order to avoid index shifting problems
        indices.sort(Collections.reverseOrder());

        for (int index : indices) {
            if (index >= 0 && index < updatedTransactions.size()) {
                updatedTransactions.remove(index);
            }
        }

        return saveTransactions(updatedTransactions);
    }

    /**
     * Filters transactions by search text and category.
     *
     * @param searchText the text to search for (can be null or empty)
     * @param category the category to filter by (can be null or empty)
     * @return a list of filtered transactions
     */
    public List<Object[]> filterTransactions(String searchText, String category) {
        if ((searchText == null || searchText.isEmpty()) &&
            (category == null || category.isEmpty())) {
            return getTransactions();
        }

        List<Object[]> filteredTransactions = new ArrayList<>();
        String searchLower = searchText != null ? searchText.toLowerCase() : "";

        for (Object[] transaction : transactions) {
            // Category filter
            if (category != null && !category.isEmpty()) {
                String transactionCategory = (String) transaction[2];
                if (!category.equals(transactionCategory)) {
                    continue;
                }
            }

            // Text search
            if (!searchLower.isEmpty()) {
                boolean matchFound = false;

                // Check first 3 columns (date, description, category)
                for (int i = 0; i < 3 && i < transaction.length; i++) {
                    if (transaction[i] != null) {
                        String value = transaction[i].toString().toLowerCase();
                        if (value.contains(searchLower)) {
                            matchFound = true;
                            break;
                        }
                    }
                }

                if (!matchFound) {
                    continue;
                }
            }

            filteredTransactions.add(transaction);
        }

        return filteredTransactions;
    }

    /**
     * Handles data refresh events from the DataRefreshManager.
     * Reloads data if relevant data has changed.
     *
     * @param type the type of data refresh event
     */
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.ALL) {

            // Reload transaction data
            loadTransactions();
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