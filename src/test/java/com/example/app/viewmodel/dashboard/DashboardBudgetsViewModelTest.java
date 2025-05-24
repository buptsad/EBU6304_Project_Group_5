package com.example.app.viewmodel.dashboard;

import com.example.app.user_data.UserBillStorage;
import com.example.app.user_data.UserBudgetStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DashboardBudgetsViewModelTest {
    private static final String TEST_USER = "testuser_budgets_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private DashboardBudgetsViewModel viewModel;

    @BeforeEach
    void setUp() {
        UserBillStorage.setUsername(TEST_USER);
        UserBudgetStorage.setUsername(TEST_USER);
        viewModel = new DashboardBudgetsViewModel(TEST_USER);
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
    void testUpdateAndDeleteCategoryBudget() {
        viewModel.updateCategoryBudget("Food", 100.0);
        assertEquals(100.0, viewModel.getCategoryBudget("Food"), 0.01);

        assertTrue(viewModel.deleteCategoryBudget("Food"));
        assertEquals(0.0, viewModel.getCategoryBudget("Food"), 0.01);
    }

    @Test
    void testGetOverallBudgetPercentage() {
        viewModel.updateCategoryBudget("Food", 100.0);
        // Add a transaction for Food
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24 10:00", "Lunch", "Food", -50.0, true});
        UserBillStorage.saveTransactions(txs);

        // Reload data
        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        double percent = viewModel.getOverallBudgetPercentage();
        assertTrue(percent > 0 && percent <= 100);
    }

    @Test
    void testGetCategoryBudgetsAndExpenses() {
        viewModel.updateCategoryBudget("Transport", 80.0);
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24 11:00", "Bus", "Transport", -20.0, true});
        UserBillStorage.saveTransactions(txs);

        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        Map<String, Double> budgets = viewModel.getCategoryBudgets();
        Map<String, Double> expenses = viewModel.getCategoryExpenses();
        assertTrue(budgets.containsKey("Transport"));
        assertTrue(expenses.getOrDefault("Transport", 0.0) > 0);
    }
}