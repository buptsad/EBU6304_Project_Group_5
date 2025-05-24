package com.example.app.user_data;

import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UserBudgetStorage class.
 * Tests the functionality for reading and writing budget data to storage,
 * including file and directory creation, data persistence, and error handling.
 */
class UserBudgetStorageTest {

    /**
     * Test username used for creating isolated test environment.
     */
    private static final String TEST_USERNAME = "testuser_budgetstorage";
    
    /**
     * Path to the budget file for the test user.
     */
    private static final String BUDGET_FILE_PATH = ".\\user_data\\" + TEST_USERNAME + "\\user_budgets.csv";
    
    /**
     * File object representing the budget file.
     */
    private static final File BUDGET_FILE = new File(BUDGET_FILE_PATH);

    /**
     * Sets up the test environment before each test.
     * Cleans up any existing test files and directories, then initializes
     * the UserBudgetStorage with the test username.
     */
    @BeforeEach
    void setUp() {
        // Clean up before test
        if (BUDGET_FILE.exists()) {
            BUDGET_FILE.delete();
        }
        File dir = BUDGET_FILE.getParentFile();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
        // Now create the file for the test
        UserBudgetStorage.setUsername(TEST_USERNAME);
    }

    /**
     * Cleans up test files and directories after all tests are complete.
     * Removes the budget file, its parent directory, and the user_data directory
     * if it's empty to avoid leaving test artifacts.
     */
    @AfterAll
    static void cleanUp() {
        File dir = BUDGET_FILE.getParentFile();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
        // Do NOT delete user_data directory!
    }

    /**
     * Tests that initializing the storage creates the necessary file and directory.
     * Verifies that both the budget file and its parent directory are created.
     */
    @Test
    @DisplayName("Should create budget file and directory if not exist")
    void testInitializeStorageCreatesFileAndDirectory() {
        assertTrue(BUDGET_FILE.exists(), "Budget file should be created");
        assertTrue(BUDGET_FILE.getParentFile().exists(), "Budget directory should be created");
    }

    /**
     * Tests that budgets can be saved and loaded correctly.
     * Verifies that all budget fields (category, amount, start date, end date)
     * are preserved when saved and loaded.
     */
    @Test
    @DisplayName("Should write and read budgets correctly")
    void testSaveAndLoadBudgets() {
        List<Object[]> budgets = new ArrayList<>();
        budgets.add(new Object[]{"Food", 500.0, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30)});
        budgets.add(new Object[]{"Housing", 1200.0, null, null});
        assertTrue(UserBudgetStorage.saveBudgets(budgets), "Should save budgets successfully");

        List<Object[]> loaded = UserBudgetStorage.loadBudgets();
        assertEquals(2, loaded.size());
        assertEquals("Food", loaded.get(0)[0]);
        assertEquals(500.0, (Double) loaded.get(0)[1], 0.01);
        assertEquals(LocalDate.of(2024, 6, 1), loaded.get(0)[2]);
        assertEquals(LocalDate.of(2024, 6, 30), loaded.get(0)[3]);
        assertEquals("Housing", loaded.get(1)[0]);
        assertEquals(1200.0, (Double) loaded.get(1)[1], 0.01);
        assertNull(loaded.get(1)[2]);
        assertNull(loaded.get(1)[3]);
    }

    /**
     * Tests that an empty list is returned if the budget file does not exist.
     * Verifies the storage's resilience to missing files by ensuring a non-null
     * empty list is returned rather than throwing an exception.
     */
    @Test
    @DisplayName("Should load empty list if file does not exist")
    void testLoadBudgetsFileNotExist() {
        if (BUDGET_FILE.exists()) BUDGET_FILE.delete();
        File dir = BUDGET_FILE.getParentFile();
        if (dir.exists()) dir.delete();
        UserBudgetStorage.setUsername(TEST_USERNAME);
        if (BUDGET_FILE.exists()) BUDGET_FILE.delete();
        List<Object[]> loaded = UserBudgetStorage.loadBudgets();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    /**
     * Tests that budgets are correctly persisted to disk.
     * Verifies by reading the file directly that budget data is properly written to the CSV file.
     * 
     * @throws Exception If there is an error reading the file
     */
    @Test
    @DisplayName("Should persist budgets to disk")
    void testBudgetsPersistedToDisk() throws Exception {
        List<Object[]> budgets = new ArrayList<>();
        budgets.add(new Object[]{"Persisted", 999.0, LocalDate.of(2025, 1, 1), null});
        assertTrue(UserBudgetStorage.saveBudgets(budgets));

        // Read file directly
        List<String> lines = java.nio.file.Files.readAllLines(BUDGET_FILE.toPath());
        assertTrue(lines.stream().anyMatch(line -> line.contains("Persisted")));
    }
}