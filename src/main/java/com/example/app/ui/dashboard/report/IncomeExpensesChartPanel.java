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

/**
 * A panel that displays a time series chart comparing income and expenses over time.
 * This component visualizes financial trends and patterns by showing daily income
 * and expense data for the last 30 days.
 * <p>
 * The chart displays two series:
 * <ul>
 *   <li>Income - Daily income values plotted with a green line</li>
 *   <li>Expenses - Daily expense values plotted with a red line</li>
 * </ul>
 
 * <p>
 * This panel implements both currency change and chart data change listeners
 * to ensure that the chart display is always current and accurate.
 
 */
public class IncomeExpensesChartPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    
    /**
     * The view model that provides data for the chart
     */
    private final IncomeExpensesChartViewModel viewModel;
    
    /**
     * The panel containing the JFreeChart chart
     */
    private ChartPanel chartPanel;
    
    /**
     * Constructs a new IncomeExpensesChartPanel with the specified view model.
     *
     * @param viewModel the view model providing the chart data
     */
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
    
    /**
     * Creates a JFreeChart time series chart displaying income and expense data over time.
     * 
     * @return a configured JFreeChart instance
     */
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
    
    /**
     * Creates a dataset containing income and expense time series data.
     * Data is retrieved from the view model and formatted for the chart.
     *
     * @return a populated TimeSeriesCollection
     */
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

    /**
     * Called when the application currency changes.
     * Updates the chart to reflect the new currency.
     *
     * @param currencyCode the new currency code
     * @param currencySymbol the new currency symbol
     */
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Refresh chart when currency changes
        refreshChart();
    }
    
    /**
     * Called when the chart data changes in the ViewModel.
     * Updates the chart to reflect the new data.
     */
    @Override
    public void onChartDataChanged() {
        // Called by ViewModel when data changes
        SwingUtilities.invokeLater(this::refreshChart);
    }
    
    /**
     * Refreshes the chart with current data from the view model.
     * Creates a new chart and updates the chart panel.
     */
    public void refreshChart() {
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }
    
    /**
     * Called when this panel is removed from its container.
     * Performs necessary cleanup by removing listeners and cleaning up the view model.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}