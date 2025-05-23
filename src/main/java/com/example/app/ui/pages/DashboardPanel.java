package com.example.app.ui.pages;

import com.example.app.ui.CurrencyManager;
import com.example.app.ui.CurrencyManager.CurrencyChangeListener;
import com.example.app.ui.dashboard.*;
import com.example.app.viewmodel.pages.DashboardViewModel;
import com.example.app.viewmodel.pages.DashboardViewModel.DashboardChangeListener;
import com.formdev.flatlaf.ui.FlatButtonBorder;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

public class DashboardPanel extends JPanel implements CurrencyChangeListener, DashboardChangeListener {
    // ViewModel reference
    private final DashboardViewModel viewModel;
    
    // UI components
    private JScrollPane contentScrollPane;
    private JPanel contentPanel;
    private JPanel summaryPanel;
    private CardLayout cardLayout;
    private JButton overviewButton;
    private JButton transactionsButton;
    private JButton budgetsButton;
    private JButton reportsButton;
    
    // Colors for button states
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);
    private static final Color HOVER_COLOR = new Color(100, 149, 237);
    
    public DashboardPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new DashboardViewModel(username);
        this.viewModel.addChangeListener(this);
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Welcome section
        JLabel welcomeLabel = new JLabel("Welcome to Your Financial Dashboard");
        welcomeLabel.setFont(new Font(welcomeLabel.getFont().getName(), Font.BOLD, 22));
        add(welcomeLabel, BorderLayout.NORTH);
        
        // Main container panel - use BoxLayout for vertical stacking with flexible sizing
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Create summary panel using data from ViewModel
        summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Create a panel to hold the buttons and content panel
        JPanel contentContainer = new JPanel(new BorderLayout(0, 5));
        
        // Sub-navigation buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        contentContainer.add(buttonsPanel, BorderLayout.NORTH);
        
        // Content panel with card layout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        
        // Add sub-panels to the card layout - these sub-panels already use MVVM pattern
        contentPanel.add(new OverviewPanel(username), DashboardViewModel.OVERVIEW_PANEL);
        contentPanel.add(new DashboardTransactionsPanel(username), DashboardViewModel.TRANSACTIONS_PANEL);
        contentPanel.add(new DashboardBudgetsPanel(username), DashboardViewModel.BUDGETS_PANEL);
        contentPanel.add(new DashboardReportsPanel(username), DashboardViewModel.REPORTS_PANEL);
        
        // Wrap contentPanel in a JScrollPane with appropriate scroll policies
        contentScrollPane = new JScrollPane(contentPanel);
        contentScrollPane.setBorder(null);
        contentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentContainer.add(contentScrollPane, BorderLayout.CENTER);
        
        mainPanel.add(contentContainer);
        
        // Use a scroll pane for the entire dashboard to handle window resizing
        JScrollPane mainScrollPane = new JScrollPane(mainPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(mainScrollPane, BorderLayout.CENTER);
        
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
        
        // Set initial active panel from ViewModel
        updateActivePanelUI(viewModel.getActivePanel());
        
        // Register as currency change listener
        CurrencyManager.getInstance().addCurrencyChangeListener(this);
    }
    
    private JPanel createSummaryPanel() {
        // Use GridBagLayout for more flexible layout that adapts to window size
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Create the four summary panels with equal spacing using data from ViewModel
        gbc.gridx = 0;
        panel.add(createSummaryBox("Total Balance", viewModel.getTotalBalance(), new Color(65, 105, 225)), gbc);
        
        gbc.gridx = 1;
        panel.add(createSummaryBox("Total Income", viewModel.getTotalIncome(), new Color(46, 139, 87)), gbc);
        
        gbc.gridx = 2;
        panel.add(createSummaryBox("Total Expenses", viewModel.getTotalExpenses(), new Color(178, 34, 34)), gbc);
        
        gbc.gridx = 3;
        panel.add(createSummaryBox("Total Savings", viewModel.getTotalSavings(), new Color(218, 165, 32)), gbc);
        
        return panel;
    }
    
    private JPanel createSummaryBox(String title, double amount, Color accentColor) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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
        overviewButton = createStyledButton("Overview", DashboardViewModel.OVERVIEW_PANEL);
        transactionsButton = createStyledButton("Transactions", DashboardViewModel.TRANSACTIONS_PANEL);
        budgetsButton = createStyledButton("Budgets", DashboardViewModel.BUDGETS_PANEL);
        reportsButton = createStyledButton("Reports", DashboardViewModel.REPORTS_PANEL);
        
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
        
        // Add action listener - use ViewModel to change active panel
        button.addActionListener(e -> viewModel.setActivePanel(panelName));
        
        return button;
    }
    
    /**
     * Update UI to reflect the active panel
     */
    private void updateActivePanelUI(String panelName) {
        // Reset all buttons
        overviewButton.setForeground(UIManager.getColor("Button.foreground"));
        transactionsButton.setForeground(UIManager.getColor("Button.foreground"));
        budgetsButton.setForeground(UIManager.getColor("Button.foreground"));
        reportsButton.setForeground(UIManager.getColor("Button.foreground"));
        
        // Highlight selected button
        switch (panelName) {
            case DashboardViewModel.OVERVIEW_PANEL:
                overviewButton.setForeground(SELECTED_COLOR);
                break;
            case DashboardViewModel.TRANSACTIONS_PANEL:
                transactionsButton.setForeground(SELECTED_COLOR);
                break;
            case DashboardViewModel.BUDGETS_PANEL:
                budgetsButton.setForeground(SELECTED_COLOR);
                break;
            case DashboardViewModel.REPORTS_PANEL:
                reportsButton.setForeground(SELECTED_COLOR);
                break;
        }
        
        // Show selected panel
        cardLayout.show(contentPanel, panelName);
    }
    
    // Implement listener methods
    
    @Override
    public void onCurrencyChanged(String currencyCode, String currencySymbol) {
        // When currency changes, refresh summary panel
        refreshSummaryPanel();
    }
    
    @Override
    public void onSummaryDataChanged() {
        // When summary data changes, refresh summary panel
        SwingUtilities.invokeLater(this::refreshSummaryPanel);
    }
    
    @Override
    public void onActivePanelChanged(String panelName) {
        // When active panel changes, update UI
        SwingUtilities.invokeLater(() -> updateActivePanelUI(panelName));
    }
    
    /**
     * Refresh the summary panel with new data
     */
    private void refreshSummaryPanel() {
        Container parent = summaryPanel.getParent();
        if (parent != null) {
            parent.remove(summaryPanel);
            summaryPanel = createSummaryPanel();
            
            if (parent instanceof JPanel) {
                JPanel parentPanel = (JPanel) parent;
                if (parentPanel.getLayout() instanceof BoxLayout) {
                    parentPanel.add(summaryPanel, 0);
                } else {
                    parentPanel.add(summaryPanel, BorderLayout.NORTH);
                }
            }
            
            parent.revalidate();
            parent.repaint();
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed from UI
        CurrencyManager.getInstance().removeCurrencyChangeListener(this);
        viewModel.removeChangeListener(this);
        viewModel.cleanup();
    }
}