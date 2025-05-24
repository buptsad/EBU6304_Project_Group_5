package com.example.app.ui.dashboard.report;

import com.example.app.viewmodel.dashboard.report.CategorySpendingChartViewModel;
import org.junit.jupiter.api.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CategorySpendingChartPanelTest {
    private CategorySpendingChartPanel panel;

    // Create a test ViewModel that provides predictable data
    static class TestViewModel extends CategorySpendingChartViewModel {
        public TestViewModel() {
            super(new TestFinanceData());
        }
        
        static class TestFinanceData extends com.example.app.model.FinanceData {
            @Override
            public Map<String, Double> getCategoryBudgets() {
                Map<String, Double> budgets = new HashMap<>();
                budgets.put("Food", 500.0);
                budgets.put("Transport", 300.0);
                budgets.put("Entertainment", 200.0);
                return budgets;
            }
            
            @Override
            public Map<String, Double> getCategoryExpenses() {
                Map<String, Double> expenses = new HashMap<>();
                expenses.put("Food", 450.0);
                expenses.put("Transport", 250.0);
                expenses.put("Entertainment", 180.0);
                return expenses;
            }
        }
    }

    @BeforeEach
    void setUp() {
        panel = new CategorySpendingChartPanel(new TestViewModel());
    }

    @Test
    void testChartPanelExists() {
        assertNotNull(panel);
        boolean foundChartPanel = !findAll(panel, ChartPanel.class).isEmpty();
        assertTrue(foundChartPanel, "ChartPanel not found");
    }

    @Test
    void testRefreshChartDoesNotThrow() {
        assertDoesNotThrow(() -> panel.refreshChart());
    }

    // Helper method to find all components of a given type
    private <T extends Component> java.util.List<T> findAll(Container root, Class<T> type) {
        java.util.List<T> found = new java.util.ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) found.add(type.cast(c));
            if (c instanceof Container) found.addAll(findAll((Container) c, type));
        }
        return found;
    }
}