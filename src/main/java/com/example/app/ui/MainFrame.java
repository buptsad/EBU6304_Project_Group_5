package com.example.app.ui;

import com.example.app.ui.pages.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * The MainFrame is the main window of the financial application.
 * It provides navigation between all major pages (Dashboard, Transactions, Budgets, Reports, AI, Settings)
 * and manages the layout and navigation bar.
 * <p>
 * Features:
 * <ul>
 *   <li>Navigation bar with page highlighting</li>
 *   <li>CardLayout for switching between pages</li>
 *   <li>Passes the current username to all sub-pages</li>
 *   <li>Responsive UI with consistent styling</li>
 * </ul>
 
 */
public class MainFrame extends JFrame {
    /** The main content panel using CardLayout */
    private JPanel contentPanel;
    /** The CardLayout for switching pages */
    private CardLayout cardLayout;
    /** Map of navigation buttons keyed by page constant */
    private Map<String, JButton> navButtons = new HashMap<>();
    /** The current page key */
    private String currentPage;
    /** The current logged-in user */
    private String currentUser;

    // Page constants
    private static final String DASHBOARD_PAGE = "DASHBOARD";
    private static final String TRANSACTIONS_PAGE = "TRANSACTIONS";
    private static final String BUDGETS_PAGE = "BUDGETS";
    private static final String REPORTS_PAGE = "REPORTS";
    private static final String AI_PAGE = "AI";
    private static final String SETTINGS_PAGE = "SETTINGS";

    // Colors and borders for navigation highlighting
    private static final Color SELECTED_COLOR = new Color(70, 130, 180);
    private static final Border SELECTED_BORDER = new MatteBorder(0, 0, 2, 0, SELECTED_COLOR);
    private static final Border DEFAULT_BORDER = new EmptyBorder(0, 0, 2, 0);

    /**
     * Constructs the MainFrame for the specified user.
     *
     * @param username the username of the current user
     */
    public MainFrame(String username) {
        this.currentUser = username;
        setTitle("Finance Manager - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 900);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Create navigation bar
        JPanel navBar = createNavigationBar();
        mainPanel.add(navBar, BorderLayout.NORTH);

        // Create content panel with CardLayout to switch between pages
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add all pages to content panel
        contentPanel.add(new DashboardPanel(username), DASHBOARD_PAGE);
        contentPanel.add(new TransactionsPanel(username), TRANSACTIONS_PAGE);
        contentPanel.add(new BudgetsPanel(username), BUDGETS_PAGE);
        contentPanel.add(new ReportsPanel(username), REPORTS_PAGE);
        contentPanel.add(new AIPanel(username), AI_PAGE);
        contentPanel.add(new SettingsPanel(username), SETTINGS_PAGE);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);

        // Show dashboard by default and highlight its button
        setActivePage(DASHBOARD_PAGE);
    }

    /**
     * Gets the current logged-in user.
     *
     * @return the current username
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * Creates the navigation bar with navigation buttons.
     *
     * @return the navigation bar panel
     */
    private JPanel createNavigationBar() {
        JPanel navBar = new JPanel();
        navBar.setLayout(new BoxLayout(navBar, BoxLayout.X_AXIS));
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Application title
        JLabel titleLabel = new JLabel("Finance Manager");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 16));
        navBar.add(titleLabel);

        // Add some space between title and navigation items
        navBar.add(Box.createRigidArea(new Dimension(30, 0)));

        // Add navigation buttons
        addNavButton(navBar, "Dashboard", DASHBOARD_PAGE);
        addNavButton(navBar, "Transactions", TRANSACTIONS_PAGE);
        addNavButton(navBar, "Budgets", BUDGETS_PAGE);
        addNavButton(navBar, "Reports", REPORTS_PAGE);
        addNavButton(navBar, "AI", AI_PAGE);
        addNavButton(navBar, "Settings", SETTINGS_PAGE);

        // Add filler to push everything to the left
        navBar.add(Box.createHorizontalGlue());

        return navBar;
    }

    /**
     * Adds a navigation button to the navigation bar.
     *
     * @param navBar the navigation bar panel
     * @param text the button label
     * @param pageKey the page key for CardLayout
     */
    private void addNavButton(JPanel navBar, String text, String pageKey) {
        JButton button = new JButton(text);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(DEFAULT_BORDER);
        button.setToolTipText("Go to " + text + " page");

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setActivePage(pageKey);
            }
        });

        navButtons.put(pageKey, button);
        navBar.add(button);
        navBar.add(Box.createRigidArea(new Dimension(15, 0)));
    }

    /**
     * Sets the active page and updates navigation button highlighting.
     *
     * @param pageKey the page key to activate
     */
    private void setActivePage(String pageKey) {
        // Reset all buttons to default style
        for (JButton button : navButtons.values()) {
            button.setBorder(DEFAULT_BORDER);
            button.setForeground(UIManager.getColor("Button.foreground"));
        }

        // Highlight the selected button
        JButton selectedButton = navButtons.get(pageKey);
        if (selectedButton != null) {
            selectedButton.setBorder(SELECTED_BORDER);
            selectedButton.setForeground(SELECTED_COLOR);
        }

        // Show the selected page
        cardLayout.show(contentPanel, pageKey);
        currentPage = pageKey;
    }
}