package com.example.app.ui;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyManagerTest {

    @BeforeEach
    void resetCurrency() {
        // Reset to default before each test
        CurrencyManager.getInstance().setCurrency("USD", "$");
    }

    @Test
    @DisplayName("Should get and set currency code and symbol")
    void testSetAndGetCurrency() {
        CurrencyManager cm = CurrencyManager.getInstance();
        cm.setCurrency("EUR", "€");
        assertEquals("EUR", cm.getCurrencyCode());
        assertEquals("€", cm.getCurrencySymbol());
    }

    @Test
    @DisplayName("Should notify listeners on currency change")
    void testListenerNotification() {
        CurrencyManager cm = CurrencyManager.getInstance();
        final boolean[] notified = {false};
        CurrencyManager.CurrencyChangeListener listener = (code, symbol) -> {
            notified[0] = true;
            assertEquals("JPY", code);
            assertEquals("¥", symbol);
        };
        cm.addCurrencyChangeListener(listener);
        cm.setCurrency("JPY", "¥");
        assertTrue(notified[0], "Listener should be notified on currency change");
        cm.removeCurrencyChangeListener(listener);
    }

    @Test
    @DisplayName("Should format currency correctly")
    void testFormatCurrency() {
        CurrencyManager cm = CurrencyManager.getInstance();
        cm.setCurrency("GBP", "£");
        assertEquals("£123.46", cm.formatCurrency(123.456));
    }
}