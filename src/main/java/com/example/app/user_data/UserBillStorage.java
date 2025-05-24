package com.example.app.user_data;

import com.example.app.ui.pages.AI.classification;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Handles storage of user bill (transaction) data.
 * This class manages the physical storage of user transaction data in CSV format.
 * <p>
 * Features:
 * <ul>
 *   <li>Loads and saves transactions to a user-specific CSV file</li>
 *   <li>Initializes storage with headers if needed</li>
 *   <li>Supports batch classification of transactions using AI</li>
 *   <li>Handles CSV escaping and parsing</li>
 * </ul>
 
 */
public class UserBillStorage {
    /** Private constructor to prevent instantiation */
    private UserBillStorage() {
        // Prevent instantiation
    }
    private static final Logger LOGGER = Logger.getLogger(UserBillStorage.class.getName());
    private static final String BILL_FILENAME = "user_bill.csv";
    private static File billFile;
    private static String username;

    // CSV format definitions
    private static final String CSV_HEADER = "Date,Description,Category,Amount,Confirmed";
    private static final String CSV_FORMAT = "%s,%s,%s,%.2f,%b";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Sets the current username and updates the file path.
     * @param username The current user's username
     */
    public static void setUsername(String username) {
        UserBillStorage.username = username;
        // Update file path to user-specific path
        String packagePath = ".\\user_data\\" + username;
        billFile = new File(packagePath, BILL_FILENAME);

        // Ensure file exists
        initializeStorage();
    }

    /**
     * Initializes the storage directory and file.
     * Creates the directory and file if they do not exist, and writes CSV header if needed.
     */
    private static void initializeStorage() {
        File directory = billFile.getParentFile();

        // Create directory if it doesn't exist
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.log(Level.INFO, "Created bill directory at: {0}", directory.getAbsolutePath());
            } else {
                LOGGER.log(Level.SEVERE, "Could not create bill directory at: {0}", directory.getAbsolutePath());
                return;
            }
        }

        // Create file if it doesn't exist
        if (!billFile.exists()) {
            try {
                if (billFile.createNewFile()) {
                    LOGGER.log(Level.INFO, "Created bill file at: {0}", billFile.getAbsolutePath());

                    // Initialize CSV file with header
                    try (PrintWriter writer = new PrintWriter(new FileWriter(billFile))) {
                        writer.println(CSV_HEADER);
                    }

                    LOGGER.log(Level.INFO, "Initialized CSV file header");
                } else {
                    LOGGER.log(Level.SEVERE, "Could not create bill file at: {0}", billFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating bill file", e);
            }
        } else {
            LOGGER.log(Level.INFO, "Bill file already exists at: {0}", billFile.getAbsolutePath());
        }
    }

    /**
     * Gets the path to the bill file.
     * @return The path to the bill file
     */
    public static String getBillFilePath() {
        return billFile.getAbsolutePath();
    }

    /**
     * Loads transactions from the CSV file.
     * @return List of transaction records
     */
    public static List<Object[]> loadTransactions() {
        List<Object[]> transactions = new ArrayList<>();

        // Confirm file exists
        if (!billFile.exists()) {
            LOGGER.log(Level.WARNING, "Bill file does not exist: {0}", billFile.getAbsolutePath());
            return transactions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(billFile))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Parse CSV line, handling quoted fields
                String[] parts = parseCSVLine(line);
                if (parts.length >= 5) {
                    try {
                        String dateStr = parts[0];
                        String description = parts[1];
                        String category = parts[2];
                        double amount = Double.parseDouble(parts[3]);
                        boolean confirmed = Boolean.parseBoolean(parts[4]);

                        Object[] transaction = {dateStr, description, category, amount, confirmed};
                        transactions.add(transaction);
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "Error parsing transaction: " + line, e);
                    }
                }
            }

            LOGGER.log(Level.INFO, "Successfully loaded transactions from: {0}", billFile.getAbsolutePath());
            LOGGER.log(Level.INFO, "Loaded {0} transactions", transactions.size());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading transactions from file: " + e.getMessage(), e);
        }

        return transactions;
    }

    /**
     * Parses a CSV line, handling commas inside quotes.
     * @param line The CSV line
     * @return Array of fields
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // Add last field
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Saves the given transactions to the CSV file.
     * @param transactions List of transactions to save
     * @return true if successful, false otherwise
     */
    public static boolean saveTransactions(List<Object[]> transactions) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(billFile))) {
            // Write CSV header
            writer.println(CSV_HEADER);

            // If no transactions, return immediately
            if (transactions.isEmpty()) {
                LOGGER.log(Level.INFO, "No transactions to save");
                return true;
            }

            // Write each transaction with its original category
            for (Object[] transaction : transactions) {
                String dateStr = (String) transaction[0];
                String description = escapeCSV((String) transaction[1]);
                String category = escapeCSV((String) transaction[2]);
                double amount = (Double) transaction[3];
                boolean confirmed = transaction.length > 4 ? (Boolean) transaction[4] : false;

                writer.println(String.format(CSV_FORMAT, dateStr, description, category, amount, confirmed));
            }

            LOGGER.log(Level.INFO, "Successfully saved {0} transactions to: {1}",
                    new Object[]{transactions.size(), billFile.getAbsolutePath()});
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving transactions to file: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Escapes special characters in a CSV field.
     * @param field The field to escape
     * @return Escaped field
     */
    private static String escapeCSV(String field) {
        if (field == null) {
            return "";
        }

        // If field contains comma, quote, or newline, wrap in quotes and double quotes inside
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Adds new transactions to the existing records.
     * @param newTransactions List of new transactions to add
     * @return true if successful, false otherwise
     */
    public static boolean addTransactions(List<Object[]> newTransactions) {
        // Load existing transactions
        List<Object[]> existingTransactions = loadTransactions();

        // Add new transactions
        existingTransactions.addAll(newTransactions);

        // Save updated transactions
        return saveTransactions(existingTransactions);
    }
}