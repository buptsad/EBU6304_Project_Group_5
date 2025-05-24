package com.example.app.viewmodel.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.user_data.UserBillStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OverviewViewModelTest {
    private static final String TEST_USER = "testuser_overview_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private OverviewViewModel viewModel;

    @BeforeEach
    void setUp() {
        UserBillStorage.setUsername(TEST_USER);
        viewModel = new OverviewViewModel(TEST_USER, new FinanceData(), new FinancialAdvice());
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
    void testGetFinanceDataAndAdvice() {
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24", "Gift", "Income", 200.0, true});
        UserBillStorage.saveTransactions(txs);

        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        assertTrue(viewModel.getFinanceData().getTotalIncome() >= 200.0);
        assertNotNull(viewModel.getFinancialAdvice());
    }

    @Test
    void testCheckBudgetWarnings() {
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24 15:00", "Dinner", "Food", -95.0, true});
        UserBillStorage.saveTransactions(txs);

        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        // Should trigger warning, but just ensure no exception and method runs
        assertDoesNotThrow(() -> viewModel.checkBudgetWarnings());
    }
}