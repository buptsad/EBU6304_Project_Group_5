package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import org.jfree.chart.JFreeChart;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class ChartFactoryTest {
    @Test
    void testCreateFinancialLineChartReturnsChart() {
        FinanceData data = new FinanceData();
        JFreeChart chart = ChartFactory.createFinancialLineChart(data);
        assertNotNull(chart);
        assertEquals("Daily Financial Overview", chart.getTitle().getText());
    }

    @Test
    void testCreateCategoryPieChartReturnsNull() {
        FinanceData data = new FinanceData();
        assertNull(ChartFactory.createCategoryPieChart(data));
    }

    @Test
    void testCreateBudgetComparisonChartReturnsNull() {
        FinanceData data = new FinanceData();
        assertNull(ChartFactory.createBudgetComparisonChart(data));
    }
}