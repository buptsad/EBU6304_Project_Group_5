package com.example.app.model;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CSVDataImporter class.
 * These tests verify the functionality for importing transaction data from CSV files,
 * including handling of valid and invalid data formats, duplicates, and file not found scenarios.
 */
class CSVDataImporterTest {

    /**
     * Temporary file used for testing CSV import operations.
     */
    private Path tempFile;

    /**
     * Creates a temporary CSV file before each test.
     * 
     * @throws IOException If the temporary file cannot be created
     */
    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("transactions", ".csv");
    }

    /**
     * Deletes the temporary file after each test to clean up resources.
     * 
     * @throws IOException If the temporary file cannot be deleted
     */
    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    /**
     * Tests that valid transaction data can be successfully imported from a CSV file.
     * Verifies that all fields are correctly parsed and returned in the expected format.
     * 
     * @throws IOException If there is an error writing to or reading from the test file
     */
    @Test
    @DisplayName("Should import valid transactions from CSV")
    void testImportValidTransactions() throws IOException {
        List<String> lines = Arrays.asList(
                "Date,Description,Category,Amount",
                "2024-06-01,Salary,Income,5000.0",
                "2024-06-02,Groceries,Food,-200.0"
        );
        Files.write(tempFile, lines);

        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(tempFile.toString());
        assertEquals(2, transactions.size());
        assertEquals("2024-06-01", transactions.get(0)[0]);
        assertEquals("Salary", transactions.get(0)[1]);
        assertEquals("Income", transactions.get(0)[2]);
        assertEquals(5000.0, (Double) transactions.get(0)[3], 0.01);
    }

    /**
     * Tests that the importer can handle invalid lines and duplicate entries.
     * Verifies that invalid lines are skipped and only unique valid transactions are returned.
     * 
     * @throws IOException If there is an error writing to or reading from the test file
     */
    @Test
    @DisplayName("Should skip invalid lines and handle duplicates")
    void testSkipInvalidAndDuplicateLines() throws IOException {
        List<String> lines = Arrays.asList(
                "Date,Description,Category,Amount",
                "2024-06-01,Salary,Income,5000.0",
                "2024-06-01,Salary,Income,5000.0", // duplicate
                "2024-06-02,Groceries,Food,-200.0",
                "InvalidLine",
                "2024-06-03,Bus Ticket,Transportation,notanumber"
        );
        Files.write(tempFile, lines);

        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(tempFile.toString());
        assertEquals(2, transactions.size());
        assertEquals("2024-06-01", transactions.get(0)[0]);
        assertEquals("2024-06-02", transactions.get(1)[0]);
    }

    /**
     * Tests that attempting to import from a non-existent file returns an empty list.
     * Verifies the importer's resilience to file not found scenarios.
     */
    @Test
    @DisplayName("Should return empty list if file does not exist")
    void testFileNotExist() {
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV("nonexistent_file.csv");
        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }
}