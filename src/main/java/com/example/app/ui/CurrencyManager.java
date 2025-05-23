package com.example.app.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 管理应用程序中的货币设置并通知监听器货币变化
 */
public class CurrencyManager {
    // 单例实例
    private static CurrencyManager instance;
    
    // 当前货币符号 (默认为美元)
    private String currencySymbol = "$";
    
    // 当前货币编码 (默认为USD)
    private String currencyCode = "USD";
    
    // 监听器列表 - 使用线程安全的CopyOnWriteArrayList
    private List<CurrencyChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    // 私有构造函数
    private CurrencyManager() {}
    
    /**
     * 获取单例实例
     */
    public static synchronized CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }
    
    /**
     * 获取当前货币符号
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    /**
     * 获取当前货币代码
     */
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    /**
     * 设置货币信息并通知所有监听器
     */
    public void setCurrency(String code, String symbol) {
        boolean changed = !this.currencyCode.equals(code) || !this.currencySymbol.equals(symbol);
        
        this.currencyCode = code;
        this.currencySymbol = symbol;
        
        if (changed) {
            notifyListeners();
        }
    }
    
    /**
     * 添加货币变化监听器
     */
    public void addCurrencyChangeListener(CurrencyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除货币变化监听器
     */
    public void removeCurrencyChangeListener(CurrencyChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 通知所有监听器货币已变化
     */
    private void notifyListeners() {
        // 使用CopyOnWriteArrayList可以安全地遍历，即使在遍历期间有修改
        for (CurrencyChangeListener listener : listeners) {
            try {
                listener.onCurrencyChanged(currencyCode, currencySymbol);
            } catch (Exception e) {
                // 捕获通知过程中的异常，防止一个监听器的异常影响其他监听器
                System.err.println("Error notifying listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 格式化金额显示，根据当前货币符号
     */
    public String formatCurrency(double amount) {
        return String.format("%s%.2f", currencySymbol, amount);
    }
    
    /**
     * 货币变化监听器接口
     */
    public interface CurrencyChangeListener {
        void onCurrencyChanged(String currencyCode, String currencySymbol);
    }
}