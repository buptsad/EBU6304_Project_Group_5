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
                // ——— 数据 ———
                "You are a bilingual personal-finance coach for a mainland-China user.",
                "Today's date: " + java.time.LocalDate.now(),
                // ——— 输出要求 ———
                "Please answer in exactly TWO plain English sentences (no bullet points, no markdown).",
                "Sentence 1: Point out today’s date.",
                "Sentence 2: Check the next 30 days. If any event in the calendar below falls within that period, name it and remind the user to watch their budget; if none, say that no major spending trigger is expected.",
                "",
                // ——— 节庆 / 大促日历 ———
                "Calendar of Chinese high-spending events:",
                "  • New Year’s Day – Jan 1",
                "  • Spring Festival – Lunar Jan 1-3 (late Jan/Feb)",
                "  • Lantern Festival – Lunar Jan 15 (Feb)",
                "  • Qingming Festival – Apr 4 ±1",
                "  • Labor Day – May 1",
                "  • Dragon-boat Festival – May 31",
                "  • 6·18 Shopping Festival – Jun 18",
                "  • Father’s Day – 3rd Sunday of Jun",
                "  • Mid-Autumn Festival – Lunar Aug 15 (Sep)",
                "  • National Day Golden Week – Oct 1-7",
                "  • Double-Eleven – Nov 11",
                "  • Back-to-school season – late Aug / early Sep"
            );


            String response = aiService.getResponse(apiKey, prompt);
            String parsedResponse = aiService.parseAIResponse(response);
            setAdvice(parsedResponse); // <-- This will trigger refresh
        } catch (IOException e) {
            System.err.println("Failed to generate advice: " + e.getMessage());
        }
    }


}