package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.model.CSVDataImporter;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OverviewPanel extends JPanel implements DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(OverviewPanel.class.getName());
    private FinanceData financeData;
    private JPanel chartPanel;
    private FinancialDetailsPanel detailsPanel;
    
    // Budget warning threshold (80%)
    private static final double BUDGET_WARNING_THRESHOLD = 90.0;
    
    // Static instance for access across the application
    public static FinancialAdvice sharedAdvice = new FinancialAdvice();

    private String username;
    
    public OverviewPanel(String username) {
        this.username = username;
        // Initialize data model
        financeData = new FinanceData();
        
        // Set the data directory for the FinanceData
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);
        
        // Load transaction data before creating UI components
        loadTransactionData();
        
        // Load budget data if needed
        financeData.loadBudgets();
        
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
        
        // Register as listener for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }
    
    /**
     * Load transaction data from user's CSV file
     */
    private void loadTransactionData() {
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);
        
        if (transactions != null && !transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            LOGGER.log(Level.INFO, "OverviewPanel: Successfully loaded {0} transactions", transactions.size());
        } else {
            LOGGER.log(Level.WARNING, "OverviewPanel: No transactions loaded from {0}", csvFilePath);
        }
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
    
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.ALL) {
            
            // Reload transaction data if needed
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS) {
                loadTransactionData();
            }
            
            // Reload budget data if needed
            if (type == DataRefreshManager.RefreshType.BUDGETS) {
                financeData.loadBudgets();
                LOGGER.log(Level.INFO, "OverviewPanel: Reloaded budget data after budget refresh notification");
            }
            
            // Get the split pane
            JSplitPane splitPane = (JSplitPane) getComponent(0);
            
            // Update chart
            JPanel newChartPanel = createChartPanel();
            splitPane.setLeftComponent(newChartPanel);
            
            // Completely replace the details panel with a new instance
            detailsPanel = new FinancialDetailsPanel(financeData, sharedAdvice);
            splitPane.setRightComponent(detailsPanel);
            
            // Revalidate and repaint to ensure UI updates
            splitPane.revalidate();
            splitPane.repaint();
            
            // Check for budget warnings
            SwingUtilities.invokeLater(this::showBudgetWarnings);
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Unregister when component is removed from UI
        DataRefreshManager.getInstance().removeListener(this);
    }
}