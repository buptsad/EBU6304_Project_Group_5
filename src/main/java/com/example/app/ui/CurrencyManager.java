package com.example.app.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 管理货币设置，支持动态通知监听器和本地化金额格式化
 */
public class CurrencyManager {
    private static final Logger logger = LoggerFactory.getLogger(CurrencyManager.class);
    private static volatile CurrencyManager instance;
    private final ReentrantLock lock = new ReentrantLock();
    
    // 封装货币对象
    public static class CurrencyData {
        private final String code;
        private final String symbol;
        
        public CurrencyData(String code, String symbol) {
            this.code = code;
            this.symbol = symbol;
        }
        
        public String getCode() { return code; }
        public String getSymbol() { return symbol; }
    }
    
    private CurrencyData currentCurrency = new CurrencyData("USD", "$");
    private final CopyOnWriteArrayList<CurrencyChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    // 双重检查锁定单例
    public static CurrencyManager getInstance() {
        if (instance == null) {
            synchronized (CurrencyManager.class) {
                if (instance == null) {
                    instance = new CurrencyManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 设置货币并校验有效性
     */
    public void setCurrency(String code, String symbol) {
        lock.lock();
        try {
            if (!isValidCurrencyCode(code)) {
                throw new IllegalArgumentException("Invalid ISO 4217 currency code: " + code);
            }
            
            boolean changed = !currentCurrency.getCode().equals(code) || !currentCurrency.getSymbol().equals(symbol);
            if (changed) {
                currentCurrency = new CurrencyData(code, symbol);
                notifyListeners();
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 校验货币代码是否符合ISO 4217标准
     */
    private boolean isValidCurrencyCode(String code) {
        try {
            Currency.getInstance(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 获取当前货币数据
     */
    public CurrencyData getCurrency() {
        return currentCurrency;
    }
    
    /**
     * 本地化金额格式化
     */
    public String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance(currentCurrency.getCode()));
        return format.format(amount);
    }
    
    /**
     * 监听器管理
     */
    public interface CurrencyChangeListener {
        void onCurrencyChanged(CurrencyData currency);
    }
    
    public void addListener(CurrencyChangeListener listener) {
        listeners.addIfAbsent(listener);
    }
    
    public void removeListener(CurrencyChangeListener listener) {
        listeners.remove(listener);
    }
    
    public void clearListeners() {
        listeners.clear();
    }
    
    /**
     * 通知所有监听器
     */
    private void notifyListeners() {
        for (CurrencyChangeListener listener : listeners) {
            try {
                listener.onCurrencyChanged(currentCurrency);
            } catch (Exception e) {
                logger.error("Failed to notify listener", e);
            }
        }
    }
}