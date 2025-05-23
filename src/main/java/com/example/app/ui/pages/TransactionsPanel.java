package com.example.app.ui.pages;

import com.example.app.model.FinanceData;
import com.example.app.ui.dialogs.CSVImportDialog;

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

public class TransactionsPanel extends JPanel {
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilterComboBox;
    private FinanceData financeData;
    private JButton addButton, deleteButton, saveButton, cancelButton, askAIButton;
    private boolean hasUnsavedChanges = false;
    
    public TransactionsPanel() {
        financeData = new FinanceData();
        setLayout(new BorderLayout());
        
        // Create the title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Transactions", JLabel.LEFT);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 0));
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Create search and filter panel
        JPanel searchPanel = createSearchFilterPanel();
        titlePanel.add(searchPanel, BorderLayout.EAST);
        
        add(titlePanel, BorderLayout.NORTH);
        
        // Create transactions table
        createTransactionsTable();
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with buttons
        JPanel bottomPanel = createButtonPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 15));
        
        // Search field
        JLabel searchLabel = new JLabel("Search: ");
        searchField = new JTextField(15);
        searchField.addActionListener(e -> applyFilters());
        
        // Category filter
        JLabel categoryLabel = new JLabel("Category: ");
        categoryFilterComboBox = new JComboBox<>();
        categoryFilterComboBox.setEditable(true);
        
        // Add empty option as default (show all)
        categoryFilterComboBox.addItem("");
        
        // Add categories from finance data
        Set<String> categories = financeData.getCategoryBudgets().keySet();
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
        
        // Add sample data from FinanceData
        populateTableWithSampleData();
        
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
    
    private void populateTableWithSampleData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<LocalDate, Double> expenses = financeData.getDailyExpenses();
        Map<LocalDate, Double> incomes = financeData.getDailyIncomes();
        
        // Process and add all dates
        List<LocalDate> dates = financeData.getDates();
        
        for (LocalDate date : dates) {
            Double expense = expenses.get(date);
            Double income = incomes.get(date);
            
            // Format date
            String dateStr = date.format(formatter);
            
            // Add expense transaction
            if (expense != null && expense > 0) {
                String category = financeData.getExpenseCategory(date);
                String description = financeData.getExpenseDescription(date);
                
                // Add negative expense
                Object[] rowData = {dateStr, description, category, -expense, false};
                tableModel.addRow(rowData);
            }
            
            // Add income transaction
            if (income != null && income > 0) {
                // For incomes, use "Income" category
                String description = financeData.getIncomeDescription(date);
                
                // Add positive income
                Object[] rowData = {dateStr, description, "Income", income, false};
                tableModel.addRow(rowData);
            }
        }
    }
    
    private String getRandomCategory(Set<String> categories, Random random) {
        List<String> categoryList = new ArrayList<>(categories);
        return categoryList.get(random.nextInt(categoryList.size()));
    }
    
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
    
    private void openCSVImportDialog() {
        CSVImportDialog dialog = new CSVImportDialog(SwingUtilities.getWindowAncestor(this), this);
        dialog.setVisible(true);
    }

    // Method to add transactions from CSV import
    public void addTransactionsFromCSV(List<Object[]> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }
        
        for (Object[] transaction : transactions) {
            tableModel.insertRow(0, transaction);
        }
        
        setHasUnsavedChanges(true);
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedCategory = (String) categoryFilterComboBox.getSelectedItem();
        
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) transactionsTable.getRowSorter();
        
        // If both filters are empty, show all rows
        if (searchText.isEmpty() && (selectedCategory == null || selectedCategory.isEmpty())) {
            sorter.setRowFilter(null);
            return;
        }
        
        // Create a filter that checks both the search text and category
        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int row = entry.getIdentifier();
                
                // Check category filter if selected
                if (selectedCategory != null && !selectedCategory.isEmpty()) {
                    String rowCategory = (String) entry.getModel().getValueAt(row, 2);
                    if (!rowCategory.equals(selectedCategory)) {
                        return false;
                    }
                }
                
                // Check search text if not empty
                if (!searchText.isEmpty()) {
                    boolean matchFound = false;
                    // Check columns 0, 1, 2 (Date, Description, Category)
                    for (int i = 0; i < 3; i++) {
                        String value = entry.getModel().getValueAt(row, i).toString().toLowerCase();
                        if (value.contains(searchText)) {
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
    
    private void addNewTransaction() {
        // Create a sample transaction
        LocalDate today = LocalDate.now();
        Random random = new Random();
        
        // Randomly choose if it's income or expense
        boolean isIncome = random.nextBoolean();
        
        String description = isIncome ? 
                             financeData.getRandomIncomeDescription(random) : 
                             financeData.getRandomExpenseDescription(random);
        String category = isIncome ? "Income" : getRandomCategory(financeData.getCategoryExpenses().keySet(), random);
        double amount = isIncome ? 
                        100 + random.nextDouble() * 400 : 
                        -(50 + random.nextDouble() * 200);
        
        // Add new row at the top of the table
        Object[] rowData = {today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 
                           description, category, amount, false};
        tableModel.insertRow(0, rowData);
        
        // Mark that we have unsaved changes
        setHasUnsavedChanges(true);
    }
    
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
    
    private void askAI() {
        // No implementation needed as per requirements
        JOptionPane.showMessageDialog(this, 
                "AI assistant would be available here to help with your transactions.", 
                "Ask AI", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void saveChanges() {
        // In a real application, you would save to a database or file
        // For this demonstration, we'll just show the changes that would be saved
        
        StringBuilder savedChanges = new StringBuilder("Changes saved:\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Collect all table data that would be saved
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String date = tableModel.getValueAt(i, 0).toString();
            String description = tableModel.getValueAt(i, 1).toString();
            String category = tableModel.getValueAt(i, 2).toString();
            Double amount = (Double) tableModel.getValueAt(i, 3);
            
            savedChanges.append(String.format("Row %d: %s | %s | %s | %.2f\n", 
                i+1, date, description, category, amount));
        }
        
        // Here you would typically save to a database or update financeData
        
        JOptionPane.showMessageDialog(this, 
                "Changes Saved", 
                "Changes Saved", JOptionPane.INFORMATION_MESSAGE);
        
        setHasUnsavedChanges(false);
    }
    
    private void cancelChanges() {
        // Reset the table with original data
        tableModel.setRowCount(0);
        populateTableWithSampleData();
        
        setHasUnsavedChanges(false);
    }
    
    private void setHasUnsavedChanges(boolean hasChanges) {
        this.hasUnsavedChanges = hasChanges;
        saveButton.setEnabled(hasChanges);
        cancelButton.setEnabled(hasChanges);
    }
}