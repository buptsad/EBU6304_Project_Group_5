package com.example.app.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for importing financial transaction data from CSV files.
 * Provides functionality to read, validate, and deduplicate transaction records.
 */
public class CSVDataImporter {
    /**
     * Private constructor to prevent instantiation of this utility class.
     * This class only contains static methods and should not be instantiated.
     */
    private CSVDataImporter() {

    }
    
    /**
     * Imports financial transactions from a CSV file.
     * The expected CSV format has at least 4 columns: date, description, category, and amount.
     * This method handles deduplication based on all transaction fields.
     *
     * @param filePath path to the CSV file to be imported
     * @return a list of transaction data as Object arrays, where each array represents
     *         a transaction with elements [date, description, category, amount]
     */
    public static List<Object[]> importTransactionsFromCSV(String filePath) {
        List<Object[]> transactions = new ArrayList<>();
        // Set for deduplication
        Set<String> uniqueTransactions = new HashSet<>();
        
        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            // Skip header line
            String line = br.readLine();
            
            // Read data lines
            while ((line = br.readLine()) != null) {
                // Skip comment lines or empty lines
                if (line.trim().startsWith("//") || line.trim().isEmpty()) {
                    continue;
                }
                
                // Split CSV line
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    System.err.println("Invalid CSV line: " + line);
                    continue;
                }
                
                String date = parts[0].trim();
                String description = parts[1].trim();
                String category = parts[2].trim();
                
                // Process amount
                double amount;
                try {
                    amount = Double.parseDouble(parts[3].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid amount: " + parts[3]);
                    continue;
                }
                
                // Create unique identifier for deduplication
                String uniqueKey = date + "|" + description + "|" + category + "|" + amount;
                if (!uniqueTransactions.contains(uniqueKey)) {
                    // Only add unique transactions
                    uniqueTransactions.add(uniqueKey);
                    Object[] transaction = new Object[] {date, description, category, amount};
                    transactions.add(transaction);
                } else {
                    System.out.println("Skipping duplicate transaction: " + date + " " + description + " " + amount);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
}