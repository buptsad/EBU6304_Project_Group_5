package com.example.app.ui.pages;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    public SettingsPanel() {
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Settings", JLabel.CENTER);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel contentLabel = new JLabel("Settings Content Goes Here");
        centerPanel.add(contentLabel);
        
        add(centerPanel, BorderLayout.CENTER);
    }
}