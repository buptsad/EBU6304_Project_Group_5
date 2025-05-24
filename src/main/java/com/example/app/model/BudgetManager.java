package com.example.app.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages the persistence of budget data through file operations.
 * This class provides functionality to save budget allocations to CSV files
 * and load them back into the application. It uses a standardized file format
 * with "user_budgets.csv" as the default filename.
 */
public class BudgetManager {
    
    /** Default filename used for storing budget data */
    private static final String BUDGETS_FILE_NAME = "user_budgets.csv";
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     * This class only contains static methods and should not be instantiated.
     */
    private BudgetManager() {

    }
    
    /**
     * Saves budget data to a CSV file with the standard format.
     * The file will contain a header row followed by category,budget pairs.
     * 
     * @param categoryBudgets Map containing category names as keys and budget amounts as values
     * @param directory Directory path where the CSV file will be saved
     */
    public static void saveBudgetsToCSV(Map<String, Double> categoryBudgets, String directory) {
        Path filePath = Paths.get(directory, BUDGETS_FILE_NAME);
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write CSV header
            writer.write("Category,Budget");
            writer.newLine();
            
            // Write each category and budget
            for (Map.Entry<String, Double> entry : categoryBudgets.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
            
            System.out.println("Budget data successfully saved to: " + filePath);
            
        } catch (IOException e) {
            System.err.println("Error saving budget data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads budget data from a CSV file into a map.
     * The method expects a file with header row and category,budget pairs.
     * Empty lines and lines starting with "//" will be skipped.
     * 
     * @param directory Directory path where the CSV file is located
     * @return A map with category names as keys and budget amounts as values.
     *         Returns an empty map if the file doesn't exist or cannot be read properly.
     */
    public static Map<String, Double> loadBudgetsFromCSV(String directory) {
        Map<String, Double> categoryBudgets = new LinkedHashMap<>();
        Path filePath = Paths.get(directory, BUDGETS_FILE_NAME);
        
        // Check if file exists
        if (!Files.exists(filePath)) {
            System.out.println("Budget file does not exist, using default budgets");
            return categoryBudgets; // Return empty map, default values will be added later
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Skip CSV header
            String line = reader.readLine();
            
            // Read each line of data
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("//")) {
                    continue; // Skip empty lines and comments
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String category = parts[0].trim();
                    try {
                        double budget = Double.parseDouble(parts[1].trim());
                        categoryBudgets.put(category, budget);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing budget value: " + parts[1]);
                    }
                }
            }
            
            System.out.println("Successfully loaded " + categoryBudgets.size() + " budget categories from " + filePath);
            
        } catch (IOException e) {
            System.err.println("Error loading budget data: " + e.getMessage());
            e.printStackTrace();
        }
        
        return categoryBudgets;
    }

}