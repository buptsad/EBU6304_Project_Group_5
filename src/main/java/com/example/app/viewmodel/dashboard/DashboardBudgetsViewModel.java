package com.example.app.viewmodel.dashboard;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.user_data.UserBillStorage;
import com.example.app.user_data.UserBudgetStorage;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ViewModel for DashboardBudgetsPanel following the MVVM pattern.
 * Acts as an intermediary between the DashboardBudgetsPanel (View) and the storage classes.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and saves budgets and expenses from user storage</li>
 *   <li>Provides category budget and expense data for the view</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of budget change listeners</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class DashboardBudgetsViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardBudgetsViewModel.class.getName());
    private final String username;
    private final List<BudgetChangeListener> listeners = new ArrayList<>();
    private Map<String, Double> categoryBudgets = new HashMap<>();
    private Map<String, Double> categoryExpenses = new HashMap<>();

    /**
     * Listener interface for components that need to be notified of budget changes.
     */
    public interface BudgetChangeListener {
        /**
         * Called when the budget data has changed and the view should be refreshed.
         */
        void onBudgetDataChanged();
    }

    /**
     * Constructs a DashboardBudgetsViewModel for the specified user.
     * Initializes storage and loads initial data.
     *
     * @param username the username for which to manage budgets
     */
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
     * Adds a listener for budget changes.
     *
     * @param listener the listener to add
     */
    public void addBudgetChangeListener(BudgetChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener for budget changes.
     *
     * @param listener the listener to remove
     */
    public void removeBudgetChangeListener(BudgetChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the budget data has changed.
     */
    private void notifyBudgetDataChanged() {
        for (BudgetChangeListener listener : new ArrayList<>(listeners)) {
            listener.onBudgetDataChanged();
        }
    }

    /**
     * Loads budget data from storage.
     */
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

    /**
     * Loads transaction data and calculates expenses by category.
     */
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

    /**
     * Gets the overall budget usage percentage.
     *
     * @return the percentage of total expenses over total budget, or 0 if no budget
     */
    public double getOverallBudgetPercentage() {
        double totalBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = categoryExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        return totalBudget > 0 ? (totalExpense / totalBudget) * 100 : 0;
    }

    /**
     * Gets the map of category budgets.
     *
     * @return a copy of the category budgets map
     */
    public Map<String, Double> getCategoryBudgets() {
        return new HashMap<>(categoryBudgets);
    }

    /**
     * Gets the map of category expenses.
     *
     * @return a copy of the category expenses map
     */
    public Map<String, Double> getCategoryExpenses() {
        return new HashMap<>(categoryExpenses);
    }

    /**
     * Gets the budget for a specific category.
     *
     * @param category the category name
     * @return the budget amount for the category, or 0 if not set
     */
    public double getCategoryBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }

    /**
     * Updates the budget for a specific category and saves to storage.
     *
     * @param category the category name
     * @param budget the new budget amount
     */
    public void updateCategoryBudget(String category, double budget) {
        categoryBudgets.put(category, budget);
        saveBudgetsToStorage();
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
    }

    /**
     * Deletes the budget for a specific category and saves to storage.
     *
     * @param category the category name
     * @return true if the category existed and was deleted, false otherwise
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

    /**
     * Saves the current category budgets to storage.
     */
    private void saveBudgetsToStorage() {
        List<Object[]> budgets = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
            budgets.add(new Object[]{entry.getKey(), entry.getValue(), null, null});
        }
        UserBudgetStorage.saveBudgets(budgets);
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