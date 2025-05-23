package com.example.app.ui.reports;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class IncomeExpensesReportPanel extends JPanel implements CurrencyChangeListener {
    
    private final FinanceData financeData;
    private ChartPanel chartPanel;
    private String timeRange = "Last 30 days";
    
    public IncomeExpensesReportPanel(FinanceData financeData) {
        this.financeData = financeData;
        
        setLayout(new BorderLayout());
        
        // Create the chart
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);
        
        add(chartPanel, BorderLayout.CENTER);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    private JFreeChart createChart() {
        XYDataset dataset = createDataset();
        
        // 获取当前货币符号
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        String title = "Income vs. Expenses (" + timeRange + ")";
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,
                "Date",
                "Amount (" + currencySymbol + ")",
                dataset,
                true,
                true,
                false
        );
        
        // Customize the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        
        // Customize the renderer
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        
        // Income line (green)
        renderer.setSeriesPaint(0, new Color(0, 150, 0));
        renderer.setSeriesStroke(0, new BasicStroke(2.5f));
        
        // Expenses line (red)
        renderer.setSeriesPaint(1, new Color(200, 0, 0));
        renderer.setSeriesStroke(1, new BasicStroke(2.5f));
        
        // Net line (blue)
        renderer.setSeriesPaint(2, new Color(0, 100, 180));
        renderer.setSeriesStroke(2, new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1.0f, new float[] {6.0f, 6.0f}, 0.0f));
        
        // Customize the date axis
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM d"));
        
        return chart;
    }
    
    private XYDataset createDataset() {
        TimeSeries incomeSeries = new TimeSeries("Income");
        TimeSeries expensesSeries = new TimeSeries("Expenses");
        TimeSeries netSeries = new TimeSeries("Net (Income - Expenses)");
        
        // Get data from the model
        List<LocalDate> dates = financeData.getDates();
        Map<LocalDate, Double> incomes = financeData.getDailyIncomes();
        Map<LocalDate, Double> expenses = financeData.getDailyExpenses();
        
        // Filter dates based on time range
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDateFromRange(timeRange);
        
        // Populate the series
        for (LocalDate date : dates) {
            // Only include dates in the selected range
            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {
                Date utilDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Day day = new Day(utilDate);
                
                Double income = incomes.get(date);
                Double expense = expenses.get(date);
                
                if (income != null) {
                    incomeSeries.add(day, income);
                }
                
                if (expense != null) {
                    expensesSeries.add(day, expense);
                }
                
                if (income != null && expense != null) {
                    netSeries.add(day, income - expense);
                }
            }
        }
        
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expensesSeries);
        dataset.addSeries(netSeries);
        
        return dataset;
    }
    
    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }
    
    public void refreshChart() {
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }
    
    private LocalDate getStartDateFromRange(String range) {
        LocalDate today = LocalDate.now();
        
        switch (range) {
            case "Last 7 days":
                return today.minusDays(7);
            case "Last 30 days":
                return today.minusDays(30);
            case "Last 90 days":
                return today.minusDays(90);
            case "This month":
                return today.withDayOfMonth(1);
            case "Last month":
                return today.minusMonths(1).withDayOfMonth(1);
            case "This year":
                return today.withDayOfYear(1);
            default:
                return today.minusDays(30);
        }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 货币变化时刷新图表
        refreshChart();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除组件时取消监听
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}