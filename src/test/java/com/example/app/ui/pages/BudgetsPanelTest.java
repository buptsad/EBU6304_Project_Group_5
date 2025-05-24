package com.example.app.ui.pages;

import org.junit.jupiter.api.*;
import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BudgetsPanelTest {
    private static final String TEST_USERNAME = "testuser_budgetspanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USERNAME;

    @BeforeEach
    void setUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
    }

    @AfterAll
    static void cleanUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
        // Do NOT delete user_data folder!
    }

    @Test
    @DisplayName("Should initialize panel without errors")
    void testPanelInitialization() {
        assertDoesNotThrow(() -> {
            BudgetsPanel panel = new BudgetsPanel(TEST_USERNAME);
            assertNotNull(panel);
            assertNotNull(panel.getViewModel());
        });
    }

    @Test
    @DisplayName("Should add a new category through ViewModel")
    void testAddNewCategoryThroughViewModel() {
        BudgetsPanel panel = new BudgetsPanel(TEST_USERNAME);
        assertNotNull(panel.getViewModel());
        
        panel.getViewModel().updateCategoryBudget("TestCat", 123.45);
        Map<String, Double> budgets = panel.getViewModel().getCategoryBudgets();
        assertTrue(budgets.containsKey("TestCat"));
        assertEquals(123.45, budgets.get("TestCat"), 0.01);
    }

    @Test
    @DisplayName("Should update overall budget percentage")
    void testOverallBudgetPercentage() {
        BudgetsPanel panel = new BudgetsPanel(TEST_USERNAME);
        assertNotNull(panel.getViewModel());
        
        panel.getViewModel().updateCategoryBudget("Food", 100.0);
        panel.getViewModel().updateCategoryBudget("Housing", 200.0);
        double percent = panel.getViewModel().getOverallBudgetPercentage();
        assertTrue(percent >= 0);
    }

    @Test
    @DisplayName("Should generate AI suggested budgets")
    void testGenerateAISuggestedBudgets() {
        BudgetsPanel panel = new BudgetsPanel(TEST_USERNAME);
        assertNotNull(panel.getViewModel());
        
        panel.getViewModel().updateCategoryBudget("Food", 100.0);
        Map<String, Double> suggested = panel.getViewModel().generateSuggestedBudgets();
        assertNotNull(suggested);
        assertFalse(suggested.isEmpty());
    }
}