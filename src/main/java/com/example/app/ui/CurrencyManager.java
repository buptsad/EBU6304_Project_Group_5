package com.example.app.ui;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the application's currency settings and notifies listeners when the currency changes.
 * <p>
 * This class is implemented as a singleton and provides methods to get/set the current currency,
 * register/unregister listeners, and format currency amounts.
 
 */
public class CurrencyManager {
    /** Singleton instance */
    private static CurrencyManager instance;

    /** Current currency symbol (default is USD "$") */
    private String currencySymbol = "$";

    /** Current currency code (default is "USD") */
    private String currencyCode = "USD";

    /** List of listeners for currency changes (thread-safe) */
    private List<CurrencyChangeListener> listeners = new CopyOnWriteArrayList<>();

    /** Private constructor for singleton */
    private CurrencyManager() {}

    /**
     * Gets the singleton instance of CurrencyManager.
     *
     * @return the CurrencyManager instance
     */
    public static synchronized CurrencyManager getInstance() {
        if (instance == null) {
            instance = new CurrencyManager();
        }
        return instance;
    }

    /**
     * Gets the current currency symbol.
     *
     * @return the currency symbol
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Gets the current currency code.
     *
     * @return the currency code
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Sets the currency code and symbol, and notifies all listeners if changed.
     *
     * @param code   the new currency code
     * @param symbol the new currency symbol
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
     * Adds a currency change listener.
     *
     * @param listener the listener to add
     */
    public void addCurrencyChangeListener(CurrencyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a currency change listener.
     *
     * @param listener the listener to remove
     */
    public void removeCurrencyChangeListener(CurrencyChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners that the currency has changed.
     */
    private void notifyListeners() {
        // CopyOnWriteArrayList allows safe iteration even if listeners are modified during iteration
        for (CurrencyChangeListener listener : listeners) {
            try {
                listener.onCurrencyChanged(currencyCode, currencySymbol);
            } catch (Exception e) {
                // Catch exceptions to prevent one listener from affecting others
                System.err.println("Error notifying listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Formats a monetary amount using the current currency symbol.
     *
     * @param amount the amount to format
     * @return the formatted currency string
     */
    public String formatCurrency(double amount) {
        return String.format("%s%.2f", currencySymbol, amount);
    }

    /**
     * Listener interface for currency changes.
     */
    public interface CurrencyChangeListener {
        /**
         * Called when the currency changes.
         *
         * @param currencyCode   the new currency code
         * @param currencySymbol the new currency symbol
         */
        void onCurrencyChanged(String currencyCode, String currencySymbol);
    }
}