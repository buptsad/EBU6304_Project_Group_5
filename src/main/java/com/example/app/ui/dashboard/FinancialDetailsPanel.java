package com.example.app.ui.dashboard;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.dashboard.FinancialDetailsViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/**
 * Panel that displays financial details using MVVM pattern.
 * This is the View component that observes the ViewModel.
 */
public class FinancialDetailsPanel extends JPanel implements CurrencyChangeListener, 
                                                           FinancialDetailsViewModel.FinancialDetailsChangeListener {
    // Reference to the ViewModel
    private final FinancialDetailsViewModel viewModel;
    
    // UI components
    private JPanel summaryPanel;
    private JPanel progressPanel;
    private JPanel tipsPanel;
    private JTextArea tipArea;
    private JLabel adviceTimeLabel;
    
    public FinancialDetailsPanel(FinancialDetailsViewModel viewModel) {
        this.viewModel = viewModel;
        
        // Set up UI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(0, 10, 0, 0));
        
        // Create panels
        summaryPanel = createSummaryPanel();
        progressPanel = createCategoryProgressPanel();
        tipsPanel = createTipsPanel();
        
        // Add panels
        add(summaryPanel);
        add(Box.createVerticalStrut(15));
        add(progressPanel);
        add(Box.createVerticalStrut(15));
        add(tipsPanel);
        add(Box.createVerticalGlue());
        
        // Register listeners
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        viewModel.addChangeListener(this);
    }
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // When currency changes, refresh all panels
        refreshAllPanels();
    }
    
    @Override
    public void onFinancialDataChanged() {
        // When financial data changes, refresh all panels
        refreshAllPanels();
    }
    
    @Override
    public void onAdviceChanged() {
        // When only advice changes, just update that part
        updateAdviceDisplay();
    }
    
    private void refreshAllPanels() {
        removeAll();
        
        summaryPanel = createSummaryPanel();
        progressPanel = createCategoryProgressPanel();
        tipsPanel = createTipsPanel();
        
        add(summaryPanel);
        add(Box.createVerticalStrut(15));
        add(progressPanel);
        add(Box.createVerticalStrut(15));
        add(tipsPanel);
        add(Box.createVerticalGlue());
        
        revalidate();
        repaint();
    }
    
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Financial Summary",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Format currency values using CurrencyManager
        CurrencyManager cm = CurrencyManager.getInstance();
        
        // Add summary items using data from ViewModel
        summaryPanel.add(createLabelPanel("Monthly Budget:", 
            cm.formatCurrency(viewModel.getMonthlyBudget())));
        summaryPanel.add(Box.createVerticalStrut(10));
        
        summaryPanel.add(createLabelPanel("Total Income:", 
            cm.formatCurrency(viewModel.getTotalIncome())));
        summaryPanel.add(Box.createVerticalStrut(10));
        
        summaryPanel.add(createLabelPanel("Total Expenses:", 
            cm.formatCurrency(viewModel.getTotalExpenses())));
        summaryPanel.add(Box.createVerticalStrut(10));
        
        summaryPanel.add(createLabelPanel("Net Savings:", 
            cm.formatCurrency(viewModel.getTotalSavings())));
        
        return summaryPanel;
    }
    
    private JPanel createCategoryProgressPanel() {
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Budget Progress by Category",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Format currency values using CurrencyManager
        CurrencyManager cm = CurrencyManager.getInstance();
        
        // Add overall budget progress
        double overallPercentage = viewModel.getOverallBudgetPercentage();
        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
        overallPanel.setBorder(BorderFactory.createTitledBorder("Overall"));
        overallPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel overallLabel = new JLabel(String.format("%s of %s (%.1f%%)", 
            cm.formatCurrency(viewModel.getTotalExpenses()), 
            cm.formatCurrency(viewModel.getMonthlyBudget()),
            overallPercentage));
        overallLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JProgressBar overallProgressBar = createProgressBar(overallPercentage);
        overallProgressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        overallProgressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        
        overallPanel.add(overallLabel);
        overallPanel.add(Box.createVerticalStrut(5));
        overallPanel.add(overallProgressBar);
        
        progressPanel.add(overallPanel);
        progressPanel.add(Box.createVerticalStrut(15));
        
        // Add category progress bars panel
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        categoriesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Get category data from ViewModel
        Map<String, Double> categoryBudgets = viewModel.getCategoryBudgets();
        Map<String, Double> categoryExpenses = viewModel.getCategoryExpenses();
        
        // Add a progress bar for each category
        for (String category : categoryBudgets.keySet()) {
            double budget = categoryBudgets.get(category);
            double expense = categoryExpenses.getOrDefault(category, 0.0);
            double percentage = budget > 0 ? (expense / budget) * 100 : 0;
            
            JPanel categoryPanel = new JPanel(new GridBagLayout());
            categoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 5);
            
            // Category label
            JLabel categoryLabel = new JLabel(category);
            categoryLabel.setPreferredSize(new Dimension(100, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            categoryPanel.add(categoryLabel, gbc);
            
            // Progress bar
            JProgressBar progressBar = createProgressBar(percentage);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            categoryPanel.add(progressBar, gbc);
            
            // Value label - format using CurrencyManager
            JLabel valueLabel = new JLabel(String.format("%s / %s", 
                cm.formatCurrency(expense), cm.formatCurrency(budget)));
            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            valueLabel.setPreferredSize(new Dimension(120, 20));
            gbc.gridx = 2;
            gbc.weightx = 0.0;
            categoryPanel.add(valueLabel, gbc);
            
            categoriesPanel.add(categoryPanel);
            categoriesPanel.add(Box.createVerticalStrut(5));
        }
        
        progressPanel.add(categoriesPanel);
        
        return progressPanel;
    }
    
    private JProgressBar createProgressBar(double percentage) {
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setStringPainted(true);
        
        // Set color based on percentage
        if (percentage < 70) {
            progressBar.setForeground(new Color(46, 139, 87)); // Green
        } else if (percentage < 90) {
            progressBar.setForeground(new Color(255, 165, 0)); // Orange
        } else {
            progressBar.setForeground(new Color(178, 34, 34)); // Red
        }
        
        return progressBar;
    }
    
    private JPanel createTipsPanel() {
        JPanel tipsPanel = new JPanel();
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        tipsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Financial Tips & Local Context",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
            ),
            new EmptyBorder(10, 10, 10, 10)
        ));
        tipsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Generate tips based on budget data from ViewModel
        StringBuilder tipText = new StringBuilder();
        Map<String, Double> categoryBudgets = viewModel.getCategoryBudgets();
        Map<String, Double> categoryExpenses = viewModel.getCategoryExpenses();
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        boolean foundOverBudget = false;
        for (String category : categoryBudgets.keySet()) {
            double budget = categoryBudgets.get(category);
            double expense = categoryExpenses.getOrDefault(category, 0.0);
            
            if (expense > budget) {
                if (!foundOverBudget) {
                    tipText.append("Attention needed in these categories:\n");
                    foundOverBudget = true;
                }
                double overage = expense - budget;
                tipText.append(String.format("• %s is %s%.2f over budget\n", 
                    category, currencySymbol, overage));
            }
        }
        
        // Add general tip based on overall budget
        double percentUsed = viewModel.getOverallBudgetPercentage();
        if (!foundOverBudget) {
            if (percentUsed > 90) {
                tipText.append("You're close to exceeding your overall budget. Consider reducing non-essential expenses.");
            } else if (percentUsed > 70) {
                tipText.append("You're managing your budget well. Monitor your spending carefully.");
            } else {
                tipText.append("You're managing your budget well. Consider saving the surplus!");
            }
        } else {
            tipText.append("\nConsider adjusting your spending in the categories above.");
        }
        
        // Add financial advice from ViewModel
        tipText.append("\n\n");
        tipText.append("Local Financial Context:\n");
        tipText.append(viewModel.getAdvice());
        
        tipArea = new JTextArea(tipText.toString());
        tipArea.setWrapStyleWord(true);
        tipArea.setLineWrap(true);
        tipArea.setEditable(false);
        tipArea.setBackground(UIManager.getColor("Panel.background"));
        tipArea.setFont(UIManager.getFont("Label.font"));
        tipArea.setBorder(null);
        tipArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIManager.getColor("Panel.background"));
        contentPanel.add(tipArea, BorderLayout.CENTER);
        
        // Add generation time at the bottom using data from ViewModel
        adviceTimeLabel = new JLabel("Financial context generated: " + viewModel.getFormattedGenerationTime());
        adviceTimeLabel.setFont(new Font(adviceTimeLabel.getFont().getName(), Font.ITALIC, 11));
        adviceTimeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        contentPanel.add(adviceTimeLabel, BorderLayout.SOUTH);
        
        tipsPanel.add(contentPanel);
        
        return tipsPanel;
    }
    
    private JPanel createLabelPanel(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font(labelComponent.getFont().getName(), Font.PLAIN, 14));
        panel.add(labelComponent, BorderLayout.WEST);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font(valueComponent.getFont().getName(), Font.BOLD, 14));
        panel.add(valueComponent, BorderLayout.EAST);
        
        return panel;
    }
    
    // Method to update advice display when it changes
    public void updateAdviceDisplay() {
        if (tipsPanel != null) {
            remove(tipsPanel);
            tipsPanel = createTipsPanel();
            add(tipsPanel, 4); // Add at the original position
            revalidate();
            repaint();
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed from UI
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}