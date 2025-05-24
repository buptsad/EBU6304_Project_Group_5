package com.example.app.ui.dashboard;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * A panel component that displays budget information for a specific spending category.
 * The panel includes category name, budget amount, actual spending, progress bar,
 * and action buttons for editing and deleting the budget category.
 * <p>
 * This component implements CurrencyChangeListener to update displayed values
 * when the application's currency settings change.
 
 */
public class BudgetCategoryPanel extends JPanel implements CurrencyChangeListener {
    /** The budget category name */
    private final String category;
    
    /** The allocated budget amount for this category */
    private final double budget;
    
    /** The actual expense amount for this category */
    private final double expense;
    
    /** The percentage of budget spent (expense/budget * 100) */
    private final double percentage;
    
    /** Label displaying the budget amount (updated when currency changes) */
    private JLabel budgetLabel;
    
    /** Label displaying the expense amount (updated when currency changes) */
    private JLabel expenseLabel;
    
    /**
     * Creates a new budget category panel with the specified budget information and action listeners.
     *
     * @param category the name of the budget category
     * @param budget the allocated budget amount
     * @param expense the actual spent amount
     * @param percentage the percentage of budget spent (expense/budget * 100)
     * @param editListener action listener for the edit button
     * @param deleteListener action listener for the delete button
     */
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
        
        // Get current currency symbol
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Create budget and expense labels, save references for future updates
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
        
        // Register as currency change listener
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    /**
     * Called when the application currency changes.
     * Updates the displayed budget and expense amounts with the new currency symbol.
     *
     * @param currencyCode the new currency code
     * @param currencySymbol the new currency symbol
     */
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Update currency symbols in labels
        budgetLabel.setText(String.format("Budget: %s%.2f", currencySymbol, budget));
        expenseLabel.setText(String.format("Spent: %s%.2f", currencySymbol, expense));
        revalidate();
        repaint();
    }
    
    /**
     * Called when this panel is removed from its container.
     * Performs cleanup by removing the currency change listener.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Remove listener when component is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}