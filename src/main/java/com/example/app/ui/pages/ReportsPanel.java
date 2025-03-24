package com.example.app.ui.pages;

import javax.swing.*;
import java.awt.*;

public class ReportsPanel extends JPanel {
    public ReportsPanel() {
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Reports", JLabel.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel contentLabel = new JLabel("Reports Content Goes Here");
        centerPanel.add(contentLabel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
}