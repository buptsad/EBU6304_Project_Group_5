package com.example.app.viewmodel.dashboard.report;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ViewModel for CategorySpendingChartPanel following MVVM pattern.
 * Acts as an intermediary between the category chart panel and finance data model.
 */
public class CategorySpendingChartViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(CategorySpendingChartViewModel.class.getName());
    private final FinanceData financeData;
    private final List<ChartDataChangeListener> listeners = new ArrayList<>();

    /**
     * Interface for components that need to be notified of chart data changes
     */
    public interface ChartDataChangeListener {
        void onChartDataChanged();
    }

    public CategorySpendingChartViewModel(FinanceData financeData) {
        this.financeData = financeData;
        
        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }

    /**
     * Add a listener for chart data changes
     */
    public void addChangeListener(ChartDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeChangeListener(ChartDataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that chart data has changed
     */
    private void notifyChartDataChanged() {
        for (ChartDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onChartDataChanged();
        }
    }

    /**
     * Get category budget data
     * @return Map of categories to budget amounts
     */
    public Map<String, Double> getCategoryBudgets() {
        return financeData.getCategoryBudgets();
    }

    /**
     * Get category expense data
     * @return Map of categories to expense amounts
     */
    public Map<String, Double> getCategoryExpenses() {
        return financeData.getCategoryExpenses();
    }

    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.ALL) {
            // Notify listeners about data change
            notifyChartDataChanged();
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