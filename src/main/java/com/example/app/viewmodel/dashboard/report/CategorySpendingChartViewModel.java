package com.example.app.viewmodel.dashboard.report;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * ViewModel for the CategorySpendingChartPanel following the MVVM pattern.
 * Acts as an intermediary between the category chart panel and the finance data model.
 * <p>
 * Features:
 * <ul>
 *   <li>Provides category budget and expense data for chart visualization</li>
 *   <li>Listens for data refresh events and notifies chart listeners</li>
 *   <li>Supports registration and removal of chart data change listeners</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class CategorySpendingChartViewModel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(CategorySpendingChartViewModel.class.getName());
    private final FinanceData financeData;
    private final List<ChartDataChangeListener> listeners = new ArrayList<>();

    /**
     * Listener interface for components that need to be notified of chart data changes.
     */
    public interface ChartDataChangeListener {
        /**
         * Called when the chart data has changed and the chart should be refreshed.
         */
        void onChartDataChanged();
    }

    /**
     * Constructs a CategorySpendingChartViewModel with the given FinanceData.
     * Registers for data refresh events.
     *
     * @param financeData the finance data model
     */
    public CategorySpendingChartViewModel(FinanceData financeData) {
        this.financeData = financeData;
        // Register for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }

    /**
     * Adds a listener for chart data changes.
     *
     * @param listener the listener to add
     */
    public void addChangeListener(ChartDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a chart data change listener.
     *
     * @param listener the listener to remove
     */
    public void removeChangeListener(ChartDataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the chart data has changed.
     */
    private void notifyChartDataChanged() {
        for (ChartDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onChartDataChanged();
        }
    }

    /**
     * Gets the category budget data.
     *
     * @return a map of categories to budget amounts
     */
    public Map<String, Double> getCategoryBudgets() {
        return financeData.getCategoryBudgets();
    }

    /**
     * Gets the category expense data.
     *
     * @return a map of categories to expense amounts
     */
    public Map<String, Double> getCategoryExpenses() {
        return financeData.getCategoryExpenses();
    }

    /**
     * Called when data is refreshed in the data model.
     * Notifies listeners if relevant data has changed.
     *
     * @param type the type of data refresh event
     */
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
     * Cleans up listeners and unregisters from the DataRefreshManager.
     * Should be called when this ViewModel is no longer needed.
     */
    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}