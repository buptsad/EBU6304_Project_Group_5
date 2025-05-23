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

public class CategorySpendingChartPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    
    private final CategorySpendingChartViewModel viewModel;
    private ChartPanel chartPanel;
    
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