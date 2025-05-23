package com.example.app.ui.dashboard;

import javax.swing.*;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.ui.dashboard.report.CategorySpendingChartPanel;
import com.example.app.ui.dashboard.report.IncomeExpensesChartPanel;
import com.example.app.viewmodel.dashboard.DashboardReportsViewModel;
import com.example.app.viewmodel.dashboard.DashboardReportsViewModel.ReportDataChangeListener;
import com.example.app.viewmodel.dashboard.report.CategorySpendingChartViewModel;
import com.example.app.viewmodel.dashboard.report.IncomeExpensesChartViewModel;

import java.awt.*;

public class DashboardReportsPanel extends JPanel implements CurrencyChangeListener, ReportDataChangeListener {
    
    // ViewModels
    private final DashboardReportsViewModel viewModel;
    private final IncomeExpensesChartViewModel incomeExpensesViewModel;
    private final CategorySpendingChartViewModel categorySpendingViewModel;
    
    // UI components
    private IncomeExpensesChartPanel incomeExpensesPanel;
    private CategorySpendingChartPanel categorySpendingPanel;
    
    public DashboardReportsPanel(String username) {
        // Initialize ViewModels
        this.viewModel = new DashboardReportsViewModel(username);
        this.viewModel.addChangeListener(this);
        
        this.incomeExpensesViewModel = new IncomeExpensesChartViewModel(viewModel.getFinanceData());
        this.categorySpendingViewModel = new CategorySpendingChartViewModel(viewModel.getFinanceData());
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel titleLabel = new JLabel("Financial Reports");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create panel to hold both charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create chart panels with their ViewModels
        incomeExpensesPanel = new IncomeExpensesChartPanel(incomeExpensesViewModel);
        categorySpendingPanel = new CategorySpendingChartPanel(categorySpendingViewModel);
        
        // Add the chart panels
        chartsPanel.add(incomeExpensesPanel);
        chartsPanel.add(categorySpendingPanel);
        
        // Add to main panel with scroll support
        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add a button for viewing full reports
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewFullReportsButton = new JButton("View Full Reports");
        viewFullReportsButton.addActionListener(e -> openFullReports());
        buttonPanel.add(viewFullReportsButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Register as currency change listener
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    /**
     * Open the full reports panel
     */
    private void openFullReports() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            // This would navigate to the full reports panel
            JOptionPane.showMessageDialog(frame, 
                "View Full Financial Reports", 
                "Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Currency changes will be handled by individual chart panels
    }
    
    @Override
    public void onReportDataChanged() {
        // Data changes will be handled by individual chart panels
        // which are already listening to their own ViewModels
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
        
        // No need to clean up child ViewModels as their panels handle that
    }
}