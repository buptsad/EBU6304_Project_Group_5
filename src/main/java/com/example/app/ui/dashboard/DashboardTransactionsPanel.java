package com.example.app.ui.dashboard;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.dashboard.DashboardTransactionsViewModel;
import com.example.app.viewmodel.dashboard.DashboardTransactionsViewModel.TransactionChangeListener;
import com.example.app.viewmodel.dashboard.DashboardTransactionsViewModel.TransactionEntry;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardTransactionsPanel extends JPanel implements CurrencyChangeListener, TransactionChangeListener {
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private final DashboardTransactionsViewModel viewModel;
    
    public DashboardTransactionsPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new DashboardTransactionsViewModel(username);
        this.viewModel.addChangeListener(this);
        
        // Setup UI
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
        
        // Register as currency change listener
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
        
        // Populate table with data from ViewModel
        populateTableWithRecentTransactions();
        
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
        
        // Apply custom renderer for amount column
        updateAmountRenderer();
    }
    
    private void updateAmountRenderer() {
        // Get currency symbol from CurrencyManager
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
    
    private void populateTableWithRecentTransactions() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Get transactions from ViewModel
        List<TransactionEntry> recentTransactions = viewModel.getRecentTransactions();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Add transactions to table model
        for (TransactionEntry entry : recentTransactions) {
            tableModel.addRow(new Object[] {
                entry.getDate().format(formatter),
                entry.getDescription(),
                entry.getCategory(),
                entry.getAmount()
            });
        }
    }
    
    private void openTransactionsPanel() {
        // Get the main window reference
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            
            // This would navigate to the full TransactionsPanel
            // In a real app, this might be handled by a navigation controller or router
            JOptionPane.showMessageDialog(frame, 
                "View All Transactions", 
                "Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Update currency formatter
        updateAmountRenderer();
        transactionsTable.repaint();
    }
    
    @Override
    public void onTransactionsChanged() {
        // Called by ViewModel when transaction data changes
        SwingUtilities.invokeLater(() -> {
            // Update table data
            populateTableWithRecentTransactions();
            
            // Update currency formatting
            updateAmountRenderer();
            
            // Refresh UI
            transactionsTable.revalidate();
            transactionsTable.repaint();
        });
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}