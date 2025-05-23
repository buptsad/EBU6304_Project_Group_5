package com.example.app.ui.dashboard.report;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

// Add import
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

// Update the class declaration
public class IncomeExpensesChartPanel extends JPanel implements CurrencyChangeListener, DataRefreshListener {
    
    private final FinanceData financeData = new FinanceData();
    private ChartPanel chartPanel;
    
    public IncomeExpensesChartPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Income vs. Expenses"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        
        // Create the chart
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 300));
        chartPanel.setMouseWheelEnabled(true);
        
        add(chartPanel, BorderLayout.CENTER);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        
        // Register as listener for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }
    
    private JFreeChart createChart() {
        // Create dataset for the chart
        TimeSeriesCollection dataset = createDataset();
        
        // 获取当前货币符号
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Create the chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Income vs. Expenses (Last 30 Days)",
                "Date",
                "Amount (" + currencySymbol + ")",
                dataset,
                true,  // Legend
                true,  // Tooltips
                false  // URLs
        );
        
        // Customize the plot
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(new Color(255, 255, 255));
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        
        // Customize the renderer
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultShapesFilled(true);
        
        // Income line (green)
        renderer.setSeriesPaint(0, new Color(0, 150, 0));
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        
        // Expenses line (red)
        renderer.setSeriesPaint(1, new Color(200, 0, 0));
        renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        
        // Customize the date axis
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MMM d"));
        
        return chart;
    }
    
    private TimeSeriesCollection createDataset() {
        // Create time series for income and expenses
        TimeSeries incomeSeries = new TimeSeries("Income");
        TimeSeries expensesSeries = new TimeSeries("Expenses");
        
        // Get data from the model
        List<LocalDate> dates = financeData.getDates();
        Map<LocalDate, Double> incomes = financeData.getDailyIncomes();
        Map<LocalDate, Double> expenses = financeData.getDailyExpenses();
        
        // Populate the series
        for (LocalDate date : dates) {
            // Convert LocalDate to Date
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
        }
        
        // Create and return the dataset
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(incomeSeries);
        dataset.addSeries(expensesSeries);
        
        return dataset;
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 货币变化时刷新图表
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }
    
    // Add this method
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.ALL) {
            // Recreate chart and update panel
            JFreeChart chart = createChart();
            chartPanel.setChart(chart);
            chartPanel.repaint();
        }
    }
    
    // Update removeNotify method
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Unregister from all listeners
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        DataRefreshManager.getInstance().removeListener(this);
    }
}