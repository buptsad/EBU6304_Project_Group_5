package com.example.app.ui.reports;

import com.example.app.viewmodel.reports.CategoryBreakdownViewModel;
import com.example.app.viewmodel.reports.CategoryBreakdownViewModel.ChartDataChangeListener;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.model.FinanceData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CategoryBreakdownPanel extends JPanel implements CurrencyChangeListener, ChartDataChangeListener {
    private final CategoryBreakdownViewModel viewModel;
    private ChartPanel chartPanel;
    private String timeRange = "Last 30 days";

    public CategoryBreakdownPanel(CategoryBreakdownViewModel viewModel) {
        this.viewModel = viewModel;
        this.viewModel.addChangeListener(this);

        setLayout(new BorderLayout());
        JFreeChart chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);

        add(chartPanel, BorderLayout.CENTER);

        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }

    private JFreeChart createChart() {
        DefaultPieDataset<String> dataset = createDataset();
        String title = "Expense Breakdown by Category (" + timeRange + ")";
        JFreeChart chart = ChartFactory.createPieChart(
                title, dataset, true, true, false
        );
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);

        int index = 0;
        for (String category : viewModel.getCategoryBudgets().keySet()) {
            plot.setSectionPaint(category, getColorForIndex(index));
            index++;
        }

        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        PieSectionLabelGenerator labelGenerator = new StandardPieSectionLabelGenerator(
                "{0}: " + currencySymbol + "{1} ({2})",
                new DecimalFormat("0.00"),
                new DecimalFormat("0.0%")
        );
        plot.setLabelGenerator(labelGenerator);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);

        return chart;
    }

    private DefaultPieDataset<String> createDataset() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        
        // Get the full category expenses map
        Map<String, Double> allCategoryExpenses = viewModel.getCategoryExpenses();
        
        // Get time-filtered transactions and recalculate category totals
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = getStartDateFromRange(timeRange);
        
        // Create filtered dataset based on the time range
        Map<String, Double> filteredExpenses = new HashMap<>();
        
        // Get all transactions from the ViewModel and filter by date
        for (FinanceData.Transaction transaction : viewModel.getTransactions()) {
            LocalDate date = transaction.getDate();
            if (date.isAfter(startDate.minusDays(1)) && date.isBefore(endDate.plusDays(1))) {
                if (transaction.isExpense()) {
                    String category = transaction.getCategory();
                    double amount = Math.abs(transaction.getAmount());
                    filteredExpenses.put(category, 
                        filteredExpenses.getOrDefault(category, 0.0) + amount);
                }
            }
        }
        
        // Add filtered data to the dataset
        for (Map.Entry<String, Double> entry : filteredExpenses.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        return dataset;
    }

    private LocalDate getStartDateFromRange(String range) {
        LocalDate today = LocalDate.now();
        switch (range) {
            case "Last 7 days": return today.minusDays(7);
            case "Last 30 days": return today.minusDays(30);
            case "Last 90 days": return today.minusDays(90);
            case "This month": return today.withDayOfMonth(1);
            case "Last month": return today.minusMonths(1).withDayOfMonth(1);
            case "This year": return today.withDayOfYear(1);
            default: return today.minusDays(30);
        }
    }

    private Color getColorForIndex(int index) {
        Color[] colors = {
            new Color(65, 105, 225),  // Royal Blue
            new Color(255, 99, 71),   // Tomato
            new Color(50, 205, 50),   // Lime Green
            new Color(255, 165, 0),   // Orange
            new Color(106, 90, 205),  // Slate Blue
            new Color(220, 20, 60),   // Crimson
            new Color(0, 139, 139)    // Dark Cyan
        };
        return colors[index % colors.length];
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public void refreshChart() {
        JFreeChart chart = createChart();
        chartPanel.setChart(chart);
        chartPanel.repaint();
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        refreshChart();
    }

    @Override
    public void onChartDataChanged() {
        SwingUtilities.invokeLater(this::refreshChart);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}