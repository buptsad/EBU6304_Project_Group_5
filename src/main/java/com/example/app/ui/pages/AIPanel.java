package com.example.app.ui.pages;

import javax.swing.*;
import java.awt.*;

public class AIPanel extends JPanel {
    public AIPanel() {
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("AI Assistant", JLabel.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel contentLabel = new JLabel("AI Assistant Content Goes Here");
        centerPanel.add(contentLabel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
}