package com.example.app.user_data;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UserBillStorage class.
 * Tests the functionality for reading and writing transaction data to storage,
 * including file and directory creation, data persistence, and error handling.
 */
class UserBillStorageTest {

    /**
     * Test username used for creating isolated test environment.
     */
    private static final String TEST_USERNAME = "testuser_billstorage";
    
    /**
     * Path to the bill file for the test user.
     */
    private static final String BILL_FILE_PATH = ".\\user_data\\" + TEST_USERNAME + "\\user_bill.csv";
    
    /**
     * File object representing the bill file.
     */
    private static final File BILL_FILE = new File(BILL_FILE_PATH);

    /**
     * Sets up the test environment before each test.
     * Cleans up any existing test files and directories, then initializes
     * the UserBillStorage with the test username.
     */
    @BeforeEach
    void setUp() {
        // Clean up before test
        if (BILL_FILE.exists()) {
            BILL_FILE.delete();
        }
        File dir = BILL_FILE.getParentFile();
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                file.delete();
            }
            dir.delete();
        }
        // Now create the file for the test
        UserBillStorage.setUsername(TEST_USERNAME);
    }

    /**
     * Cleans up test files and directories after all tests are complete.
     * Removes the bill file, its parent directory, and the user_data directory
     * if it's empty to avoid leaving test artifacts.
     */
    @AfterAll
    static void cleanUp() {
        File dir = BILL_FILE.getParentFile();
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
     * Verifies that both the bill file and its parent directory are created.
     */
    @Test
    @DisplayName("Should create bill file and directory if not exist")
    void testInitializeStorageCreatesFileAndDirectory() {
        assertTrue(BILL_FILE.exists(), "Bill file should be created");
        assertTrue(BILL_FILE.getParentFile().exists(), "Bill directory should be created");
    }

    /**
     * Tests that transactions can be saved and loaded correctly.
     * Verifies that all transaction fields (date, description, category, amount, recurring flag)
     * are preserved when saved and loaded.
     */
    @Test
    @DisplayName("Should write and read transactions correctly")
    void testSaveAndLoadTransactions() {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2024-06-01 12:00", "Lunch", "Food", -20.0, true});
        transactions.add(new Object[]{"2024-06-02 08:30", "Bus", "Transportation", -2.5, false});
        assertTrue(UserBillStorage.saveTransactions(transactions), "Should save transactions successfully");

        List<Object[]> loaded = UserBillStorage.loadTransactions();
        assertEquals(2, loaded.size());
        assertEquals("2024-06-01 12:00", loaded.get(0)[0]);
        assertEquals("Lunch", loaded.get(0)[1]);
        assertNotNull(loaded.get(0)[2]);
        assertFalse(((String) loaded.get(0)[2]).isEmpty());
        assertEquals(-20.0, (Double) loaded.get(0)[3], 0.01);
        assertEquals(true, loaded.get(0)[4]);
        assertEquals("2024-06-02 08:30", loaded.get(1)[0]);
        assertEquals("Bus", loaded.get(1)[1]);
        assertNotNull(loaded.get(1)[2]);
        assertFalse(((String) loaded.get(1)[2]).isEmpty());
        assertEquals(-2.5, (Double) loaded.get(1)[3], 0.01);
        assertEquals(false, loaded.get(1)[4]);
    }

    /**
     * Tests that an empty list is returned if the bill file does not exist.
     * Verifies the storage's resilience to missing files by ensuring a non-null
     * empty list is returned rather than throwing an exception.
     */
    @Test
    @DisplayName("Should load empty list if file does not exist")
    void testLoadTransactionsFileNotExist() {
        if (BILL_FILE.exists()) BILL_FILE.delete();
        File dir = BILL_FILE.getParentFile();
        if (dir.exists()) dir.delete();
        UserBillStorage.setUsername(TEST_USERNAME);
        if (BILL_FILE.exists()) BILL_FILE.delete();
        List<Object[]> loaded = UserBillStorage.loadTransactions();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }

    /**
     * Tests that transactions are correctly persisted to disk.
     * Verifies by reading the file directly that transaction data is properly written to the CSV file.
     * 
     * @throws Exception If there is an error reading the file
     */
    @Test
    @DisplayName("Should persist transactions to disk")
    void testTransactionsPersistedToDisk() throws Exception {
        List<Object[]> transactions = new ArrayList<>();
        transactions.add(new Object[]{"2025-01-01 09:00", "Persisted", "Other", 123.45, true});
        assertTrue(UserBillStorage.saveTransactions(transactions));

        // Read file directly
        List<String> lines = java.nio.file.Files.readAllLines(BILL_FILE.toPath());
        assertTrue(lines.stream().anyMatch(line -> line.contains("Persisted")));
    }
}