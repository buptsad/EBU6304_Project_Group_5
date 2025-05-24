package com.example.app.ui.reports;

import com.example.app.viewmodel.reports.IncomeExpensesReportViewModel;
import org.junit.jupiter.api.*;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncomeExpensesReportPanelTest {
    private IncomeExpensesReportPanel panel;
    
    // Create a test implementation of IncomeExpensesReportViewModel
    static class TestViewModel extends IncomeExpensesReportViewModel {
        public TestViewModel() {
            super(new TestFinanceData());
        }
        
        static class TestFinanceData extends com.example.app.model.FinanceData {
            @Override
            public List<LocalDate> getDates() {
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
        panel = new IncomeExpensesReportPanel(new TestViewModel());
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