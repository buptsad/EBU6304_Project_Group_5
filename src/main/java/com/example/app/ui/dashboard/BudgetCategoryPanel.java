package com.example.app.ui.dashboard;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class BudgetCategoryPanel extends JPanel implements CurrencyChangeListener {
    private final String category;
    private final double budget;
    private final double expense;
    private final double percentage;
    private JLabel budgetLabel;
    private JLabel expenseLabel;
    
    public BudgetCategoryPanel(String category, double budget, double expense, double percentage,
                              ActionListener editListener, ActionListener deleteListener) {
        this.category = category;
        this.budget = budget;
        this.expense = expense;
        this.percentage = percentage;
        
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Category name and budget amounts
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font(categoryLabel.getFont().getName(), Font.BOLD, 14));
        leftPanel.add(categoryLabel, BorderLayout.NORTH);
        
        JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        
        // 获取当前货币符号
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // 创建预算和支出标签，保存引用以便更新
        budgetLabel = new JLabel(String.format("Budget: %s%.2f", currencySymbol, budget));
        budgetLabel.setFont(new Font(budgetLabel.getFont().getName(), Font.PLAIN, 12));
        
        expenseLabel = new JLabel(String.format("Spent: %s%.2f", currencySymbol, expense));
        expenseLabel.setFont(new Font(expenseLabel.getFont().getName(), Font.PLAIN, 12));
        
        if (expense > budget) {
            expenseLabel.setForeground(new Color(231, 76, 60)); // Red for over budget
        }
        
        detailsPanel.add(budgetLabel);
        detailsPanel.add(expenseLabel);
        leftPanel.add(detailsPanel, BorderLayout.SOUTH);
        
        add(leftPanel, BorderLayout.WEST);
        
        // Progress bar
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setStringPainted(true);
        
        // Set color based on percentage
        if (percentage < 80) {
            progressBar.setForeground(new Color(46, 204, 113)); // Green
        } else if (percentage < 100) {
            progressBar.setForeground(new Color(241, 196, 15)); // Yellow
        } else {
            progressBar.setForeground(new Color(231, 76, 60)); // Red
        }
        
        progressPanel.add(progressBar, BorderLayout.CENTER);
        add(progressPanel, BorderLayout.CENTER);
        
        // Action buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        JButton editButton = new JButton("Edit");
        editButton.addActionListener(editListener);
        editButton.setFocusPainted(false);
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(deleteListener);
        deleteButton.setFocusPainted(false);
        
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        
        add(buttonsPanel, BorderLayout.EAST);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 更新标签中的货币符号
        budgetLabel.setText(String.format("Budget: %s%.2f", currencySymbol, budget));
        expenseLabel.setText(String.format("Spent: %s%.2f", currencySymbol, expense));
        revalidate();
        repaint();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除组件时取消监听
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}