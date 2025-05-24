package com.example.app.ui.pages;

import com.example.app.ui.dialogs.CSVImportDialog;
import com.example.app.viewmodel.TransactionsViewModel;
import com.example.app.viewmodel.TransactionsViewModel.TransactionChangeListener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The TransactionsPanel provides a user interface for managing financial transactions.
 * It allows users to view, add, edit, delete, search, filter, and import transactions.
 * This panel follows the MVVM pattern and listens to changes from its ViewModel.
 * <p>
 * Features:
 * <ul>
 *   <li>Display transactions in a sortable, editable table</li>
 *   <li>Search and filter by category</li>
 *   <li>Add, delete, and edit transactions</li>
 *   <li>Import transactions from CSV</li>
 *   <li>Save and cancel changes</li>
 *   <li>Ask AI for assistance (placeholder)</li>
 * </ul>
 
 */
public class TransactionsPanel extends JPanel implements TransactionChangeListener {
    private static final Logger LOGGER = Logger.getLogger(TransactionsPanel.class.getName());

    /** ViewModel reference */
    private final TransactionsViewModel viewModel;

    /** Table displaying transactions */
    private JTable transactionsTable;
    /** Table model for transactions */
    private DefaultTableModel tableModel;
    /** Search field for filtering transactions */
    private JTextField searchField;
    /** Category filter combo box */
    private JComboBox<String> categoryFilterComboBox;
    /** UI buttons */
    private JButton addButton, deleteButton, saveButton, cancelButton, askAIButton;
    /** Tracks if there are unsaved changes */
    private boolean hasUnsavedChanges = false;
    /** Stores original transactions for cancel operation */
    private List<Object[]> originalTransactions;

    /**
     * Constructs a new TransactionsPanel for the specified user.
     *
     * @param username the username of the current user
     */
    public TransactionsPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new TransactionsViewModel(username);

        setLayout(new BorderLayout());

        // Create header with title and search panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Transactions", JLabel.LEFT);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Create search/filter panel
        JPanel searchPanel = createSearchFilterPanel();
        headerPanel.add(searchPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Create transactions table
        createTransactionsTable();
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel with buttons
        JPanel bottomPanel = createButtonPanel();
        add(bottomPanel, BorderLayout.SOUTH);

        // Register as listener after UI is constructed
        this.viewModel.addTransactionChangeListener(this);

        // Load initial data after UI is ready
        SwingUtilities.invokeLater(() -> {
            viewModel.loadTransactions();
        });
    }

    /**
     * Creates the search and filter panel.
     *
     * @return the search/filter panel
     */
    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 15));

        JLabel searchLabel = new JLabel("Search: ");
        searchField = new JTextField(15);
        searchField.addActionListener(e -> applyFilters());

        JLabel categoryLabel = new JLabel("Category: ");
        categoryFilterComboBox = new JComboBox<>();
        categoryFilterComboBox.setEditable(true);

        // Add empty option as default (show all)
        categoryFilterComboBox.addItem("");

        // Add categories from ViewModel
        Set<String> categories = viewModel.getCategories();
        for (String category : categories) {
            categoryFilterComboBox.addItem(category);
        }

        categoryFilterComboBox.addActionListener(e -> applyFilters());

        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(categoryLabel);
        panel.add(categoryFilterComboBox);

        return panel;
    }

    /**
     * Creates and configures the transactions table.
     */
    private void createTransactionsTable() {
        // Define table columns
        String[] columns = {"Date", "Description", "Category", "Amount", "Delete"};

        // Create table model that supports boolean for checkbox column
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return Boolean.class; // Checkbox column
                if (column == 3) return Double.class;  // Amount column
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 4 || tableModel.getValueAt(row, 4) instanceof Boolean; // Make all columns editable except checkbox column
            }
        };

        // Create and configure table
        transactionsTable = new JTable(tableModel);
        transactionsTable.setRowHeight(30);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(Color.LIGHT_GRAY);

        // Add table cell edit listener to track changes
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                setHasUnsavedChanges(true);
            }
        });

        // Set column widths
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Description
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Category
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Amount
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Delete checkbox

        // Make the table sortable
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        transactionsTable.setRowSorter(sorter);
    }

    /**
     * Creates the bottom panel with action buttons.
     *
     * @return the button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Left side buttons
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("Add Transaction");
        deleteButton = new JButton("Delete Selected");
        JButton loadFromCSVButton = new JButton("Load from CSV");

        addButton.addActionListener(e -> addNewTransaction());
        deleteButton.addActionListener(e -> deleteSelectedTransactions());
        loadFromCSVButton.addActionListener(e -> openCSVImportDialog());

        leftPanel.add(addButton);
        leftPanel.add(deleteButton);
        leftPanel.add(loadFromCSVButton);

        // Right side buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        askAIButton = new JButton("Ask AI");
        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");

        askAIButton.addActionListener(e -> askAI());
        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> cancelChanges());

        // Disable save/cancel buttons initially
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        rightPanel.add(askAIButton);
        rightPanel.add(saveButton);
        rightPanel.add(cancelButton);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Applies search and category filters to the transactions table.
     */
    private void applyFilters() {
        String searchText = searchField.getText().trim();
        String selectedCategory = (String) categoryFilterComboBox.getSelectedItem();

        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) transactionsTable.getRowSorter();

        // Use ViewModel to filter transactions
        List<Object[]> filteredData = viewModel.filterTransactions(searchText, selectedCategory);

        if (filteredData.size() == viewModel.getTransactions().size()) {
            // If filter returns all transactions, just clear the filter
            sorter.setRowFilter(null);
        } else {
            // Otherwise create a row filter based on our filtered data
            RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    int row = entry.getIdentifier();

                    // Check category filter if selected
                    if (selectedCategory != null && !selectedCategory.isEmpty()) {
                        String rowCategory = (String) entry.getModel().getValueAt(row, 2);
                        if (!selectedCategory.equals(rowCategory)) {
                            return false;
                        }
                    }

                    // Check search text if not empty
                    if (!searchText.isEmpty()) {
                        boolean matchFound = false;
                        // Check columns 0, 1, 2 (Date, Description, Category)
                        for (int i = 0; i < 3; i++) {
                            String value = entry.getModel().getValueAt(row, i).toString().toLowerCase();
                            if (value.contains(searchText.toLowerCase())) {
                                matchFound = true;
                                break;
                            }
                        }
                        return matchFound;
                    }

                    return true;
                }
            };

            sorter.setRowFilter(filter);
        }
    }

    /**
     * Opens the CSV import dialog for importing transactions.
     */
    private void openCSVImportDialog() {
        CSVImportDialog dialog = new CSVImportDialog(SwingUtilities.getWindowAncestor(this), this, null);
        dialog.setVisible(true);
    }

    /**
     * Adds transactions imported from CSV to the table and marks as unsaved.
     *
     * @param importedTransactions the list of imported transactions
     */
    public void addTransactionsFromCSV(List<Object[]> importedTransactions) {
        if (importedTransactions == null || importedTransactions.isEmpty()) {
            return;
        }

        // Add to UI table
        for (Object[] transaction : importedTransactions) {
            tableModel.addRow(transaction);
        }

        setHasUnsavedChanges(true);
    }

    /**
     * Adds a new empty transaction row to the table.
     */
    private void addNewTransaction() {
        // Create empty transaction with today's date
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Object[] rowData = {dateStr, "", "", 0.0, false};

        // Add to table
        tableModel.insertRow(0, rowData);

        setHasUnsavedChanges(true);
    }

    /**
     * Deletes selected transactions (checked rows) from the table.
     */
    private void deleteSelectedTransactions() {
        // Collect indices of rows to remove (in reverse order to avoid index shifting problems)
        List<Integer> rowsToRemove = new ArrayList<>();

        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            Boolean isChecked = (Boolean) tableModel.getValueAt(i, 4);
            if (isChecked != null && isChecked) {
                rowsToRemove.add(i);
            }
        }

        // Remove the rows
        for (int row : rowsToRemove) {
            tableModel.removeRow(row);
        }

        if (!rowsToRemove.isEmpty()) {
            setHasUnsavedChanges(true);
        }
    }

    /**
     * Placeholder for AI assistant integration.
     */
    private void askAI() {
        JOptionPane.showMessageDialog(this,
                "AI assistant would be available here to help with your transactions.",
                "Ask AI", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Saves changes made to the transactions table through the ViewModel.
     */
    private void saveChanges() {
        try {
            // Collect all table data to save
            List<Object[]> transactionsToSave = new ArrayList<>();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String date = tableModel.getValueAt(i, 0).toString();
                String description = tableModel.getValueAt(i, 1).toString();
                String category = tableModel.getValueAt(i, 2).toString();
                Double amount = (Double) tableModel.getValueAt(i, 3);

                // Create transaction data array (matches CSV format)
                Object[] transactionData = {date, description, category, amount, false};
                transactionsToSave.add(transactionData);
            }

            // Save transactions through ViewModel
            boolean success = viewModel.saveTransactions(transactionsToSave);

            if (success) {
                // Store as original data for cancellation
                originalTransactions = new ArrayList<>(transactionsToSave);

                // Show success message
                JOptionPane.showMessageDialog(this,
                        "Changes saved successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                setHasUnsavedChanges(false);
            } else {
                throw new Exception("Failed to save transactions");
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to save transactions", ex);
            JOptionPane.showMessageDialog(this,
                    "Error saving transactions: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Cancels unsaved changes and restores the table to the last saved state.
     */
    private void cancelChanges() {
        // Reset the table with original data from ViewModel
        onTransactionsChanged();
        setHasUnsavedChanges(false);
    }

    /**
     * Sets the unsaved changes flag and updates button states.
     *
     * @param hasChanges true if there are unsaved changes, false otherwise
     */
    private void setHasUnsavedChanges(boolean hasChanges) {
        this.hasUnsavedChanges = hasChanges;
        saveButton.setEnabled(hasChanges);
        cancelButton.setEnabled(hasChanges);
    }

    /**
     * Called when transactions data changes in the ViewModel.
     * Updates the table and category filter.
     */
    @Override
    public void onTransactionsChanged() {
        SwingUtilities.invokeLater(() -> {
            List<Object[]> currentTransactions = viewModel.getTransactions();
            LOGGER.log(Level.INFO, "Updating UI with {0} transactions", currentTransactions.size());

            // Store for cancellation
            originalTransactions = new ArrayList<>(currentTransactions);

            // Clear and rebuild the table
            tableModel.setRowCount(0);

            // Rebuild category filter
            categoryFilterComboBox.removeAllItems();
            categoryFilterComboBox.addItem(""); // Empty option for "show all"

            Set<String> categories = viewModel.getCategories();
            for (String category : categories) {
                categoryFilterComboBox.addItem(category);
            }

            // Add transactions to table
            for (Object[] transaction : currentTransactions) {
                if (transaction.length >= 4) {
                    String date = transaction[0].toString();
                    String description = transaction[1].toString();
                    String category = transaction[2].toString();
                    Double amount = 0.0;

                    try {
                        if (transaction[3] instanceof Double) {
                            amount = (Double) transaction[3];
                        } else {
                            amount = Double.parseDouble(transaction[3].toString());
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.WARNING, "Invalid amount format: " + transaction[3], e);
                    }

                    // Add to table (with checkbox column set to false)
                    tableModel.addRow(new Object[] {date, description, category, amount, false});
                }
            }

            // Reset unsaved changes flag
            setHasUnsavedChanges(false);
        });
    }

    /**
     * Called when this panel is removed from its container.
     * Cleans up listeners and resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed from UI
        viewModel.removeTransactionChangeListener(this);
        viewModel.cleanup();
    }
}