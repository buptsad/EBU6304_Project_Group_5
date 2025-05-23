package com.example.app.ui.dashboard.report;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.dashboard.report.IncomeExpensesChartViewModel;
import com.example.app.viewmodel.dashboard.report.IncomeExpensesChartViewModel.ChartDataChangeListener;

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

public class IncomeExpensesChartPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    
    private final IncomeExpensesChartViewModel viewModel;
    private ChartPanel chartPanel;
    
    public IncomeExpensesChartPanel(IncomeExpensesChartViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addChangeListener(this);
        
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
        
        // Register as currency change listener
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    private JFreeChart createChart() {
        // Create dataset for the chart
        TimeSeriesCollection dataset = createDataset();
        
        // Get current currency symbol
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
        
        // Get data from the ViewModel
        List<LocalDate> dates = viewModel.getDates();
        Map<LocalDate, Double> incomes = viewModel.getDailyIncomes();
        Map<LocalDate, Double> expenses = viewModel.getDailyExpenses();
        
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
        // Refresh chart when currency changes
        refreshChart();
    }
    
    @Override
    public void onChartDataChanged() {
        // Called by ViewModel when data changes
        SwingUtilities.invokeLater(this::refreshChart);
    }
    
    /**
     * Refresh the chart with current data
     */
    public void refreshChart() {
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}