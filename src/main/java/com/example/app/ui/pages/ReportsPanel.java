package com.example.app.ui.pages;

import com.example.app.viewmodel.pages.ReportsViewModel;
import com.example.app.viewmodel.pages.ReportsViewModel.ReportsChangeListener;
import com.example.app.model.FinanceData;
import com.example.app.ui.reports.*;
import com.example.app.viewmodel.reports.IncomeExpensesReportViewModel;
import com.example.app.viewmodel.reports.CategoryBreakdownViewModel;
import com.example.app.viewmodel.reports.TrendReportViewModel;
import javax.swing.*;
import java.awt.*;

/**
 * The ReportsPanel displays various financial reports and charts for the user.
 * It allows switching between different report types, filtering by time period and interval,
 * and refreshing data. This panel follows the MVVM pattern and listens to changes from its ViewModel.
 * <p>
 * Features:
 * <ul>
 *   <li>Switch between Income vs Expenses, Expense Breakdown, and Trends Analysis charts</li>
 *   <li>Filter reports by time period and data interval</li>
 *   <li>Apply filters and reload transaction data</li>
 *   <li>Responsive to ViewModel data changes</li>
 * </ul>
 
 */
public class ReportsPanel extends JPanel implements ReportsChangeListener {
    /** The ViewModel providing report data and logic */
    private final ReportsViewModel viewModel;
    /** Container for chart panels using CardLayout */
    private JPanel chartContainer;
    /** CardLayout for switching between chart panels */
    private CardLayout cardLayout;

    // Chart panels
    /** Panel for displaying income vs expenses reports */
    private IncomeExpensesReportPanel incomeExpensesPanel;
    /** Panel for displaying category breakdown reports */
    private CategoryBreakdownPanel categoryBreakdownPanel;
    /** Panel for displaying trend analysis reports */
    private TrendReportPanel trendReportPanel;

    // Constants for card layout
    private static final String INCOME_EXPENSE_PANEL = "INCOME_EXPENSE";
    private static final String CATEGORY_BREAKDOWN_PANEL = "CATEGORY_BREAKDOWN";
    private static final String TREND_PANEL = "TREND";

    /**
     * Constructs a new ReportsPanel for the specified user.
     *
     * @param username the username of the current user
     */
    public ReportsPanel(String username) {
        this.viewModel = new ReportsViewModel(username);
        this.viewModel.addChangeListener(this);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top panel with title
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Control panel for filters
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.WEST);

        // Chart container with card layout
        cardLayout = new CardLayout();
        chartContainer = new JPanel(cardLayout);

        // Create sub-panel ViewModels
        IncomeExpensesReportViewModel incomeVM = new IncomeExpensesReportViewModel(viewModel.getFinanceData());
        CategoryBreakdownViewModel categoryVM = new CategoryBreakdownViewModel(viewModel.getFinanceData());
        TrendReportViewModel trendVM = new TrendReportViewModel(viewModel.getFinanceData());

        // Pass ViewModels to sub-panels
        incomeExpensesPanel = new IncomeExpensesReportPanel(incomeVM);
        categoryBreakdownPanel = new CategoryBreakdownPanel(categoryVM);
        trendReportPanel = new TrendReportPanel(trendVM);

        chartContainer.add(incomeExpensesPanel, INCOME_EXPENSE_PANEL);
        chartContainer.add(categoryBreakdownPanel, CATEGORY_BREAKDOWN_PANEL);
        chartContainer.add(trendReportPanel, TREND_PANEL);

        cardLayout.show(chartContainer, INCOME_EXPENSE_PANEL);

        JScrollPane scrollPane = new JScrollPane(chartContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates the header panel with the title.
     *
     * @return the header panel
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Financial Reports");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.WEST);
        return panel;
    }

    /**
     * Creates the control panel for selecting report type, time period, and interval.
     *
     * @return the control panel
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));
        panel.setPreferredSize(new Dimension(220, 300));

        // Report Type Section
        panel.add(createSectionLabel("Report Type"));

        ButtonGroup reportTypeGroup = new ButtonGroup();
        JRadioButton incomeExpenseBtn = createRadioButton("Income vs Expenses", true);
        JRadioButton categoryBreakdownBtn = createRadioButton("Expense Breakdown", false);
        JRadioButton trendsBtn = createRadioButton("Trends Analysis", false);

        reportTypeGroup.add(incomeExpenseBtn);
        reportTypeGroup.add(categoryBreakdownBtn);
        reportTypeGroup.add(trendsBtn);

        panel.add(incomeExpenseBtn);
        panel.add(categoryBreakdownBtn);
        panel.add(trendsBtn);

        panel.add(Box.createVerticalStrut(20));

        // Time Period Section
        panel.add(createSectionLabel("Time Period"));

        String[] timePeriods = {"Last 7 days", "Last 30 days", "Last 90 days", "This month", "Last month", "This year"};
        JComboBox<String> timePeriodsCombo = new JComboBox<>(timePeriods);
        timePeriodsCombo.setMaximumSize(new Dimension(220, 30));
        timePeriodsCombo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(timePeriodsCombo);

        panel.add(Box.createVerticalStrut(20));

        // Interval Section (for trend reports)
        panel.add(createSectionLabel("Data Interval"));
        String[] intervals = {"Daily", "Weekly", "Fortnightly", "Monthly", "Quarterly", "Yearly"};
        JComboBox<String> intervalsCombo = new JComboBox<>(intervals);
        intervalsCombo.setMaximumSize(new Dimension(220, 30));
        intervalsCombo.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(intervalsCombo);

        panel.add(Box.createVerticalStrut(20));

        // Apply button
        JButton applyButton = new JButton("Apply Filters");
        applyButton.setAlignmentX(LEFT_ALIGNMENT);
        applyButton.setMaximumSize(new Dimension(220, 35));
        panel.add(applyButton);

        // Load data button
        JButton loadDataButton = new JButton("Load Data");
        loadDataButton.setAlignmentX(LEFT_ALIGNMENT);
        loadDataButton.setMaximumSize(new Dimension(220, 35));
        panel.add(loadDataButton);

        // Add action listeners
        timePeriodsCombo.addActionListener(e -> {
            updateReportTimeRange(timePeriodsCombo.getSelectedItem().toString());
        });

        intervalsCombo.addActionListener(e -> {
            updateReportInterval(intervalsCombo.getSelectedItem().toString());
        });

        incomeExpenseBtn.addActionListener(e -> {
            if (incomeExpenseBtn.isSelected()) {
                cardLayout.show(chartContainer, INCOME_EXPENSE_PANEL);
            }
        });

        categoryBreakdownBtn.addActionListener(e -> {
            if (categoryBreakdownBtn.isSelected()) {
                cardLayout.show(chartContainer, CATEGORY_BREAKDOWN_PANEL);
            }
        });

        trendsBtn.addActionListener(e -> {
            if (trendsBtn.isSelected()) {
                cardLayout.show(chartContainer, TREND_PANEL);
            }
        });

        applyButton.addActionListener(e -> {
            applyFilters(
                timePeriodsCombo.getSelectedItem().toString(),
                intervalsCombo.getSelectedItem().toString()
            );
        });

        loadDataButton.addActionListener(e -> {
            viewModel.loadTransactionData();
            // Refresh all charts
            incomeExpensesPanel.refreshChart();
            categoryBreakdownPanel.refreshChart();
            trendReportPanel.refreshChart();
            JOptionPane.showMessageDialog(this, "Transaction data loaded successfully", "Load Complete", JOptionPane.INFORMATION_MESSAGE);
        });

        return panel;
    }

    /**
     * Creates a section label for the control panel.
     *
     * @param text the label text
     * @return the section label
     */
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(label.getFont().getName(), Font.BOLD, 14));
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return label;
    }

    /**
     * Creates a radio button for report type selection.
     *
     * @param text the button text
     * @param selected whether the button is initially selected
     * @return the radio button
     */
    private JRadioButton createRadioButton(String text, boolean selected) {
        JRadioButton radioButton = new JRadioButton(text, selected);
        radioButton.setAlignmentX(LEFT_ALIGNMENT);
        return radioButton;
    }

    /**
     * Updates the time range for all report panels.
     *
     * @param timeRange the selected time range
     */
    private void updateReportTimeRange(String timeRange) {
        incomeExpensesPanel.setTimeRange(timeRange);
        categoryBreakdownPanel.setTimeRange(timeRange);
        trendReportPanel.setTimeRange(timeRange);
    }

    /**
     * Updates the data interval for the trend report panel.
     *
     * @param interval the selected interval
     */
    private void updateReportInterval(String interval) {
        trendReportPanel.setInterval(interval);
    }

    /**
     * Applies the selected filters to all report panels and refreshes the charts.
     *
     * @param timeRange the selected time range
     * @param interval the selected data interval
     */
    private void applyFilters(String timeRange, String interval) {
        updateReportTimeRange(timeRange);
        updateReportInterval(interval);

        // Refresh all panels
        incomeExpensesPanel.refreshChart();
        categoryBreakdownPanel.refreshChart();
        trendReportPanel.refreshChart();
    }

    /**
     * Called when report data changes in the ViewModel.
     * Refreshes all charts.
     */
    @Override
    public void onReportsDataChanged() {
        // Refresh all charts when data changes
        incomeExpensesPanel.refreshChart();
        categoryBreakdownPanel.refreshChart();
        trendReportPanel.refreshChart();
    }

    /**
     * Called when this panel is removed from its container.
     * Cleans up listeners and resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}