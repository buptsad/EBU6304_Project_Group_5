package com.example.app.viewmodel.reports;

import com.example.app.model.FinanceData;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TrendReportViewModelTest {
    private static final String TEST_USER = "testuser_trendreport_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private FinanceData financeData;
    private TrendReportViewModel viewModel;

    @BeforeEach
    void setUp() {
        financeData = new FinanceData();
        viewModel = new TrendReportViewModel(financeData);
    }

    @AfterEach
    void tearDown() {
        File dir = new File(DATA_DIR);
        if (dir.exists()) {
            for (File f : dir.listFiles()) f.delete();
            dir.delete();
        }
    }

    @Test
    void testGetters() {
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24", "Salary", "Income", 1000.0});
        txs.add(new Object[]{"2025-05-24", "Groceries", "Food", -100.0});
        financeData.importTransactions(txs);

        List<LocalDate> dates = viewModel.getDates();
        assertNotNull(dates);
        assertTrue(dates.contains(LocalDate.parse("2025-05-24")));

        assertNotNull(viewModel.getDailyIncomes());
        assertNotNull(viewModel.getDailyExpenses());
        assertTrue(viewModel.getMonthlyBudget() > 0);
        assertTrue(viewModel.getDailyBudget() > 0);
    }

    @Test
    void testAddRemoveChangeListener() {
        List<Boolean> called = new ArrayList<>();
        TrendReportViewModel.ChartDataChangeListener listener = () -> called.add(true);

        viewModel.addChangeListener(listener);
        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        assertTrue(called.size() > 0);

        called.clear();
        viewModel.removeChangeListener(listener);
        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        assertEquals(0, called.size());
    }

    @Test
    void testCleanup() {
        viewModel.cleanup();
        // Should not throw
    }
}