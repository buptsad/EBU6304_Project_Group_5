package com.example.app.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FinanceData {
    // Static sample data for demonstration
    private static final double MONTHLY_BUDGET = 4000.00;
    private static final double DAILY_BUDGET = MONTHLY_BUDGET / 30;
    
    // Budget allocation by category
    private Map<String, Double> categoryBudgets;
    
    // Data maps for daily records
    private Map<LocalDate, Double> dailyIncomes;
    private Map<LocalDate, Double> dailyExpenses;
    
    // Expenses and incomes by category
    private Map<String, Double> categoryExpenses;
    private Map<String, Double> categoryIncomes;
    
    // Maps to store consistent transaction descriptions for each date
    private Map<LocalDate, String> dailyExpenseDescriptions;
    private Map<LocalDate, String> dailyIncomeDescriptions;
    private Map<LocalDate, String> dailyExpenseCategories;
    
    // List to store all transactions
    private List<Transaction> transactions;
    
    // 用于存储预算文件的目录路径
    private String dataDirectory;
    
    public FinanceData() {
        // Initialize data structures
        initializeEmptyData();
    }
    
    private void initializeEmptyData() {
        // Initialize empty data structures
        dailyIncomes = new HashMap<>();
        dailyExpenses = new HashMap<>();
        dailyExpenseDescriptions = new HashMap<>();
        dailyIncomeDescriptions = new HashMap<>();
        dailyExpenseCategories = new HashMap<>();
        transactions = new ArrayList<>();
        
        // Initialize category budgets
        categoryBudgets = new LinkedHashMap<>();
        
        // Default budget values will be set after importing data
        
        // Initialize category expenses and incomes
        categoryExpenses = new LinkedHashMap<>();
        categoryIncomes = new LinkedHashMap<>();
    }
    
    // Method to import transactions from CSV
    public void importTransactions(List<Object[]> importedTransactions) {
        // First, make sure we have some budget categories
        if (categoryBudgets.isEmpty()) {
            // Try loading from file
            if (dataDirectory != null) {
                Map<String, Double> loadedBudgets = BudgetManager.loadBudgetsFromCSV(dataDirectory);
                if (!loadedBudgets.isEmpty()) {
                    categoryBudgets.putAll(loadedBudgets);
                } else {
                    // Initialize with default values if no file exists
                    initializeDefaultBudgets();
                }
            } else {
                // No directory set, use defaults
                initializeDefaultBudgets();
            }
        }
        
        // Now continue with transaction import as before
        dailyIncomes.clear();
        dailyExpenses.clear();
        dailyIncomeDescriptions.clear();
        dailyExpenseDescriptions.clear();
        dailyExpenseCategories.clear();
        transactions.clear();
        
        // 用于收集所有可能的类别
        Set<String> incomeCategories = new HashSet<>();
        Set<String> expenseCategories = new HashSet<>();
        
        // 导入交易数据并收集类别
        for (Object[] transaction : importedTransactions) {
            String dateStr = (String) transaction[0];
            String description = (String) transaction[1];
            String csvCategory = (String) transaction[2];
            double amount = (Double) transaction[3];
            
            try {
                // 解析日期
                LocalDate date = LocalDate.parse(dateStr);
                
                // 确定交易类型和分类
                String category;
                boolean isIncome = amount >= 0;
                
                // 根据交易描述和CSV类别确定最终类别
                if (isIncome) {
                    category = determineIncomeCategory(description, csvCategory);
                    incomeCategories.add(category);
                } else {
                    category = determineExpenseCategory(description, csvCategory);
                    expenseCategories.add(category);
                }
                
                // 添加到交易列表
                Transaction newTransaction = new Transaction(date, description, category, amount);
                transactions.add(newTransaction);
                
                // 更新日常数据映射
                if (isIncome) {  // 收入
                    dailyIncomes.put(date, dailyIncomes.getOrDefault(date, 0.0) + amount);
                    dailyIncomeDescriptions.put(date, description);
                    
                    // 更新收入类别统计
                    categoryIncomes.put(category, categoryIncomes.getOrDefault(category, 0.0) + amount);
                } else {  // 支出
                    double absAmount = Math.abs(amount);
                    dailyExpenses.put(date, dailyExpenses.getOrDefault(date, 0.0) + absAmount);
                    dailyExpenseDescriptions.put(date, description);
                    dailyExpenseCategories.put(date, category);
                    
                    // 更新支出类别统计
                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + absAmount);
                }
            } catch (Exception e) {
                System.err.println("处理交易记录时出错: " + e.getMessage() + 
                    " (日期: " + dateStr + ", 描述: " + description + ")");
            }
        }
        
        // 根据收集到的类别分配预算
        allocateBudgets(expenseCategories);
    }
    
    public LocalDate getStartDate() {
    return getDates().stream().min(LocalDate::compareTo).orElse(null);
}

    public LocalDate getEndDate() {
        return getDates().stream().max(LocalDate::compareTo).orElse(null);
    }
    // Add this new method to initialize default budget categories
    private void initializeDefaultBudgets() {
        categoryBudgets.put("Housing", 1400.00);
        categoryBudgets.put("Food", 800.00);
        categoryBudgets.put("Transportation", 400.00);
        categoryBudgets.put("Entertainment", 250.00);
        categoryBudgets.put("Shopping", 400.00);
        categoryBudgets.put("Service", 350.00);
        categoryBudgets.put("Gift", 200.00);
        categoryBudgets.put("Other", 200.00);
    }
    
    // 根据收集到的类别自动分配预算
    private void allocateBudgets(Set<String> expenseCategories) {
        double totalBudget = MONTHLY_BUDGET;
        
        // 清空现有预算
        categoryBudgets.clear();
        
        if (expenseCategories.isEmpty()) {
            // 如果没有导入支出类别，设置默认类别
            categoryBudgets.put("Other", totalBudget);
            return;
        }
        
        // 平均分配预算
        double budgetPerCategory = totalBudget / expenseCategories.size();
        
        // 为每个类别分配预算
        for (String category : expenseCategories) {
            // 对于某些特殊类别可以调整预算分配比例
            double categoryBudget;
            switch (category.toLowerCase()) {
                case "food":
                    categoryBudget = totalBudget * 0.25; // 25% 用于食物
                    break;
                case "housing":
                    categoryBudget = totalBudget * 0.35; // 35% 用于住房
                    break;
                case "transportation":
                    categoryBudget = totalBudget * 0.10; // 10% 用于交通
                    break;
                case "entertainment":
                    categoryBudget = totalBudget * 0.05; // 5% 用于娱乐
                    break;
                default:
                    categoryBudget = budgetPerCategory; // 其他类别均分剩余预算
            }
            categoryBudgets.put(category, categoryBudget);
        }
        
        // 确保预算总和与月度预算相符
        double allocatedBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        if (allocatedBudget != totalBudget) {
            // 调整"其他"类别的预算以平衡
            if (categoryBudgets.containsKey("Other")) {
                double otherBudget = categoryBudgets.get("Other") + (totalBudget - allocatedBudget);
                categoryBudgets.put("Other", otherBudget);
            } else {
                categoryBudgets.put("Other", totalBudget - allocatedBudget);
            }
        }
    }
    
    private String determineIncomeCategory(String description, String csvCategory) {
        // for those with categories not in budget categories, categorize them into "Other"
        System.err.println("Current budget categories: " + categoryBudgets.keySet());
        if (!categoryBudgets.containsKey(csvCategory)) {
            return "Other";
        }
        else return csvCategory;
    }
    
    private String determineExpenseCategory(String description, String csvCategory) {
        // for those with catagories not in budget categories, catagorize them into "Other"
        // categoryBudgets has all categories
        if (!categoryBudgets.containsKey(csvCategory)) {
            return "Other";
        }
        else return csvCategory;
    }
    
    // 其他方法保持不变...
    
    // 新增获取收入类别数据的方法
    public Map<String, Double> getCategoryIncomes() {
        return categoryIncomes;
    }
    
    // 修改后的 getCategoryBudgets 方法，如果预算为空，初始化默认值
    public Map<String, Double> getCategoryBudgets() {
        if (categoryBudgets.isEmpty()) {
            // 如果没有数据导入，提供默认预算
            categoryBudgets.put("Housing", 1400.00);
            categoryBudgets.put("Food", 800.00);
            categoryBudgets.put("Transportation", 400.00);
            categoryBudgets.put("Utilities", 350.00);
            categoryBudgets.put("Entertainment", 250.00);
            categoryBudgets.put("Healthcare", 300.00);
            categoryBudgets.put("Other", 500.00);
        }
        return categoryBudgets;
    }
    
    // Inner class to represent a transaction
    public static class Transaction {
        private LocalDate date;
        private String description;
        private String category;
        private double amount;
        
        public Transaction(LocalDate date, String description, String category, double amount) {
            this.date = date;
            this.description = description;
            this.category = category;
            this.amount = amount;
        }
        
        public LocalDate getDate() { return date; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public double getAmount() { return amount; }
        public boolean isExpense() { return amount < 0; }
        public boolean isIncome() { return amount >= 0; }
    }
    
    public double getTotalBalance() {
        return 12587.43;
    }
    
    public double getTotalIncome() {
        return dailyIncomes.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    public double getTotalExpenses() {
        return dailyExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    public double getTotalSavings() {
        return getTotalIncome() - getTotalExpenses();
    }
    
    public double getMonthlyBudget() {
        return MONTHLY_BUDGET;
    }
    
    public double getDailyBudget() {
        return DAILY_BUDGET;
    }
    
    // Methods to get data for charts
    public List<LocalDate> getDates() {
        Set<LocalDate> allDates = new HashSet<>();
        allDates.addAll(dailyIncomes.keySet());
        allDates.addAll(dailyExpenses.keySet());
        List<LocalDate> dates = new ArrayList<>(allDates);
        dates.sort(LocalDate::compareTo);
        return dates;
    }
    
    public Map<LocalDate, Double> getDailyIncomes() {
        return dailyIncomes;
    }
    
    public Map<LocalDate, Double> getDailyExpenses() {
        return dailyExpenses;
    }
    
    public Map<String, Double> getCategoryExpenses() {
        return categoryExpenses;
    }
    
    public double getCategoryExpense(String category) {
        return categoryExpenses.getOrDefault(category, 0.0);
    }
    
    public double getCategoryPercentage(String category) {
        double budget = getCategoryBudget(category);
        double expense = getCategoryExpense(category);
        return budget > 0 ? (expense / budget) * 100 : 0;
    }
    
    public double getOverallBudgetPercentage() {
        double totalBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = getTotalExpenses();
        
        if (totalBudget > 0) {
            return (totalExpense / totalBudget) * 100;
        }
        return 0.0;
    }
    
    // Methods to access consistent transaction descriptions
    public String getExpenseDescription(LocalDate date) {
        return dailyExpenseDescriptions.getOrDefault(date, "Unknown expense");
    }
    
    public String getIncomeDescription(LocalDate date) {
        return dailyIncomeDescriptions.getOrDefault(date, "Unknown income");
    }
    
    public String getExpenseCategory(LocalDate date) {
        return dailyExpenseCategories.getOrDefault(date, "Other");
    }
    
    // 修改后的方法，不再依赖随机生成的描述
    public String getRandomExpenseDescription(Random random) {
        return "Expense";
    }
    
    public String getRandomIncomeDescription(Random random) {
        return "Income";
    }
    
    // 修改这些方法返回空数组，而不是样例描述
    public String[] getExpenseDescriptions() {
        return new String[0];
    }
    
    public String[] getIncomeDescriptions() {
        return new String[0];
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    /**
     * 设置数据目录路径
     */
    public void setDataDirectory(String directory) {
        this.dataDirectory = directory;
    }

    /**
     * 加载预算数据
     */
    public void loadBudgets() {
        if (dataDirectory != null) {
            Map<String, Double> loadedBudgets = BudgetManager.loadBudgetsFromCSV(dataDirectory);
            if (!loadedBudgets.isEmpty()) {
                categoryBudgets.clear();
                categoryBudgets.putAll(loadedBudgets);
                System.out.println("已加载预算数据");
                // Notify listeners that budget data has changed
                DataRefreshManager.getInstance().refreshBudgets();
            }
        }
    }

    /**
     * 保存预算数据
     */
    public void saveBudgets() {
        if (dataDirectory != null && !categoryBudgets.isEmpty()) {
            BudgetManager.saveBudgetsToCSV(categoryBudgets, dataDirectory);
            System.out.println("已保存预算数据");
        }
    }

    /**
     * 更新类别预算
     */
    public void updateCategoryBudget(String category, double budget) {
        categoryBudgets.put(category, budget);
        saveBudgets(); // Update the saved budgets
        // Notify listeners that budget data has changed
        DataRefreshManager.getInstance().refreshBudgets();
    }

    /**
     * 删除类别预算
     */
    public boolean deleteCategoryBudget(String category) {
        if (categoryBudgets.containsKey(category)) {
            categoryBudgets.remove(category);
            saveBudgets(); // Save the changes
            // Notify listeners that budget data has changed
            DataRefreshManager.getInstance().refreshBudgets();
            return true;
        }
        return false;
    }

    /**
     * 获取指定类别的预算
     */
    public double getCategoryBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }

    /**
     * Import transactions and notify listeners
     */
    public void importTransactionsAndNotify(List<Object[]> importedTransactions) {
        importTransactions(importedTransactions);
        // Notify listeners that transaction data has changed
        DataRefreshManager.getInstance().refreshTransactions();
    }

}