package com.example.app.ui.pages;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ReportsPanelTest {
    private static final String TEST_USERNAME = "testuser_reportspanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USERNAME;

    @BeforeEach
    void setUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) file.delete();
            userDir.delete();
        }
    }

    @AfterAll
    static void cleanUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) file.delete();
            userDir.delete();
        }
        // Do NOT delete user_data folder!
    }

    @Test
    @DisplayName("Should initialize ReportsPanel without errors")
    void testPanelInitialization() {
        assertDoesNotThrow(() -> {
            ReportsPanel panel = new ReportsPanel(TEST_USERNAME);
            assertNotNull(panel);
        });
    }

    @Test
    @DisplayName("Should contain chart container and control panel")
    void testPanelComponents() {
        ReportsPanel panel = new ReportsPanel(TEST_USERNAME);
        assertNotNull(panel.getComponent(0)); // header
        assertNotNull(panel.getComponent(1)); // control panel
        assertNotNull(panel.getComponent(2)); // chart scroll pane
    }
}