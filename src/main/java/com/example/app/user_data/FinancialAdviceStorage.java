package com.example.app.user_data;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Storage handler for financial advice
 * This class manages the physical storage of AI-generated financial advice in the user_data package
 */
public class FinancialAdviceStorage {
    private static final Logger LOGGER = Logger.getLogger(FinancialAdviceStorage.class.getName());
    private static final String ADVICE_FILENAME = "user_advice.txt";
    private static File adviceFile;
    private static String username; // User name field
    
    // Date format for storing advice generation time
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Set current username and update file path
     * @param username Current user's username
     */
    public static void setUsername(String username) {
        FinancialAdviceStorage.username = username;
        // Update file path to user-specific path
        String packagePath = ".\\user_data\\" + username;
        adviceFile = new File(packagePath, ADVICE_FILENAME);
        
        // Ensure file exists
        initializeStorage();
    }
    
    /**
     * Initialize the storage directory and file
     */
    private static void initializeStorage() {
        File directory = adviceFile.getParentFile();
        
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                LOGGER.log(Level.INFO, "Created advice directory at: {0}", directory.getAbsolutePath());
            } else {
                LOGGER.log(Level.SEVERE, "Failed to create advice directory at: {0}", directory.getAbsolutePath());
                return;
            }
        }
        
        // Create file if it doesn't exist
        if (!adviceFile.exists()) {
            try {
                if (adviceFile.createNewFile()) {
                    LOGGER.log(Level.INFO, "Created advice file at: {0}", adviceFile.getAbsolutePath());
                    
                    // Create default advice content
                    String defaultAdvice = "Welcome to your financial assistant! I'll analyze your transactions " +
                            "and provide personalized advice to help you manage your finances better.";
                    LocalDateTime now = LocalDateTime.now();
                    
                    // Save default advice with current timestamp
                    try (PrintWriter writer = new PrintWriter(new FileWriter(adviceFile))) {
                        writer.println(now.format(DATE_FORMATTER));
                        writer.println(defaultAdvice);
                    }
                    
                    LOGGER.log(Level.INFO, "Initialized advice file with default content");
                } else {
                    LOGGER.log(Level.SEVERE, "Failed to create advice file at: {0}", adviceFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error creating advice file", e);
            }
        } else {
            LOGGER.log(Level.INFO, "Advice file already exists at: {0}", adviceFile.getAbsolutePath());
        }
    }
    
    /**
     * Get the path to the advice file
     * @return Path to the advice file
     */
    public static String getAdviceFilePath() {
        return adviceFile.getAbsolutePath();
    }
    
    /**
     * Load financial advice from file
     * @return Object array containing [advice text, generation time] or null if loading failed
     */
    public static Object[] loadAdvice() {
        // Confirm file exists
        if (!adviceFile.exists()) {
            LOGGER.log(Level.WARNING, "Advice file does not exist: {0}", adviceFile.getAbsolutePath());
            return null;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(adviceFile))) {
            String dateStr = reader.readLine(); // First line is the date
            if (dateStr == null) {
                LOGGER.log(Level.WARNING, "Empty advice file");
                return null;
            }
            
            LocalDateTime generationTime = LocalDateTime.parse(dateStr, DATE_FORMATTER);
            
            // Read the rest of the file as advice content
            StringBuilder adviceBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                adviceBuilder.append(line).append("\n");
            }
            
            if (adviceBuilder.length() > 0) {
                // Remove last newline character
                adviceBuilder.setLength(adviceBuilder.length() - 1);
            }
            
            String adviceText = adviceBuilder.toString();
            
            LOGGER.log(Level.INFO, "Successfully loaded advice from: {0}", adviceFile.getAbsolutePath());
            return new Object[]{adviceText, generationTime};
            
        } catch (IOException | java.time.format.DateTimeParseException e) {
            LOGGER.log(Level.SEVERE, "Error loading advice from file: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Save financial advice to file
     * @param advice Advice text to save
     * @param generationTime Time when advice was generated
     * @return true if successful, false otherwise
     */
    public static boolean saveAdvice(String advice, LocalDateTime generationTime) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(adviceFile))) {
            writer.println(generationTime.format(DATE_FORMATTER));
            writer.println(advice);
            
            LOGGER.log(Level.INFO, "Successfully saved advice to: {0}", adviceFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving advice to file: " + e.getMessage(), e);
            return false;
        }
    }
}