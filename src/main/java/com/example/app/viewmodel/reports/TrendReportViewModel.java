package com.example.app.viewmodel.reports;

import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import com.example.app.model.FinanceData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrendReportViewModel implements DataRefreshListener {
    private final FinanceData financeData;
    private final List<ChartDataChangeListener> listeners = new ArrayList<>();

    public interface ChartDataChangeListener {
        void onChartDataChanged();
    }

    public TrendReportViewModel(FinanceData financeData) {
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

    // Data access methods for the view
    public List<LocalDate> getDates() {
        return financeData.getDates();
    }

    public Map<LocalDate, Double> getDailyIncomes() {
        return financeData.getDailyIncomes();
    }

    public Map<LocalDate, Double> getDailyExpenses() {
        return financeData.getDailyExpenses();
    }

    public double getMonthlyBudget() {
        return financeData.getMonthlyBudget();
    }

    public double getDailyBudget() {
        return financeData.getDailyBudget();
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