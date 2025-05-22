package com.example.app.ui.dashboard;

import com.example.app.model.FinanceData;
import com.example.app.model.CSVDataImporter;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashboardTransactionsPanel extends JPanel implements CurrencyChangeListener, DataRefreshListener {
    private JTable transactionsTable;
    private FinanceData financeData;
    private DefaultTableModel tableModel;
    private static final int MAX_TRANSACTIONS = 20; // 显示的最大交易数量

    private String username;
    
    public DashboardTransactionsPanel(String username) {
        this.username = username;
        financeData = new FinanceData();
        
        // 先从CSV文件加载数据
        loadTransactionData();
        
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
        viewAllButton.addActionListener(e -> openTransactionsPanel());
        buttonPanel.add(viewAllButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        
        // Register as listener for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }
    
    /**
     * 从CSV文件加载交易数据
     */
    private void loadTransactionData() {
        // 使用用户特定的路径
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);
        
        if (!transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            System.out.println("DashboardTransactionsPanel: 成功导入 " + transactions.size() + " 条交易记录");
        } else {
            System.err.println("DashboardTransactionsPanel: 没有交易记录被导入");
        }
    }
    
    /**
     * 打开完整交易面板
     */
    private void openTransactionsPanel() {
        // 获取主窗口的引用
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            
            // 这里可以添加导航到TransactionsPanel的代码
            // 例如，如果有一个主TabPanel，可以切换到Transactions选项卡
            // mainTabPanel.setSelectedIndex(transactionsTabIndex);
            
            JOptionPane.showMessageDialog(frame, 
                "查看所有交易记录", 
                "导航", JOptionPane.INFORMATION_MESSAGE);
        }
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
        
        // 应用金额列格式化渲染器
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
        
        // 获取所有交易记录
        List<FinanceData.Transaction> allTransactions = financeData.getTransactions();
        
        // 创建一个可以排序的交易记录列表
        List<TransactionEntry> transactionEntries = new ArrayList<>();
        
        // 将 FinanceData.Transaction 对象转换为 TransactionEntry 对象
        for (FinanceData.Transaction transaction : allTransactions) {
            transactionEntries.add(new TransactionEntry(
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getAmount()
            ));
        }
        
        // 按日期倒序排序（最新的在前）
        transactionEntries.sort(Comparator.comparing(TransactionEntry::getDate).reversed());
        
        // 只保留前MAX_TRANSACTIONS个记录
        int displayCount = Math.min(MAX_TRANSACTIONS, transactionEntries.size());
        List<TransactionEntry> recentTransactions = transactionEntries.subList(0, displayCount);
        
        // 添加交易记录到表格模型
        for (TransactionEntry entry : recentTransactions) {
            model.addRow(new Object[] {
                entry.getDate().format(formatter),
                entry.getDescription(),
                entry.getCategory(),
                entry.getAmount()
            });
        }
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
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.ALL) {
            // Reload data
            loadTransactionData();
            
            // Refresh table
            tableModel.setRowCount(0);
            populateTableWithRecentTransactions(tableModel);
            
            // Update currency formatting
            updateAmountRenderer();
            
            // Refresh UI
            transactionsTable.revalidate();
            transactionsTable.repaint();
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Unregister from all listeners
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        DataRefreshManager.getInstance().removeListener(this);
    }
}