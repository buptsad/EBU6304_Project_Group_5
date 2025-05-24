package com.example.app.model;

import java.time.LocalDate;
import java.util.*;

/**
 * Financial data model that manages transaction data, budget allocations,
 * and provides methods for financial analysis and reporting.
 * This class serves as the central data repository for the financial application.
 */
public class FinanceData {
    /** Static sample data for demonstration */
    private static final double MONTHLY_BUDGET = 4000.00;
    private static final double DAILY_BUDGET = MONTHLY_BUDGET / 30;
    
    /** Budget allocation by category */
    private Map<String, Double> categoryBudgets;
    
    /** Data maps for daily records */
    private Map<LocalDate, Double> dailyIncomes;
    private Map<LocalDate, Double> dailyExpenses;
    
    /** Expenses and incomes by category */
    private Map<String, Double> categoryExpenses;
    private Map<String, Double> categoryIncomes;
    
    /** Maps to store consistent transaction descriptions for each date */
    private Map<LocalDate, String> dailyExpenseDescriptions;
    private Map<LocalDate, String> dailyIncomeDescriptions;
    private Map<LocalDate, String> dailyExpenseCategories;
    
    /** List to store all transactions */
    private List<Transaction> transactions;
    
    /** Directory path for storing budget files */
    private String dataDirectory;
    
    /**
     * Constructs a new FinanceData object with initialized data structures.
     */
    public FinanceData() {
        // Initialize data structures
        initializeEmptyData();
    }
    
    /**
     * Initializes all data structures with empty collections.
     */
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
    
    /**
     * Imports transactions from CSV data into the finance model.
     * Processes transaction records and updates all relevant data maps.
     * 
     * @param importedTransactions list of transaction records as object arrays
     */
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
        
        // For collecting all possible categories
        Set<String> incomeCategories = new HashSet<>();
        Set<String> expenseCategories = new HashSet<>();
        
        // Import transaction data and collect categories
        for (Object[] transaction : importedTransactions) {
            String dateStr = (String) transaction[0];
            String description = (String) transaction[1];
            String csvCategory = (String) transaction[2];
            double amount = (Double) transaction[3];
            
            try {
                // Parse date
                LocalDate date = LocalDate.parse(dateStr);
                
                // Determine transaction type and category
                String category;
                boolean isIncome = amount >= 0;
                
                // Determine final category based on transaction description and CSV category
                if (isIncome) {
                    category = determineIncomeCategory(description, csvCategory);
                    incomeCategories.add(category);
                } else {
                    category = determineExpenseCategory(description, csvCategory);
                    expenseCategories.add(category);
                }
                
                // Add to transaction list
                Transaction newTransaction = new Transaction(date, description, category, amount);
                transactions.add(newTransaction);
                
                // Update daily data maps
                if (isIncome) {  // Income
                    dailyIncomes.put(date, dailyIncomes.getOrDefault(date, 0.0) + amount);
                    dailyIncomeDescriptions.put(date, description);
                    
                    // Update income category statistics
                    categoryIncomes.put(category, categoryIncomes.getOrDefault(category, 0.0) + amount);
                } else {  // Expense
                    double absAmount = Math.abs(amount);
                    dailyExpenses.put(date, dailyExpenses.getOrDefault(date, 0.0) + absAmount);
                    dailyExpenseDescriptions.put(date, description);
                    dailyExpenseCategories.put(date, category);
                    
                    // Update expense category statistics
                    categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + absAmount);
                }
            } catch (Exception e) {
                System.err.println("Error processing transaction: " + e.getMessage() + 
                    " (Date: " + dateStr + ", Description: " + description + ")");
            }
        }
        
        // Allocate budgets based on collected categories
        allocateBudgets(expenseCategories);
    }
    
    /**
     * Initializes budget categories with default values.
     */
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
    
    /**
     * Automatically allocate budgets based on collected expense categories.
     * Applies default allocation percentages for common categories.
     *
     * @param expenseCategories set of expense categories to allocate budget for
     */
    private void allocateBudgets(Set<String> expenseCategories) {
        double totalBudget = MONTHLY_BUDGET;
        
        // Clear existing budgets
        categoryBudgets.clear();
        
        if (expenseCategories.isEmpty()) {
            // If no expense categories are imported, set default category
            categoryBudgets.put("Other", totalBudget);
            return;
        }
        
        // Allocate budget evenly
        double budgetPerCategory = totalBudget / expenseCategories.size();
        
        // Allocate budget for each category
        for (String category : expenseCategories) {
            // Adjust budget allocation ratio for certain special categories
            double categoryBudget;
            switch (category.toLowerCase()) {
                case "food":
                    categoryBudget = totalBudget * 0.25; // 25% for food
                    break;
                case "housing":
                    categoryBudget = totalBudget * 0.35; // 35% for housing
                    break;
                case "transportation":
                    categoryBudget = totalBudget * 0.10; // 10% for transportation
                    break;
                case "entertainment":
                    categoryBudget = totalBudget * 0.05; // 5% for entertainment
                    break;
                default:
                    categoryBudget = budgetPerCategory; // Other categories share the remaining budget equally
            }
            categoryBudgets.put(category, categoryBudget);
        }
        
        // Ensure budget total matches monthly budget
        double allocatedBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        if (allocatedBudget != totalBudget) {
            // Adjust "Other" category budget for balance
            if (categoryBudgets.containsKey("Other")) {
                double otherBudget = categoryBudgets.get("Other") + (totalBudget - allocatedBudget);
                categoryBudgets.put("Other", otherBudget);
            } else {
                categoryBudgets.put("Other", totalBudget - allocatedBudget);
            }
        }
    }
    
    /**
     * Determines the appropriate income category based on description and CSV category.
     *
     * @param description the transaction description
     * @param csvCategory the category from CSV
     * @return the determined income category
     */
    private String determineIncomeCategory(String description, String csvCategory) {
        // For categories not in budget categories, categorize them into "Other"
        System.err.println("Current budget categories: " + categoryBudgets.keySet());
        if (!categoryBudgets.containsKey(csvCategory)) {
            return "Other";
        }
        else return csvCategory;
    }
    
    /**
     * Determines the appropriate expense category based on description and CSV category.
     *
     * @param description the transaction description
     * @param csvCategory the category from CSV
     * @return the determined expense category
     */
    private String determineExpenseCategory(String description, String csvCategory) {
        // For categories not in budget categories, categorize them into "Other"
        // categoryBudgets has all categories
        if (!categoryBudgets.containsKey(csvCategory)) {
            return "Other";
        }
        else return csvCategory;
    }
    
    // Other methods remain unchanged...
    
    /**
     * Gets income data categorized by category.
     * 
     * @return map of income amounts by category
     */
    public Map<String, Double> getCategoryIncomes() {
        return categoryIncomes;
    }
    
    /**
     * Gets budget allocation by category. 
     * If budget is empty, initializes with default values.
     * 
     * @return map of budget allocations by category
     */
    public Map<String, Double> getCategoryBudgets() {
        if (categoryBudgets.isEmpty()) {
            // If no data is imported, provide default budget
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
    
    /**
     * Inner class to represent a financial transaction.
     */
    public static class Transaction {
        private LocalDate date;
        private String description;
        private String category;
        private double amount;
        
        /**
         * Creates a new transaction with the specified attributes.
         *
         * @param date the transaction date
         * @param description the transaction description
         * @param category the transaction category
         * @param amount the transaction amount (negative for expenses, positive for incomes)
         */
        public Transaction(LocalDate date, String description, String category, double amount) {
            this.date = date;
            this.description = description;
            this.category = category;
            this.amount = amount;
        }
        
        /**
         * Gets the date of this transaction.
         * @return the transaction date
         */
        public LocalDate getDate() { return date; }
        
        /**
         * Gets the description of this transaction.
         * @return the transaction description
         */
        public String getDescription() { return description; }
        
        /**
         * Gets the category of this transaction.
         * @return the transaction category
         */
        public String getCategory() { return category; }
        
        /**
         * Gets the amount of this transaction.
         * @return the transaction amount
         */
        public double getAmount() { return amount; }
        
        /**
         * Determines if this transaction is an expense.
         * @return true if this is an expense (negative amount), false otherwise
         */
        public boolean isExpense() { return amount < 0; }
        
        /**
         * Determines if this transaction is an income.
         * @return true if this is an income (positive or zero amount), false otherwise
         */
        public boolean isIncome() { return amount >= 0; }
    }
    
    /**
     * Gets the total account balance.
     *
     * @return the total balance
     */
    public double getTotalBalance() {
        return 12587.43;
    }
    
    /**
     * Calculates the total income from all transactions.
     *
     * @return the total income amount
     */
    public double getTotalIncome() {
        return dailyIncomes.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Calculates the total expenses from all transactions.
     *
     * @return the total expense amount
     */
    public double getTotalExpenses() {
        return dailyExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Calculates total savings (income minus expenses).
     *
     * @return the total savings amount
     */
    public double getTotalSavings() {
        return getTotalIncome() - getTotalExpenses();
    }
    
    /**
     * Gets the monthly budget amount.
     *
     * @return the monthly budget
     */
    public double getMonthlyBudget() {
        return MONTHLY_BUDGET;
    }
    
    /**
     * Gets the daily budget amount.
     *
     * @return the daily budget
     */
    public double getDailyBudget() {
        return DAILY_BUDGET;
    }
    
    /**
     * Gets all unique dates from both income and expense records.
     *
     * @return sorted list of dates
     */
    public List<LocalDate> getDates() {
        Set<LocalDate> allDates = new HashSet<>();
        allDates.addAll(dailyIncomes.keySet());
        allDates.addAll(dailyExpenses.keySet());
        List<LocalDate> dates = new ArrayList<>(allDates);
        dates.sort(LocalDate::compareTo);
        return dates;
    }
    
    /**
     * Gets map of daily income values.
     *
     * @return map of income amounts by date
     */
    public Map<LocalDate, Double> getDailyIncomes() {
        return dailyIncomes;
    }
    
    /**
     * Gets map of daily expense values.
     *
     * @return map of expense amounts by date
     */
    public Map<LocalDate, Double> getDailyExpenses() {
        return dailyExpenses;
    }
    
    /**
     * Gets map of expense amounts by category.
     *
     * @return map of expense amounts by category
     */
    public Map<String, Double> getCategoryExpenses() {
        return categoryExpenses;
    }
    
    /**
     * Gets the expense amount for a specific category.
     *
     * @param category the expense category
     * @return the expense amount for the category, or 0 if none
     */
    public double getCategoryExpense(String category) {
        return categoryExpenses.getOrDefault(category, 0.0);
    }
    
    /**
     * Calculates the percentage of budget spent for a specific category.
     *
     * @param category the category to calculate percentage for
     * @return percentage of budget spent (0-100)
     */
    public double getCategoryPercentage(String category) {
        double budget = getCategoryBudget(category);
        double expense = getCategoryExpense(category);
        return budget > 0 ? (expense / budget) * 100 : 0;
    }
    
    /**
     * Calculates the overall percentage of total budget spent.
     *
     * @return percentage of total budget spent (0-100)
     */
    public double getOverallBudgetPercentage() {
        double totalBudget = categoryBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = getTotalExpenses();
        
        if (totalBudget > 0) {
            return (totalExpense / totalBudget) * 100;
        }
        return 0.0;
    }
    
    /**
     * Gets the expense description for a specific date.
     *
     * @param date the date to get description for
     * @return the expense description, or "Unknown expense" if none exists
     */
    public String getExpenseDescription(LocalDate date) {
        return dailyExpenseDescriptions.getOrDefault(date, "Unknown expense");
    }
    
    /**
     * Gets the income description for a specific date.
     *
     * @param date the date to get description for
     * @return the income description, or "Unknown income" if none exists
     */
    public String getIncomeDescription(LocalDate date) {
        return dailyIncomeDescriptions.getOrDefault(date, "Unknown income");
    }
    
    /**
     * Gets the expense category for a specific date.
     *
     * @param date the date to get category for
     * @return the expense category, or "Other" if none exists
     */
    public String getExpenseCategory(LocalDate date) {
        return dailyExpenseCategories.getOrDefault(date, "Other");
    }
    
    /**
     * Gets a generic expense description.
     * No longer depends on randomly generated descriptions.
     *
     * @param random random number generator
     * @return a generic expense description
     */
    public String getRandomExpenseDescription(Random random) {
        return "Expense";
    }
    
    /**
     * Gets a generic income description.
     * No longer depends on randomly generated descriptions.
     *
     * @param random random number generator
     * @return a generic income description
     */
    public String getRandomIncomeDescription(Random random) {
        return "Income";
    }
    
    /**
     * Gets an array of expense descriptions.
     * Returns empty array instead of sample descriptions.
     *
     * @return an empty array of expense descriptions
     */
    public String[] getExpenseDescriptions() {
        return new String[0];
    }
    
    /**
     * Gets an array of income descriptions.
     * Returns empty array instead of sample descriptions.
     *
     * @return an empty array of income descriptions
     */
    public String[] getIncomeDescriptions() {
        return new String[0];
    }
    
    /**
     * Gets all transactions.
     *
     * @return list of all transactions
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    /**
     * Sets the directory path for storing budget data files.
     *
     * @param directory the directory path
     */
    public void setDataDirectory(String directory) {
        this.dataDirectory = directory;
    }

    /**
     * Loads budget data from CSV file in the configured directory.
     * Notifies listeners when budget data changes.
     */
    public void loadBudgets() {
        if (dataDirectory != null) {
            Map<String, Double> loadedBudgets = BudgetManager.loadBudgetsFromCSV(dataDirectory);
            if (!loadedBudgets.isEmpty()) {
                categoryBudgets.clear();
                categoryBudgets.putAll(loadedBudgets);
                System.out.println("Budget data loaded successfully");
                // Notify listeners that budget data has changed
                DataRefreshManager.getInstance().refreshBudgets();
            }
        }
    }

    /**
     * Saves budget data to CSV file in the configured directory.
     */
    public void saveBudgets() {
        if (dataDirectory != null && !categoryBudgets.isEmpty()) {
            BudgetManager.saveBudgetsToCSV(categoryBudgets, dataDirectory);
            System.out.println("Budget data saved successfully");
        }
    }

    /**
     * Updates budget amount for a specific category and saves changes.
     * Notifies listeners when budget data changes.
     *
     * @param category the category to update
     * @param budget the new budget amount
     */
    public void updateCategoryBudget(String category, double budget) {
        categoryBudgets.put(category, budget);
        saveBudgets(); // Update the saved budgets
        // Notify listeners that budget data has changed
        DataRefreshManager.getInstance().refreshBudgets();
    }

    /**
     * Deletes a budget category and saves changes.
     * Notifies listeners when budget data changes.
     *
     * @param category the category to delete
     * @return true if the category was deleted, false if it didn't exist
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
     * Gets the budget amount for a specific category.
     *
     * @param category the category to get budget for
     * @return the budget amount, or 0 if no budget exists for the category
     */
    public double getCategoryBudget(String category) {
        return categoryBudgets.getOrDefault(category, 0.0);
    }

    /**
     * Imports transactions and notifies listeners of data change.
     *
     * @param importedTransactions list of transaction records to import
     */
    public void importTransactionsAndNotify(List<Object[]> importedTransactions) {
        importTransactions(importedTransactions);
        // Notify listeners that transaction data has changed
        DataRefreshManager.getInstance().refreshTransactions();
    }
}