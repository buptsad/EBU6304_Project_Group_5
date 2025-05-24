package com.example.app.viewmodel.dashboard.report;

import com.example.app.model.FinanceData;
import com.example.app.model.DataRefreshManager;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CategorySpendingChartViewModelTest {
    private static final String TEST_USER = "testuser_categoryspending_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private CategorySpendingChartViewModel viewModel;
    private StubFinanceData financeData;

    static class StubFinanceData extends FinanceData {
        private final Map<String, Double> budgets;
        private final Map<String, Double> expenses;

        StubFinanceData() {
            budgets = new HashMap<>();
            expenses = new HashMap<>();
            budgets.put("Food", 300.0);
            budgets.put("Transport", 100.0);
            expenses.put("Food", 120.0);
            expenses.put("Transport", 30.0);
        }

        @Override public Map<String, Double> getCategoryBudgets() { return budgets; }
        @Override public Map<String, Double> getCategoryExpenses() { return expenses; }
    }

    @BeforeEach
    void setUp() {
        financeData = new StubFinanceData();
        viewModel = new CategorySpendingChartViewModel(financeData);
    }

    @AfterEach
    void tearDown() {
        viewModel.cleanup();
        java.io.File dir = new java.io.File(DATA_DIR);
        if (dir.exists()) {
            for (java.io.File f : dir.listFiles()) f.delete();
            dir.delete();
        }
    }

    @Test
    void testGetCategoryBudgetsAndExpenses() {
        Map<String, Double> budgets = viewModel.getCategoryBudgets();
        Map<String, Double> expenses = viewModel.getCategoryExpenses();
        assertEquals(300.0, budgets.get("Food"));
        assertEquals(100.0, budgets.get("Transport"));
        assertEquals(120.0, expenses.get("Food"));
        assertEquals(30.0, expenses.get("Transport"));
    }

    @Test
    void testChangeListenerNotification() {
        final boolean[] notified = {false};
        CategorySpendingChartViewModel.ChartDataChangeListener listener = () -> notified[0] = true;
        viewModel.addChangeListener(listener);
        viewModel.onDataRefresh(DataRefreshManager.RefreshType.BUDGETS);
        assertTrue(notified[0]);
        notified[0] = false;
        viewModel.removeChangeListener(listener);
        viewModel.onDataRefresh(DataRefreshManager.RefreshType.BUDGETS);
        assertFalse(notified[0]);
    }

    @Test
    void testCleanupRemovesListeners() {
        final boolean[] notified = {false};
        CategorySpendingChartViewModel.ChartDataChangeListener listener = () -> notified[0] = true;
        viewModel.addChangeListener(listener);
        viewModel.cleanup();
        viewModel.onDataRefresh(DataRefreshManager.RefreshType.ALL);
        assertFalse(notified[0]);
    }
}