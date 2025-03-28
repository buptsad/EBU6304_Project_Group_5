package com.example.app.ui.pages;

import javax.swing.*;
import java.awt.*;

public class TransactionsPanel extends JPanel {
    public TransactionsPanel() {
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Transactions", JLabel.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel contentLabel = new JLabel("Transactions Content Goes Here");
        centerPanel.add(contentLabel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
}