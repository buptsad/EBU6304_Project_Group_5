package com.example.app.model;

import com.example.app.user_data.FinancialAdviceStorage;
import com.example.app.ui.pages.AI.getRes;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FinancialAdvice implements Serializable {
    private static final long serialVersionUID = 1L;
    private String advice;
    private LocalDateTime generationTime;
    private String username;

    private transient FinanceData financeData;
    public void setFinanceData(FinanceData fd) { this.financeData = fd; }
    // Default advice text if generation fails
    private static final String DEFAULT_ADVICE = 
            "";
    
    public FinancialAdvice() {
        this.advice = DEFAULT_ADVICE;
        this.generationTime = LocalDateTime.now();
    }
    
    /**
     * Initialize with a specific username to load/save user-specific advice
     * @param username The username for advice storage
     */
    public void initialize(String username) {
        this.username = username;
        FinancialAdviceStorage.setUsername(username);
        regenerate();
        loadFromStorage();
    }
    
    /**
     * Load advice from storage file
     */
    private void loadFromStorage() {
        Object[] loadedData = FinancialAdviceStorage.loadAdvice();
        if (loadedData != null) {
            this.advice = (String) loadedData[0];
            this.generationTime = (LocalDateTime) loadedData[1];
        }
    }
    
    public String getAdvice() {
        return advice;
    }
    
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
     * Save current advice to storage file
     */
    private void saveToStorage() {
        if (username != null) {
            FinancialAdviceStorage.saveAdvice(advice, generationTime);
        }
    }
    
    public LocalDateTime getGenerationTime() {
        return generationTime;
    }
    
    public String getFormattedGenerationTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return generationTime.format(formatter);
    }
    
    /**
     * Regenerate financial advice using AI
     */
    public void regenerate() {
        if (financeData == null) {
        System.err.println("[Advice] financeData not injected – regenerate() aborted.");
        return;
    }
        Map<String, Double> cat = financeData.getCategoryExpenses();   // 你已有此方法
            List<LocalDate> dates = financeData.getDates();
            if (dates.isEmpty()) return;              // 没数据就退出
            LocalDate start = Collections.min(dates);
            LocalDate end   = Collections.max(dates);
        // ② 拼成 prompt 里要的数据块
        StringBuilder dataBlock = new StringBuilder();
        cat.forEach((k,v) -> dataBlock.append(String.format("%s: %.2f\n", k, v)));
        System.out.println(dataBlock.toString().trim());
        if (username == null) return;
        try {
            String apiKey = "sk-fdf26a37926f46ab8d4884c2cd533db8";
            getRes aiService = new getRes();
            String prompt = String.join("\n",
            "You are a bilingual personal-finance coach for a Chinese user.",
            "Data range: " + start + " to " + end,                      // ← 日期
            "Expenses by category:", 
            dataBlock.toString().trim(),                               // ← 类别 + 金额
            "",
            "1) Carefully check for seasonal spending spikes common in China, and point them out.",
            "   e.g. New Year's Day: January 1 (1 day off);" + //
                                "Spring Festival: the first day of the first lunar month to the third day of the first lunar month (3 days off);" + //
                                "Ching Ming Festival: April 4 (1 day off);" + //
                                "Labor Day: May 1 (1 day off);" + //
                                "Dragon Boat Festival: On the day of the Dragon Boat Festival of the lunar calendar (1 day off);" + //
                                "Mid-Autumn Festival: the day of the Mid-Autumn Festival in the lunar calendar (1 day off);" + //
                                "National Day: October 1 to 3 (3 days off).",
            "  11.11, 6.18, back-to-school season,Father/Mother's Day,else.",
            "2) If any category looks abnormally high for the season,",
            "   For example, if you see a spike in spending on food and drink ,point it out and give 1–2 actionable tips.",
            "3) Give 3-4 concise sentences in total,except the advice, do not output anything else,and try to express it in words, without any markdown symbols like **."
            );
            String response = aiService.getResponse(apiKey, prompt);
            String parsedResponse = aiService.parseAIResponse(response);
            setAdvice(parsedResponse); // <-- This will trigger refresh
        } catch (IOException e) {
            System.err.println("Failed to generate advice: " + e.getMessage());
        }
    }


}