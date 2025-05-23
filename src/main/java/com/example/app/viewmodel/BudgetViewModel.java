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
 * ViewModel for the Budget functionality, following MVVM pattern.
 * Acts as an intermediary between the BudgetsPanel (View) and the storage classes.
 */
public class BudgetViewModel implements DataRefreshListener {
    private final String username;
    private final List<BudgetChangeListener> listeners = new ArrayList<>();
    private Map<String, Double> categoryBudgets = new HashMap<>();
    private Map<String, Double> categoryExpenses = new HashMap<>();
    
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
     * Interface for components that need to be notified of budget changes
     */
    public interface BudgetChangeListener {
        void onBudgetDataChanged();
    }
    
    // Add a listener for budget changes
    public void addBudgetChangeListener(BudgetChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    // Remove a listener
    public void removeBudgetChangeListener(BudgetChangeListener listener) {
        listeners.remove(listener);
    }
    
    // Notify all listeners that budget data has changed
    private void notifyBudgetDataChanged() {
        for (BudgetChangeListener listener : new ArrayList<>(listeners)) {
            listener.onBudgetDataChanged();
        }
    }
    
    // Implement DataRefreshListener method
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
                // Convert to positive for expense tracking
                double expenseAmount = Math.abs(amount);
                expenses.put(category, expenses.getOrDefault(category, 0.0) + expenseAmount);
            }
        }
        
        this.categoryExpenses = expenses;
    }
    
    // Public getters
    public Map<String, Double> getCategoryBudgets() {
        return new HashMap<>(categoryBudgets);
    }
    
    public Map<String, Double> getCategoryExpenses() {
        return new HashMap<>(categoryExpenses);
    }
    
    public double getOverallBudgetPercentage() {
        double totalBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = categoryExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
        
        return totalBudget > 0 ? (totalExpense / totalBudget) * 100 : 0;
    }
    
    public double getCategoryExpense(String category) {
        return categoryExpenses.getOrDefault(category, 0.0);
    }
    
    public double getCategoryBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }
    
    // CRUD operations
    public void updateCategoryBudget(String category, double budget) {
        // Update in-memory data
        categoryBudgets.put(category, budget);
        
        // Save to storage
        saveBudgetsToStorage();
        
        // Notify data refresh
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
    }
    
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
    
    private void saveBudgetsToStorage() {
        List<Object[]> budgets = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
            // Format: [Category, Amount, StartDate (null), EndDate (null)]
            budgets.add(new Object[]{entry.getKey(), entry.getValue(), null, null});
        }
        
        UserBudgetStorage.saveBudgets(budgets);
    }
    
    // Generate AI budget suggestions
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
            "当前预算分配如下：%s。总预算为 %.2f。"
          + " 请在总金额不变的情况下，将总预算在各类中重新分配，给出一个更合理的预算分配方案。"
          + " 请以 JSON 格式输出，键是类别名称，值是对应金额，此外不要输出其它任何内容。",
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
                System.err.println("解析 AI JSON 失败: " + e.getMessage());
                // If parsing fails, return current budgets
                return new LinkedHashMap<>(categoryBudgets);
            }
        } catch (IOException e) {
            System.err.println("获取 AI 响应失败: " + e.getMessage());
            // If API call fails, return current budgets
            return new LinkedHashMap<>(categoryBudgets);
        }
        
        return suggestedBudgets;
    }
    
    // Apply AI suggested budgets to current budgets
    public void applySuggestedBudgets(Map<String, Double> suggestedBudgets) {
        // Update in-memory data
        categoryBudgets.putAll(suggestedBudgets);
        
        // Save to storage
        saveBudgetsToStorage();
        
        // Notify data refresh
        DataRefreshManager.getInstance().notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
    }
    
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
    
    // Clean up when no longer needed
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}