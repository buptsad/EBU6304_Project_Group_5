package com.example.app.model;

import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FinanceData class.
 * Verifies data structure initialization, transaction importing, budget management,
 * category percentage calculations, and other core functionality of the FinanceData model.
 */
class FinanceDataTest {

    /**
     * The FinanceData instance used for testing.
     */
    private FinanceData financeData;

    /**
     * Sets up a fresh FinanceData instance before each test.
     */
    @BeforeEach
    void setUp() {
        financeData = new FinanceData();
    }

    /**
     * Tests that the initial state of FinanceData has properly initialized empty data structures.
     * Verifies that all collections are initialized and not null.
     */
    @Test
    @DisplayName("Initial state should have empty data structures")
    void testInitialState() {
        assertNotNull(financeData.getCategoryBudgets());
        assertNotNull(financeData.getCategoryExpenses());
        assertNotNull(financeData.getCategoryIncomes());
        assertNotNull(financeData.getDailyExpenses());
        assertNotNull(financeData.getDailyIncomes());
        assertNotNull(financeData.getTransactions());
    }

    /**
     * Tests that importing transactions correctly updates all relevant data structures.
     * Verifies transaction list, daily and category summaries, and total calculations.
     */
    @Test
    @DisplayName("Importing transactions updates all relevant data")
    void testImportTransactions() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-01", "Salary", "Income", 5000.0});
        transactions.add(new Object[]{"2024-06-02", "Groceries", "Food", -200.0});
        transactions.add(new Object[]{"2024-06-03", "Bus Ticket", "Transportation", -20.0});
        transactions.add(new Object[]{"2024-06-03", "Gift Received", "Gift", 100.0});

        financeData.importTransactions(transactions);

        // Check transactions list
        assertEquals(4, financeData.getTransactions().size());

        // Daily incomes and expenses
        assertEquals(2, financeData.getDailyIncomes().size());
        assertEquals(2, financeData.getDailyExpenses().size());

        // Category incomes and expenses
        assertTrue(financeData.getCategoryIncomes().containsKey("Other"));
        assertTrue(financeData.getCategoryExpenses().containsKey("Food"));
        assertTrue(financeData.getCategoryExpenses().containsKey("Transportation"));

        // Totals
        assertEquals(5100.0, financeData.getTotalIncome(), 0.01);
        assertEquals(220.0, financeData.getTotalExpenses(), 0.01);
        assertEquals(4880.0, financeData.getTotalSavings(), 0.01);
    }

    /**
     * Tests that budget allocation and retrieval functions correctly.
     * Verifies that budget categories are properly created and default allocations are reasonable.
     */
    @Test
    @DisplayName("Budget allocation and retrieval works")
    void testBudgetAllocation() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-01", "Groceries", "Food", -100.0});
        transactions.add(new Object[]{"2024-06-02", "Rent", "Housing", -1200.0});
        financeData.importTransactions(transactions);

        Map<String, Double> budgets = financeData.getCategoryBudgets();
        assertTrue(budgets.containsKey("Food"));
        assertTrue(budgets.containsKey("Housing"));
        assertTrue(budgets.containsKey("Other"));
        assertEquals(4000.0, budgets.values().stream().mapToDouble(Double::doubleValue).sum(), 0.1);
    }

    /**
     * Tests that category percentage calculations return correct values.
     * Verifies that percentages are within the expected range (0-100%).
     */
    @Test
    @DisplayName("Category percentage calculation is correct")
    void testCategoryPercentage() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-01", "Groceries", "Food", -400.0});
        financeData.importTransactions(transactions);
        double percent = financeData.getCategoryPercentage("Food");
        assertTrue(percent > 0 && percent <= 100);
    }

    /**
     * Tests that expense and income descriptions and categories are correctly stored and retrieved.
     * Verifies that the correct descriptions and categories are returned for a specific date.
     */
    @Test
    @DisplayName("Expense and income descriptions and categories are correct")
    void testDescriptionsAndCategories() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-04", "Dinner", "Food", -50.0});
        transactions.add(new Object[]{"2024-06-04", "Salary", "Income", 3000.0});
        financeData.importTransactions(transactions);

        LocalDate date = LocalDate.parse("2024-06-04");
        assertEquals("Dinner", financeData.getExpenseDescription(date));
        assertEquals("Salary", financeData.getIncomeDescription(date));
        assertEquals("Food", financeData.getExpenseCategory(date));
    }

    /**
     * Tests that budget updates and deletions work correctly.
     * Verifies that budgets can be added, updated, and removed.
     */
    @Test
    @DisplayName("Budget update and deletion works")
    void testBudgetUpdateAndDelete() {
        financeData.updateCategoryBudget("TestCat", 123.45);
        assertEquals(123.45, financeData.getCategoryBudget("TestCat"), 0.01);

        boolean deleted = financeData.deleteCategoryBudget("TestCat");
        assertTrue(deleted);
        assertEquals(0.0, financeData.getCategoryBudget("TestCat"), 0.01);
    }

    /**
     * Tests that the overall budget percentage calculation returns the correct ratio.
     * Verifies that the percentage is within the expected range (0-100%).
     */
    @Test
    @DisplayName("getOverallBudgetPercentage returns correct ratio")
    void testOverallBudgetPercentage() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-01", "Rent", "Housing", -1400.0});
        transactions.add(new Object[]{"2024-06-02", "Groceries", "Food", -800.0});
        financeData.importTransactions(transactions);

        double percent = financeData.getOverallBudgetPercentage();
        assertTrue(percent > 0 && percent <= 100);
    }

    /**
     * Tests that the getDates method returns a sorted list of unique dates.
     * Verifies that dates are returned in chronological order regardless of input order.
     */
    @Test
    @DisplayName("getDates returns sorted unique dates")
    void testGetDates() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-02", "Groceries", "Food", -100.0});
        transactions.add(new Object[]{"2024-06-01", "Salary", "Income", 3000.0});
        financeData.importTransactions(transactions);

        List<LocalDate> dates = financeData.getDates();
        assertEquals(2, dates.size());
        assertEquals(LocalDate.parse("2024-06-01"), dates.get(0));
        assertEquals(LocalDate.parse("2024-06-02"), dates.get(1));
    }
}