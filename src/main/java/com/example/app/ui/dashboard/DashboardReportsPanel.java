package com.example.app.ui.dashboard;

import javax.swing.*;

import com.example.app.ui.dashboard.report.CategorySpendingChartPanel;
import com.example.app.ui.dashboard.report.IncomeExpensesChartPanel;

import java.awt.*;

public class DashboardReportsPanel extends JPanel {
    public DashboardReportsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel titleLabel = new JLabel("Financial Reports");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);
        
        // Create panel to hold both charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add the two chart panels
        chartsPanel.add(new IncomeExpensesChartPanel());
        chartsPanel.add(new CategorySpendingChartPanel());
        
        // Add to main panel with scroll support
        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }
}