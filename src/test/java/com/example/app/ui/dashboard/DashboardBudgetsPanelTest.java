package com.example.app.ui.dashboard;

import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class DashboardBudgetsPanelTest {
    private static final String TEST_USER = "testuser_dashboardbudgetspanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USER;

    private JFrame frame;
    private DashboardBudgetsPanel panel;

    @BeforeEach
    void setUp() {
        cleanUserDir();
        SwingUtilities.invokeLater(() -> {
            panel = new DashboardBudgetsPanel(TEST_USER);
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
            frame.setSize(700, 500);
            frame.setVisible(true);
        });
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }

    @AfterEach
    void tearDown() {
        if (frame != null) SwingUtilities.invokeLater(() -> frame.dispose());
        cleanUserDir();
    }

    static void cleanUserDir() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) file.delete();
            userDir.delete();
        }
    }

    @Test
    void testPanelContainsBudgetTable() {
        JTable table = (JTable) findComponent(panel, JTable.class);
        assertNotNull(table);
        assertTrue(table.getColumnCount() >= 4);
    }

    static Component findComponent(Container parent, Class<?> type) {
        for (Component c : parent.getComponents()) {
            if (type.isInstance(c)) return c;
            if (c instanceof Container) {
                Component found = findComponent((Container) c, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}