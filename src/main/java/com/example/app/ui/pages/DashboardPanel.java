package com.example.app.ui.pages;

import com.example.app.model.FinanceData;
import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.ui.dashboard.*;
import com.formdev.flatlaf.ui.FlatButtonBorder;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

public class DashboardPanel extends JPanel implements CurrencyChangeListener {
    private JScrollPane contentScrollPane;
    private JPanel contentPanel;
    private JPanel summaryPanel; // Declare summaryPanel as a class-level field
    private CardLayout cardLayout;
    private JButton overviewButton;
    private JButton transactionsButton;
    private JButton budgetsButton;
    private JButton reportsButton;
    
    // Constants for card layout
    private static final String OVERVIEW_PANEL = "OVERVIEW";
    private static final String TRANSACTIONS_PANEL = "TRANSACTIONS";
    private static final String BUDGETS_PANEL = "BUDGETS";
    private static final String REPORTS_PANEL = "REPORTS";
    
    // Colors for button states
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);
    private static final Color HOVER_COLOR = new Color(100, 149, 237);
    private static final Color DEFAULT_COLOR = UIManager.getColor("Button.background");
    
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15)); // Reduced border padding
        
        // Welcome section
        JLabel welcomeLabel = new JLabel("Welcome to Your Financial Dashboard");
        welcomeLabel.setFont(new Font(welcomeLabel.getFont().getName(), Font.BOLD, 22));
        add(welcomeLabel, BorderLayout.NORTH);
        
        // Main container panel - use BoxLayout for vertical stacking with flexible sizing
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 0, 0, 0)); // Reduced top padding
        
        summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between panels
        
        // Create a panel to hold the buttons and content panel
        JPanel contentContainer = new JPanel(new BorderLayout(0, 5)); // Reduced spacing

        // Sub-navigation buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        contentContainer.add(buttonsPanel, BorderLayout.NORTH);
        
        // Content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Add sub-panels to the card layout
        contentPanel.add(new OverviewPanel(), OVERVIEW_PANEL);
        contentPanel.add(new DashboardTransactionsPanel(), TRANSACTIONS_PANEL);
        contentPanel.add(new DashboardBudgetsPanel(), BUDGETS_PANEL);
        contentPanel.add(new DashboardReportsPanel(), REPORTS_PANEL);
        
        // Wrap contentPanel in a JScrollPane with appropriate scroll policies
        contentScrollPane = new JScrollPane(contentPanel);
        contentScrollPane.setBorder(null);
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Allow horizontal scrolling when needed
        contentScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smoother scrolling
        
        contentContainer.add(contentScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(contentContainer);
        
        // Use a scroll pane for the entire dashboard to handle window resizing
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(mainScrollPane, BorderLayout.CENTER);
        
        // Set default view
        setActivePanel(OVERVIEW_PANEL);
        
        // Create a ButtonGroup
        ButtonGroup buttonGroup = new ButtonGroup();
        
        // Add your buttons to the group
        buttonGroup.add(overviewButton);
        buttonGroup.add(transactionsButton);
        buttonGroup.add(budgetsButton);
        buttonGroup.add(reportsButton);
        
        // Create a consistent look for the buttons
        for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
            AbstractButton button = buttons.nextElement();
            
            // Set consistent styling
            button.setFocusPainted(false);
            button.setBorderPainted(true);
            button.setContentAreaFilled(false);
            
            // Add hover effect
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setContentAreaFilled(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    button.setContentAreaFilled(false);
                }
            });
            
            // Optional: Add a nicer border
            button.setBorder(new CompoundBorder(
                new FlatButtonBorder(),
                new EmptyBorder(5, 10, 5, 10)
            ));
        }
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    private void createSummaryPanels() {
        // 获取父容器
        JPanel mainPanel = (JPanel) getComponent(0);
        
        // 移除旧的summaryPanel
        Component[] components = mainPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JPanel && component.getName() != null && 
                    component.getName().equals("summaryPanel")) {
                mainPanel.remove(component);
                break;
            }
        }
        
        // 创建新的summaryPanel
        JPanel summaryPanel = createSummaryPanel();
        summaryPanel.setName("summaryPanel"); // 设置名称以便识别
        mainPanel.add(summaryPanel, BorderLayout.NORTH);
        
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private JPanel createSummaryPanel() {
        // Use GridBagLayout for more flexible layout that adapts to window size
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Get financial data
        FinanceData financeData = new FinanceData();
        
        // Create the four summary panels with equal spacing
        gbc.gridx = 0;
        panel.add(createSummaryBox("Total Balance", financeData.getTotalBalance(), new Color(65, 105, 225)), gbc);
        
        gbc.gridx = 1;
        panel.add(createSummaryBox("Total Income", financeData.getTotalIncome(), new Color(46, 139, 87)), gbc);
        
        gbc.gridx = 2;
        panel.add(createSummaryBox("Total Expenses", financeData.getTotalExpenses(), new Color(178, 34, 34)), gbc);
        
        gbc.gridx = 3;
        panel.add(createSummaryBox("Total Savings", financeData.getTotalSavings(), new Color(218, 165, 32)), gbc);
        
        return panel;
    }
    
    private JPanel createSummaryBox(String title, double amount, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Reduced padding
        ));
        
        // Set minimum and preferred sizes to help with scaling
        panel.setMinimumSize(new Dimension(180, 80));
        panel.setPreferredSize(new Dimension(220, 100));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(accentColor);
        
        String formattedAmount = CurrencyManager.getInstance().formatCurrency(amount);
        JLabel amountLabel = new JLabel(formattedAmount);
        amountLabel.setFont(new Font(amountLabel.getFont().getName(), Font.BOLD, 18));
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(amountLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        // Create buttons with hover effect
        overviewButton = createStyledButton("Overview", OVERVIEW_PANEL);
        transactionsButton = createStyledButton("Transactions", TRANSACTIONS_PANEL);
        budgetsButton = createStyledButton("Budgets", BUDGETS_PANEL);
        reportsButton = createStyledButton("Reports", REPORTS_PANEL);
        
        panel.add(overviewButton);
        panel.add(transactionsButton);
        panel.add(budgetsButton);
        panel.add(reportsButton);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(8, 15, 8, 15));
        
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getForeground().equals(SELECTED_COLOR)) {
                    button.setForeground(HOVER_COLOR);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!button.getForeground().equals(SELECTED_COLOR)) {
                    button.setForeground(UIManager.getColor("Button.foreground"));
                }
            }
        });
        
        // Add action listener
        button.addActionListener(e -> setActivePanel(panelName));
        
        return button;
    }
    
    private void setActivePanel(String panelName) {
        // Reset all buttons
        overviewButton.setForeground(UIManager.getColor("Button.foreground"));
        transactionsButton.setForeground(UIManager.getColor("Button.foreground"));
        budgetsButton.setForeground(UIManager.getColor("Button.foreground"));
        reportsButton.setForeground(UIManager.getColor("Button.foreground"));
        
        // Highlight selected button
        switch (panelName) {
            case OVERVIEW_PANEL:
                overviewButton.setForeground(SELECTED_COLOR);
                break;
            case TRANSACTIONS_PANEL:
                transactionsButton.setForeground(SELECTED_COLOR);
                break;
            case BUDGETS_PANEL:
                budgetsButton.setForeground(SELECTED_COLOR);
                break;
            case REPORTS_PANEL:
                reportsButton.setForeground(SELECTED_COLOR);
                break;
        }
        
        // Show selected panel
        cardLayout.show(contentPanel, panelName);
    }

    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // 当货币变化时，重新创建摘要面板
        try {
            // 获取摘要面板的父容器
            Container parent = summaryPanel.getParent();
            if (parent != null) {
                // 从父容器中移除旧的摘要面板
                parent.remove(summaryPanel);
                
                // 创建新的摘要面板
                summaryPanel = createSummaryPanel();
                
                // 将新的摘要面板添加到父容器
                if (parent instanceof JPanel) {
                    JPanel parentPanel = (JPanel) parent;
                    if (parentPanel.getLayout() instanceof BorderLayout) {
                        parentPanel.add(summaryPanel, BorderLayout.NORTH);
                    } else {
                        parentPanel.add(summaryPanel);
                    }
                }
                
                // 重新验证和重绘
                parent.revalidate();
                parent.repaint();
            }
        } catch (Exception e) {
            System.err.println("Error updating summary panels: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除组件时取消监听
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
    }
}