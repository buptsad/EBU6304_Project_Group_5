package com.example.app.viewmodel.dashboard;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.user_data.UserBillStorage;
import com.example.app.user_data.UserBudgetStorage;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for DashboardBudgetsPanel following MVVM pattern.
 * Acts as an intermediary between the DashboardBudgetsPanel (View) and the storage classes.
 */
public class DashboardBudgetsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardBudgetsViewModel.class.getName());
    private final String username;
    private final List<BudgetChangeListener> listeners = new ArrayList<>();
    private Map<String, Double> categoryBudgets = new HashMap<>();
    private Map<String, Double> categoryExpenses = new HashMap<>();

    /**
     * Interface for components that need to be notified of budget changes
     */
    public interface BudgetChangeListener {
        void onBudgetDataChanged();
    }

    public DashboardBudgetsViewModel(String username) {
        this.username = username;

        // Initialize storage with username
        UserBillStorage.setUsername(username);
        UserBudgetStorage.setUsername(username);

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Load initial data
        loadTransactionData();
        loadBudgetData();
    }

    /**
     * Add a listener for budget changes
     */
    public void addBudgetChangeListener(BudgetChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeBudgetChangeListener(BudgetChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that budget data has changed
     */
    private void notifyBudgetDataChanged() {
        for (BudgetChangeListener listener : new ArrayList<>(listeners)) {
            listener.onBudgetDataChanged();
        }
    }

    // Load budget data from storage
    private void loadBudgetData() {
        List<Object[]> budgets = UserBudgetStorage.loadBudgets();
        Map<String, Double> newBudgets = new HashMap<>();
        for (Object[] budget : budgets) {
            String category = (String) budget[0];
            double amount = (Double) budget[1];
            newBudgets.put(category, amount);
        }
        this.categoryBudgets = newBudgets;
    }

    // Load transaction data and calculate expenses by category
    private void loadTransactionData() {
        List<Object[]> transactions = UserBillStorage.loadTransactions();
        Map<String, Double> expenses = new HashMap<>();
        for (Object[] transaction : transactions) {
            String category = (String) transaction[2];
            double amount = (Double) transaction[3];
            if (amount < 0) { // Only count expenses (negative amounts)
                double expenseAmount = Math.abs(amount);
                expenses.put(category, expenses.getOrDefault(category, 0.0) + expenseAmount);
            }
        }
        this.categoryExpenses = expenses;
    }

    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.BUDGETS ||
            type == DataRefreshManager.RefreshType.ALL) {

            if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
                type == DataRefreshManager.RefreshType.ALL) {
                loadTransactionData();
            }
            if (type == DataRefreshManager.RefreshType.BUDGETS ||
                type == DataRefreshManager.RefreshType.ALL) {
                loadBudgetData();
            }
            notifyBudgetDataChanged();
        }
    }

    // Getters for the view
    public double getOverallBudgetPercentage() {
        double totalBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = categoryExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalBudget > 0 ? (totalExpense / totalBudget) * 100 : 0;
    }

    public Map<String, Double> getCategoryBudgets() {
        return new HashMap<>(categoryBudgets);
    }

    public Map<String, Double> getCategoryExpenses() {
        return new HashMap<>(categoryExpenses);
    }

    public double getCategoryBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }

    /**
     * Update a category budget
     */
    public void updateCategoryBudget(String category, double budget) {
        categoryBudgets.put(category, budget);
        saveBudgetsToStorage();
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
    }

    /**
     * Delete a category budget
     */
    public boolean deleteCategoryBudget(String category) {
        if (!categoryBudgets.containsKey(category)) {
            return false;
        }
        categoryBudgets.remove(category);
        saveBudgetsToStorage();
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
        return true;
    }

    private void saveBudgetsToStorage() {
        List<Object[]> budgets = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
            budgets.add(new Object[]{entry.getKey(), entry.getValue(), null, null});
        }
        UserBudgetStorage.saveBudgets(budgets);
    }

    /**
     * Clean up when no longer needed
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}