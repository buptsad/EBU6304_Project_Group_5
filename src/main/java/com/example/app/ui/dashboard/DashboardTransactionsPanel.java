package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashboardTransactionsPanel extends JPanel implements CurrencyChangeListener {
    private JTable transactionsTable;
    private FinanceData financeData;
    private DefaultTableModel tableModel;
    
    public DashboardTransactionsPanel() {
        financeData = new FinanceData();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel titleLabel = new JLabel("Recent Transactions");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create transactions table
        createTransactionsTable();
        JScrollPane scrollPane = new JScrollPane(transactionsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add button panel at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewAllButton = new JButton("View All Transactions");
        buttonPanel.add(viewAllButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    private void createTransactionsTable() {
        // Define table columns
        String[] columns = {"Date", "Description", "Category", "Amount"};
        
        // Create table model with proper column classes
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 3) return Double.class; // For proper sorting of amounts
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        // Use FinanceData to populate the table
        populateTableWithRecentTransactions(tableModel);
        
        // Create and configure table
        transactionsTable = new JTable(tableModel);
        transactionsTable.setRowHeight(30);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setShowGrid(true);
        transactionsTable.setGridColor(Color.LIGHT_GRAY);
        
        // Set column widths
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Description
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Category
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Amount
        
        // Create a custom renderer to color amounts (red for negative, green for positive)
        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component comp = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                
                if (value instanceof Double) {
                    double amount = (Double) value;
                    if (amount < 0) {
                        comp.setForeground(new Color(220, 50, 50)); // Red for expenses
                    } else {
                        comp.setForeground(new Color(50, 150, 50)); // Green for income
                    }
                    
                    // Format with currency symbol and align right
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
                    setText(String.format("%s%.2f",currencySymbol, amount));
                }
                
                return comp;
            }
        };
        
        // Apply renderer to amount column
        transactionsTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        updateAmountRenderer();
    }

    private void updateAmountRenderer() {
        // 获取货币符号
        final String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Create a custom renderer to color amounts (red for negative, green for positive)
        DefaultTableCellRenderer amountRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component comp = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                
                if (value instanceof Double) {
                    double amount = (Double) value;
                    if (amount < 0) {
                        comp.setForeground(new Color(220, 50, 50)); // Red for expenses
                    } else {
                        comp.setForeground(new Color(50, 150, 50)); // Green for income
                    }
                    
                    // Format with currency symbol and align right
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("%s%.2f", currencySymbol, Math.abs(amount)));
                }
                
                return comp;
            }
        };
        
        // Apply renderer to amount column
        if (transactionsTable != null) {
            transactionsTable.getColumnModel().getColumn(3).setCellRenderer(amountRenderer);
        }
    }
    
    private void populateTableWithRecentTransactions(DefaultTableModel model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<LocalDate, Double> expenses = financeData.getDailyExpenses();
        Map<LocalDate, Double> incomes = financeData.getDailyIncomes();
        
        // Create a combined list of all transaction data
        List<TransactionEntry> allTransactions = new ArrayList<>();
        
        // Get all dates
        List<LocalDate> dates = financeData.getDates();
        
        // Process each date
        for (LocalDate date : dates) {
            Double expense = expenses.get(date);
            Double income = incomes.get(date);
            
            // Add expense transaction
            if (expense != null && expense > 0) {
                String category = financeData.getExpenseCategory(date);
                String description = financeData.getExpenseDescription(date);
                
                allTransactions.add(new TransactionEntry(
                    date, description, category, -expense
                ));
            }
            
            // Add income transaction
            if (income != null && income > 0) {
                String description = financeData.getIncomeDescription(date);
                
                allTransactions.add(new TransactionEntry(
                    date, description, "Income", income
                ));
            }
        }
        
        // Sort transactions by date (newest first)
        allTransactions.sort(Comparator.comparing(TransactionEntry::getDate).reversed());
        
        // Take only the most recent transactions (maximum 7)
        List<TransactionEntry> recentTransactions = allTransactions.subList(
                0, Math.min(7, allTransactions.size()));
        
        // Add the transactions to the model
        for (TransactionEntry entry : recentTransactions) {
            model.addRow(new Object[] {
                entry.getDate().format(formatter),
                entry.getDescription(),
                entry.getCategory(),
                entry.getAmount()
            });
        }
    }
    
    private String getRandomCategory(Set<String> categories, Random random) {
        List<String> categoryList = new ArrayList<>(categories);
        return categoryList.get(random.nextInt(categoryList.size()));
    }
    
    // Helper class to store and sort transaction data
    private static class TransactionEntry {
        private LocalDate date;
        private String description;
        private String category;
        private double amount;
        
        public TransactionEntry(LocalDate date, String description, String category, double amount) {
            this.date = date;
            this.description = description;
            this.category = category;
            this.amount = amount;
        }
        
        public LocalDate getDate() { return date; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 货币变化时更新渲染器并刷新表格
        updateAmountRenderer();
        transactionsTable.repaint();
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除组件时取消监听
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}