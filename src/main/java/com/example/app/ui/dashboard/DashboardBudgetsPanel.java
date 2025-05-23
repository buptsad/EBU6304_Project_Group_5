package com.example.app.ui.dashboard;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.viewmodel.dashboard.DashboardBudgetsViewModel;
import com.example.app.viewmodel.dashboard.DashboardBudgetsViewModel.BudgetChangeListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.logging.Logger;

public class DashboardBudgetsPanel extends JPanel implements CurrencyChangeListener, BudgetChangeListener {
    private static final Logger LOGGER = Logger.getLogger(DashboardBudgetsPanel.class.getName());
    
    // ViewModel reference
    private final DashboardBudgetsViewModel viewModel;
    
    // UI components
    private final JPanel categoriesPanel;
    private String currencySymbol;
    
    public DashboardBudgetsPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new DashboardBudgetsViewModel(username);
        this.viewModel.addBudgetChangeListener(this);
        
        // Initialize currency symbol
        this.currencySymbol = CurrencyManager.getInstance().getCurrencySymbol();
        
        // Set up UI
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Budget Management");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Overall budget progress
        JPanel overallPanel = new JPanel(new BorderLayout());
        double overallPercentage = viewModel.getOverallBudgetPercentage();
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
        categoriesPanel = new JPanel(new BorderLayout());
        // 设置首选大小和最大大小
        categoriesPanel.setPreferredSize(new Dimension(400, 200));
        categoriesPanel.setMaximumSize(new Dimension(600, 400));
        
        // 创建表格模型
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只允许编辑按钮列
                return column == 4;
            }
        };
        tableModel.addColumn("Category");
        tableModel.addColumn("Budget");
        tableModel.addColumn("Spent");
        tableModel.addColumn("Usage");
        tableModel.addColumn("Actions");

        // 创建表格并设置属性
        JTable budgetTable = new JTable(tableModel);
        budgetTable.setRowHeight(40);
        budgetTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        budgetTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        budgetTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        budgetTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        budgetTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        // 添加表格网格线设置
        budgetTable.setShowGrid(true);
        budgetTable.setGridColor(Color.GRAY);
        budgetTable.setIntercellSpacing(new Dimension(1, 1));
        budgetTable.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // 为进度条列添加自定义渲染器
        budgetTable.getColumnModel().getColumn(3).setCellRenderer(new ProgressBarRenderer());
        
        // 为操作按钮列添加自定义渲染器和编辑器
        budgetTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        budgetTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // 将表格添加到滚动面板
        JScrollPane tableScrollPane = new JScrollPane(budgetTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        categoriesPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Add category panels
        updateCategoryTable(budgetTable);
        
        JScrollPane scrollPane = new JScrollPane(categoriesPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        // Register as listeners
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    // 更新类别表格数据
    private void updateCategoryTable(JTable table) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // 清空现有行
        
        Map<String, Double> budgets = viewModel.getCategoryBudgets();
        Map<String, Double> expenses = viewModel.getCategoryExpenses();
        
        for (String category : budgets.keySet()) {
            double budget = budgets.get(category);
            double expense = expenses.getOrDefault(category, 0.0);
            double percentage = budget > 0 ? (expense / budget) * 100 : 0;
            
            model.addRow(new Object[]{
                category,
                currencySymbol + String.format("%.2f", budget),
                currencySymbol + String.format("%.2f", expense),
                percentage,
                "" // 操作按钮列占位
            });
        }
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
    
    private void editCategory(String category) {
        double currentBudget = viewModel.getCategoryBudget(category);
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Edit Category", 
                category, 
                currentBudget);
        
        if (dialog.showDialog()) {
            double newBudget = dialog.getBudget();
            
            // Update through view model
            viewModel.updateCategoryBudget(category, newBudget);

            JOptionPane.showMessageDialog(this,
                    "Category updated: " + category + " New budget: " + currencySymbol + newBudget,
                    "Category Updated",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void deleteCategory(String category) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete category: " + category + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        
        if (result == JOptionPane.YES_OPTION) {
            // Delete through view model
            if (viewModel.deleteCategoryBudget(category)) {
                JOptionPane.showMessageDialog(this,
                        "Category deleted: " + category,
                        "Category Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void addNewCategory() {
        BudgetDialog dialog = new BudgetDialog(
                SwingUtilities.getWindowAncestor(this), 
                "Add Category", 
                "", 
                0.0);
                
        if (dialog.showDialog()) {
            String category = dialog.getCategory();
            double budget = dialog.getBudget();
            
            // Update through view model
            viewModel.updateCategoryBudget(category, budget);

            JOptionPane.showMessageDialog(this,
                    "New category added: " + category + " Budget: " + currencySymbol + budget,
                    "Category Added",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void onBudgetDataChanged() {
        // 更新UI当视图模型通知预算变化时
        updateCategoryTable((JTable)((JScrollPane)categoriesPanel.getComponent(0)).getViewport().getView());
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        this.currencySymbol = currencySymbol;
        updateCategoryTable((JTable)((JScrollPane)categoriesPanel.getComponent(0)).getViewport().getView());
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed from UI
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeBudgetChangeListener(this);
        viewModel.cleanup();
    }
    
    // Public method to update username if needed
    public void setUsername(String username) {
        // In MVVM, we'd create a new ViewModel for the new user
        // For simplicity, we'll replace the entire panel in the parent component
        
        // Let the parent container know that it needs to recreate this panel
        Container parent = getParent();
        if (parent != null) {
            parent.remove(this);
            parent.add(new DashboardBudgetsPanel(username));
            parent.revalidate();
            parent.repaint();
        }
    }
    
    // Add a button to open the full budget panel view
    private void openFullBudgetPanel() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof JFrame) {
            JFrame frame = (JFrame) window;
            // Code to navigate to the full BudgetsPanel would go here
            JOptionPane.showMessageDialog(frame, 
                "Navigate to full Budget Management", 
                "Navigation", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // 进度条单元格渲染器
    class ProgressBarRenderer extends JProgressBar implements javax.swing.table.TableCellRenderer {
        public ProgressBarRenderer() {
            super(0, 100);
            setStringPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected, boolean hasFocus,
                                                     int row, int column) {
            double percentage = (Double) value;
            setValue((int) percentage);
            
            // 根据百分比设置颜色
            if (percentage < 80) {
                setForeground(new Color(46, 204, 113)); // 绿色
            } else if (percentage < 100) {
                setForeground(new Color(241, 196, 15)); // 黄色
            } else {
                setForeground(new Color(231, 76, 60));  // 红色
            }
            
            setString(String.format("%.2f%%", percentage));
            return this;
        }
    }

    // 按钮渲染器
    class ButtonRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton editButton;
        private JButton deleteButton;
        
        public ButtonRenderer() {
            setLayout(new GridLayout(1, 2, 5, 0));
            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");
            add(editButton);
            add(deleteButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column) {
            return this;
        }
    }

    // 按钮编辑器
    class ButtonEditor extends DefaultCellEditor {
        protected JPanel panel;
        protected JButton editButton;
        protected JButton deleteButton;
        private String category;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new GridLayout(1, 2, 5, 0));
            editButton = new JButton("Edit");
            deleteButton = new JButton("Delete");

            editButton.addActionListener(e -> {
                fireEditingStopped();
                // 获取当前行的类别名称
                editCategory(category);
            });
            
            deleteButton.addActionListener(e -> {
                fireEditingStopped();
                // 获取当前行的类别名称
                deleteCategory(category);
            });
            
            panel.add(editButton);
            panel.add(deleteButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                  boolean isSelected, int row, int column) {
            // 获取当前行的类别名称
            category = (String) table.getValueAt(row, 0);
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}