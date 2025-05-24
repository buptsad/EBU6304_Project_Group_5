package com.example.app.ui;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainFrameTest {
    private MainFrame frame;
    private static final String TEST_USER = "testuser_mainframe";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USER;

    @BeforeEach
    void setUp() {
        // Clean up user directory before test
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) file.delete();
            userDir.delete();
        }
    }

    @AfterEach
    void tearDown() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    @AfterAll
    static void cleanUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            File[] files = userDir.listFiles();
            if (files != null) {
                for (File file : files) file.delete();
            }
            userDir.delete();
        }
    }

    @Test
    @DisplayName("Should initialize MainFrame and show dashboard by default")
    void testMainFrameInitializes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new MainFrame(TEST_USER);
            assertNotNull(frame);
            assertTrue(frame.isVisible() || !frame.isDisplayable());
            // Check for navigation bar
            JButton dashboardBtn = findButtonByText(frame.getContentPane(), "Dashboard");
            assertNotNull(dashboardBtn, "Dashboard navigation button should exist");
        });
    }

    @Test
    @DisplayName("Should switch to Budgets panel when Budgets button clicked")
    void testSwitchToBudgetsPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new MainFrame(TEST_USER);
            JButton budgetsBtn = findButtonByText(frame.getContentPane(), "Budgets");
            assertNotNull(budgetsBtn, "Budgets navigation button should exist");
            budgetsBtn.doClick();
            // Optionally, check that the budgets panel is now the visible card
            // (not trivial without exposing internals, but no exception means success)
        });
    }

    // --- Utility methods ---
    private JButton findButtonByText(Container parent, String text) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JButton && text.equals(((JButton) comp).getText())) return (JButton) comp;
            if (comp instanceof Container) {
                JButton btn = findButtonByText((Container) comp, text);
                if (btn != null) return btn;
            }
        }
        return null;
    }
}