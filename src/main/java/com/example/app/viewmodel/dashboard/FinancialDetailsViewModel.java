package com.example.app.viewmodel.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * ViewModel for FinancialDetailsPanel following MVVM pattern.
 * Acts as an intermediary between the FinancialDetailsPanel (View) and the data models.
 */
public class FinancialDetailsViewModel implements DataRefreshListener {
    private final FinanceData financeData;
    private final FinancialAdvice financialAdvice;
    private final List<FinancialDetailsChangeListener> listeners = new ArrayList<>();
    
    /**
     * Interface for components that need to be notified of financial details changes
     */
    public interface FinancialDetailsChangeListener {
        void onFinancialDataChanged();
        void onAdviceChanged();
    }
    
    public FinancialDetailsViewModel(FinanceData financeData, FinancialAdvice financialAdvice) {
        this.financeData = financeData;
        this.financialAdvice = financialAdvice;
        
        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }
    
    /**
     * Add a listener for financial details changes
     */
    public void addChangeListener(FinancialDetailsChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove a listener
     */
    public void removeChangeListener(FinancialDetailsChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that financial data has changed
     */
    private void notifyFinancialDataChanged() {
        for (FinancialDetailsChangeListener listener : new ArrayList<>(listeners)) {
            listener.onFinancialDataChanged();
        }
    }
    
    /**
     * Notify all listeners that advice has changed
     */
    private void notifyAdviceChanged() {
        for (FinancialDetailsChangeListener listener : new ArrayList<>(listeners)) {
            listener.onAdviceChanged();
        }
    }
    
    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.ADVICE) {
            notifyAdviceChanged();
        }
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.ALL) {
            notifyFinancialDataChanged();
        }
    }
    
    // Methods to get financial data for the view
    public double getMonthlyBudget() {
        return financeData.getMonthlyBudget();
    }
    
    public double getTotalIncome() {
        return financeData.getTotalIncome();
    }
    
    public double getTotalExpenses() {
        return financeData.getTotalExpenses();
    }
    
    public double getTotalSavings() {
        return financeData.getTotalSavings();
    }
    
    public double getOverallBudgetPercentage() {
        return financeData.getOverallBudgetPercentage();
    }
    
    public Map<String, Double> getCategoryBudgets() {
        return financeData.getCategoryBudgets();
    }
    
    public Map<String, Double> getCategoryExpenses() {
        return financeData.getCategoryExpenses();
    }
    
    // Get financial advice data
    public String getAdvice() {
        return financialAdvice.getAdvice();
    }
    
    public String getFormattedGenerationTime() {
        return financialAdvice.getFormattedGenerationTime();
    }
    
    /**
     * Clean up when no longer needed
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}