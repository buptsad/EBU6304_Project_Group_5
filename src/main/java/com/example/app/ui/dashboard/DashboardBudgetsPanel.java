package com.example.app.ui.dashboard;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.dashboard.DashboardBudgetsViewModel;
import com.example.app.viewmodel.dashboard.DashboardBudgetsViewModel.BudgetChangeListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A panel that displays and manages budget categories with their allocations and spending.
 * This component follows the MVVM pattern and communicates with a ViewModel to display
 * budget data and handle user interactions.
 * <p>
 * The panel includes:
 * <ul>
 *   <li>An overall budget progress indicator</li>
 *   <li>A table of budget categories with their allocated amounts and spending</li>
 *   <li>UI elements for editing and deleting budget categories</li>
 * </ul>
 */
public class DashboardBudgetsPanel extends JPanel implements CurrencyChangeListener, BudgetChangeListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardBudgetsPanel.class.getName());
    
    /** ViewModel reference */
    private final DashboardBudgetsViewModel viewModel;
    
    /** UI components */
    private final JPanel categoriesPanel;
    /** Currency symbol for displaying budget amounts */
    private String currencySymbol;
    
    /**
     * Creates a new budget dashboard panel for the specified user.
     *
     * @param username the username of the current user
     */
    public DashboardBudgetsPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new DashboardBudgetsViewModel(username);
        this.viewModel.addBudgetChangeListener(this);
        
        // Initialize currency symbol
        this.currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Set up UI
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Overall budget progress
        JPanel overallPanel = new JPanel(new BorderLayout());
        double overallPercentage = viewModel.getOverallBudgetPercentage();
        JLabel overallLabel = new JLabel(String.format("Overall Budget: %.2f%% used", overallPercentage));
        overallLabel.setFont(new Font(overallLabel.getFont().getName(), Font.BOLD, 14));
        overallPanel.add(overallLabel, BorderLayout.NORTH);
        
        JProgressBar overallProgressBar = createProgressBar(overallPercentage);
        overallProgressBar.setPreferredSize(new Dimension(getWidth(), 15));
        overallPanel.add(overallProgressBar, BorderLayout.CENTER);
        
        headerPanel.add(overallPanel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        add(headerPanel, BorderLayout.NORTH);
        
        // Categories panel
        categoriesPanel = new JPanel(new BorderLayout());
        // Set preferred and maximum size
        categoriesPanel.setPreferredSize(new Dimension(400, 200));
        categoriesPanel.setMaximumSize(new Dimension(600, 400));
        
        // Create table model
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only allow editing the buttons column
                return column == 4;
            }
        };
        tableModel.addColumn("Category");
        tableModel.addColumn("Budget");
        tableModel.addColumn("Spent");
        tableModel.addColumn("Usage");
        tableModel.addColumn("Actions");

        // Create table and set properties
        JTable budgetTable = new JTable(tableModel);
        budgetTable.setRowHeight(40);
        budgetTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        budgetTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        budgetTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        budgetTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        budgetTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        // Add table grid line settings
        budgetTable.setShowGrid(true);
        budgetTable.setGridColor(Color.GRAY);
        budgetTable.setIntercellSpacing(new Dimension(1, 1));
        budgetTable.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Add custom renderer for progress bar column
        budgetTable.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarRenderer());
        
        // Add custom renderer and editor for action buttons column
        budgetTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        budgetTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Add table to scroll pane
        JScrollPane tableScrollPane = new JScrollPane(budgetTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        categoriesPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Add category panels
        updateCategoryTable(budgetTable);
        
        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Register as listeners
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    /**
     * Updates the category table data with the current budget and expense information.
     *
     * @param table the JTable to update with budget data
     */
    private void updateCategoryTable(JTable table) {
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
    
    /**
     * Creates a progress bar with the appropriate color based on percentage value.
     *
     * @param percentage the percentage value (0-100) to display in the progress bar
     * @return a configured JProgressBar instance
     */
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
    
    /**
     * Opens a dialog to edit a budget category and updates the model if confirmed.
     *
     * @param category the name of the category to edit
     */
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
    
    /**
     * Shows a confirmation dialog and deletes a budget category if confirmed.
     *
     * @param category the name of the category to delete
     */
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
    
    /**
     * Opens a dialog to add a new budget category and updates the model if confirmed.
     */
    private void addNewCategory() {
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Add Category", 
                "", 
                0.0);
                
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

    /**
     * Called when budget data changes in the view model.
     * Updates the UI to reflect the changes.
     */
    @Override
    public void onBudgetDataChanged() {
        // Update UI when the view model notifies of budget changes
        updateCategoryTable((JTable)((JScrollPane)categoriesPanel.getComponent(0)).getViewport().getView());
    }

    /**
     * Called when currency settings change.
     * Updates the UI to reflect the new currency.
     *
     * @param currencyCode the new currency code
     * @param currencySymbol the new currency symbol
     */
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        this.currencySymbol = currencySymbol;
        updateCategoryTable((JTable)((JScrollPane)categoriesPanel.getComponent(0)).getViewport().getView());
    }
    
    /**
     * Called when this panel is removed from its container.
     * Cleans up event listeners and other resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed from UI
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeBudgetChangeListener(this);
        viewModel.cleanup();
    }
    
    /**
     * Updates the panel to display data for a different user.
     * Replaces this panel with a new one in the parent container.
     *
     * @param username the username of the user to display data for
     */
    public void setUsername(String username) {
        // In MVVM, we'd create a new ViewModel for the new user
        // For simplicity, we'll replace the entire panel in the parent component
        
        // Let the parent container know that it needs to recreate this panel
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.add(new DashboardBudgetsPanel(username));
            parent.revalidate();
            parent.repaint();
        }
    }
    
    /**
     * Opens the full budget management panel in the main application window.
     */
    private void openFullBudgetPanel() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            // Code to navigate to the full BudgetsPanel would go here
            JOptionPane.showMessageDialog(frame, 
                "Navigate to full Budget Management", 
                "Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Custom renderer for displaying progress bars in table cells.
     * Colors change based on percentage values.
     */
    class ProgressBarRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
        /**
         * Creates a new progress bar renderer.
         */
        public ProgressBarRenderer() {
            super(0, 100);
            setStringPainted(true);
        }

        /**
         * Returns a configured progress bar for rendering in the table cell.
         *
         * @param table the JTable containing the cell renderer
         * @param value the value to display in this cell
         * @param isSelected whether the cell is selected
         * @param hasFocus whether the cell has focus
         * @param row the row index of the cell
         * @param column the column index of the cell
         * @return the progress bar component to render the cell
         */
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

    /**
     * Custom renderer for displaying edit and delete buttons in table cells.
     */
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;
        
        /**
         * Creates a new button renderer with Edit and Delete buttons.
         */
        public ButtonRenderer() {
            setLayout(new GridLayout(1, 2, 5, 0));
            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");
            add(editButton);
            add(deleteButton);
        }
        
        /**
         * Returns a panel containing Edit and Delete buttons for rendering in the table cell.
         *
         * @param table the JTable containing the cell renderer
         * @param value the value to display in this cell
         * @param isSelected whether the cell is selected
         * @param hasFocus whether the cell has focus
         * @param row the row index of the cell
         * @param column the column index of the cell
         * @return the button panel component to render the cell
         */
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
            return this;
        }
    }

    /**
     * Custom cell editor for handling edit and delete button actions in table cells.
     */
    class ButtonEditor extends DefaultCellEditor {
        protected JPanel panel;
        protected JButton editButton;
        protected JButton deleteButton;
        private String category;
        
        /**
         * Creates a new button editor with action handlers for Edit and Delete buttons.
         *
         * @param checkBox a checkbox component (required by DefaultCellEditor)
         */
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new GridLayout(1, 2, 5, 0));
            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            editButton.addActionListener(e -> {
                fireEditingStopped();
                // Get the category name from the current row
                editCategory(category);
            });
            
            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                // Get the category name from the current row
                deleteCategory(category);
            });
            
            panel.add(editButton);
            panel.add(deleteButton);
        }
        
        /**
         * Prepares the editor with the category from the selected row.
         *
         * @param table the JTable containing the cell being edited
         * @param value the value in the cell being edited
         * @param isSelected whether the cell is selected
         * @param row the row of the cell being edited
         * @param column the column of the cell being edited
         * @return the component for editing
         */
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                  boolean isSelected, int row, int column) {
            // Get the category name from the current row
            category = (String) table.getValueAt(row, 0);
            return panel;
        }
        
        /**
         * Returns the edited value.
         *
         * @return the edited value
         */
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}