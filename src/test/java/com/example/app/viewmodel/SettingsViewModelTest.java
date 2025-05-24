package com.example.app.viewmodel;

import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SettingsViewModelTest {
    private static final String TEST_USERNAME = "testuser_settings";
    private SettingsViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new SettingsViewModel(TEST_USERNAME);
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
    void testUpdateProfile() {
        viewModel.updateProfile("Alice", "alice@example.com", "1234567890");
        assertEquals("Alice", viewModel.getName());
        assertEquals("alice@example.com", viewModel.getEmail());
        assertEquals("1234567890", viewModel.getPhone());
    }

    @Test
    void testUpdateCurrency() {
        viewModel.updateCurrency("USD", "$");
        assertEquals("USD", viewModel.getCurrencyCode());
        assertEquals("$", viewModel.getCurrencySymbol());
    }

    @Test
    void testUpdateTheme() {
        viewModel.updateTheme(true);
        assertTrue(viewModel.isDarkTheme());
    }

    @Test
    void testUpdateNotifications() {
        viewModel.updateNotifications(true, false);
        assertTrue(viewModel.isBudgetAlertsEnabled());
        assertFalse(viewModel.isTransactionAlertsEnabled());
    }

    @Test
    void testPasswordValidationAndUpdate() {
        assertTrue(viewModel.validateCurrentPassword(""));
        assertFalse(viewModel.updatePassword("", "", ""));
        assertFalse(viewModel.updatePassword("old", "new", "different"));
    }

    @Test
    void testResetToDefaults() {
        viewModel.resetToDefaults();
        assertNotNull(viewModel.getName());
    }
}