package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class OverviewPanel extends JPanel {
    private FinanceData financeData;
    private JPanel chartPanel;
    private FinancialDetailsPanel detailsPanel;
    
    // Budget warning threshold (80%)
    private static final double BUDGET_WARNING_THRESHOLD = 90.0;
    
    // Static instance for access across the application
    public static FinancialAdvice sharedAdvice = new FinancialAdvice();
    
    public OverviewPanel() {
        // Initialize data model
        financeData = new FinanceData();
        
        // Set up panel
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // Create a split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.6); // Left panel gets 60% of space
        splitPane.setDividerSize(5);
        splitPane.setBorder(null);
        
        // Create the left panel for the chart
        chartPanel = createChartPanel();
        splitPane.setLeftComponent(chartPanel);
        
        // Create the right panel for additional details and advice
        detailsPanel = new FinancialDetailsPanel(financeData, sharedAdvice);
        splitPane.setRightComponent(detailsPanel);
        
        // Add the main content to the panel
        add(splitPane, BorderLayout.CENTER);
        
        // Check budget warnings after panel is initialized
        SwingUtilities.invokeLater(this::showBudgetWarnings);
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create chart
        JFreeChart chart = ChartFactory.createFinancialLineChart(financeData);
        
        // Create chart panel with chart
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Method to show budget warnings
    private void showBudgetWarnings() {
        StringBuilder warningMessage = new StringBuilder("<html><body>");
        boolean hasWarnings = false;
        
        // Check overall budget
        double overallPercentage = financeData.getOverallBudgetPercentage();
        if (overallPercentage >= BUDGET_WARNING_THRESHOLD) {
            warningMessage.append("<p style='color:#e74c3c'><b>Overall budget:</b> ")
                          .append(String.format("%.1f%%", overallPercentage))
                          .append(" used</p>");
            hasWarnings = true;
        }
        
        // Check category budgets
        Map<String, Double> categoryBudgets = financeData.getCategoryBudgets();
        for (String category : categoryBudgets.keySet()) {
            double percentage = financeData.getCategoryPercentage(category);
            if (percentage >= BUDGET_WARNING_THRESHOLD) {
                warningMessage.append("<p style='color:#e74c3c'><b>")
                              .append(category)
                              .append(":</b> ")
                              .append(String.format("%.1f%%", percentage))
                              .append(" used</p>");
                hasWarnings = true;
            }
        }
        
        warningMessage.append("</body></html>");
        
        // Show warning dialog if any budget is running out
        if (hasWarnings) {
            JOptionPane optionPane = new JOptionPane(
                warningMessage.toString(),
                JOptionPane.WARNING_MESSAGE
            );
            JDialog dialog = optionPane.createDialog(this, "Budget Alert");
            // Using the default warning icon from JOptionPane
            dialog.setVisible(true);
        }
    }
    
    // Method to update the advice display
    public void updateAdviceDisplay() {
        if (detailsPanel != null) {
            detailsPanel.updateAdviceDisplay();
        }
    }
}