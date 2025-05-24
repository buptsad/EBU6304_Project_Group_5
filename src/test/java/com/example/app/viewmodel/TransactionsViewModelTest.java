package com.example.app.viewmodel;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TransactionsViewModelTest {
    private static final String TEST_USERNAME = "testuser_transactions";
    private TransactionsViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new TransactionsViewModel(TEST_USERNAME);
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
    void testAddAndDeleteTransaction() {
        Object[] transaction = {"2025-01-01 10:00", "Test", "TestCat", 10.0, true};
        assertTrue(viewModel.addTransaction(transaction));

        List<Object[]> txs = viewModel.getTransactions();
        assertFalse(txs.isEmpty());

        List<Integer> indices = Collections.singletonList(0);
        assertTrue(viewModel.deleteTransactions(indices));
    }

    @Test
    void testFilterTransactions() {
        Object[] transaction = {"2025-01-01 10:00", "Lunch", "Food", 10.0, true};
        viewModel.addTransaction(transaction);

        List<Object[]> filtered = viewModel.filterTransactions("Lunch", "Food");
        assertFalse(filtered.isEmpty());
    }

    @Test
    void testGetCategories() {
        Object[] transaction = {"2025-01-01 10:00", "Dinner", "Food", 20.0, true};
        viewModel.addTransaction(transaction);

        Set<String> categories = viewModel.getCategories();
        assertTrue(categories.contains("Food"));
    }
}