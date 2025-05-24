package com.example.app.ui.dialogs;

import com.example.app.ui.pages.TransactionsPanel;
import com.example.app.user_data.UserBillStorage;
import com.example.app.model.FinanceData; // Import added

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A dialog for importing financial transactions from CSV files.
 * This dialog allows users to map CSV columns to transaction fields,
 * preview the data, and import it into the application.
 * <p>
 * Features:
 * <ul>
 *   <li>CSV file selection</li>
 *   <li>Column mapping</li>
 *   <li>Template selection for common CSV formats</li>
 *   <li>Date format specification</li>
 *   <li>Transaction type identification</li>
 *   <li>Data preview</li>
 *   <li>Error handling</li>
 * </ul>
 
 */
public class CSVImportDialog extends JDialog {
    /** Reference to the parent transactions panel */
    private TransactionsPanel parentPanel;
    
    /** Table for displaying transaction previews */
    private JTable previewTable;
    
    /** Table model for the preview table */
    private DefaultTableModel previewTableModel;
    
    /** List of CSV column headers */
    private List<String> csvHeaders;
    
    /** List of CSV data rows */
    private List<List<String>> csvData;
    
    /** Combo box for selecting the date column */
    private JComboBox<String> dateColumnCombo;
    
    /** Combo box for selecting the description column */
    private JComboBox<String> descriptionColumnCombo;
    
    /** Combo box for selecting the category column */
    private JComboBox<String> categoryColumnCombo;
    
    /** Combo box for selecting the amount column */
    private JComboBox<String> amountColumnCombo;
    
    /** Combo box for selecting the transaction type column */
    private JComboBox<String> typeColumnCombo;
    
    /** Text field for specifying income identifiers */
    private JTextField incomeIdentifierField;
    
    /** Text field for specifying expense identifiers */
    private JTextField expenseIdentifierField;
    
    /** Checkbox to enable transaction type column usage */
    private JCheckBox useTypeColumnCheckBox;
    
    /** Combo box for selecting the date format */
    private JComboBox<String> dateFormatCombo;
    
    /** Label displaying the number of records found */
    private JLabel recordCountLabel;

    /** Combo box for selecting CSV templates */
    private JComboBox<String> templateComboBox;
    
    /** Flag to prevent template reset when applying a template */
    private boolean applyingTemplate = false;
    
    /** Reference to the finance data model */
    private FinanceData financeData;

    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(CSVImportDialog.class.getName());
    
    /**
     * Creates a new CSV import dialog.
     *
     * @param owner the owner window of this dialog
     * @param parentPanel the parent transactions panel to receive imported data
     * @param financeData the finance data model to update with imported transactions
     */
    public CSVImportDialog(Window owner, TransactionsPanel parentPanel, FinanceData financeData) {
        super(owner, "Import Transactions from CSV", ModalityType.APPLICATION_MODAL);
        this.parentPanel = parentPanel;
        this.financeData = financeData; // Save reference to FinanceData
        
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        
        // Initialize with empty data until CSV is selected
        csvHeaders = new ArrayList<>();
        csvData = new ArrayList<>();
        
        // Top panel for file selection
        JPanel topPanel = createFileSelectionPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with mapping and preview
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel mappingPanel = createMappingPanel();
        JPanel previewPanel = createPreviewPanel();
        
        centerPanel.add(mappingPanel);
        centerPanel.add(previewPanel);
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with action buttons
        JPanel bottomPanel = createButtonPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the file selection panel with browse button.
     *
     * @return the file selection panel
     */
    private JPanel createFileSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        JLabel fileLabel = new JLabel("CSV File:");
        JTextField filePathField = new JTextField(30);
        filePathField.setEditable(false);
        
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePathField.setText(selectedFile.getAbsolutePath());
                loadCSVFile(selectedFile);
            }
        });
        
        panel.add(fileLabel);
        panel.add(filePathField);
        panel.add(browseButton);
        
        return panel;
    }
    
    /**
     * Creates the column mapping panel with template selection.
     *
     * @return the column mapping panel
     */
    private JPanel createMappingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Column Mapping"));
        
        // Template selection panel at the top
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        templatePanel.add(new JLabel("CSV Template:"));
        templateComboBox = new JComboBox<>(new String[] {
            "Custom", "WeChat Pay"
            // More templates can be added here
        });
        templateComboBox.addActionListener(e -> {
            if (!applyingTemplate) {
                String selectedTemplate = (String) templateComboBox.getSelectedItem();
                if (selectedTemplate != null && !selectedTemplate.equals("Custom")) {
                    applyTemplate(selectedTemplate);
                }
            }
        });
        templatePanel.add(templateComboBox);
        
        // Using BoxLayout for main panel to ensure components stack vertically
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add template panel to the main panel
        mainPanel.add(templatePanel);
        
        // Add spacing after template panel
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Calculate additional rows for transaction type settings
        JPanel mappingGrid = new JPanel(new GridLayout(7, 2, 5, 10));
        
        // Date column mapping
        mappingGrid.add(new JLabel("Date Column:"));
        dateColumnCombo = new JComboBox<>();
        mappingGrid.add(dateColumnCombo);
        
        // Date format - Add more date format options including formats with slashes
        mappingGrid.add(new JLabel("Date/Time Format:"));
        dateFormatCombo = new JComboBox<>(new String[] {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm", "yyyy/MM/dd HH:mm:ss", 
            "MM/dd/yyyy", "MM/dd/yyyy HH:mm", "MM/dd/yyyy HH:mm:ss", 
            "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM/yyyy HH:mm:ss",
            "MM-dd-yyyy", "MM-dd-yyyy HH:mm", "MM-dd-yyyy HH:mm:ss",
            "dd-MM-yyyy", "dd-MM-yyyy HH:mm:ss"
        });
        dateFormatCombo.setEditable(true);
        dateFormatCombo.addActionListener(e -> {
            updatePreview();
            if (!applyingTemplate) templateComboBox.setSelectedItem("Custom");
        });
        mappingGrid.add(dateFormatCombo);
        
        // Description column mapping
        mappingGrid.add(new JLabel("Description Column:"));
        descriptionColumnCombo = new JComboBox<>();
        descriptionColumnCombo.addActionListener(e -> {
            if (!applyingTemplate) templateComboBox.setSelectedItem("Custom");
            updatePreview();
        });
        mappingGrid.add(descriptionColumnCombo);
        
        // Category column mapping
        mappingGrid.add(new JLabel("Category Column:"));
        categoryColumnCombo = new JComboBox<>();
        categoryColumnCombo.addActionListener(e -> {
            if (!applyingTemplate) templateComboBox.setSelectedItem("Custom");
            updatePreview();
        });
        mappingGrid.add(categoryColumnCombo);
        
        // Amount column mapping
        mappingGrid.add(new JLabel("Amount Column:"));
        amountColumnCombo = new JComboBox<>();
        amountColumnCombo.addActionListener(e -> {
            if (!applyingTemplate) templateComboBox.setSelectedItem("Custom");
            updatePreview();
        });
        mappingGrid.add(amountColumnCombo);
        
        // Transaction type column (new)
        mappingGrid.add(new JLabel("Transaction Type Column:"));
        typeColumnCombo = new JComboBox<>();
        typeColumnCombo.addActionListener(e -> {
            if (!applyingTemplate) templateComboBox.setSelectedItem("Custom");
            updatePreview();
        });
        mappingGrid.add(typeColumnCombo);
        
        // Use transaction type column checkbox
        mappingGrid.add(new JLabel("Use Type Column:"));
        useTypeColumnCheckBox = new JCheckBox("All amounts are positive");
        useTypeColumnCheckBox.addActionListener(e -> {
            boolean enabled = useTypeColumnCheckBox.isSelected();
            typeColumnCombo.setEnabled(enabled);
            incomeIdentifierField.setEnabled(enabled);
            expenseIdentifierField.setEnabled(enabled);
            if (!applyingTemplate) templateComboBox.setSelectedItem("Custom");
            updatePreview();
        });
        mappingGrid.add(useTypeColumnCheckBox);
        
        // Add mapping grid to main panel
        mainPanel.add(mappingGrid);
        
        // Add some spacing between the grid and the identifiers
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Income/Expense identifiers panel
        JPanel identifiersPanel = new JPanel(new GridLayout(1, 4, 5, 0));
        identifiersPanel.add(new JLabel("Income:"));
        incomeIdentifierField = new JTextField("Income,Revenue,Deposit");
        incomeIdentifierField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTemplate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTemplate(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTemplate(); }
            private void updateTemplate() {
                if (!applyingTemplate) {
                    templateComboBox.setSelectedItem("Custom");
                    updatePreview();
                }
            }
        });
        identifiersPanel.add(incomeIdentifierField);
        identifiersPanel.add(new JLabel("Expense:"));
        expenseIdentifierField = new JTextField("Expense,Withdrawal,Debit");
        expenseIdentifierField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateTemplate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateTemplate(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateTemplate(); }
            private void updateTemplate() {
                if (!applyingTemplate) {
                    templateComboBox.setSelectedItem("Custom");
                    updatePreview();
                }
            }
        });
        identifiersPanel.add(expenseIdentifierField);
        
        // Add identifiers panel to main panel
        mainPanel.add(identifiersPanel);
        
        // Initially disable transaction type related fields
        typeColumnCombo.setEnabled(false);
        incomeIdentifierField.setEnabled(false);
        expenseIdentifierField.setEnabled(false);
        
        panel.add(mainPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * Creates the transaction preview panel with a table display.
     *
     * @return the preview panel
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Transaction Preview"));
        
        // Create preview table with same columns as transaction table
        String[] columns = {"Date", "Description", "Category", "Amount", "Delete"};
        previewTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return Boolean.class; // Checkbox column
                if (column == 3) return Double.class;  // Amount column
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the delete checkbox is editable
            }
        };
        
        previewTable = new JTable(previewTableModel);
        previewTable.setRowHeight(30);
        previewTable.setShowGrid(true);
        previewTable.setGridColor(Color.LIGHT_GRAY);
        
        JScrollPane scrollPane = new JScrollPane(previewTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Add a label indicating the number of records
        recordCountLabel = new JLabel("0 records found");
        recordCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(recordCountLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the button panel with cancel and import buttons.
     *
     * @return the button panel
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JButton cancelButton = new JButton("Cancel");
        JButton importButton = new JButton("Import");
        
        cancelButton.addActionListener(e -> dispose());
        importButton.addActionListener(e -> importTransactions());
        
        panel.add(cancelButton);
        panel.add(importButton);
        
        return panel;
    }
    
    /**
     * Loads and parses a CSV file.
     * Extracts headers and data rows, then updates the UI.
     *
     * @param file the CSV file to load
     */
    private void loadCSVFile(File file) {
        csvHeaders = new ArrayList<>();
        csvData = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                // Parse headers
                csvHeaders = parseCSVLine(line);
                
                // Update combo boxes with headers
                updateComboBoxes();
                
                // Parse data rows
                while ((line = reader.readLine()) != null) {
                    List<String> rowData = parseCSVLine(line);
                    if (rowData.size() == csvHeaders.size()) {
                        csvData.add(rowData);
                    }
                }
                
                // Update preview
                updatePreview();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "Error reading CSV file: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Parses a CSV line into a list of fields.
     * Handles quoted fields and commas within quotes.
     *
     * @param line the CSV line to parse
     * @return a list of fields extracted from the line
     */
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        // Add the last field
        result.add(currentField.toString().trim());
        
        return result;
    }
    
    /**
     * Updates the combo boxes with CSV headers.
     * Attempts to make intelligent selections based on header names.
     */
    private void updateComboBoxes() {
        // Clear existing items
        dateColumnCombo.removeAllItems();
        descriptionColumnCombo.removeAllItems();
        categoryColumnCombo.removeAllItems();
        amountColumnCombo.removeAllItems();
        typeColumnCombo.removeAllItems();
        
        // Add empty option
        dateColumnCombo.addItem("");
        descriptionColumnCombo.addItem("");
        categoryColumnCombo.addItem("");
        amountColumnCombo.addItem("");
        typeColumnCombo.addItem("");
        
        // Add headers to combo boxes
        for (String header : csvHeaders) {
            dateColumnCombo.addItem(header);
            descriptionColumnCombo.addItem(header);
            categoryColumnCombo.addItem(header);
            amountColumnCombo.addItem(header);
            typeColumnCombo.addItem(header);
        }
        
        // Try to make intelligent default selections based on header names
        for (String header : csvHeaders) {
            String headerLower = header.toLowerCase();
            
            if (headerLower.contains("date")) {
                dateColumnCombo.setSelectedItem(header);
            } else if (headerLower.contains("desc") || headerLower.contains("memo") || 
                       headerLower.contains("narration")) {
                descriptionColumnCombo.setSelectedItem(header);
            } else if (headerLower.contains("categ") || headerLower.contains("type")) {
                categoryColumnCombo.setSelectedItem(header);
            } else if (headerLower.contains("amount") || headerLower.contains("sum") || 
                       headerLower.contains("value")) {
                amountColumnCombo.setSelectedItem(header);
            } else if (headerLower.contains("type") || headerLower.contains("direction") || 
                      headerLower.contains("flow") || headerLower.contains("inout")) {
                typeColumnCombo.setSelectedItem(header);
                useTypeColumnCheckBox.setSelected(true);
                typeColumnCombo.setEnabled(true);
                incomeIdentifierField.setEnabled(true);
                expenseIdentifierField.setEnabled(true);
            }
        }
    }
    
    /**
     * Updates the transaction preview table with parsed data.
     * Demonstrates how transactions will be imported based on current settings.
     */
    private void updatePreview() {
        // Clear existing preview data
        previewTableModel.setRowCount(0);
        
        if (csvData.isEmpty()) {
            return;
        }
        
        // Get selected column indices
        int dateColIdx = getSelectedIndex(dateColumnCombo);
        int descColIdx = getSelectedIndex(descriptionColumnCombo);
        int catColIdx = getSelectedIndex(categoryColumnCombo);
        int amountColIdx = getSelectedIndex(amountColumnCombo);
        int typeColIdx = getSelectedIndex(typeColumnCombo);
        
        // Get transaction type settings
        boolean useTypeColumn = useTypeColumnCheckBox.isSelected();
        Set<String> incomeIdentifiers = parseIdentifiers(incomeIdentifierField.getText());
        Set<String> expenseIdentifiers = parseIdentifiers(expenseIdentifierField.getText());
        
        String dateFormat = (String) dateFormatCombo.getSelectedItem();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        
        // Add preview rows (max 10 for performance)
        int rowCount = Math.min(csvData.size(), 10);
        for (int i = 0; i < rowCount; i++) {
            List<String> rowData = csvData.get(i);
            
            String dateStr = (dateColIdx >= 0 && dateColIdx < rowData.size()) ? 
                             rowData.get(dateColIdx) : "";
            String description = (descColIdx >= 0 && descColIdx < rowData.size()) ? 
                                rowData.get(descColIdx) : "";
            String category = (catColIdx >= 0 && catColIdx < rowData.size()) ? 
                              rowData.get(catColIdx) : "Uncategorised";
            String amountStr = (amountColIdx >= 0 && amountColIdx < rowData.size()) ? 
                              rowData.get(amountColIdx) : "0.0";
            
            // Try to parse date
            String formattedDate = dateStr;
            try {
                if (!dateStr.isEmpty()) {
                    formattedDate = parseDate(dateStr, dateFormat);
                }
            } catch (DateTimeParseException e) {
                // Keep original string if parsing fails
            }
            
            // Try to parse amount and apply transaction type if needed
            double amount = 0.0;
            try {
                // Remove any currency symbols and commas
                String cleanAmount = amountStr.replaceAll("[^\\d.-]", "");
                amount = Double.parseDouble(cleanAmount);
                
                // Apply transaction type if enabled
                if (useTypeColumn && typeColIdx >= 0 && typeColIdx < rowData.size()) {
                    String typeValue = rowData.get(typeColIdx).trim();
                    
                    // Check if this is an expense based on identifiers
                    if (matchesAnyIdentifier(typeValue, expenseIdentifiers)) {
                        amount = -Math.abs(amount); // Make negative
                    } 
                    // Check if this is income based on identifiers
                    else if (matchesAnyIdentifier(typeValue, incomeIdentifiers)) {
                        amount = Math.abs(amount); // Make positive
                    }
                    // If no match, keep as is (could add warning)
                }
            } catch (NumberFormatException e) {
                // Use 0.0 if parsing fails
            }
            
            // Add to preview table
            Object[] tableRow = {formattedDate, description, category, amount, false};
            previewTableModel.addRow(tableRow);
        }
        
        // Update record count label using the class field
        recordCountLabel.setText(csvData.size() + " records found, showing " + rowCount);
    }
    
    /**
     * Parses a comma-separated string of identifiers into a set.
     *
     * @param identifiersString comma-separated list of identifiers
     * @return a set of lowercase identifiers
     */
    private Set<String> parseIdentifiers(String identifiersString) {
        Set<String> result = new HashSet<>();
        if (identifiersString == null || identifiersString.trim().isEmpty()) {
            return result;
        }
        
        String[] parts = identifiersString.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed.toLowerCase());
            }
        }
        return result;
    }
    
    /**
     * Checks if a value matches any of the provided identifiers.
     * Performs both exact and partial matching.
     *
     * @param value the value to check
     * @param identifiers the set of identifiers to match against
     * @return true if the value matches any identifier, false otherwise
     */
    private boolean matchesAnyIdentifier(String value, Set<String> identifiers) {
        if (value == null || value.isEmpty() || identifiers.isEmpty()) {
            return false;
        }
        
        String lowerValue = value.toLowerCase();
        
        // Direct match
        if (identifiers.contains(lowerValue)) {
            return true;
        }
        
        // Partial match (if value contains any identifier)
        for (String identifier : identifiers) {
            if (lowerValue.contains(identifier)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets the index of the selected column in the CSV headers.
     *
     * @param comboBox the combo box containing the selection
     * @return the index of the selected column, or -1 if none selected
     */
    private int getSelectedIndex(JComboBox<String> comboBox) {
        String selected = (String) comboBox.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            return -1;
        }
        return csvHeaders.indexOf(selected);
    }
    
    /**
     * Imports the transactions from the CSV file based on current settings.
     * Validates data, handles errors, and updates the application with new transactions.
     */
    private void importTransactions() {
        // Check if we have data to import
        if (csvData.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No data to import. Please load a CSV file first.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get selected column indices
        int dateColIdx = getSelectedIndex(dateColumnCombo);
        int descColIdx = getSelectedIndex(descriptionColumnCombo);
        int catColIdx = getSelectedIndex(categoryColumnCombo);
        int amountColIdx = getSelectedIndex(amountColumnCombo);
        int typeColIdx = getSelectedIndex(typeColumnCombo);
        
        // Check if required columns are selected
        if (dateColIdx < 0 || amountColIdx < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select at least Date and Amount columns.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check for transaction type column if it's enabled
        boolean useTypeColumn = useTypeColumnCheckBox.isSelected();
        if (useTypeColumn && typeColIdx < 0) {
            JOptionPane.showMessageDialog(this, 
                "Please select a Transaction Type column or disable its use.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Get transaction type settings
        Set<String> incomeIdentifiers = parseIdentifiers(incomeIdentifierField.getText());
        Set<String> expenseIdentifiers = parseIdentifiers(expenseIdentifierField.getText());
        
        String dateFormat = (String) dateFormatCombo.getSelectedItem();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        
        List<Object[]> transactions = new ArrayList<>();
        int skippedRows = 0;
        StringBuilder errorMessages = new StringBuilder();
        
        for (int rowIndex = 0; rowIndex < csvData.size(); rowIndex++) {
            List<String> rowData = csvData.get(rowIndex);
            
            String dateStr = (dateColIdx >= 0 && dateColIdx < rowData.size()) ? 
                             rowData.get(dateColIdx) : "";
            String description = (descColIdx >= 0 && descColIdx < rowData.size()) ? 
                                rowData.get(descColIdx) : "";
            String category = (catColIdx >= 0 && catColIdx < rowData.size()) ? 
                              rowData.get(catColIdx) : "Uncategorised";
            String amountStr = (amountColIdx >= 0 && amountColIdx < rowData.size()) ? 
                              rowData.get(amountColIdx) : "0.0";
            
            // Try to parse date
            String formattedDate = "";
            try {
                if (!dateStr.isEmpty()) {
                    formattedDate = parseDate(dateStr, dateFormat);
                } else {
                    errorMessages.append("Row ").append(rowIndex + 1).append(": Empty date field\n");
                    skippedRows++;
                    continue;
                }
            } catch (DateTimeParseException e) {
                // Log error and skip rows with parsing failures
                errorMessages.append("Row ").append(rowIndex + 1)
                              .append(": Failed to parse date '").append(dateStr)
                              .append("' using format '").append(dateFormat).append("' - ")
                              .append(e.getMessage()).append("\n");
                skippedRows++;
                continue;
            }
            
            // Try to parse amount and apply transaction type if needed
            double amount = 0.0;
            try {
                // Remove any currency symbols and commas
                String cleanAmount = amountStr.replaceAll("[^\\d.-]", "");
                if (cleanAmount.isEmpty()) {
                    errorMessages.append("Row ").append(rowIndex + 1).append(": Empty amount field\n");
                    skippedRows++;
                    continue;
                }
                amount = Double.parseDouble(cleanAmount);
                
                // Apply transaction type if enabled
                if (useTypeColumn && typeColIdx >= 0 && typeColIdx < rowData.size()) {
                    String typeValue = rowData.get(typeColIdx).trim();
                    
                    // Check if this is an expense based on identifiers
                    if (matchesAnyIdentifier(typeValue, expenseIdentifiers)) {
                        amount = -Math.abs(amount); // Make negative
                    } 
                    // Check if this is income based on identifiers
                    else if (matchesAnyIdentifier(typeValue, incomeIdentifiers)) {
                        amount = Math.abs(amount); // Make positive
                    } else {
                        // Neither income nor expense identifier matched
                        errorMessages.append("Row ").append(rowIndex + 1)
                                  .append(": Could not determine transaction type from '")
                                  .append(typeValue).append("'\n");
                    }
                }
            } catch (NumberFormatException e) {
                // Log error and skip row if amount parsing fails
                errorMessages.append("Row ").append(rowIndex + 1)
                              .append(": Failed to parse amount '").append(amountStr).append("'\n");
                skippedRows++;
                continue;
            }
            
            // Create transaction row
            Object[] transaction = {formattedDate, description, category, amount, false};
            transactions.add(transaction);
            LOGGER.log(Level.INFO, "Parsed transaction: {0}", Arrays.toString(transaction));
        }
        
        // Display warning if some rows were skipped
        if (skippedRows > 0) {
            String message = "Warning: " + skippedRows + " of " + csvData.size() + 
                             " rows were skipped due to parsing errors.\n\n";
            if (errorMessages.length() > 0) {
                // Limit the number of error messages to avoid huge dialog
                String errors = errorMessages.toString();
                if (errors.length() > 500) {
                    errors = errors.substring(0, 500) + "...\n(more errors not shown)";
                }
                message += "Errors:\n" + errors;
            }
            
            JOptionPane.showMessageDialog(this, message, 
                "Import Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        // Check if we have any transactions to save
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No valid transactions found to import. Please check your CSV data and column mappings.", 
                "Import Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Save transactions using UserBillStorage
        boolean saveSuccess = UserBillStorage.addTransactions(transactions);
        
        if (!saveSuccess) {
            JOptionPane.showMessageDialog(this, 
                "There was an error saving transactions to storage.", 
                "Save Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Add imported transactions to FinanceData
        financeData.importTransactions(transactions);
        
        // Import the transactions into the main panel
        parentPanel.addTransactionsFromCSV(transactions);
        
        // Show success message
        JOptionPane.showMessageDialog(this, 
            transactions.size() + " transactions imported successfully and saved to user_bill.csv", 
            "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        
        // Close the dialog
        dispose();
    }

    /**
     * Applies a predefined template for common CSV formats.
     * Sets column mappings, date format, and transaction type settings.
     *
     * @param templateName the name of the template to apply
     */
    private void applyTemplate(String templateName) {
        applyingTemplate = true;
        
        try {
            switch (templateName) {
                case "WeChat Pay":
                    // Date settings
                    dateFormatCombo.setSelectedItem("yyyy/M/d HH:mm"); // Modified to match format like 2025/4/14 12:19
                    
                    // Column mappings
                    setComboBoxItem(dateColumnCombo, "Transaction Time");
                    setComboBoxItem(descriptionColumnCombo, "Product");
                    categoryColumnCombo.setSelectedIndex(0); // Default/empty
                    setComboBoxItem(amountColumnCombo, "Amount");
                    
                    // Transaction type settings
                    useTypeColumnCheckBox.setSelected(true);
                    setComboBoxItem(typeColumnCombo, "Income/Expense");
                    typeColumnCombo.setEnabled(true);
                    
                    // Set identifiers
                    incomeIdentifierField.setText("Income");
                    expenseIdentifierField.setText("Expense");
                    incomeIdentifierField.setEnabled(true);
                    expenseIdentifierField.setEnabled(true);
                    break;
                    
                // More templates can be added in the future
                default:
                    // Reset to defaults
                    dateFormatCombo.setSelectedIndex(0);
                    dateColumnCombo.setSelectedIndex(0);
                    descriptionColumnCombo.setSelectedIndex(0);
                    categoryColumnCombo.setSelectedIndex(0);
                    amountColumnCombo.setSelectedIndex(0);
                    typeColumnCombo.setSelectedIndex(0);
                    useTypeColumnCheckBox.setSelected(false);
                    typeColumnCombo.setEnabled(false);
                    incomeIdentifierField.setText("Income,Revenue,Deposit");
                    expenseIdentifierField.setText("Expense,Withdrawal,Debit");
                    incomeIdentifierField.setEnabled(false);
                    expenseIdentifierField.setEnabled(false);
                    break;
            }
            
            // Update the preview with new settings
            updatePreview();
        } finally {
            applyingTemplate = false;
        }
    }
    
    /**
     * Sets a combo box selection based on text content.
     * Tries exact match first, then partial match.
     *
     * @param comboBox the combo box to set
     * @param text the text to match
     */
    private void setComboBoxItem(JComboBox<String> comboBox, String text) {
        // First try exact match
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (text.equals(comboBox.getItemAt(i))) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        
        // If no exact match, try to find an item that contains the text
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            String itemText = comboBox.getItemAt(i);
            if (!itemText.isEmpty() && itemText.toLowerCase().contains(text.toLowerCase())) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
        
        // If still not found, leave as is
    }

    /**
     * Parses a date string using multiple formats.
     * Tries the primary format first, then falls back to other formats if needed.
     *
     * @param dateStr the date string to parse
     * @param primaryFormat the primary date format to try first
     * @return a formatted date string in 'yyyy-MM-dd' format
     * @throws DateTimeParseException if the date cannot be parsed with any format
     */
    private String parseDate(String dateStr, String primaryFormat) {
        if (dateStr == null || dateStr.isEmpty()) {
            return "";
        }

        // Try using the primary format first
        try {
            return parseDateWithFormat(dateStr, primaryFormat);
        } catch (DateTimeParseException e) {
            // Try other possible formats
            String[] formatsToTry = {
                // Formats with hyphens
                "yyyy-MM-dd", "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss",
                // Formats with slashes
                "yyyy/MM/dd", "yyyy/M/d HH:mm", "yyyy/M/d HH:mm:ss",
                "yyyy/MM/dd", "yyyy/MM/dd HH:mm", "yyyy/MM/dd HH:mm:ss",
                // US formats
                "MM/dd/yyyy", "MM/dd/yyyy HH:mm", "MM/dd/yyyy HH:mm:ss",
                // European formats
                "dd/MM/yyyy", "dd/MM/yyyy HH:mm", "dd/MM/yyyy HH:mm:ss",
                // Other common formats
                "yyyy.MM.dd", "dd.MM.yyyy", "MM.dd.yyyy"
            };
            
            for (String format : formatsToTry) {
                if (format.equals(primaryFormat)) continue; // Skip format already tried
                
                try {
                    return parseDateWithFormat(dateStr, format);
                } catch (DateTimeParseException ignored) {
                    // Continue to next format
                }
            }
            
            // All formats failed, throw the original exception
            throw e;
        }
    }

    /**
     * Parses a date string with a specific format.
     * Handles both date-only and date-time formats.
     *
     * @param dateStr the date string to parse
     * @param format the format to use for parsing
     * @return a formatted date string in 'yyyy-MM-dd' format
     * @throws DateTimeParseException if the date cannot be parsed with the given format
     */
    private String parseDateWithFormat(String dateStr, String format) throws DateTimeParseException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDate date;
        
        if (format.contains("HH:mm") || format.contains("HH:mm:ss")) {
            // If it's a date-time format, first parse as LocalDateTime then convert to LocalDate
            date = LocalDateTime.parse(dateStr, formatter).toLocalDate();
        } else {
            // Pure date format
            date = LocalDate.parse(dateStr, formatter);
        }
        
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}