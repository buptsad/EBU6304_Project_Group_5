package com.example.app.ui.pages;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.ui.dashboard.BudgetCategoryPanel;
import com.example.app.ui.dashboard.BudgetDialog;
import com.example.app.ui.pages.AI.getRes;
import com.example.app.model.CSVDataImporter;
import com.example.app.model.DataRefreshListener;
import com.example.app.model.DataRefreshManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.json.JSONObject;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class BudgetsPanel extends JPanel implements CurrencyChangeListener, DataRefreshListener {
    private final FinanceData financeData;
    private final JPanel userBudgetsPanel;
    private final JPanel aiSuggestedPanel;
    private Random random = new Random();
    private String username;

    public BudgetsPanel(String username) {
        this.username = username;
        this.financeData = new FinanceData();
        
        // 设置数据目录并加载预算
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);
        
        // 先加载事务数据
        loadTransactionData();
        
        // 再加载预算数据
        financeData.loadBudgets();
        
        setLayout(new BorderLayout(20, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 22));
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

        // Main content panel - split into left and right
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // Left panel - Current budget allocation
        JPanel userPanel = new JPanel(new BorderLayout());
        TitledBorder userBorder = BorderFactory.createTitledBorder("Your Budget Allocation");
        userBorder.setTitleFont(new Font(getFont().getName(), Font.BOLD, 16));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                userBorder,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        userBudgetsPanel = new JPanel();
        userBudgetsPanel.setLayout(new BoxLayout(userBudgetsPanel, BoxLayout.Y_AXIS));
        updateUserCategoryPanels();

        JScrollPane userScrollPane = new JScrollPane(userBudgetsPanel);
        userScrollPane.setBorder(BorderFactory.createEmptyBorder());
        userScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        // Add category button
        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Category");
        addButton.setIcon(UIManager.getIcon("Tree.addIcon"));
        addButton.addActionListener(e -> addNewCategory());
        userButtonPanel.add(addButton);
        userPanel.add(userButtonPanel, BorderLayout.SOUTH);

        // Right panel - AI suggested allocation
        JPanel aiPanel = new JPanel(new BorderLayout());
        TitledBorder aiBorder = BorderFactory.createTitledBorder("AI Suggested Budget");
        aiBorder.setTitleFont(new Font(getFont().getName(), Font.BOLD, 16));
        aiPanel.setBorder(BorderFactory.createCompoundBorder(
                aiBorder,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        aiSuggestedPanel = new JPanel();
        aiSuggestedPanel.setLayout(new BoxLayout(aiSuggestedPanel, BoxLayout.Y_AXIS));
        updateAISuggestedPanels();

        JScrollPane aiScrollPane = new JScrollPane(aiSuggestedPanel);
        aiScrollPane.setBorder(BorderFactory.createEmptyBorder());
        aiScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);

        // Shuffle button for AI suggestions
        JPanel aiButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton shuffleButton = new JButton("Shuffle Suggestions");
        shuffleButton.setIcon(UIManager.getIcon("Table.descendingSortIcon"));
        shuffleButton.addActionListener(e -> shuffleAISuggestions());
        
        JButton applyButton = new JButton("Apply Suggestions");
        applyButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        applyButton.addActionListener(e -> applyAISuggestions());
        
        aiButtonPanel.add(shuffleButton);
        aiButtonPanel.add(applyButton);
        aiPanel.add(aiButtonPanel, BorderLayout.SOUTH);

        // Add both panels to the content area
        contentPanel.add(userPanel);
        contentPanel.add(aiPanel);
        add(contentPanel, BorderLayout.CENTER);
        
        // 注册货币变化监听器
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        
        // Register as listener for data refresh events
        DataRefreshManager.getInstance().addListener(this);
    }

    private void updateUserCategoryPanels() {
        userBudgetsPanel.removeAll();
        
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
            
            userBudgetsPanel.add(categoryPanel);
            userBudgetsPanel.add(Box.createVerticalStrut(10));
        }
        
        // Add total
        double totalBudget = budgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = expenses.values().stream().mapToDouble(Double::doubleValue).sum();
        
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        JLabel totalLabel = new JLabel(String.format("<html><b>Total: %s%.2f</b></html>", currencySymbol,totalBudget));
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 14));
        totalPanel.add(totalLabel, BorderLayout.WEST);
        
        userBudgetsPanel.add(Box.createVerticalStrut(10));
        userBudgetsPanel.add(totalPanel);
        
        revalidate();
        repaint();
    }
    
    private void updateAISuggestedPanels() {
        aiSuggestedPanel.removeAll();
        
        // Get actual budgets as a starting point
        Map<String, Double> actualBudgets = financeData.getCategoryBudgets();
        double totalBudget = actualBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Create AI suggestions (in this demo, just random variations)
        Map<String, Double> suggestedBudgets = generateSuggestedBudgets(actualBudgets, totalBudget);
        
        // Display each category with comparison to actual budget
        for (String category : suggestedBudgets.keySet()) {
            double suggestedBudget = suggestedBudgets.get(category);
            double actualBudget = actualBudgets.getOrDefault(category, 0.0);
            double difference = suggestedBudget - actualBudget;
            
            JPanel categoryPanel = createAISuggestionPanel(category, suggestedBudget, difference);
            aiSuggestedPanel.add(categoryPanel);
            aiSuggestedPanel.add(Box.createVerticalStrut(10));
        }
        
        // Add total
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        JLabel totalLabel = new JLabel(String.format("<html><b>Total: %s%.2f</b></html>",currencySymbol ,totalBudget));
        totalLabel.setFont(new Font(totalLabel.getFont().getName(), Font.BOLD, 14));
        totalPanel.add(totalLabel, BorderLayout.WEST);
        
        aiSuggestedPanel.add(Box.createVerticalStrut(10));
        aiSuggestedPanel.add(totalPanel);
        
        revalidate();
        repaint();
    }
    
    private JPanel createAISuggestionPanel(String category, double budget, double difference) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Category name and budget amount
        JPanel leftPanel = new JPanel(new BorderLayout());
        JLabel categoryLabel = new JLabel(category);
        categoryLabel.setFont(new Font(categoryLabel.getFont().getName(), Font.BOLD, 14));
        leftPanel.add(categoryLabel, BorderLayout.NORTH);
        
        JLabel budgetLabel = new JLabel(String.format("%s%.2f", currencySymbol, budget));
        budgetLabel.setFont(new Font(budgetLabel.getFont().getName(), Font.PLAIN, 14));
        leftPanel.add(budgetLabel, BorderLayout.SOUTH);
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // Difference indicator
        JPanel rightPanel = new JPanel(new BorderLayout());
        
                // 修改预算显示
                JLabel aiBudgetLabel = new JLabel(String.format("%s%.2f", currencySymbol, budget));
                
                // 修改差异显示
                String diffText;
                Color diffColor;
                
                if (Math.abs(difference) < 0.01) {
                    diffText = "No change";
                    diffColor = Color.GRAY;
                } else if (difference > 0) {
                    diffText = String.format("+%s%.2f", currencySymbol, difference);
                    diffColor = new Color(46, 204, 113); // Green
                } else {
                    diffText = String.format("-%s%.2f", currencySymbol, Math.abs(difference));
                    diffColor = new Color(231, 76, 60); // Red
                }
        
        JLabel diffLabel = new JLabel(diffText);
        diffLabel.setForeground(diffColor);
        diffLabel.setFont(new Font(diffLabel.getFont().getName(), Font.BOLD, 14));
        rightPanel.add(diffLabel, BorderLayout.CENTER);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private Map<String, Double> generateSuggestedBudgets(Map<String, Double> currentBudgets, double totalBudget) {
        Map<String, Double> suggestedBudgets = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> e : currentBudgets.entrySet()) {
            sb.append(e.getKey())
            .append(": ")
            .append(e.getValue())
            .append("; ");
        }
        if (sb.length() >= 2) {
            sb.setLength(sb.length() - 2);  // 去掉末尾的 "; "
        }
        String budgetString = sb.toString();
        System.out.println(budgetString);
        String aiPrompt = String.format(
            "当前预算分配如下：%s。总预算为 %.2f。"
        + " 请在总金额不变的情况下，将总预算在各类中重新分配，给出一个更合理的预算分配方案。"
        + " 请以 JSON 格式输出，键是类别名称，值是对应金额，此外不要输出其它任何内容。",
            budgetString,
            totalBudget
        );

        String API_KEY;
        String response;
        try {
        API_KEY = "sk-fdf26a37926f46ab8d4884c2cd533db8";
        response = new getRes().getResponse(API_KEY, aiPrompt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String res = null;
        try {
            res = new getRes().parseAIResponse(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(res);
        // 1. trim 空白
        res = res.trim();

        // 2. 去掉开头的 ``` 及可选语言标识
        if (res.startsWith("```")) {
            int firstNewline = res.indexOf('\n');
            if (firstNewline != -1) {
                res = res.substring(firstNewline + 1).trim();
            } else {
                // 整个字符串只有 ```？ 那就清空
                res = "";
            }
        }
        // 3. 去掉结尾的 ```
        if (res.endsWith("```")) {
            int lastBackticks = res.lastIndexOf("```");
            res = res.substring(0, lastBackticks).trim();
        }

        // 4. 若还有 "json" 前缀，一并去掉
        if (res.startsWith("json")) {
            int brace = res.indexOf('{');
            if (brace != -1) {
                res = res.substring(brace).trim();
            }
        }
        // 5. 再用 org.json 解析
        try {
            JSONObject json = new JSONObject(res);
            for (String category : currentBudgets.keySet()) {
                double val = json.has(category)
                        ? json.getDouble(category)
                        : currentBudgets.get(category);
                suggestedBudgets.put(category, val);
            }
        } catch (Exception e) {
            System.err.println("解析 AI JSON 失败: " + e.getMessage());
        }
        return suggestedBudgets;
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
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();

        BudgetDialog dialog = new BudgetDialog(SwingUtilities.getWindowAncestor(this), "Add Category", "", 0.0);
        if (dialog.showDialog()) {
            String category = dialog.getCategory();
            double budget = dialog.getBudget();
            
            // 更新模型并保存到文件
            financeData.updateCategoryBudget(category, budget);
            
            // 刷新UI
            updateUserCategoryPanels();
            updateAISuggestedPanels();
            
            JOptionPane.showMessageDialog(this, 
                    "已添加新类别: " + category + " 预算为: " + currencySymbol + budget,
                    "类别已添加", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void editCategory(String category) {
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();

        double currentBudget = financeData.getCategoryBudget(category);
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Edit Category", 
                category, 
                currentBudget);
        
        if (dialog.showDialog()) {
            double newBudget = dialog.getBudget();
            
            // 更新模型并保存到文件
            financeData.updateCategoryBudget(category, newBudget);
            
            // 刷新UI
            updateUserCategoryPanels();
            updateAISuggestedPanels();
            
            JOptionPane.showMessageDialog(this, 
                    "已更新类别: " + category + " 的预算为: " + currencySymbol + newBudget,
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
            // 从模型中删除并保存到文件
            if (financeData.deleteCategoryBudget(category)) {
                // 刷新UI
                updateUserCategoryPanels();
                updateAISuggestedPanels();
                
                JOptionPane.showMessageDialog(this, 
                        "类别已删除: " + category,
                        "类别已删除", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void shuffleAISuggestions() {
        updateAISuggestedPanels();
        JOptionPane.showMessageDialog(this,
                "New AI budget suggestions generated!",
                "Suggestions Updated",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void applyAISuggestions() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "确定要应用AI建议的预算分配到你的预算中吗?",
                "应用AI建议",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // 获取AI建议的预算
            Map<String, Double> actualBudgets = financeData.getCategoryBudgets();
            double totalBudget = actualBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
            Map<String, Double> suggestedBudgets = generateSuggestedBudgets(actualBudgets, totalBudget);
            
            // 更新每个类别的预算
            for (Map.Entry<String, Double> entry : suggestedBudgets.entrySet()) {
                financeData.updateCategoryBudget(entry.getKey(), entry.getValue());
            }
            
            // 刷新UI
            updateUserCategoryPanels();
            updateAISuggestedPanels();
            
            JOptionPane.showMessageDialog(this,
                    "AI建议的预算已应用到你的预算分配中!",
                    "已应用建议",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void loadTransactionData() {
        // 使用用户特定的路径
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);

        if (!transactions.isEmpty()) {
            financeData.importTransactions(transactions);
            System.out.println("成功导入 " + transactions.size() + " 条交易记录");
        } else {
            System.err.println("没有交易记录被导入");
        }
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 货币变化时刷新面板
        updateUserCategoryPanels();
        updateAISuggestedPanels();
    }

    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        if (type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.ALL) {
            
            // Reload data if needed
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS) {
                loadTransactionData();
            }
            
            // Refresh UI components
            updateUserCategoryPanels();
            updateAISuggestedPanels();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // Unregister when component is removed from UI
        DataRefreshManager.getInstance().removeListener(this);
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}


