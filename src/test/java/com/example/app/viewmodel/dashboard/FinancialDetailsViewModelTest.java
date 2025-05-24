package com.example.app.viewmodel.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.user_data.UserBillStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FinancialDetailsViewModelTest {
    private static final String TEST_USER = "testuser_financialdetails_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private FinancialDetailsViewModel viewModel;
    private FinanceData financeData;
    private FinancialAdvice advice;

    @BeforeEach
    void setUp() {
        UserBillStorage.setUsername(TEST_USER);
        financeData = new FinanceData();
        advice = new FinancialAdvice();
        viewModel = new FinancialDetailsViewModel(financeData, advice);
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
        txs.add(new Object[]{"2025-05-24", "Freelance", "Income", 300.0, true});
        txs.add(new Object[]{"2025-05-24", "Groceries", "Food", -50.0, true});
        UserBillStorage.saveTransactions(txs);

        financeData.importTransactions(txs); // <-- Add this line

        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);

        assertTrue(viewModel.getMonthlyBudget() >= 0);
        assertTrue(viewModel.getTotalIncome() >= 300.0);
        assertTrue(viewModel.getTotalExpenses() >= 50.0);
        assertNotNull(viewModel.getCategoryBudgets());
        assertNotNull(viewModel.getCategoryExpenses());
        assertNotNull(viewModel.getAdvice());
        assertNotNull(viewModel.getFormattedGenerationTime());
    }
}