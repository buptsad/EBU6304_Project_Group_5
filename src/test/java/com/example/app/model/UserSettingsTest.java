package com.example.app.model;

import com.example.app.user_data.UserSettingsStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UserSettings class.
 * Tests the singleton pattern implementation, default initialization,
 * settings persistence, and settings reset functionality.
 */
class UserSettingsTest {

    /**
     * Test username used for creating isolated test environment.
     */
    private static final String TEST_USERNAME = "testuser_settings";
    
    /**
     * Path to the settings file for the test user.
     */
    private static final String SETTINGS_FILE_PATH = ".\\user_data\\" + TEST_USERNAME + "\\user_settings.properties";

    /**
     * Cleans up test files and directories after all tests are complete.
     * Removes the settings file and its parent directory to avoid test artifacts.
     */
    @AfterAll
    static void cleanUp() {
        // Clean up the settings file after all tests
        File file = new File(SETTINGS_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        File directory = file.getParentFile();
        if (directory.exists()) {
            directory.delete();
        }
    }

    /**
     * Sets up the test environment before each test.
     * Configures the storage username, removes any existing settings file,
     * and resets the UserSettings singleton instance.
     */
    @BeforeEach
    void setUp() {
        // Set username for storage and remove any existing settings file for isolation
        UserSettingsStorage.setUsername(TEST_USERNAME);
        File file = new File(SETTINGS_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        // Reset singleton for isolation
        resetUserSettingsSingleton();
    }

    /**
     * Cleans up after each test by removing the settings file
     * and resetting the singleton instance.
     */
    @AfterEach
    void tearDown() {
        // Clean up settings file after test
        File file = new File(SETTINGS_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        resetUserSettingsSingleton();
    }

    /**
     * Helper method to reset the UserSettings singleton instance using reflection.
     * This ensures test isolation by creating a fresh instance for each test.
     */
    private void resetUserSettingsSingleton() {
        try {
            java.lang.reflect.Field instanceField = UserSettings.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Tests that UserSettings initializes with default values when no settings file exists.
     * Verifies that all properties have their expected default values.
     */
    @Test
    @DisplayName("Should initialize with default values if no file exists")
    void testDefaultInitialization() {
        UserSettings settings = UserSettings.getInstance();
        assertEquals("", settings.getName());
        assertEquals("", settings.getEmail());
        assertEquals("", settings.getPhone());
        assertEquals("USD", settings.getCurrencyCode());
        assertEquals("$", settings.getCurrencySymbol());
        assertFalse(settings.isDarkTheme());
        assertTrue(settings.isBudgetAlertsEnabled());
        assertTrue(settings.isTransactionAlertsEnabled());
        assertEquals("", settings.getPasswordHash());
    }

    /**
     * Tests that settings are correctly saved to storage and can be loaded later.
     * Sets various settings values, saves them, resets the singleton,
     * and verifies the values are preserved when a new instance is created.
     */
    @Test
    @DisplayName("Should save and load settings correctly")
    void testSaveAndLoadSettings() {
        UserSettings settings = UserSettings.getInstance();
        settings.setName("Alice");
        settings.setEmail("alice@example.com");
        settings.setPhone("1234567890");
        settings.setCurrencyCode("CNY");
        settings.setCurrencySymbol("¥");
        settings.setDarkTheme(true);
        settings.setBudgetAlertsEnabled(false);
        settings.setTransactionAlertsEnabled(false);
        settings.setPasswordHash("hash123");
        settings.saveSettings();

        // Reset singleton and reload
        resetUserSettingsSingleton();
        UserSettings settings2 = UserSettings.getInstance();

        assertEquals("Alice", settings2.getName());
        assertEquals("alice@example.com", settings2.getEmail());
        assertEquals("1234567890", settings2.getPhone());
        assertEquals("CNY", settings2.getCurrencyCode());
        assertEquals("¥", settings2.getCurrencySymbol());
        assertTrue(settings2.isDarkTheme());
        assertFalse(settings2.isBudgetAlertsEnabled());
        assertFalse(settings2.isTransactionAlertsEnabled());
        assertEquals("hash123", settings2.getPasswordHash());
    }

    /**
     * Tests that the resetToDefaults method properly restores all default values.
     * Sets custom values, calls resetToDefaults, and verifies that all properties
     * return to their default state.
     */
    @Test
    @DisplayName("Should reset to default values")
    void testResetToDefaults() {
        UserSettings settings = UserSettings.getInstance();
        settings.setName("Bob");
        settings.setEmail("bob@example.com");
        settings.setPhone("9876543210");
        settings.setCurrencyCode("EUR");
        settings.setCurrencySymbol("€");
        settings.setDarkTheme(true);
        settings.setBudgetAlertsEnabled(false);
        settings.setTransactionAlertsEnabled(false);
        settings.setPasswordHash("hash456");
        settings.saveSettings();

        settings.resetToDefaults();

        assertEquals("", settings.getName());
        assertEquals("", settings.getEmail());
        assertEquals("", settings.getPhone());
        assertEquals("USD", settings.getCurrencyCode());
        assertEquals("$", settings.getCurrencySymbol());
        assertFalse(settings.isDarkTheme());
        assertTrue(settings.isBudgetAlertsEnabled());
        assertTrue(settings.isTransactionAlertsEnabled());
        assertEquals("", settings.getPasswordHash());
    }

    /**
     * Tests that changes to settings are correctly persisted to the settings file.
     * Verifies that the file exists and contains the expected property values.
     */
    @Test
    @DisplayName("Should persist changes to settings file")
    void testSettingsFilePersistence() {
        UserSettings settings = UserSettings.getInstance();
        settings.setName("PersistedUser");
        settings.saveSettings();

        // Read file directly
        File file = new File(SETTINGS_FILE_PATH);
        assertTrue(file.exists());

        Properties props = new Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            props.load(fis);
        } catch (Exception e) {
            fail("Failed to read settings file: " + e.getMessage());
        }
        assertEquals("PersistedUser", props.getProperty("user.name"));
    }
}