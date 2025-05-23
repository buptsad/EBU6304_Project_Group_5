package com.example.app.user_data;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Storage handler for user budget data
 * This class manages the physical storage of budget data in the user_data package
 */
public class UserBudgetStorage {
    private static final Logger LOGGER = Logger.getLogger(UserBudgetStorage.class.getName());
    private static final String BUDGET_FILENAME = "user_budgets.csv";
    private static File budgetFile;
    private static String username;
    
    // Define CSV format
    private static final String CSV_HEADER = "Category,Amount,StartDate,EndDate";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Set current username and update file path
     * @param username Current user's username
     */
    public static void setUsername(String username) {
        UserBudgetStorage.username = username;
        // Update file path to user-specific path
        String packagePath = ".\\user_data\\" + username;
        budgetFile = new File(packagePath, BUDGET_FILENAME);
        
        // Ensure file exists
        initializeStorage();
    }
    
    /**
     * Initialize the storage directory and file
     */
    private static void initializeStorage() {
        File directory = budgetFile.getParentFile();
        
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.log(Level.INFO, "Created budget directory at: {0}", directory.getAbsolutePath());
            } else {
                LOGGER.log(Level.SEVERE, "Failed to create budget directory at: {0}", directory.getAbsolutePath());
                return;
            }
        }
        
        // Create file if it doesn't exist
        if (!budgetFile.exists()) {
            try {
                if (budgetFile.createNewFile()) {
                    LOGGER.log(Level.INFO, "Created budget file at: {0}", budgetFile.getAbsolutePath());
                    
                    // Initialize CSV file with header
                    try (PrintWriter writer = new PrintWriter(new FileWriter(budgetFile))) {
                        writer.println(CSV_HEADER);
                    }
                    
                    LOGGER.log(Level.INFO, "Initialized CSV file header");
                } else {
                    LOGGER.log(Level.SEVERE, "Failed to create budget file at: {0}", budgetFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating budget file", e);
            }
        } else {
            LOGGER.log(Level.INFO, "Budget file already exists at: {0}", budgetFile.getAbsolutePath());
        }
    }
    
    /**
     * Get the path to the budget file
     * @return Path to the budget file
     */
    public static String getBudgetFilePath() {
        return budgetFile.getAbsolutePath();
    }
    
    /**
     * Load budgets from CSV file
     * @return List of budget entries as [Category, Amount, StartDate, EndDate]
     */
    public static List<Object[]> loadBudgets() {
        List<Object[]> budgets = new ArrayList<>();
        
        // Confirm file exists
        if (!budgetFile.exists()) {
            LOGGER.log(Level.WARNING, "Budget file does not exist: {0}", budgetFile.getAbsolutePath());
            return budgets;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(budgetFile))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Process CSV line, handle fields that may contain commas
                String[] parts = parseCSVLine(line);
                if (parts.length >= 2) {
                    try {
                        String category = parts[0];
                        double amount = Double.parseDouble(parts[1]);
                        
                        LocalDate startDate = null;
                        LocalDate endDate = null;
                        
                        if (parts.length >= 3 && !parts[2].trim().isEmpty()) {
                            startDate = LocalDate.parse(parts[2], DATE_FORMATTER);
                        }
                        
                        if (parts.length >= 4 && !parts[3].trim().isEmpty()) {
                            endDate = LocalDate.parse(parts[3], DATE_FORMATTER);
                        }
                        
                        Object[] budget = {category, amount, startDate, endDate};
                        budgets.add(budget);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error parsing budget entry: " + line, e);
                    }
                }
            }
            
            LOGGER.log(Level.INFO, "Successfully loaded budgets from: {0}", budgetFile.getAbsolutePath());
            LOGGER.log(Level.INFO, "Loaded {0} budget entries", budgets.size());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading budgets from file: " + e.getMessage(), e);
        }
        
        return budgets;
    }
    
    /**
     * Save budgets to CSV file
     * @param budgets List of budget entries
     * @return true if successful, false otherwise
     */
    public static boolean saveBudgets(List<Object[]> budgets) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(budgetFile))) {
            // Write CSV header
            writer.println(CSV_HEADER);
            
            // Write each budget entry
            for (Object[] budget : budgets) {
                String category = escapeCSV((String) budget[0]);
                double amount = (Double) budget[1];
                
                String startDateStr = "";
                if (budget[2] != null) {
                    startDateStr = ((LocalDate) budget[2]).format(DATE_FORMATTER);
                }
                
                String endDateStr = "";
                if (budget[3] != null) {
                    endDateStr = ((LocalDate) budget[3]).format(DATE_FORMATTER);
                }
                
                writer.println(category + "," + amount + "," + startDateStr + "," + endDateStr);
            }
            
            LOGGER.log(Level.INFO, "Successfully saved {0} budgets to: {1}", 
                    new Object[]{budgets.size(), budgetFile.getAbsolutePath()});
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving budgets to file: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Parse a single CSV line, handling quoted fields that might contain commas
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
        
        // Add the last field
        result.add(current.toString());
        
        return result.toArray(new String[0]);
    }
    
    /**
     * Escape special characters in CSV fields
     */
    private static String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        
        // If the field contains commas, quotes, or newlines, surround with quotes and escape inner quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // Add this method if it doesn't exist
    public static synchronized UserBudgetStorage getInstance(String username) {
        setUsername(username);
        return new UserBudgetStorage(); // Or use a singleton pattern with a map of instances by username
    }
}