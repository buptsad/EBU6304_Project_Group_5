package com.example.app.ui.dashboard.report;

import com.example.app.viewmodel.dashboard.report.IncomeExpensesChartViewModel;
import org.junit.jupiter.api.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class IncomeExpensesChartPanelTest {
    private IncomeExpensesChartPanel panel;

    // Create a test ViewModel that provides predictable data
    static class TestViewModel extends IncomeExpensesChartViewModel {
        public TestViewModel() {
            super(new TestFinanceData());
        }
        
        static class TestFinanceData extends com.example.app.model.FinanceData {
            @Override
            public java.util.List<LocalDate> getDates() {
                return Arrays.asList(
                    LocalDate.now().minusDays(2),
                    LocalDate.now().minusDays(1),
                    LocalDate.now()
                );
            }
            
            @Override
            public Map<LocalDate, Double> getDailyIncomes() {
                Map<LocalDate, Double> incomes = new HashMap<>();
                incomes.put(LocalDate.now().minusDays(2), 100.0);
                incomes.put(LocalDate.now().minusDays(1), 150.0);
                incomes.put(LocalDate.now(), 200.0);
                return incomes;
            }
            
            @Override
            public Map<LocalDate, Double> getDailyExpenses() {
                Map<LocalDate, Double> expenses = new HashMap<>();
                expenses.put(LocalDate.now().minusDays(2), 50.0);
                expenses.put(LocalDate.now().minusDays(1), 75.0);
                expenses.put(LocalDate.now(), 100.0);
                return expenses;
            }
        }
    }

    @BeforeEach
    void setUp() {
        panel = new IncomeExpensesChartPanel(new TestViewModel());
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