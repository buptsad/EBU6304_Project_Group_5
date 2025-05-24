package com.example.app.viewmodel.dashboard;

import com.example.app.user_data.UserBillStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DashboardTransactionsViewModelTest {
    private static final String TEST_USER = "testuser_transactions_vm";
    private static final String DATA_DIR = ".\\user_data\\" + TEST_USER;
    private DashboardTransactionsViewModel viewModel;

    @BeforeEach
    void setUp() {
        UserBillStorage.setUsername(TEST_USER);
        viewModel = new DashboardTransactionsViewModel(TEST_USER);
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
    void testGetRecentTransactions() {
        List<Object[]> txs = new ArrayList<>();
        txs.add(new Object[]{"2025-05-24 12:00", "Coffee", "Food", -5.0, true});
        txs.add(new Object[]{"2025-05-23 09:00", "Book", "Education", -15.0, true});
        UserBillStorage.saveTransactions(txs);

        viewModel.onDataRefresh(com.example.app.model.DataRefreshManager.RefreshType.TRANSACTIONS);
        List<DashboardTransactionsViewModel.TransactionEntry> recent = viewModel.getRecentTransactions();
        assertFalse(recent.isEmpty());
        assertEquals("Coffee", recent.get(0).getDescription());
    }
}