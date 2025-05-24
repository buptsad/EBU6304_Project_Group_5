package com.example.app.model;

import com.example.app.user_data.FinancialAdviceStorage;
import com.example.app.ui.pages.AI.getRes;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A model class representing personalized financial advice for users.
 * This class handles the generation, storage, and retrieval of AI-generated
 * financial advice based on user data.
 */
public class FinancialAdvice implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * The financial advice text content.
     */
    private String advice;
    
    /**
     * The timestamp when the advice was generated.
     */
    private LocalDateTime generationTime;
    
    /**
     * The username associated with this advice.
     */
    private String username;
    
    /**
     * Default advice text used if AI generation fails or before
     * the first advice is generated.
     */
    private static final String DEFAULT_ADVICE = 
            "You've spent 6000 on Spring Festival. It is 60% of your monthly budget. " +
            "Please be careful with your spending.\n" +
            "The Qingming Festival is coming soon, and you may need to spend more. " +
            "Please be careful with your budgets.";
    
    /**
     * Creates a new FinancialAdvice instance with default advice.
     */
    public FinancialAdvice() {
        this.advice = DEFAULT_ADVICE;
        this.generationTime = LocalDateTime.now();
    }
    
    /**
     * Initializes the advice with a specific username to load/save user-specific advice.
     * 
     * @param username The username for advice storage
     */
    public void initialize(String username) {
        this.username = username;
        FinancialAdviceStorage.setUsername(username);
        loadFromStorage();
    }
    
    /**
     * Loads advice from storage file.
     */
    private void loadFromStorage() {
        Object[] loadedData = FinancialAdviceStorage.loadAdvice();
        if (loadedData != null) {
            this.advice = (String) loadedData[0];
            this.generationTime = (LocalDateTime) loadedData[1];
        }
    }
    
    /**
     * Gets the current financial advice text.
     * 
     * @return The financial advice text
     */
    public String getAdvice() {
        return advice;
    }
    
    /**
     * Sets new financial advice text, updates the generation time,
     * saves to storage, and notifies listeners of the change.
     * 
     * @param advice The new financial advice text to set
     */
    public void setAdvice(String advice) {
        this.advice = advice;
        this.generationTime = LocalDateTime.now();
        saveToStorage();
        // Notify listeners that advice has changed
        com.example.app.model.DataRefreshManager.getInstance().notifyRefresh(
            com.example.app.model.DataRefreshManager.RefreshType.ADVICE
        );
    }
    
    /**
     * Saves current advice to storage file.
     */
    private void saveToStorage() {
        if (username != null) {
            FinancialAdviceStorage.saveAdvice(advice, generationTime);
        }
    }
    
    /**
     * Gets the timestamp when the advice was generated.
     * 
     * @return The generation timestamp
     */
    public LocalDateTime getGenerationTime() {
        return generationTime;
    }
    
    /**
     * Gets the formatted generation time as a human-readable string.
     * 
     * @return The formatted date and time string (e.g., "May 24, 2025 14:30")
     */
    public String getFormattedGenerationTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return generationTime.format(formatter);
    }
    
    /**
     * Regenerates financial advice using AI.
     * Makes an API call to the DeepSeek AI service to get personalized
     * financial advice based on the user's data.
     */
    public void regenerate() {
        if (username == null) return;
        try {
            String apiKey = "sk-fdf26a37926f46ab8d4884c2cd533db8";
            getRes aiService = new getRes();
            String prompt = "Please analyze my financial data and provide personalized advice. " +
                    "Focus on my spending patterns, budget adherence, and suggestions for saving money. " +
                    "Keep the advice concise but actionable, within 3-4 sentences.";
            String response = aiService.getResponse(apiKey, prompt);
            String parsedResponse = aiService.parseAIResponse(response);
            setAdvice(parsedResponse); // <-- This will trigger refresh
        } catch (IOException e) {
            System.err.println("Failed to generate advice: " + e.getMessage());
        }
    }
}