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

/**
 * Panel for managing budget allocations and AI-suggested budget improvements
 */
public class BudgetsPanel extends JPanel implements CurrencyChangeListener, DataRefreshListener {
    // Core data fields
    private final FinanceData financeData;   // Stores all budget and expense data
    private final JPanel userBudgetsPanel;   // UI panel for user-defined budgets
    private final JPanel aiSuggestedPanel;   // UI panel for AI-suggested budgets
    private Random random = new Random();    // For generating random suggestions
    private String username;                 // Current user identifier
    
    // Constants for UI settings
    private static final int HEADER_FONT_SIZE = 22;
    private static final int LABEL_FONT_SIZE = 14;
    private static final int PANEL_SPACING = 20;
    private static final int PADDING = 10;

    /**
     * Constructs the budget management panel
     * @param username Current logged-in user
     */
    public BudgetsPanel(String username) {
        this.username = username;
        this.financeData = new FinanceData();
        
        // Set data directory and load budget data
        String dataDirectory = ".\\user_data\\" + username;
        financeData.setDataDirectory(dataDirectory);
        
        // Load transaction data first to calculate expenses
        loadTransactionData();
        
        // Then load budget allocations
        financeData.loadBudgets();
        
        // Configure main panel layout
        setLayout(new BorderLayout(PANEL_SPACING, 0));
        setBorder(BorderFactory.createEmptyBorder(PANEL_SPACING, PANEL_SPACING, PANEL_SPACING, PANEL_SPACING));

        // Create and add header section
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create main content with user and AI panels
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, PANEL_SPACING, 0));

        // User budget panel - left side
        JPanel userPanel = createUserBudgetPanel();
        contentPanel.add(userPanel);

        // AI suggested budget panel - right side
        JPanel aiPanel = createAISuggestedPanel();
        contentPanel.add(aiPanel);

        // Add both panels to the content area
        add(contentPanel, BorderLayout.CENTER);
        
        // Register as listener for currency and data refresh events
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
        DataRefreshManager.getInstance().addListener(this);
    }
    
    /**
     * Creates the header panel with title and overall budget progress
     * @return Configured header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        // Title label
        JLabel titleLabel = new JLabel("Budget Management");
        Font titleFont = new Font(titleLabel.getFont().getName(), Font.BOLD, HEADER_FONT_SIZE);
        titleLabel.setFont(titleFont);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Overall budget progress section
        JPanel overallPanel = new JPanel(new BorderLayout());
        
        // Calculate overall budget usage percentage
        double overallPercentage = financeData.getOverallBudgetPercentage();
        
        // Display percentage text
        JLabel overallLabel = new JLabel(String.format("Overall Budget: %.2f%% used", overallPercentage));
        Font labelFont = new Font(overallLabel.getFont().getName(), Font.BOLD, LABEL_FONT_SIZE);
        overallLabel.setFont(labelFont);
        overallPanel.add(overallLabel, BorderLayout.NORTH);
        
        // Add visual progress bar
        JProgressBar overallProgressBar = createProgressBar(overallPercentage);
        overallProgressBar.setPreferredSize(new Dimension(getWidth(), 15));
        overallPanel.add(overallProgressBar, BorderLayout.CENTER);
        
        headerPanel.add(overallPanel, BorderLayout.SOUTH);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        return headerPanel;
    }
    
    /**
     * Creates the user budget panel with current allocations
     * @return Configured user budget panel
     */
    private JPanel createUserBudgetPanel() {
        // Create panel with border layout
        JPanel userPanel = new JPanel(new BorderLayout());
        
        // Create titled border with larger font
        TitledBorder userBorder = BorderFactory.createTitledBorder("Your Budget Allocation");
        Font borderFont = new Font(getFont().getName(), Font.BOLD, 16);
        userBorder.setTitleFont(borderFont);
        
        // Add compound border with padding
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                userBorder,
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));

        // Create panel for budget categories with vertical box layout
        userBudgetsPanel = new JPanel();
        userBudgetsPanel.setLayout(new BoxLayout(userBudgetsPanel, BoxLayout.Y_AXIS));
        updateUserCategoryPanels();

        // Add scrolling capability
        JScrollPane userScrollPane = new JScrollPane(userBudgetsPanel);
        userScrollPane.setBorder(BorderFactory.createEmptyBorder());
        userScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        userPanel.add(userScrollPane, BorderLayout.CENTER);

        // Add "Add Category" button at bottom
        JPanel userButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Category");
        addButton.setIcon(UIManager.getIcon("Tree.addIcon"));
        addButton.addActionListener(e -> addNewCategory());
        userButtonPanel.add(addButton);
        userPanel.add(userButtonPanel, BorderLayout.SOUTH);
        
        return userPanel;
    }
    
    /**
     * Creates the AI suggested budget panel
     * @return Configured AI suggestion panel
     */
    private JPanel createAISuggestedPanel() {
        // Create panel with border layout
        JPanel aiPanel = new JPanel(new BorderLayout());
        
        // Create titled border with larger font
        TitledBorder aiBorder = BorderFactory.createTitledBorder("AI Suggested Budget");
        Font borderFont = new Font(getFont().getName(), Font.BOLD, 16);
        aiBorder.setTitleFont(borderFont);
        
        // Add compound border with padding
        aiPanel.setBorder(BorderFactory.createCompoundBorder(
                aiBorder,
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));

        // Create panel for AI suggestions with vertical box layout
        aiSuggestedPanel = new JPanel();
        aiSuggestedPanel.setLayout(new BoxLayout(aiSuggestedPanel, BoxLayout.Y_AXIS));
        updateAISuggestedPanels();

        // Add scrolling capability
        JScrollPane aiScrollPane = new JScrollPane(aiSuggestedPanel);
        aiScrollPane.setBorder(BorderFactory.createEmptyBorder());
        aiScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        aiPanel.add(aiScrollPane, BorderLayout.CENTER);

        // Add buttons for manipulating suggestions
        JPanel aiButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Shuffle button for generating new suggestions
        JButton shuffleButton = new JButton("Shuffle Suggestions");
        shuffleButton.setIcon(UIManager.getIcon("Table.descendingSortIcon"));
        shuffleButton.addActionListener(e -> shuffleAISuggestions());
        
        // Apply button to use AI suggestions
        JButton applyButton = new JButton("Apply Suggestions");
        applyButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        applyButton.addActionListener(e -> applyAISuggestions());
        
        aiButtonPanel.add(shuffleButton);
        aiButtonPanel.add(applyButton);
        aiPanel.add(aiButtonPanel, BorderLayout.SOUTH);
        
        return aiPanel;
    }

    /**
     * Updates the user budget category panels with current data
     */
    private void updateUserCategoryPanels() {
        userBudgetsPanel.removeAll();
        
        // Get current budget and expense data
        Map<String, Double> budgets = financeData.getCategoryBudgets();
        Map<String, Double> expenses = financeData.getCategoryExpenses();
        
        // Create panel for each budget category
        for (String category : budgets.keySet()) {
            double budget = budgets.get(category);
            double expense = expenses.getOrDefault(category, 0.0);
            
            // Calculate percentage of budget used
            double percentage = budget > 0 ? (expense / budget) * 100 : 0;
            
            // Create panel with edit and delete functionality
            BudgetCategoryPanel categoryPanel = new BudgetCategoryPanel(
                    category, budget, expense, percentage,
                    e -> editCategory(category),
                    e -> deleteCategory(category)
            );
            
            // Add panel and spacing
            userBudgetsPanel.add(categoryPanel);
            userBudgetsPanel.add(Box.createVerticalStrut(PADDING));
        }
        
        // Add total budget section at bottom
        double totalBudget = budgets.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalExpense = expenses.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Create total panel with top border
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        // Format total with current currency
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        String formattedTotal = String.format("<html><b>Total: %s%.2f</b></html>", currencySymbol, totalBudget);
        JLabel totalLabel = new JLabel(formattedTotal);
        Font boldFont = new Font(totalLabel.getFont().getName(), Font.BOLD, LABEL_FONT_SIZE);
        totalLabel.setFont(boldFont);
        totalPanel.add(totalLabel, BorderLayout.WEST);
        
        // Add spacing and total panel
        userBudgetsPanel.add(Box.createVerticalStrut(PADDING));
        userBudgetsPanel.add(totalPanel);
        
        // Refresh UI
        revalidate();
        repaint();
    }
    
    /**
     * Updates the AI suggested budget panels
     */
    private void updateAISuggestedPanels() {
        aiSuggestedPanel.removeAll();
        
        // Start with current budgets
        Map<String, Double> actualBudgets = financeData.getCategoryBudgets();
        double totalBudget = actualBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
        
        // Generate AI-suggested budget allocation
        Map<String, Double> suggestedBudgets = generateSuggestedBudgets(actualBudgets, totalBudget);
        
        // Display each category with comparison to actual budget
        for (String category : suggestedBudgets.keySet()) {
            double suggestedBudget = suggestedBudgets.get(category);
            double actualBudget = actualBudgets.getOrDefault(category, 0.0);
            
            // Calculate difference between suggested and actual
            double difference = suggestedBudget - actualBudget;
            
            // Create and add panel for this suggestion
            JPanel categoryPanel = createAISuggestionPanel(category, suggestedBudget, difference);
            aiSuggestedPanel.add(categoryPanel);
            aiSuggestedPanel.add(Box.createVerticalStrut(PADDING));
        }
        
        // Add total budget section at bottom
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        
        // Format total with current currency
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        String formattedTotal = String.format("<html><b>Total: %s%.2f</b></html>", currencySymbol, totalBudget);
        JLabel totalLabel = new JLabel(formattedTotal);
        Font boldFont = new Font(totalLabel.getFont().getName(), Font.BOLD, LABEL_FONT_SIZE);
        totalLabel.setFont(boldFont);
        totalPanel.add(totalLabel, BorderLayout.WEST);
        
        // Add spacing and total panel
        aiSuggestedPanel.add(Box.createVerticalStrut(PADDING));
        aiSuggestedPanel.add(totalPanel);
        
        // Refresh UI
        revalidate();
        repaint();
    }
    
    /**
     * Creates a panel for displaying an AI budget suggestion
     * @param category Budget category name
     * @param budget Suggested budget amount
     * @param difference Difference from current budget
     * @return Configured panel showing the suggestion
     */
    private JPanel createAISuggestionPanel(String category, double budget, double difference) {
        // Create main panel with horiztonal spacing
        JPanel panel = new JPanel(new BorderLayout(PADDING, 0));
        
        // Add border with padding
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));

        // Get current currency symbol for displaying amounts
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Left side - Category name and budget amount
        JPanel leftPanel = new JPanel(new BorderLayout());
        
        // Category name with bold font
        JLabel categoryLabel = new JLabel(category);
        Font boldFont = new Font(categoryLabel.getFont().getName(), Font.BOLD, LABEL_FONT_SIZE);
        categoryLabel.setFont(boldFont);
        leftPanel.add(categoryLabel, BorderLayout.NORTH);
        
        // Budget amount with regular font
        String formattedBudget = String.format("%s%.2f", currencySymbol, budget);
        JLabel budgetLabel = new JLabel(formattedBudget);
        Font regularFont = new Font(budgetLabel.getFont().getName(), Font.PLAIN, LABEL_FONT_SIZE);
        budgetLabel.setFont(regularFont);
        leftPanel.add(budgetLabel, BorderLayout.SOUTH);
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Difference indicator
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        // Constants for comparison
        final double EPSILON = 0.01;  // Threshold for "no change"
        
        // Format difference text and determine appropriate color
        String diffText;
        Color diffColor;
        
        if (Math.abs(difference) < EPSILON) {
            // No significant change
            diffText = "No change";
            diffColor = Color.GRAY;
        } else if (difference > 0) {
            // Suggested budget is higher than current
            diffText = String.format("+%s%.2f", currencySymbol, difference);
            diffColor = new Color(46, 204, 113); // Green color for increase
        } else {
            // Suggested budget is lower than current
            diffText = String.format("-%s%.2f", currencySymbol, Math.abs(difference));
            diffColor = new Color(231, 76, 60); // Red color for decrease
        }
        
        // Create and style difference label
        JLabel diffLabel = new JLabel(diffText);
        diffLabel.setForeground(diffColor);
        diffLabel.setFont(boldFont);
        rightPanel.add(diffLabel, BorderLayout.CENTER);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Generates suggested budget allocations based on current budgets
     * @param currentBudgets Current budget allocations
     * @param totalBudget Total budget amount
     * @return Map of suggested budget allocations
     */
    private Map<String, Double> generateSuggestedBudgets(Map<String, Double> currentBudgets, double totalBudget) {
        // Create result map
        Map<String, Double> suggestedBudgets = new LinkedHashMap<>();
        
        // Build string representation of current budgets for AI prompt
        StringBuilder budgetBuilder = new StringBuilder();
        for (Map.Entry<String, Double> entry : currentBudgets.entrySet()) {
            budgetBuilder.append(entry.getKey())
                .append(": ")
                .append(entry.getValue())
                .append("; ");
        }
        
        // Remove trailing separator if present
        if (budgetBuilder.length() >= 2) {
            budgetBuilder.setLength(budgetBuilder.length() - 2);  // Remove final "; "
        }
        
        // Get current budget allocation as string
        String budgetString = budgetBuilder.toString();
        System.out.println(budgetString);
        
        // Format prompt for AI suggesting budget reallocation
        String aiPrompt = String.format(
            "当前预算分配如下：%s。总预算为 %.2f。"
          + " 请在总金额不变的情况下，将总预算在各类中重新分配，给出一个更合理的预算分配方案。"
          + " 请以 JSON 格式输出，键是类别名称，值是对应金额，此外不要输出其它任何内容。",
            budgetString,
            totalBudget
        );

        try {
            // API key for AI service
            String API_KEY = "sk-fdf26a37926f46ab8d4884c2cd533db8";
            
            // Get response from AI service
            String response = new getRes().getResponse(API_KEY, aiPrompt);
            
            // Parse AI response
            String jsonResponse = new getRes().parseAIResponse(response);
            System.out.println(jsonResponse);
            
            // Clean up response text
            jsonResponse = cleanupAIResponse(jsonResponse);
            
            // Parse JSON into budget allocations
            JSONObject json = new JSONObject(jsonResponse);
            
            // For each existing category, get suggestion or keep current
            for (String category : currentBudgets.keySet()) {
                double suggestedValue = json.has(category)
                        ? json.getDouble(category)
                        : currentBudgets.get(category);
                suggestedBudgets.put(category, suggestedValue);
            }
        } catch (Exception e) {
            // Log error and fall back to current budgets
            System.err.println("AI 建议生成失败: " + e.getMessage());
            suggestedBudgets = new HashMap<>(currentBudgets);
        }
        
        return suggestedBudgets;
    }
    
    /**
     * Cleans up AI response text to extract JSON content
     * @param response Raw AI response text
     * @return Cleaned JSON string
     */
    private String cleanupAIResponse(String response) {
        // 1. Trim whitespace
        String result = response.trim();

        // 2. Remove opening code block markers and language identifier
        if (result.startsWith("```")) {
            int firstNewline = result.indexOf('\n');
            if (firstNewline != -1) {
                result = result.substring(firstNewline + 1).trim();
            } else {
                result = "";
            }
        }
        
        // 3. Remove closing code block markers
        if (result.endsWith("```")) {
            int lastBackticks = result.lastIndexOf("```");
            result = result.substring(0, lastBackticks).trim();
        }

        // 4. Remove "json" prefix if present
        if (result.startsWith("json")) {
            int braceIndex = result.indexOf('{');
            if (braceIndex != -1) {
                result = result.substring(braceIndex).trim();
            }
        }
        
        return result;
    }
    
    /**
     * Creates a progress bar for showing budget usage
     * @param percentage Usage percentage (0-100)
     * @return Configured progress bar
     */
    private JProgressBar createProgressBar(double percentage) {
        // Create progress bar with proper range
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue((int) percentage);
        progressBar.setStringPainted(true);
        
        // Set color based on percentage value
        if (percentage < 80) {
            // Below 80% - Good (green)
            progressBar.setForeground(new Color(46, 204, 113));
        } else if (percentage < 100) {
            // 80-100% - Warning (yellow)
            progressBar.setForeground(new Color(241, 196, 15));
        } else {
            // Over 100% - Overspent (red)
            progressBar.setForeground(new Color(231, 76, 60));
        }
        
        return progressBar;
    }
    
    /**
     * Opens dialog to add a new budget category
     */
    private void addNewCategory() {
        // Get current currency for display
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();

        // Show dialog to collect category and budget amount
        BudgetDialog dialog = new BudgetDialog(SwingUtilities.getWindowAncestor(this), "Add Category", "", 0.0);
        if (dialog.showDialog()) {
            // Get user input
            String category = dialog.getCategory();
            double budget = dialog.getBudget();
            
            // Update data model and save to file
            financeData.updateCategoryBudget(category, budget);
            
            // Refresh UI
            updateUserCategoryPanels();
            updateAISuggestedPanels();
            
            // Show confirmation message
            JOptionPane.showMessageDialog(this, 
                    "New category added: " + category + " Budget: " + currencySymbol + budget,
                    "Category Added", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Opens dialog to edit an existing budget category
     * @param category Category name to edit
     */
    private void editCategory(String category) {
        // Get current currency for display
        String currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();

        // Get current budget for this category
        double currentBudget = financeData.getCategoryBudget(category);
        
        // Show dialog pre-filled with current values
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Edit Category", 
                category, 
                currentBudget);
        
        if (dialog.showDialog()) {
            // Get updated budget amount
            double newBudget = dialog.getBudget();
            
            // Update data model and save to file
            financeData.updateCategoryBudget(category, newBudget);
            
            // Refresh UI
            updateUserCategoryPanels();
            updateAISuggestedPanels();
            
            // Show confirmation message
            JOptionPane.showMessageDialog(this, 
                    "Category updated: " + category + " New budget: " + currencySymbol + newBudget,
                    "Category Updated", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Deletes an existing budget category after confirmation
     * @param category Category name to delete
     */
    private void deleteCategory(String category) {
        // Show confirmation dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete category: " + category + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Delete from data model and save to file
            boolean success = financeData.deleteCategoryBudget(category);
            
            if (success) {
                // Refresh UI
                updateUserCategoryPanels();
                updateAISuggestedPanels();
                
                // Show confirmation message
                JOptionPane.showMessageDialog(this, 
                        "Category deleted: " + category,
                        "Category Deleted", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    /**
     * Generates new AI budget suggestions
     */
    private void shuffleAISuggestions() {
        // Generate new suggestions and update UI
        updateAISuggestedPanels();
        
        // Show confirmation message
        JOptionPane.showMessageDialog(this,
                "New AI budget suggestions generated!",
                "Suggestions Updated",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Applies the current AI suggestions to the user's budget
     */
    private void applyAISuggestions() {
        // Show confirmation dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to apply the AI-suggested budget allocation to your budget?",
                "Apply AI Suggestions",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Get current budgets and total
            Map<String, Double> actualBudgets = financeData.getCategoryBudgets();
            double totalBudget = actualBudgets.values().stream().mapToDouble(Double::doubleValue).sum();
            
            // Generate AI suggestions
            Map<String, Double> suggestedBudgets = generateSuggestedBudgets(actualBudgets, totalBudget);
            
            // Apply each suggestion to data model
            for (Map.Entry<String, Double> entry : suggestedBudgets.entrySet()) {
                financeData.updateCategoryBudget(entry.getKey(), entry.getValue());
            }
            
            // Refresh UI
            updateUserCategoryPanels();
            updateAISuggestedPanels();
            
            // Show confirmation message
            JOptionPane.showMessageDialog(this,
                    "AI-suggested budget has been applied to your budget allocation!",
                    "Suggestions Applied",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Loads transaction data from CSV file
     */
    private void loadTransactionData() {
        // Build path to user-specific transaction file
        String csvFilePath = ".\\user_data\\" + username + "\\user_bill.csv";
        
        // Import transactions from CSV
        List<Object[]> transactions = CSVDataImporter.importTransactionsFromCSV(csvFilePath);

        if (!transactions.isEmpty()) {
            // Update finance data with imported transactions
            financeData.importTransactions(transactions);
            System.out.println("Successfully imported " + transactions.size() + " transactions");
        } else {
            System.err.println("No transactions were imported");
        }
    }

    /**
     * Handles currency change events
     */
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // Refresh both panels when currency changes
        updateUserCategoryPanels();
        updateAISuggestedPanels();
    }

    /**
     * Handles data refresh events
     */
    @Override
    public void onDataRefresh(DataRefreshManager.RefreshType type) {
        // Check refresh type
        if (type == DataRefreshManager.RefreshType.BUDGETS || 
            type == DataRefreshManager.RefreshType.TRANSACTIONS || 
            type == DataRefreshManager.RefreshType.ALL) {
            
            // Reload transaction data if needed
            if (type == DataRefreshManager.RefreshType.TRANSACTIONS || 
                type == DataRefreshManager.RefreshType.ALL) {
                loadTransactionData();
            }
            
            // Refresh UI components
            updateUserCategoryPanels();
            updateAISuggestedPanels();
        }
    }

    /**
     * Unregisters listeners when component is removed
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up by removing listeners
        DataRefreshManager.getInstance().removeListener(this);
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}


