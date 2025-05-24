package com.example.app.ui.reports;

import com.example.app.viewmodel.reports.CategoryBreakdownViewModel;
import org.junit.jupiter.api.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CategoryBreakdownPanelTest {
    private CategoryBreakdownPanel panel;
    
    // Create a test implementation of CategoryBreakdownViewModel
    static class TestViewModel extends CategoryBreakdownViewModel {
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
        panel = new CategoryBreakdownPanel(new TestViewModel());
    }

    @Test
    void testChartPanelExists() {
        assertNotNull(panel);
        boolean foundChartPanel = !findAll(panel, ChartPanel.class).isEmpty();
        assertTrue(foundChartPanel, "ChartPanel not found");
    }
    
    @Test
    void testTimeRangeFunctions() {
        assertDoesNotThrow(() -> {
            panel.setTimeRange("Last 7 days");
            panel.refreshChart();
            
            panel.setTimeRange("This month");
            panel.refreshChart();
            
            panel.setTimeRange("Last 30 days");
            panel.refreshChart();
        });
    }

    // Helper method to find all components of a given type
    private <T extends Component> List<T> findAll(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) found.add(type.cast(c));
            if (c instanceof Container) found.addAll(findAll((Container) c, type));
        }
        return found;
    }
}