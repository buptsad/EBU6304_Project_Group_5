package com.example.app.model;

import com.example.app.user_data.FinancialAdviceStorage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the FinancialAdvice class.
 * These tests verify the functionality for creating, loading, and saving financial advice,
 * including storage persistence, time tracking, and error handling.
 */
class FinancialAdviceTest {

    /**
     * Test username used for creating isolated test environment.
     */
    private static final String TEST_USERNAME = "testuser_junit";
    
    /**
     * Path to the advice file for the test user.
     */
    private static final String ADVICE_FILE_PATH = ".\\user_data\\" + TEST_USERNAME + "\\user_advice.txt";

    /**
     * Cleans up test files and directories after all tests are complete.
     * Removes the advice file and its parent directory to avoid test artifacts.
     */
    @AfterAll
    static void cleanUp() {
        // Clean up the advice file after all tests
        File file = new File(ADVICE_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
        File directory = file.getParentFile();
        if (directory.exists()) {
            directory.delete();
        }
    }

    /**
     * Removes the advice file before each test to ensure test isolation.
     * This prevents state from one test affecting another.
     */
    @BeforeEach
    void cleanAdviceFile() {
        // Remove the advice file before each test to ensure isolation
        File file = new File(ADVICE_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Tests that the default constructor sets default advice and generation time.
     * Verifies that the advice text contains expected content and a timestamp is created.
     */
    @Test
    @DisplayName("Default constructor sets default advice and time")
    void testDefaultConstructor() {
        FinancialAdvice advice = new FinancialAdvice();
        assertNotNull(advice.getAdvice());
        assertNotNull(advice.getGenerationTime());
        assertTrue(advice.getAdvice().contains("Spring Festival"));
    }

    /**
     * Tests that the initialize method loads advice from storage and sets the username.
     * Verifies that previously saved advice is correctly loaded during initialization.
     */
    @Test
    @DisplayName("initialize loads advice from storage and sets username")
    void testInitializeLoadsFromStorage() {
        // Save known advice to storage
        String expectedAdvice = "Advice from storage";
        LocalDateTime now = LocalDateTime.now().withNano(0);
        FinancialAdviceStorage.setUsername(TEST_USERNAME);
        FinancialAdviceStorage.saveAdvice(expectedAdvice, now);

        FinancialAdvice advice = new FinancialAdvice();
        advice.initialize(TEST_USERNAME);

        assertEquals(expectedAdvice, advice.getAdvice());
        // Allow for possible second difference due to file write/read
        assertEquals(now, advice.getGenerationTime());
    }

    /**
     * Tests that the setAdvice method updates advice text, timestamp, and saves to storage.
     * Verifies that advice content and generation time are updated, and that changes are persisted to disk.
     */
    @Test
    @DisplayName("setAdvice updates advice, time, and saves to storage")
    void testSetAdviceUpdatesAdviceAndSaves() {
        FinancialAdvice advice = new FinancialAdvice();
        advice.initialize(TEST_USERNAME);

        String newAdvice = "New advice for user";
        LocalDateTime beforeSet = LocalDateTime.now().withNano(0);

        advice.setAdvice(newAdvice);

        assertEquals(newAdvice, advice.getAdvice());
        assertTrue(!advice.getGenerationTime().isBefore(beforeSet));

        // Check file content
        File file = new File(ADVICE_FILE_PATH);
        assertTrue(file.exists());

        // Read file and check content
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String dateStr = reader.readLine();
            String fileAdvice = reader.readLine();
            assertNotNull(dateStr);
            assertNotNull(fileAdvice);
            assertEquals(newAdvice, fileAdvice);
            // Check date format
            LocalDateTime fileTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            assertEquals(advice.getGenerationTime().withNano(0), fileTime);
        } catch (Exception e) {
            fail("Failed to read advice file: " + e.getMessage());
        }
    }

    /**
     * Tests that the regenerate method handles IOException gracefully.
     * Verifies that no exceptions are thrown even when API calls might fail.
     */
    @Test
    @DisplayName("regenerate handles IOException gracefully")
    void testRegenerateHandlesIOException() {
        FinancialAdvice advice = new FinancialAdvice();
        advice.initialize(TEST_USERNAME);
        // This will attempt to call the real getRes API, which may fail, but should not throw
        assertDoesNotThrow(advice::regenerate);
    }
}