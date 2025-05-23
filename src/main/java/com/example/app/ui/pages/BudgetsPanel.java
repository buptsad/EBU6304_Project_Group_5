package com.example.app.ui.pages;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.ui.dashboard.BudgetDialog;
import com.example.app.viewmodel.BudgetViewModel;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.*;

public class BudgetsPanel extends JPanel implements CurrencyChangeListener, BudgetViewModel.BudgetChangeListener {
    private final BudgetViewModel viewModel;
    private final JPanel userBudgetsPanel;
    private final JPanel aiSuggestedPanel;
    private Map<String, Double> currentSuggestedBudgets; // Store last generated suggestions
    private String currencySymbol;

    public BudgetsPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new BudgetViewModel(username);
        this.viewModel.addBudgetChangeListener(this);
        this.currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Overall budget progress
        JPanel overallPanel = createOverallBudgetPanel();
        headerPanel.add(overallPanel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel - split into left and right
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Left panel - Current budget allocation (Table version)
        JPanel userPanel = new JPanel(new BorderLayout());
        TitledBorder userBorder = BorderFactory.createTitledBorder("Your Budget Allocation");
        userBorder.setTitleFont(new Font(getFont().getName(), Font.BOLD, 16));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                userBorder,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create user budget table
        userBudgetsPanel = new JPanel(new BorderLayout());
        
        // Create table model for user budgets
        DefaultTableModel userTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing the buttons column
                return column == 4;
            }
        };
        userTableModel.addColumn("Category");
        userTableModel.addColumn("Budget");
        userTableModel.addColumn("Spent");
        userTableModel.addColumn("Usage");
        userTableModel.addColumn("Actions");

        // Create user budget table
        JTable userBudgetTable = new JTable(userTableModel);
        userBudgetTable.setRowHeight(40);
        userBudgetTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        userBudgetTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        userBudgetTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        userBudgetTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        userBudgetTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        // Add custom renderers
        userBudgetTable.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarRenderer());
        userBudgetTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        userBudgetTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // 添加表格网格线设置
        userBudgetTable.setShowGrid(true);
        userBudgetTable.setGridColor(Color.GRAY);
        userBudgetTable.setIntercellSpacing(new Dimension(1, 1));
        userBudgetTable.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JScrollPane userTableScrollPane = new JScrollPane(userBudgetTable);
        userTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        userBudgetsPanel.add(userTableScrollPane, BorderLayout.CENTER);
        
        updateUserCategoryTable(userBudgetTable);
        userPanel.add(userBudgetsPanel, BorderLayout.CENTER);

        // Add category button
        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Category");
        addButton.setIcon(UIManager.getIcon("Tree.addIcon"));
        addButton.addActionListener(e -> addNewCategory());
        userButtonPanel.add(addButton);
        userPanel.add(userButtonPanel, BorderLayout.SOUTH);

        // Right panel - AI suggested allocation (Table version)
        JPanel aiPanel = new JPanel(new BorderLayout());
        TitledBorder aiBorder = BorderFactory.createTitledBorder("AI Suggested Budget");
        aiBorder.setTitleFont(new Font(getFont().getName(), Font.BOLD, 16));
        aiPanel.setBorder(BorderFactory.createCompoundBorder(
                aiBorder,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create AI suggestions table
        aiSuggestedPanel = new JPanel(new BorderLayout());
        
        // Create table model for AI suggestions
        DefaultTableModel aiTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // All cells non-editable
            }
        };
        aiTableModel.addColumn("Category");
        aiTableModel.addColumn("Suggested Budget");
        aiTableModel.addColumn("Difference");

        JTable aiSuggestionsTable = new JTable(aiTableModel);
        aiSuggestionsTable.setRowHeight(40);
        aiSuggestionsTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        aiSuggestionsTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        aiSuggestionsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        
        // Add custom renderer for difference column
        aiSuggestionsTable.getColumnModel().getColumn(2).setCellRenderer(new DifferenceRenderer());
        
        // 添加表格网格线设置
        aiSuggestionsTable.setShowGrid(true);
        aiSuggestionsTable.setGridColor(Color.GRAY);
        aiSuggestionsTable.setIntercellSpacing(new Dimension(1, 1)); 
        aiSuggestionsTable.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        JScrollPane aiTableScrollPane = new JScrollPane(aiSuggestionsTable);
        aiTableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        aiSuggestedPanel.add(aiTableScrollPane, BorderLayout.CENTER);
        
        updateAISuggestedTable(aiSuggestionsTable);
        aiPanel.add(aiSuggestedPanel, BorderLayout.CENTER);

        // Shuffle button for AI suggestions
        JPanel aiButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton shuffleButton = new JButton("Shuffle Suggestions");
        shuffleButton.setIcon(UIManager.getIcon("Table.descendingSortIcon"));
        shuffleButton.addActionListener(e -> shuffleAISuggestions(aiSuggestionsTable));
        
        JButton applyButton = new JButton("Apply Suggestions");
        applyButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        applyButton.addActionListener(e -> applyAISuggestions());
        
        aiButtonPanel.add(shuffleButton);
        aiButtonPanel.add(applyButton);
        aiPanel.add(aiButtonPanel, BorderLayout.SOUTH);

        // Add both panels to the content area
        contentPanel.add(userPanel);
        contentPanel.add(aiPanel);
        add(contentPanel, BorderLayout.CENTER);
        
        // Register as currency change listener
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }

    private JPanel createOverallBudgetPanel() {
        JPanel overallPanel = new JPanel(new BorderLayout());
        double overallPercentage = viewModel.getOverallBudgetPercentage();
        JLabel overallLabel = new JLabel(String.format("Overall Budget: %.2f%% used", overallPercentage));
        overallLabel.setFont(new Font(overallLabel.getFont().getName(), Font.BOLD, 14));
        overallPanel.add(overallLabel, BorderLayout.NORTH);
        
        JProgressBar overallProgressBar = createProgressBar(overallPercentage);
        overallProgressBar.setPreferredSize(new Dimension(getWidth(), 15));
        overallPanel.add(overallProgressBar, BorderLayout.CENTER);
        
        return overallPanel;
    }

    private void updateUserCategoryTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear existing rows
        
        Map<String, Double> budgets = viewModel.getCategoryBudgets();
        Map<String, Double> expenses = viewModel.getCategoryExpenses();
        
        for (String category : budgets.keySet()) {
            double budget = budgets.get(category);
            double expense = expenses.getOrDefault(category, 0.0);
            double percentage = budget > 0 ? (expense / budget) * 100 : 0;
            
            model.addRow(new Object[]{
                category,
                currencySymbol + String.format("%.2f", budget),
                currencySymbol + String.format("%.2f", expense),
                percentage,
                "" // Placeholder for action buttons
            });
        }
    }
    
    private void updateAISuggestedTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear existing rows
        
        // Get actual budgets
        Map<String, Double> actualBudgets = viewModel.getCategoryBudgets();
        
        // Create or use existing AI suggestions
        if (currentSuggestedBudgets == null) {
            currentSuggestedBudgets = viewModel.generateSuggestedBudgets();
        }
        
        // Display each category with comparison to actual budget
        for (String category : currentSuggestedBudgets.keySet()) {
            double suggestedBudget = currentSuggestedBudgets.get(category);
            double actualBudget = actualBudgets.getOrDefault(category, 0.0);
            double difference = suggestedBudget - actualBudget;
            
            model.addRow(new Object[]{
                category,
                currencySymbol + String.format("%.2f", suggestedBudget),
                difference // We'll use a custom renderer for this
            });
        }
    }
    
    private JProgressBar createProgressBar(double percentage) {
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
        
        return progressBar;
    }
    
    private void addNewCategory() {
        BudgetDialog dialog = new BudgetDialog(SwingUtilities.getWindowAncestor(this), "Add Category", "", 0.0);
        if (dialog.showDialog()) {
            String category = dialog.getCategory();
            double budget = dialog.getBudget();
            
            // Update through view model
            viewModel.updateCategoryBudget(category, budget);
            
            JOptionPane.showMessageDialog(this, 
                    "New category added: " + category + " Budget: " + currencySymbol + budget,
                    "Category Added", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void editCategory(String category) {
        double currentBudget = viewModel.getCategoryBudget(category);
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Edit Category", 
                category, 
                currentBudget);
        
        if (dialog.showDialog()) {
            double newBudget = dialog.getBudget();
            
            // Update through view model
            viewModel.updateCategoryBudget(category, newBudget);
            
            JOptionPane.showMessageDialog(this, 
                    "Category updated: " + category + " New budget: " + currencySymbol + newBudget,
                    "Category Updated", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteCategory(String category) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete category: " + category + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Delete through view model
            if (viewModel.deleteCategoryBudget(category)) {
                JOptionPane.showMessageDialog(this, 
                        "Category deleted: " + category,
                        "Category Deleted", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void shuffleAISuggestions(JTable aiTable) {
        // Generate new suggestions through view model
        currentSuggestedBudgets = viewModel.generateSuggestedBudgets();
        updateAISuggestedTable(aiTable);
        
        JOptionPane.showMessageDialog(this,
                "New AI budget suggestions generated!",
                "Suggestions Updated",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void applyAISuggestions() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to apply AI suggested budget allocations to your budget?",
                "Apply AI Suggestions",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Apply suggestions through view model
            viewModel.applySuggestedBudgets(currentSuggestedBudgets);
            
            JOptionPane.showMessageDialog(this,
                    "AI suggested budgets have been applied to your budget allocation!",
                    "Suggestions Applied",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        this.currencySymbol = currencySymbol;
        // Update tables when currency changes
        updateUserCategoryTable((JTable)((JScrollPane)userBudgetsPanel.getComponent(0)).getViewport().getView());
        updateAISuggestedTable((JTable)((JScrollPane)aiSuggestedPanel.getComponent(0)).getViewport().getView());
    }

    @Override
    public void onBudgetDataChanged() {
        // Update UI when view model notifies of data changes
        updateUserCategoryTable((JTable)((JScrollPane)userBudgetsPanel.getComponent(0)).getViewport().getView());
        updateAISuggestedTable((JTable)((JScrollPane)aiSuggestedPanel.getComponent(0)).getViewport().getView());
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeBudgetChangeListener(this);
        viewModel.cleanup();
    }
    
    // Custom renderer classes
    
    // Progress bar renderer for usage column
    class ProgressBarRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressBarRenderer() {
            super(0, 100);
            setStringPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            double percentage = (Double) value;
            setValue((int) percentage);
            
            // Set color based on percentage
            if (percentage < 80) {
                setForeground(new Color(46, 204, 113)); // Green
            } else if (percentage < 100) {
                setForeground(new Color(241, 196, 15)); // Yellow
            } else {
                setForeground(new Color(231, 76, 60));  // Red
            }
            
            setString(String.format("%.2f%%", percentage));
            return this;
        }
    }

    // Button renderer for actions column
    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;
        
        public ButtonRenderer() {
            setLayout(new GridLayout(1, 2, 5, 0));
            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");
            add(editButton);
            add(deleteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
            return this;
        }
    }

    // Button editor for actions column
    class ButtonEditor extends DefaultCellEditor {
        protected JPanel panel;
        protected JButton editButton;
        protected JButton deleteButton;
        private String category;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new GridLayout(1, 2, 5, 0));
            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            editButton.addActionListener(e -> {
                fireEditingStopped();
                // Get current row's category
                editCategory(category);
            });
            
            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                // Get current row's category
                deleteCategory(category);
            });
            
            panel.add(editButton);
            panel.add(deleteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                  boolean isSelected, int row, int column) {
            // Get current row's category
            category = (String) table.getValueAt(row, 0);
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
    
    // Difference renderer for AI suggestions
    class DifferenceRenderer extends JLabel implements TableCellRenderer {
        public DifferenceRenderer() {
            setOpaque(true);
            setHorizontalAlignment(JLabel.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
            double difference = (Double) value;
            
            // Format difference display
            String diffText;
            Color diffColor;
            
            if (Math.abs(difference) < 0.01) {
                diffText = "No change";
                diffColor = Color.GRAY;
            } else if (difference > 0) {
                diffText = String.format("+%s%.2f", currencySymbol, difference);
                diffColor = new Color(46, 204, 113); // Green
            } else {
                diffText = String.format("-%s%.2f", currencySymbol, Math.abs(difference));
                diffColor = new Color(231, 76, 60); // Red
            }
            
            setText(diffText);
            setForeground(diffColor);
            setFont(new Font(getFont().getName(), Font.BOLD, 12));
            
            return this;
        }
    }
}


