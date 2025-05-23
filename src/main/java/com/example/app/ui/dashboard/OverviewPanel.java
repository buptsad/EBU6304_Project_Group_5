package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.viewmodel.dashboard.FinancialDetailsViewModel;
import com.example.app.viewmodel.dashboard.OverviewViewModel;
import com.example.app.viewmodel.dashboard.OverviewViewModel.OverviewChangeListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Logger;

public class OverviewPanel extends JPanel implements OverviewChangeListener {
    private static final Logger LOGGER = Logger.getLogger(OverviewPanel.class.getName());
    
    // View components
    private JPanel chartPanel;
    private FinancialDetailsPanel detailsPanel;
    
    // ViewModels
    private final OverviewViewModel viewModel;
    private FinancialDetailsViewModel detailsViewModel;
    
    // Static instance for access across the application
    public static FinancialAdvice sharedAdvice = new FinancialAdvice();
    
    public OverviewPanel(String username) {
        // Initialize ViewModels
        FinanceData financeData = new FinanceData();
        viewModel = new OverviewViewModel(username, financeData, sharedAdvice);
        viewModel.addChangeListener(this);

        // Explicitly check for warnings after registering as listener
        viewModel.checkBudgetWarnings();
        
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
        detailsViewModel = new FinancialDetailsViewModel(financeData, sharedAdvice);
        detailsPanel = new FinancialDetailsPanel(detailsViewModel);
        splitPane.setRightComponent(detailsPanel);
        
        // Add the main content to the panel
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Create chart using data from the ViewModel
        JFreeChart chart = ChartFactory.createFinancialLineChart(viewModel.getFinanceData());
        
        // Create chart panel with chart
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        
        panel.add(chartPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Implementation of OverviewChangeListener interface
    
    @Override
    public void onFinancialDataChanged() {
        // Get the split pane
        JSplitPane splitPane = (JSplitPane) getComponent(0);
        
        // Update chart
        JPanel newChartPanel = createChartPanel();
        splitPane.setLeftComponent(newChartPanel);
        
        // Completely replace the details panel with a new instance
        detailsViewModel = new FinancialDetailsViewModel(
            viewModel.getFinanceData(), viewModel.getFinancialAdvice());
        detailsPanel = new FinancialDetailsPanel(detailsViewModel);
        splitPane.setRightComponent(detailsPanel);
        
        // Revalidate and repaint to ensure UI updates
        splitPane.revalidate();
        splitPane.repaint();
    }
    
    @Override
    public void onBudgetWarningsDetected(String warningMessage) {
        // Show warning dialog with the message from ViewModel
        JOptionPane optionPane = new JOptionPane(
            warningMessage,
            JOptionPane.WARNING_MESSAGE
        );
        JDialog dialog = optionPane.createDialog(this, "Budget Alert");
        dialog.setVisible(true);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up resources
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}