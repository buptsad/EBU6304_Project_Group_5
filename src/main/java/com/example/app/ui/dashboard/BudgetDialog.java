package com.example.app.ui.dashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A dialog for creating or editing budget categories.
 * <p>
 * This modal dialog allows users to input or modify a budget category name
 * and its corresponding budget amount. The dialog includes validation
 * to ensure valid inputs are provided.
 
 */
public class BudgetDialog extends JDialog {
    /** Text field for entering the category name */
    private JTextField categoryField;
    
    /** Text field for entering the budget amount */
    private JTextField budgetField;
    
    /** Flag indicating whether the user confirmed the dialog (saved) or canceled */
    private boolean confirmed = false;
    
    /**
     * Creates a new budget dialog with the specified initial values.
     *
     * @param owner the owner window of this dialog
     * @param title the title of the dialog
     * @param initialCategory the initial category name to display
     * @param initialBudget the initial budget amount to display
     */
    public BudgetDialog(Window owner, String title, String initialCategory, double initialBudget) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setupUI(initialCategory, initialBudget);
    }
    
    /**
     * Sets up the user interface components of the dialog.
     *
     * @param initialCategory the initial category name to display
     * @param initialBudget the initial budget amount to display
     */
    private void setupUI(String initialCategory, double initialBudget) {
        setLayout(new BorderLayout());
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Category field
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Category:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        categoryField = new JTextField(initialCategory, 20);
        formPanel.add(categoryField, gbc);
        
        // Budget field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Budget Amount:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        budgetField = new JTextField(String.valueOf(initialBudget), 20);
        // Only allow numbers and decimal point
        budgetField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!((c >= '0' && c <= '9') || c == '.' || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                    e.consume();
                }
                // Allow only one decimal point
                if (c == '.' && budgetField.getText().contains(".")) {
                    e.consume();
                }
            }
        });
        formPanel.add(budgetField, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * Validates user input to ensure category name is not empty
     * and budget amount is a positive number.
     *
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput() {
        if (categoryField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Category name cannot be empty", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            double amount = Double.parseDouble(budgetField.getText());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Budget must be a positive number", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Shows the dialog and waits for user input.
     *
     * @return true if the user confirmed (saved), false if canceled
     */
    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
    
    /**
     * Gets the category name entered by the user.
     *
     * @return the category name, with leading and trailing whitespace removed
     */
    public String getCategory() {
        return categoryField.getText().trim();
    }
    
    /**
     * Gets the budget amount entered by the user.
     *
     * @return the budget amount, or 0.0 if the input couldn't be parsed as a number
     */
    public double getBudget() {
        try {
            return Double.parseDouble(budgetField.getText());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}