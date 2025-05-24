package com.example.app.viewmodel;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.ui.pages.AI.getRes;
import com.example.app.user_data.UserBillStorage;
import com.example.app.user_data.UserBudgetStorage;

import org.json.JSONObject;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * ViewModel for the Budget functionality, following the MVVM pattern.
 * Acts as an intermediary between the BudgetsPanel (View) and the storage classes.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and saves budgets and expenses from user storage</li>
 *   <li>Provides category budget and expense data for the view</li>
 *   <li>Listens for data refresh events and notifies listeners</li>
 *   <li>Supports registration and removal of budget change listeners</li>
 *   <li>Handles AI-based budget suggestions</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class BudgetViewModel implements DataRefreshListener {
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
     * Constructs a BudgetViewModel for the specified user.
     * Initializes storage and loads initial data.
     *
     * @param username the username for which to manage budgets
     */
    public BudgetViewModel(String username) {
        this.username = username;

        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);

        // Initialize storage with username
        UserBillStorage.setUsername(username);
        UserBudgetStorage.setUsername(username);

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
     * Handles data refresh events from the DataRefreshManager.
     * Reloads data and notifies listeners if relevant data has changed.
     *
     * @param type the type of data refresh event
     */
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.BUDGETS ||
            type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.ALL) {

            // Reload data if needed
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
                type == DataRefreshManager.RefreshType.ALL) {
                loadTransactionData();
            }

            if (type == DataRefreshManager.RefreshType.BUDGETS ||
                type == DataRefreshManager.RefreshType.ALL) {
                loadBudgetData();
            }

            // Notify view model listeners
            notifyBudgetDataChanged();
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
                // Convert to positive for expense tracking
                double expenseAmount = Math.abs(amount);
                expenses.put(category, expenses.getOrDefault(category, 0.0) + expenseAmount);
            }
        }

        this.categoryExpenses = expenses;
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
     * Gets the expense for a specific category.
     *
     * @param category the category name
     * @return the expense amount for the category, or 0 if not set
     */
    public double getCategoryExpense(String category) {
        return categoryExpenses.getOrDefault(category, 0.0);
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
        // Update in-memory data
        categoryBudgets.put(category, budget);

        // Save to storage
        saveBudgetsToStorage();

        // Notify data refresh
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
    }

    /**
     * Deletes the budget for a specific category and saves to storage.
     *
     * @param category the category name
     * @return true if the category existed and was deleted, false otherwise
     */
    public boolean deleteCategoryBudget(String category) {
        // Remove from in-memory data
        if (!categoryBudgets.containsKey(category)) {
            return false;
        }

        categoryBudgets.remove(category);

        // Save to storage
        saveBudgetsToStorage();

        // Notify data refresh
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
        return true;
    }

    /**
     * Saves the current category budgets to storage.
     */
    private void saveBudgetsToStorage() {
        List<Object[]> budgets = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
            // Format: [Category, Amount, StartDate (null), EndDate (null)]
            budgets.add(new Object[]{entry.getKey(), entry.getValue(), null, null});
        }

        UserBudgetStorage.saveBudgets(budgets);
    }

    /**
     * Generates AI-based suggested budgets based on current budgets.
     * Calls the AI service and parses the JSON response.
     *
     * @return a map of suggested budgets by category
     */
    public Map<String, Double> generateSuggestedBudgets() {
        Map<String, Double> suggestedBudgets = new LinkedHashMap<>();
        double totalBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : categoryBudgets.entrySet()) {
            sb.append(e.getKey())
              .append(": ")
              .append(e.getValue())
              .append("; ");
        }

        if (sb.length() >= 2) {
            sb.setLength(sb.length() - 2);  // Remove trailing "; "
        }

        String budgetString = sb.toString();

        String aiPrompt = String.format(
            "Current budget allocation: %s. Total budget is %.2f. "
          + "Please redistribute the total budget among the categories for a more reasonable allocation, keeping the total unchanged. "
          + "Output in JSON format, with category names as keys and amounts as values. Do not output anything else.",
            budgetString,
            totalBudget
        );

        try {
            String API_KEY = "sk-fdf26a37926f46ab8d4884c2cd533db8";
            String response = new getRes().getResponse(API_KEY, aiPrompt);
            String res = new getRes().parseAIResponse(response);

            // Clean up and parse response
            res = cleanupJsonResponse(res);

            try {
                JSONObject json = new JSONObject(res);
                for (String category : categoryBudgets.keySet()) {
                    double val = json.has(category)
                            ? json.getDouble(category)
                            : categoryBudgets.get(category);
                    suggestedBudgets.put(category, val);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse AI JSON: " + e.getMessage());
                // If parsing fails, return current budgets
                return new LinkedHashMap<>(categoryBudgets);
            }
        } catch (IOException e) {
            System.err.println("Failed to get AI response: " + e.getMessage());
            // If API call fails, return current budgets
            return new LinkedHashMap<>(categoryBudgets);
        }

        return suggestedBudgets;
    }

    /**
     * Applies AI suggested budgets to current budgets and saves to storage.
     *
     * @param suggestedBudgets the map of suggested budgets to apply
     */
    public void applySuggestedBudgets(Map<String, Double> suggestedBudgets) {
        // Update in-memory data
        categoryBudgets.putAll(suggestedBudgets);

        // Save to storage
        saveBudgetsToStorage();

        // Notify data refresh
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
    }

    /**
     * Cleans up and normalizes the AI JSON response string.
     *
     * @param res the raw AI response string
     * @return the cleaned JSON string
     */
    private String cleanupJsonResponse(String res) {
        // 1. Trim whitespace
        res = res.trim();

        // 2. Remove leading code fence and language identifier
        if (res.startsWith("```")) {
            int firstNewline = res.indexOf('\n');
            if (firstNewline != -1) {
                res = res.substring(firstNewline + 1).trim();
            } else {
                res = "";
            }
        }

        // 3. Remove trailing code fence
        if (res.endsWith("```")) {
            int lastBackticks = res.lastIndexOf("```");
            res = res.substring(0, lastBackticks).trim();
        }

        // 4. Remove json prefix if present
        if (res.startsWith("json")) {
            int brace = res.indexOf('{');
            if (brace != -1) {
                res = res.substring(brace).trim();
            }
        }

        return res;
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