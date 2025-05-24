package com.example.app.user_data;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

             /**
 * Unit tests for the UserSettingsStorage class.
 * Tests the functionality for reading and writing user settings to storage,
 * including file and directory creation, settings persistence, and default value handling.
 */
class UserSettingsStorageTest {

    /**
     * Test username used for creating isolated test environment.
     */
    private static final String TEST_USERNAME = "testuser_settingsstorage";
    
    /**
     * Path to the settings file for the test user.
     */
    private static final String SETTINGS_FILE_PATH = ".\\user_data\\" + TEST_USERNAME + "\\user_settings.properties";
    
    /**
     * File object representing the settings file.
     */
    private static final File SETTINGS_FILE = new File(SETTINGS_FILE_PATH);

    /**
     * Sets up the test environment before each test.
     * Cleans up any existing test files and directories, then initializes
     * the UserSettingsStorage with the test username.
     */
    @BeforeEach
    void setUp() {
        // Clean up before test
        if (SETTINGS_FILE.exists()) {
            SETTINGS_FILE.delete();
        }
        File dir = SETTINGS_FILE.getParentFile();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
        // Now create the file for the test
        UserSettingsStorage.setUsername(TEST_USERNAME);
    }

    /**
     * Cleans up test files and directories after all tests are complete.
     * Removes the settings file, its parent directory, and the user_data directory
     * if it's empty to avoid leaving test artifacts.
     */
    @AfterAll
    static void cleanUp() {
        // Clean up settings file and parent directory after all tests
        if (SETTINGS_FILE.exists()) {
            SETTINGS_FILE.delete();
        }
        File dir = SETTINGS_FILE.getParentFile();
        if (dir.exists() && dir.isDirectory()) {
            // Delete all files in the directory
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
        // Clean up user_data directory if empty
        File userDataDir = new File(".\\user_data");
        if (userDataDir.exists() && userDataDir.isDirectory() && userDataDir.list().length == 0) {
            userDataDir.delete();
        }
    }

    /**
     * Tests that initializing the storage creates the necessary file and directory.
     * Verifies that both the settings file and its parent directory are created.
     */
    @Test
    @DisplayName("Should create settings file and directory if not exist")
    void testInitializeStorageCreatesFileAndDirectory() {
        // File and directory should exist after setUsername
        assertTrue(SETTINGS_FILE.exists(), "Settings file should be created");
        assertTrue(SETTINGS_FILE.getParentFile().exists(), "Settings directory should be created");
    }

    /**
     * Tests that settings can be saved and loaded correctly.
     * Verifies that all properties are preserved when saved and loaded.
     */
    @Test
    @DisplayName("Should write and read settings correctly")
    void testSaveAndLoadSettings() {
        Properties props = new Properties();
        props.setProperty("user.name", "Alice");
        props.setProperty("user.email", "alice@example.com");
        props.setProperty("currency.code", "CNY");
        props.setProperty("theme.dark", "true");

        assertTrue(UserSettingsStorage.saveSettings(props), "Should save settings successfully");

        Properties loaded = UserSettingsStorage.loadSettings();
        assertNotNull(loaded, "Loaded properties should not be null");
        assertEquals("Alice", loaded.getProperty("user.name"));
        assertEquals("alice@example.com", loaded.getProperty("user.email"));
        assertEquals("CNY", loaded.getProperty("currency.code"));
        assertEquals("true", loaded.getProperty("theme.dark"));
    }

    /**
     * Tests that default settings are loaded if the settings file does not exist.
     * Verifies that when the file is missing, default settings are created with expected values.
     */
    @Test
    @DisplayName("Should load default settings if file does not exist")
    void testLoadDefaultSettingsIfFileMissing() {
        // Delete file and directory to simulate missing file
        if (SETTINGS_FILE.exists()) SETTINGS_FILE.delete();
        File dir = SETTINGS_FILE.getParentFile();
        if (dir.exists()) dir.delete();

        // setUsername should recreate file with defaults
        UserSettingsStorage.setUsername(TEST_USERNAME);
        Properties loaded = UserSettingsStorage.loadSettings();
        assertNotNull(loaded, "Loaded properties should not be null");
        assertEquals(TEST_USERNAME, loaded.getProperty("user.name"));
        assertEquals("USD", loaded.getProperty("currency.code"));
        assertEquals("$", loaded.getProperty("currency.symbol"));
        assertEquals("false", loaded.getProperty("theme.dark"));
        assertEquals("true", loaded.getProperty("notifications.budget.enabled"));
        assertEquals("true", loaded.getProperty("notifications.transaction.enabled"));
        assertEquals("", loaded.getProperty("security.password.hash"));
    }

    /**
     * Tests that null is returned if the file is missing after initialization.
     * Verifies that if the file is deleted after storage initialization, loadSettings returns null.
     */
    @Test
    @DisplayName("Should return null if file is missing after initialization")
    void testLoadSettingsReturnsNullIfFileMissing() {
        // Delete file after initialization
        if (SETTINGS_FILE.exists()) SETTINGS_FILE.delete();
        Properties loaded = UserSettingsStorage.loadSettings();
        assertNull(loaded, "Should return null if file is missing");
    }

    /**
     * Tests that settings are correctly persisted to disk.
     * Verifies the file content by reading the file directly and checking property values.
     * 
     * @throws Exception If there is an error reading the file
     */
    @Test
    @DisplayName("Should persist settings to disk")
    void testSettingsPersistedToDisk() throws Exception {
        Properties props = new Properties();
        props.setProperty("user.name", "PersistedUser");
        assertTrue(UserSettingsStorage.saveSettings(props));

        // Read file directly
        Properties fileProps = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            fileProps.load(fis);
        }
        assertEquals("PersistedUser", fileProps.getProperty("user.name"));
    }
}