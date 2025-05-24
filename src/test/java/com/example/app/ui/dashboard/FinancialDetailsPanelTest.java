package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.viewmodel.dashboard.FinancialDetailsViewModel;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FinancialDetailsPanelTest {
    private static final String TEST_USER = "testuser_financialdetailspanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USER;

    private JFrame frame;
    private FinancialDetailsPanel panel;

    @BeforeEach
    void setUp() {
        cleanUserDir();
        SwingUtilities.invokeLater(() -> {
            FinanceData data = new FinanceData();
            FinancialAdvice advice = new FinancialAdvice();
            FinancialDetailsViewModel vm = new FinancialDetailsViewModel(data, advice);
            panel = new FinancialDetailsPanel(vm);
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
            frame.setSize(600, 500);
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
    void testPanelSectionsExist() {
        assertNotNull(panel);
        assertTrue(panel.getComponentCount() >= 3);
    }

    @Test
    void testUpdateAdviceDisplayRefreshesTipsPanel() throws Exception {
        Component oldTips = panel.getComponent(4);
        SwingUtilities.invokeAndWait(panel::updateAdviceDisplay);
        assertNotSame(oldTips, panel.getComponent(4));
    }
}