package com.example.app.ui;

import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ThemeManagerTest {

    private static final String CONFIG_FILE = "theme.properties";

    @BeforeEach
    void cleanUpBefore() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) file.delete();
        // Reset singleton for clean state
        java.lang.reflect.Field instanceField;
        try {
            instanceField = ThemeManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception ignored) {}
    }

    @AfterAll
    static void cleanUpAfter() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) file.delete();
    }

    @Test
    @DisplayName("Should default to dark theme if no config file")
    void testDefaultTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        assertTrue(tm.isDarkTheme(), "Default theme should be dark");
    }

    @Test
    @DisplayName("Should set and persist light theme")
    void testSetAndPersistTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.setTheme(false); // Set to light
        assertFalse(tm.isDarkTheme());

        // Reset singleton to force reload from file
        try {
            java.lang.reflect.Field instanceField = ThemeManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception ignored) {}

        ThemeManager tm2 = ThemeManager.getInstance();
        assertFalse(tm2.isDarkTheme(), "Theme should persist as light");
    }

    @Test
    @DisplayName("Should set and persist dark theme")
    void testSetAndPersistDarkTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        tm.setTheme(true); // Set to dark
        assertTrue(tm.isDarkTheme());

        // Reset singleton to force reload from file
        try {
            java.lang.reflect.Field instanceField = ThemeManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception ignored) {}

        ThemeManager tm2 = ThemeManager.getInstance();
        assertTrue(tm2.isDarkTheme(), "Theme should persist as dark");
    }
}