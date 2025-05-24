package com.example.app.viewmodel.pages;

import com.example.app.model.FinanceData;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ReportsViewModelTest {
    private static final String TEST_USERNAME = "testuser_reports";
    private ReportsViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new ReportsViewModel(TEST_USERNAME);
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
    void testFinanceDataNotNull() {
        assertNotNull(viewModel.getFinanceData());
    }

    @Test
    void testLoadTransactionDataNoCrash() {
        viewModel.loadTransactionData();
        assertNotNull(viewModel.getFinanceData());
    }
}