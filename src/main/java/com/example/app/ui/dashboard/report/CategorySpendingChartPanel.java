package com.example.app.ui.dashboard.report;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.dashboard.report.CategorySpendingChartViewModel;
import com.example.app.viewmodel.dashboard.report.CategorySpendingChartViewModel.ChartDataChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * A panel that displays a bar chart comparing budget allocations and actual spending by category.
 * This component observes both currency changes and chart data changes to keep the visualization current.
 * <p>
 * The chart displays two series:
 * <ul>
 *   <li>Budget - The allocated budget amount for each spending category</li>
 *   <li>Actual - The actual amount spent in each category</li>
 * </ul>
 
 */
public class CategorySpendingChartPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    /** ViewModel that provides data for the chart */
    private final CategorySpendingChartViewModel viewModel;
    /** Chart panel for displaying the spending chart */
    private ChartPanel chartPanel;

    /**
     * Constructs a new CategorySpendingChartPanel with the specified view model.
     *
     * @param viewModel the view model providing the chart data
     */
    public CategorySpendingChartPanel(CategorySpendingChartViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addChangeListener(this);
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Spending by Category"),
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
     * Creates a JFreeChart bar chart displaying budget and actual spending by category.
     * 
     * @return a configured JFreeChart instance
     */
    private JFreeChart createChart() {
        // Create dataset for the chart
        DefaultCategoryDataset dataset = createDataset();
        
        // Get current currency symbol
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Create the chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Budget vs. Actual Spending",
                "Category",
                "Amount (" + currencySymbol + ")",
                dataset,
                PlotOrientation.VERTICAL,
                true,  // Legend
                true,  // Tooltips
                false  // URLs
        );
        
        // Customize the plot
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        
        // Customize domain axis
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryMargin(0.2);
        
        // Customize range axis
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        // Customize the renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setItemMargin(0.1);
        
        // Budget bars (blue)
        renderer.setSeriesPaint(0, new Color(65, 105, 225));
        
        // Expense bars (orange)
        renderer.setSeriesPaint(1, new Color(255, 140, 0));
        
        return chart;
    }
    
    /**
     * Creates a dataset containing budget and expense data for each category.
     *
     * @return a populated DefaultCategoryDataset
     */
    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get data from the ViewModel
        Map<String, Double> categoryBudgets = viewModel.getCategoryBudgets();
        Map<String, Double> categoryExpenses = viewModel.getCategoryExpenses();
        
        // Add budget and expense data for each category
        for (String category : categoryBudgets.keySet()) {
            double budget = categoryBudgets.get(category);
            double expense = categoryExpenses.getOrDefault(category, 0.0);
            
            dataset.addValue(budget, "Budget", category);
            dataset.addValue(expense, "Actual", category);
        }
        
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