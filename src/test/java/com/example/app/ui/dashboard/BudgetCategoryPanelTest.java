package com.example.app.ui.dashboard;

import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class BudgetCategoryPanelTest {
    private BudgetCategoryPanel panel;

    @BeforeEach
    void setUp() {
        panel = new BudgetCategoryPanel(
                "TestCat", 100.0, 50.0, 50.0,
                e -> {}, e -> {});
    }

    @Test
    void testLabelsAndProgressBarExist() {
        assertNotNull(panel);
        boolean foundBudget = false, foundExpense = false, foundProgress = false;

        for (JLabel label : findAll(panel, JLabel.class)) {
            String text = label.getText();
            if (text != null && text.contains("Budget:")) foundBudget = true;
            if (text != null && text.contains("Spent:")) foundExpense = true;
        }
        foundProgress = !findAll(panel, JProgressBar.class).isEmpty();

        assertTrue(foundBudget, "Budget label not found");
        assertTrue(foundExpense, "Expense label not found");
        assertTrue(foundProgress, "Progress bar not found");
    }

    // Recursively find all components of a given type
    private <T extends Component> java.util.List<T> findAll(Container root, Class<T> type) {
        java.util.List<T> found = new java.util.ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) found.add(type.cast(c));
            if (c instanceof Container) found.addAll(findAll((Container) c, type));
        }
        return found;
    }
}