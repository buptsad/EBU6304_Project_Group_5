package com.example.app.ui.reports;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
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
import java.util.Map;

public class CategoryBreakdownPanel extends JPanel implements CurrencyChangeListener {
    
    private final FinanceData financeData;
    private ChartPanel chartPanel;
    private String timeRange = "Last 30 days";
    
    public CategoryBreakdownPanel(FinanceData financeData) {
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
        DefaultPieDataset dataset = createDataset();

        String title = "Expense Breakdown by Category (" + timeRange + ")";
        JFreeChart chart = ChartFactory.createPieChart(
                title,
                dataset,
                true,
                true,
                false
        );
        
        // Customize the plot
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        
        // Use colors for categories
        int index = 0;
        for (String category : financeData.getCategoryBudgets().keySet()) {
            plot.setSectionPaint(category, getColorForIndex(index));
            index++;
        }
        
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Customize labels
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
    
    private DefaultPieDataset createDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        // Get category data
        Map<String, Double> categoryExpenses = financeData.getCategoryExpenses();
        
        // Add each category to the dataset
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        
        return dataset;
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