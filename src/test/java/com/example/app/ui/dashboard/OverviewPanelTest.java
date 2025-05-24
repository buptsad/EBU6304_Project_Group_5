package com.example.app.ui.dashboard;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class OverviewPanelTest {
    private static final String TEST_USER = "testuser_overviewpanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USER;

    private JFrame frame;
    private OverviewPanel panel;

    @BeforeEach
    void setUp() {
        cleanUserDir();
        SwingUtilities.invokeLater(() -> {
            panel = new OverviewPanel(TEST_USER);
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
            frame.setSize(800, 600);
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
    void testPanelComponentsExist() {
        assertNotNull(panel);
        assertTrue(panel.getComponentCount() > 0);
        // Should contain a JSplitPane as main component
        Component c = panel.getComponent(0);
        assertTrue(c instanceof JSplitPane);
        JSplitPane split = (JSplitPane) c;
        assertNotNull(split.getLeftComponent());
        assertNotNull(split.getRightComponent());
    }

    @Test
    void testOnFinancialDataChangedUpdatesComponents() throws Exception {
        JSplitPane split = (JSplitPane) panel.getComponent(0);
        Component oldLeft = split.getLeftComponent();
        Component oldRight = split.getRightComponent();
        SwingUtilities.invokeAndWait(panel::onFinancialDataChanged);
        assertNotSame(oldLeft, split.getLeftComponent());
        assertNotSame(oldRight, split.getRightComponent());
    }
}