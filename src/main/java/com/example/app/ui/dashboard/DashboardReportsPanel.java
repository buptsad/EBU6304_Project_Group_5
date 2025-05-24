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

/**
 * A panel displaying financial reports using charts and graphs.
 * This component follows the MVVM pattern, using view models to manage data
 * and presentation logic for the financial reports.
 * <p>
 * The panel includes multiple chart visualizations:
 * <ul>
 *   <li>Income vs. Expenses chart showing time series data</li>
 *   <li>Category Spending chart comparing budget vs. actual spending by category</li>
 * </ul>
 
 */
public class DashboardReportsPanel extends JPanel implements CurrencyChangeListener, ReportDataChangeListener {
    
    /** The main view model for the reports dashboard */
    private final DashboardReportsViewModel viewModel;
    
    /** View model specifically for the income vs. expenses chart */
    private final IncomeExpensesChartViewModel incomeExpensesViewModel;
    
    /** View model specifically for the category spending chart */
    private final CategorySpendingChartViewModel categorySpendingViewModel;
    
    /** Panel displaying the income vs. expenses chart */
    private IncomeExpensesChartPanel incomeExpensesPanel;
    
    /** Panel displaying the category spending chart */
    private CategorySpendingChartPanel categorySpendingPanel;
    
    /**
     * Constructs a new reports dashboard panel for the specified user.
     * Initializes view models and UI components for financial reporting.
     *
     * @param username the username of the current user
     */
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
     * Opens the full reports panel in the main application window.
     * Currently shows a dialog message as a placeholder.
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
    
    /**
     * Called when the application currency changes.
     * Currency changes are handled by individual chart panels in this implementation.
     *
     * @param currencyCode the new currency code
     * @param currencySymbol the new currency symbol
     */
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Currency changes will be handled by individual chart panels
    }
    
    /**
     * Called when report data changes in the view model.
     * Data changes are handled by individual chart panels in this implementation.
     */
    @Override
    public void onReportDataChanged() {
        // Data changes will be handled by individual chart panels
        // which are already listening to their own ViewModels
    }
    
    /**
     * Called when this panel is removed from its container.
     * Performs necessary cleanup by removing listeners and cleaning up resources.
     */
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