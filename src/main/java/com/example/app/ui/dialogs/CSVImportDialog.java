package com.example.app.ui.dialogs;

import com.example.app.ui.pages.TransactionsPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class CSVImportDialog extends JDialog {
    private TransactionsPanel parentPanel;
    private JTable previewTable;
    private DefaultTableModel previewTableModel;
    private List<String> csvHeaders;
    private List<List<String>> csvData;
    
    // Combo boxes for mapping CSV columns to transaction attributes
    private JComboBox<String> dateColumnCombo;
    private JComboBox<String> descriptionColumnCombo;
    private JComboBox<String> categoryColumnCombo;
    private JComboBox<String> amountColumnCombo;
    private JComboBox<String> typeColumnCombo; // New: Transaction type column
    
    // Text fields for identifying income vs expense
    private JTextField incomeIdentifierField;
    private JTextField expenseIdentifierField;
    
    // Checkbox to use transaction type column
    private JCheckBox useTypeColumnCheckBox;
    
    // Date format for parsing
    private JComboBox<String> dateFormatCombo;
    
    // Store a reference to the record count label
    private JLabel recordCountLabel;

    // Add these fields to the class
    private JComboBox<String> templateComboBox;
    private boolean applyingTemplate = false; // Flag to prevent resetting when applying template
    
    public CSVImportDialog(Window owner, TransactionsPanel parentPanel) {
        super(owner, "Import Transactions from CSV", ModalityType.APPLICATION_MODAL);
        this.parentPanel = parentPanel;
        
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
    
    // Modify the createMappingPanel method to add template selection
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
        
        // Date format
        mappingGrid.add(new JLabel("Date/Time Format:"));
        dateFormatCombo = new JComboBox<>(new String[] {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss",
            "MM/dd/yyyy", "MM/dd/yyyy HH:mm:ss", 
            "dd/MM/yyyy", "dd/MM/yyyy HH:mm:ss",
            "MM-dd-yyyy", "MM-dd-yyyy HH:mm:ss",
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
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
    
    // Helper method to parse comma-separated identifiers into a set
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
    
    // Helper method to check if a value matches any of the identifiers
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
    
    private int getSelectedIndex(JComboBox<String> comboBox) {
        String selected = (String) comboBox.getSelectedItem();
        if (selected == null || selected.isEmpty()) {
            return -1;
        }
        return csvHeaders.indexOf(selected);
    }
    
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
        
        for (List<String> rowData : csvData) {
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
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (DateTimeParseException e) {
                // Skip this row if date parsing fails
                continue;
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
                    // If no match, keep as is
                }
            } catch (NumberFormatException e) {
                // Skip this row if amount parsing fails
                continue;
            }
            
            // Create transaction row
            Object[] transaction = {formattedDate, description, category, amount, false};
            transactions.add(transaction);
        }
        
        // Import the transactions into the main panel
        parentPanel.addTransactionsFromCSV(transactions);
        
        // Show success message
        JOptionPane.showMessageDialog(this, 
            transactions.size() + " transactions imported successfully.", 
            "Import Complete", JOptionPane.INFORMATION_MESSAGE);
        
        // Close the dialog
        dispose();
    }

    // Add this method for template handling
    private void applyTemplate(String templateName) {
        applyingTemplate = true;
        
        try {
            switch (templateName) {
                case "WeChat Pay":
                    // Date settings
                    dateFormatCombo.setSelectedItem("yyyy-MM-dd HH:mm:ss");
                    
                    // Column mappings
                    setComboBoxItem(dateColumnCombo, "交易时间");
                    setComboBoxItem(descriptionColumnCombo, "商品");
                    categoryColumnCombo.setSelectedIndex(0); // Default/empty
                    setComboBoxItem(amountColumnCombo, "金额(元)");
                    
                    // Transaction type settings
                    useTypeColumnCheckBox.setSelected(true);
                    setComboBoxItem(typeColumnCombo, "收/支");
                    typeColumnCombo.setEnabled(true);
                    
                    // Set identifiers
                    incomeIdentifierField.setText("收入");
                    expenseIdentifierField.setText("支出");
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
    
    // Helper method to set combo box item by text
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
}