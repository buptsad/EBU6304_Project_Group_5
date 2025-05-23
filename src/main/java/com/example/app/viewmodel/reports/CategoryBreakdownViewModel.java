package com.example.app.viewmodel.reports;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryBreakdownViewModel implements DataRefreshListener {
    private final FinanceData financeData;
    private final List<ChartDataChangeListener> listeners = new ArrayList<>();

    public interface ChartDataChangeListener {
        void onChartDataChanged();
    }

    public CategoryBreakdownViewModel(FinanceData financeData) {
        this.financeData = financeData;
        DataRefreshManager.getInstance().addListener(this);
    }

    public void addChangeListener(ChartDataChangeListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeChangeListener(ChartDataChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyChartDataChanged() {
        for (ChartDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onChartDataChanged();
        }
    }

    public Map<String, Double> getCategoryExpenses() {
        return financeData.getCategoryExpenses();
    }

    public Map<String, Double> getCategoryBudgets() {
        return financeData.getCategoryBudgets();
    }

    // Add this missing method
    public List<FinanceData.Transaction> getTransactions() {
        return financeData.getTransactions();
    }

    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS ||
            type == DataRefreshManager.RefreshType.BUDGETS ||
            type == DataRefreshManager.RefreshType.ALL) {
            notifyChartDataChanged();
        }
    }

    public void cleanup() {
        DataRefreshManager.getInstance().removeListener(this);
        listeners.clear();
    }
}