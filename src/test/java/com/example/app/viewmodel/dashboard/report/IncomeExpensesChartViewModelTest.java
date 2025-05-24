package com.example.app.viewmodel.dashboard.report;

import com.example.app.model.FinanceData;
import com.example.app.model.DataRefreshManager;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IncomeExpensesChartViewModelTest {
    private static final String TEST_USER = "testuser_incomeexpenses_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private IncomeExpensesChartViewModel viewModel;
    private StubFinanceData financeData;

    static class StubFinanceData extends FinanceData {
        private final List<LocalDate> dates;
        private final Map<LocalDate, Double> incomes;
        private final Map<LocalDate, Double> expenses;

        StubFinanceData() {
            dates = Arrays.asList(LocalDate.of(2025, 5, 23), LocalDate.of(2025, 5, 24));
            incomes = new HashMap<>();
            expenses = new HashMap<>();
            incomes.put(dates.get(0), 100.0);
            incomes.put(dates.get(1), 200.0);
            expenses.put(dates.get(0), 50.0);
            expenses.put(dates.get(1), 75.0);
        }

        @Override public List<LocalDate> getDates() { return dates; }
        @Override public Map<LocalDate, Double> getDailyIncomes() { return incomes; }
        @Override public Map<LocalDate, Double> getDailyExpenses() { return expenses; }
    }

    @BeforeEach
    void setUp() {
        financeData = new StubFinanceData();
        viewModel = new IncomeExpensesChartViewModel(financeData);
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
    void testGetDatesAndData() {
        List<LocalDate> dates = viewModel.getDates();
        assertEquals(2, dates.size());
        assertEquals(LocalDate.of(2025, 5, 23), dates.get(0));
        assertEquals(100.0, viewModel.getDailyIncomes().get(dates.get(0)));
        assertEquals(50.0, viewModel.getDailyExpenses().get(dates.get(0)));
    }

    @Test
    void testChangeListenerNotification() {
        final boolean[] notified = {false};
        IncomeExpensesChartViewModel.ChartDataChangeListener listener = () -> notified[0] = true;
        viewModel.addChangeListener(listener);
        viewModel.onDataRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
        assertTrue(notified[0]);
        notified[0] = false;
        viewModel.removeChangeListener(listener);
        viewModel.onDataRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
        assertFalse(notified[0]);
    }

    @Test
    void testCleanupRemovesListeners() {
        final boolean[] notified = {false};
        IncomeExpensesChartViewModel.ChartDataChangeListener listener = () -> notified[0] = true;
        viewModel.addChangeListener(listener);
        viewModel.cleanup();
        viewModel.onDataRefresh(DataRefreshManager.RefreshType.ALL);
        assertFalse(notified[0]);
    }
}