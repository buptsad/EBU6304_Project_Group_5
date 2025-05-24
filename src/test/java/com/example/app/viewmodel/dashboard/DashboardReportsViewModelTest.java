package com.example.app.viewmodel.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.user_data.UserBillStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DashboardReportsViewModelTest {
    private static final String TEST_USER = "testuser_reports_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private DashboardReportsViewModel viewModel;

    @BeforeEach
    void setUp() {
        UserBillStorage.setUsername(TEST_USER);
        viewModel = new DashboardReportsViewModel(TEST_USER);
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
    void testGetFinanceData() {
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24", "Salary", "Food", 1000.0, true});
        UserBillStorage.saveTransactions(txs);

        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        FinanceData data = viewModel.getFinanceData();
        assertTrue(data.getTotalIncome() > 999.0);
    }
}