package com.example.app.ui.pages;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class TransactionsPanelTest {
    private static final String TEST_USERNAME = "testuser_transactionspanel";
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
            File[] files = userDir.listFiles();
            if (files != null) {
                for (File file : files) file.delete();
            }
            userDir.delete();
        }
    }

    @Test
    @DisplayName("Should initialize TransactionsPanel without errors")
    void testPanelInitialization() {
        assertDoesNotThrow(() -> {
            TransactionsPanel panel = new TransactionsPanel(TEST_USERNAME);
            assertNotNull(panel);
        });
    }

    @Test
    @DisplayName("Should add a new transaction row")
    void testAddTransactionRow() {
        TransactionsPanel panel = new TransactionsPanel(TEST_USERNAME);
        JTable table = (JTable) TestUtils.getField(panel, "transactionsTable");
        int before = table.getRowCount();
        SwingUtilities.invokeLater(() -> {
            JButton addButton = (JButton) TestUtils.getField(panel, "addButton");
            addButton.doClick();
            assertTrue(table.getRowCount() > before);
        });
    }
}

// Utility for reflection access to private fields
class TestUtils {
    static Object getField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}