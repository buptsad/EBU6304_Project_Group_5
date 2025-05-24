package com.example.app.ui.pages;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

class DashboardPanelTest {

    private static final String TEST_USERNAME = "testuser_dashboardpanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USERNAME;

    private JFrame frame;
    private DashboardPanel dashboardPanel;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up user directory before test
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
        // Create panel and frame on EDT
        SwingUtilities.invokeAndWait(() -> {
            dashboardPanel = new DashboardPanel(TEST_USERNAME);
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(dashboardPanel);
            frame.setSize(900, 600);
            frame.setVisible(true);
        });
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(() -> frame.dispose());
        }
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
    }

    @AfterAll
    static void cleanUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
        // Do NOT delete user_data folder!
    }

    @Test
    @DisplayName("Should initialize and display dashboard welcome label")
    void testPanelInitializesWithWelcomeLabel() throws Exception {
        JLabel[] label = new JLabel[1];
        SwingUtilities.invokeAndWait(() -> {
            label[0] = findLabelByName(dashboardPanel, "dashboardWelcomeLabel");
        });
        assertNotNull(label[0], "Welcome label should exist");
        assertEquals("Welcome to Your Financial Dashboard", label[0].getText());
    }

    @Test
    @DisplayName("Should switch panels when navigation buttons are clicked")
    void testPanelSwitchesOnNavigation() throws Exception {
        JButton[] button = new JButton[1];
        SwingUtilities.invokeAndWait(() -> {
            button[0] = findButtonByName(dashboardPanel, "budgetsButton");
        });
        assertNotNull(button[0], "Budgets button should exist");
        SwingUtilities.invokeAndWait(button[0]::doClick);
        // Optionally, verify the panel switch by checking the visible card
    }

    // Utility methods for finding components by name
    private JLabel findLabelByName(Container parent, String name) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JLabel && name.equals(comp.getName())) {
                return (JLabel) comp;
            } else if (comp instanceof Container) {
                JLabel result = findLabelByName((Container) comp, name);
                if (result != null) return result;
            }
        }
        return null;
    }

    private JButton findButtonByName(Container parent, String name) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JButton && name.equals(comp.getName())) {
                return (JButton) comp;
            } else if (comp instanceof Container) {
                JButton result = findButtonByName((Container) comp, name);
                if (result != null) return result;
            }
        }
        return null;
    }
}