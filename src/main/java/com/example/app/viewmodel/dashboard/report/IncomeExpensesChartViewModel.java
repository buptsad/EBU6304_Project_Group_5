package com.example.app.viewmodel.dashboard.report;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ViewModel for IncomeExpensesChartPanel following MVVM pattern.
 * Acts as an intermediary between the chart panel and finance data model.
 */
public class IncomeExpensesChartViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(IncomeExpensesChartViewModel.class.getName());
    private final FinanceData financeData;
    private final List<ChartDataChangeListener> listeners = new ArrayList<>();

    /**
     * Interface for components that need to be notified of chart data changes
     */
    public interface ChartDataChangeListener {
        void onChartDataChanged();
    }

    public IncomeExpensesChartViewModel(FinanceData financeData) {
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
     * Get all dates for the chart
     * @return List of dates in chronological order
     */
    public List<LocalDate> getDates() {
        return financeData.getDates();
    }

    /**
     * Get daily income data
     * @return Map of dates to income amounts
     */
    public Map<LocalDate, Double> getDailyIncomes() {
        return financeData.getDailyIncomes();
    }

    /**
     * Get daily expense data
     * @return Map of dates to expense amounts
     */
    public Map<LocalDate, Double> getDailyExpenses() {
        return financeData.getDailyExpenses();
    }

    // Implement DataRefreshListener method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
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