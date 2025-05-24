package com.example.app.viewmodel.pages;

import com.example.app.model.FinanceData;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DashboardViewModelTest {
    private static final String TEST_USERNAME = "testuser_dashboard";
    private DashboardViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new DashboardViewModel(TEST_USERNAME);
    }

    @AfterEach
    void tearDown() {
        // Remove test directory and files
        File dir = new File(".\\user_data\\" + TEST_USERNAME);
        if (dir.exists()) {
            for (File file : dir.listFiles()) file.delete();
            dir.delete();
        }
    }

    @Test
    void testGetUsername() {
        assertEquals(TEST_USERNAME, viewModel.getUsername());
    }

    @Test
    void testSetAndGetActivePanel() {
        viewModel.setActivePanel(DashboardViewModel.BUDGETS_PANEL);
        assertEquals(DashboardViewModel.BUDGETS_PANEL, viewModel.getActivePanel());
    }

    @Test
    void testFinanceDataNotNull() {
        assertNotNull(viewModel.getFinanceData());
    }
}