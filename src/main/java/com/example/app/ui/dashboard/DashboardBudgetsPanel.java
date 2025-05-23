package com.example.app.ui.dashboard;

import com.example.app.model.CSVDataImporter;
import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashboardBudgetsPanel extends JPanel implements CurrencyManager.CurrencyChangeListener, DataRefreshListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardBudgetsPanel.class.getName());
    
    private FinanceData financeData;
    private final JPanel categoriesPanel;
    private String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
    private String username; // 存储当前用户名
    private String userDataPath; // 存储用户特定的数据路径
    
    public DashboardBudgetsPanel(String username) {
        this.username = username;
        this.userDataPath = ".\\user_data\\" + username; // 设置用户特定的数据路径
        
        // 初始化财务数据
        financeData = new FinanceData();
        
        // 设置用户特定的数据目录
        financeData.setDataDirectory(userDataPath);
        
        LOGGER.log(Level.INFO, "正在为用户 {0} 加载预算数据，路径: {1}", new Object[]{username, userDataPath});
        
        // 先加载交易数据
        loadTransactionData();
        
        // 再加载预算数据
        financeData.loadBudgets();
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Overall budget progress
        JPanel overallPanel = new JPanel(new BorderLayout());
        double overallPercentage = financeData.getOverallBudgetPercentage();
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
        categoriesPanel = new JPanel();
        categoriesPanel.setLayout(new BoxLayout(categoriesPanel, BoxLayout.Y_AXIS));
        
        // Add category panels
        updateCategoryPanels();
        
        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        
        // Register as listener for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }
    
    /**
     * 从用户特定的CSV文件加载交易数据
     */
    private void loadTransactionData() {
        String csvFilePath = userDataPath + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);
        
        if (!transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            LOGGER.log(Level.INFO, "用户 {0}: 成功导入 {1} 条交易记录", new Object[]{username, transactions.size()});
        } else {
            LOGGER.log(Level.WARNING, "用户 {0}: 没有交易记录被导入", username);
        }
    }
    
    /**
     * 打开完整预算面板
     */
    private void openFullBudgetPanel() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            // 这里可以添加导航到完整BudgetsPanel的代码
            JOptionPane.showMessageDialog(frame, 
                "查看完整预算管理", 
                "导航", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void updateCategoryPanels() {
        categoriesPanel.removeAll();
        
        Map<String, Double> budgets = financeData.getCategoryBudgets();
        Map<String, Double> expenses = financeData.getCategoryExpenses();
        
        for (String category : budgets.keySet()) {
            double budget = budgets.get(category);
            double expense = expenses.getOrDefault(category, 0.0);
            double percentage = budget > 0 ? (expense / budget) * 100 : 0;
            
            BudgetCategoryPanel categoryPanel = new BudgetCategoryPanel(
                    category, budget, expense, percentage,
                    e -> editCategory(category),
                    e -> deleteCategory(category)
            );
            
            categoriesPanel.add(categoryPanel);
            categoriesPanel.add(Box.createVerticalStrut(10));
        }
        
        revalidate();
        repaint();
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
            
            // 更新财务数据并保存到CSV文件
            financeData.updateCategoryBudget(category, budget);
            
            // 更新UI
            updateCategoryPanels();
            
            JOptionPane.showMessageDialog(this, 
                    "新类别已添加: " + category + " 预算: " + currencySymbol + budget,
                    "类别已添加", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void editCategory(String category) {
        double currentBudget = financeData.getCategoryBudget(category);
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Edit Category", 
                category, 
                currentBudget);
        
        if (dialog.showDialog()) {
            double newBudget = dialog.getBudget();
            
            // 更新财务数据并保存到CSV文件
            financeData.updateCategoryBudget(category, newBudget);
            
            // 更新UI
            updateCategoryPanels();
            
            JOptionPane.showMessageDialog(this, 
                    "类别已更新: " + category + " 新预算: " + currencySymbol + newBudget,
                    "类别已更新", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteCategory(String category) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要删除类别: " + category + " 吗?",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // 从财务数据中删除类别并保存到CSV文件
            if (financeData.deleteCategoryBudget(category)) {
                // 更新UI
                updateCategoryPanels();
                
                JOptionPane.showMessageDialog(this, 
                        "类别已删除: " + category,
                        "类别已删除", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        this.currencySymbol = currencySymbol;
        categoriesPanel.removeAll();
        updateCategoryPanels();
        revalidate();
        repaint();
    }
    
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.ALL) {
            
            // Reload data if needed
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS) {
                loadTransactionData();
            }
            else if (type == DataRefreshManager.RefreshType.BUDGETS) {
                financeData.loadBudgets();
            }
            
            // Update the UI
            updateCategoryPanels();
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Unregister from all listeners
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        DataRefreshManager.getInstance().removeListener(this);
    }
    
    // 提供一个公共方法来更新用户名和对应的数据路径
    public void setUsername(String username) {
        this.username = username;
        this.userDataPath = ".\\user_data\\" + username;
        
        // 更新数据路径并重新加载数据
        financeData.setDataDirectory(userDataPath);
        loadTransactionData();
        financeData.loadBudgets();
        
        // 更新UI
        updateCategoryPanels();
    }
}