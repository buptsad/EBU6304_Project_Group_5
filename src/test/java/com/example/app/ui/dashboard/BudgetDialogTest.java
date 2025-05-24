package com.example.app.ui.dashboard;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class BudgetDialogTest {
    private BudgetDialog dialog;

    @BeforeEach
    void setUp() {
        SwingUtilities.invokeLater(() -> {
            dialog = new BudgetDialog(null, "Test Dialog", "TestCat", 123.45);
        });
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}
    }

    @AfterEach
    void tearDown() {
        if (dialog != null) SwingUtilities.invokeLater(() -> dialog.dispose());
    }

    @Test
    void testFieldsExistAndInitialValues() {
        assertNotNull(dialog);
        JTextField categoryField = findField(dialog, JTextField.class, "TestCat");
        JTextField budgetField = findField(dialog, JTextField.class, "123.45");
        assertNotNull(categoryField, "Category field not found or value mismatch");
        assertNotNull(budgetField, "Budget field not found or value mismatch");
    }

    @Test
    void testGetCategoryAndBudget() {
        assertEquals("TestCat", dialog.getCategory());
        assertEquals(123.45, dialog.getBudget(), 0.001);
    }

    // Utility: find JTextField with given text
    private JTextField findField(Container root, Class<JTextField> type, String expectedText) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c) && expectedText.equals(((JTextField) c).getText())) return (JTextField) c;
            if (c instanceof Container) {
                JTextField found = findField((Container) c, type, expectedText);
                if (found != null) return found;
            }
        }
        return null;
    }
}