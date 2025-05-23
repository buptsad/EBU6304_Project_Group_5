package com.example.app.viewmodel;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.user_data.UserBillStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for TransactionsPanel following MVVM pattern.
 * Acts as an intermediary between the TransactionsPanel (View) and the storage classes.
 */
public class TransactionsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(TransactionsViewModel.class.getName());
    private final String username;
    private final List<TransactionChangeListener> listeners = new ArrayList<>();
    private List<Object[]> transactions = new ArrayList<>();
    private Set<String> categories = new HashSet<>();

    /**
     * Interface for components that need to be notified of transaction changes
     */
    public interface TransactionChangeListener {
        void onTransactionsChanged();
    }

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
     * Add a listener for transaction changes
     */
    public void addTransactionChangeListener(TransactionChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeTransactionChangeListener(TransactionChangeListener listener) {
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
     * Load transactions from storage
     */
    public void loadTransactions() {
        transactions = UserBillStorage.loadTransactions();
        updateCategorySet();
        notifyTransactionsChanged();
        LOGGER.log(Level.INFO, "Loaded {0} transactions", transactions.size());
    }

    /**
     * Update the set of categories from current transactions
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
     * Get all loaded transactions
     */
    public List<Object[]> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /**
     * Get all unique categories from transactions
     */
    public Set<String> getCategories() {
        return new HashSet<>(categories);
    }

    /**
     * Save transactions to storage
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
     * Add new transactions to existing ones
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
     * Add a single transaction
     */
    public boolean addTransaction(Object[] transaction) {
        List<Object[]> singleTransaction = new ArrayList<>();
        singleTransaction.add(transaction);
        return addTransactions(singleTransaction);
    }

    /**
     * Delete transactions by their indices
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
     * Filter transactions by search text and category
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

    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.ALL) {
            
            // Reload transaction data
            loadTransactions();
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