package com.example.app.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FinancialAdvice implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String advice;
    private LocalDateTime generationTime;
    
    public FinancialAdvice() {
        this.advice = "You've spend 6000 on Spring Festival. It is 60% of your monthly budget. Please be careful with your spending. \nThe Qingming Festival is coming soon, and you may need to spend more. Please be careful with your budgets.";
        this.generationTime = LocalDateTime.now();
    }
    
    public String getAdvice() {
        return advice;
    }
    
    public void setAdvice(String advice) {
        this.advice = advice;
    }
    
    public LocalDateTime getGenerationTime() {
        return generationTime;
    }
    
    public String getFormattedGenerationTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return generationTime.format(formatter);
    }
    
    public void regenerate() {
        // In a real implementation, this would generate new advice
        // For the demo, we'll just update the timestamp
        this.generationTime = LocalDateTime.now();
    }
}