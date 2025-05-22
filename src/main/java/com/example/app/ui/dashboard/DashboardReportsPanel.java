package com.example.app.ui.dashboard;

import javax.swing.*;

import com.example.app.model.CSVDataImporter;
import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.reports.*;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.ui.dashboard.report.CategorySpendingChartPanel;
import com.example.app.ui.dashboard.report.IncomeExpensesChartPanel;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

import java.awt.*;
import java.util.List;

public class DashboardReportsPanel extends JPanel implements CurrencyChangeListener, DataRefreshListener {
    
    private final FinanceData financeData = new FinanceData();
    private IncomeExpensesReportPanel incomeExpensesPanel;
    private CategoryBreakdownPanel categoryBreakdownPanel;
    private String username; // 添加用户名字段
    
    public DashboardReportsPanel(String username) { // 修改构造函数接收用户名
        this.username = username; // 保存用户名
        
        // 初始化财务数据
        // 从文件加载交易数据
        loadTransactionData();
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel titleLabel = new JLabel("Financial Reports");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create panel to hold both charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建图表并传入财务数据
        incomeExpensesPanel = new IncomeExpensesReportPanel(financeData);
        categoryBreakdownPanel = new CategoryBreakdownPanel(financeData);
        
        // Add the two chart panels
        chartsPanel.add(incomeExpensesPanel);
        chartsPanel.add(categoryBreakdownPanel);
        
        // Add to main panel with scroll support
        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // 添加一个查看完整报表的按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton viewFullReportsButton = new JButton("View Full Reports");
        viewFullReportsButton.addActionListener(e -> openFullReports());
        buttonPanel.add(viewFullReportsButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 注册货币变化监听
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
            System.out.println("DashboardReportsPanel: 成功导入 " + transactions.size() + " 条交易记录");
        } else {
            System.err.println("DashboardReportsPanel: 没有交易记录被导入");
        }
    }
    
    /**
     * 打开完整报表面板
     */
    private void openFullReports() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            // 这里添加导航到完整报表的逻辑
            JOptionPane.showMessageDialog(frame, 
                "查看完整财务报表", 
                "导航", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 货币变化时刷新图表
        if (incomeExpensesPanel != null) {
            incomeExpensesPanel.refreshChart();
        }
        if (categoryBreakdownPanel != null) {
            categoryBreakdownPanel.refreshChart();
        }
    }
    
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.ALL) {
            // Reload transaction data
            loadTransactionData();
            
            // Refresh charts
            if (incomeExpensesPanel != null) {
                incomeExpensesPanel.refreshChart();
            }
            
            if (categoryBreakdownPanel != null) {
                categoryBreakdownPanel.refreshChart();
            }
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除组件时取消监听
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        DataRefreshManager.getInstance().removeListener(this);
    }
}