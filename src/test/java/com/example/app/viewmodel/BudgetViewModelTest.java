package com.example.app.viewmodel;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BudgetViewModelTest {
    private static final String TEST_USERNAME = "testuser_budget";
    private BudgetViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new BudgetViewModel(TEST_USERNAME);
    }

    @AfterEach
    void tearDown() {
        File dir = new File(".\\user_data\\" + TEST_USERNAME);
        if (dir.exists()) {
            for (File file : dir.listFiles()) file.delete();
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
    void testGetCategoryBudgetsAndExpenses() {
        viewModel.updateCategoryBudget("Transport", 50.0);
        Map<String, Double> budgets = viewModel.getCategoryBudgets();
        assertTrue(budgets.containsKey("Transport"));
        assertEquals(50.0, budgets.get("Transport"), 0.01);

        Map<String, Double> expenses = viewModel.getCategoryExpenses();
        assertNotNull(expenses);
    }

    @Test
    void testOverallBudgetPercentage() {
        viewModel.updateCategoryBudget("A", 100.0);
        assertTrue(viewModel.getOverallBudgetPercentage() >= 0.0);
    }
}