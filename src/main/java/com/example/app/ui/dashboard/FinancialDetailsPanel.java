package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.FinancialAdvice;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

public class FinancialDetailsPanel extends JPanel implements CurrencyChangeListener, DataRefreshListener {
    private FinanceData financeData;
    private FinancialAdvice financialAdvice;
    private JPanel summaryPanel;
    private JPanel progressPanel;
    private JPanel tipsPanel;
    private JTextArea tipArea;
    private JLabel adviceTimeLabel;
    
    public FinancialDetailsPanel(FinanceData financeData) {
        this(financeData, OverviewPanel.sharedAdvice);
    }
    
    public FinancialDetailsPanel(FinanceData financeData, FinancialAdvice financialAdvice) {
        this.financeData = financeData;
        this.financialAdvice = financialAdvice;
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(0, 10, 0, 0));
        
        // 创建面板
        summaryPanel = createSummaryPanel();
        progressPanel = createCategoryProgressPanel();
        tipsPanel = createTipsPanel();
        
        // 添加面板
        add(summaryPanel);
        add(Box.createVerticalStrut(15));
        add(progressPanel);
        add(Box.createVerticalStrut(15));
        add(tipsPanel);
        add(Box.createVerticalGlue());
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        
        // Register as listener for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 当货币变化时刷新显示
        removeAll();
        
        // 重新创建面板
        summaryPanel = createSummaryPanel();
        progressPanel = createCategoryProgressPanel();
        tipsPanel = createTipsPanel();
        
        // 重新添加面板
        add(summaryPanel);
        add(Box.createVerticalStrut(15));
        add(progressPanel);
        add(Box.createVerticalStrut(15));
        add(tipsPanel);
        add(Box.createVerticalGlue());
        
        // 刷新UI
        revalidate();
        repaint();
    }
    
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.ALL) {
            // Refresh all panels
            removeAll();
            
            // Recreate panels
            summaryPanel = createSummaryPanel();
            progressPanel = createCategoryProgressPanel();
            tipsPanel = createTipsPanel();
            
            // Re-add panels
            add(summaryPanel);
            add(Box.createVerticalStrut(15));
            add(progressPanel);
            add(Box.createVerticalStrut(15));
            add(tipsPanel);
            add(Box.createVerticalGlue());
            
            // Refresh UI
            revalidate();
            repaint();
        }
    }
    
    private JPanel createSummaryPanel() {
        // ... existing code unchanged ...
        
        // Same as before
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
        
        // 使用CurrencyManager格式化金额
        CurrencyManager cm = CurrencyManager.getInstance();
        
        // 添加摘要项
        summaryPanel.add(createLabelPanel("Monthly Budget:", 
            cm.formatCurrency(financeData.getMonthlyBudget())));
        summaryPanel.add(Box.createVerticalStrut(10));
        
        summaryPanel.add(createLabelPanel("Total Income:", 
            cm.formatCurrency(financeData.getTotalIncome())));
        summaryPanel.add(Box.createVerticalStrut(10));
        
        summaryPanel.add(createLabelPanel("Total Expenses:", 
            cm.formatCurrency(financeData.getTotalExpenses())));
        summaryPanel.add(Box.createVerticalStrut(10));
        
        summaryPanel.add(createLabelPanel("Net Savings:", 
            cm.formatCurrency(financeData.getTotalSavings())));
        
        return summaryPanel;
    }
    
    private JPanel createCategoryProgressPanel() {
        // ... existing code unchanged ...
        
        // Same as before
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
        
        // 使用CurrencyManager格式化金额
        CurrencyManager cm = CurrencyManager.getInstance();
        
        // 添加总体预算进度
        double overallPercentage = financeData.getOverallBudgetPercentage();
        JPanel overallPanel = new JPanel();
        overallPanel.setLayout(new BoxLayout(overallPanel, BoxLayout.Y_AXIS));
        overallPanel.setBorder(BorderFactory.createTitledBorder("Overall"));
        overallPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel overallLabel = new JLabel(String.format("%s of %s (%.1f%%)", 
            cm.formatCurrency(financeData.getTotalExpenses()), 
            cm.formatCurrency(financeData.getMonthlyBudget()),
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
        
        // 添加分类预算进度条面板
        JPanel categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        categoriesPanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        categoriesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // 为每个分类添加进度条
        Map<String, Double> categoryBudgets = financeData.getCategoryBudgets();
        Map<String, Double> categoryExpenses = financeData.getCategoryExpenses();
        
        for (String category : categoryBudgets.keySet()) {
            double budget = categoryBudgets.get(category);
            double expense = categoryExpenses.getOrDefault(category, 0.0);
            double percentage = (expense / budget) * 100;
            
            JPanel categoryPanel = new JPanel(new GridBagLayout());
            categoryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 5);
            
            // 分类标签
            JLabel categoryLabel = new JLabel(category);
            categoryLabel.setPreferredSize(new Dimension(100, 20));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            categoryPanel.add(categoryLabel, gbc);
            
            // 进度条
            JProgressBar progressBar = createProgressBar(percentage);
            gbc.gridx = 1;
            gbc.weightx = 1.0;
            categoryPanel.add(progressBar, gbc);
            
            // 数值标签 - 使用CurrencyManager格式化
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
        // ... existing code unchanged ...
        
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
        
        // Check for categories that are over budget
        StringBuilder tipText = new StringBuilder();
        Map<String, Double> categoryBudgets = financeData.getCategoryBudgets();
        Map<String, Double> categoryExpenses = financeData.getCategoryExpenses();
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
                // 使用当前货币符号
                tipText.append(String.format("• %s is %s%.2f over budget\n", 
                    category, currencySymbol, overage));
            }
        }
        
        // Add general tip based on overall budget
        double percentUsed = financeData.getOverallBudgetPercentage();
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
        
        // Add local financial context
        tipText.append("\n\n");
        tipText.append("Local Financial Context:\n");
        tipText.append(financialAdvice.getAdvice());
        
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
        
        // Add generation time at the bottom
        adviceTimeLabel = new JLabel("Financial context generated: " + financialAdvice.getFormattedGenerationTime());
        adviceTimeLabel.setFont(new Font(adviceTimeLabel.getFont().getName(), Font.ITALIC, 11));
        adviceTimeLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
        contentPanel.add(adviceTimeLabel, BorderLayout.SOUTH);
        
        tipsPanel.add(contentPanel);
        
        return tipsPanel;
    }
    
    private JPanel createLabelPanel(String label, String value) {
        // ... existing code unchanged ...
        
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
        // Unregister from all listeners
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        DataRefreshManager.getInstance().removeListener(this);
    }
}