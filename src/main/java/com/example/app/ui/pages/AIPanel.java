package com.example.app.ui.pages;

import com.example.app.viewmodel.AIViewModel;
import com.example.app.viewmodel.AIViewModel.ChatMessage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The AI panel view component in the MVVM architecture.
 * This panel is responsible for displaying the chat interface and forwarding user actions to the ViewModel.
 * <p>
 * Features:
 * <ul>
 *   <li>Displays a chat area for AI and user messages</li>
 *   <li>Allows sending messages to the AI assistant</li>
 *   <li>Provides a button to regenerate financial advice</li>
 *   <li>Updates UI in response to ViewModel events</li>
 * </ul>
 
 */
public class AIPanel extends JPanel implements AIViewModel.AIDataChangeListener {
    /** Reference to the ViewModel */
    private final AIViewModel viewModel;

    /** Text area for displaying chat messages */
    private JTextArea chatArea;

    /** Input field for user messages */
    private JTextField inputField;

    /** Button to send user messages */
    private JButton sendButton;

    /** Button to regenerate financial advice */
    private JButton regenerateButton;

    /**
     * Constructs a new AI panel for the specified user.
     *
     * @param username the username of the current user
     */
    public AIPanel(String username) {
        // Initialize ViewModel
        this.viewModel = new AIViewModel(username);
        this.viewModel.addListener(this);

        // Initialize UI
        initializeUI();

        // Load initial messages from ViewModel
        for (ChatMessage message : viewModel.getMessages()) {
            appendMessage(message.getFormattedMessage());
        }
    }

    /**
     * Initializes the UI components and layout.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("AI Assistant", JLabel.LEFT);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 22));

        // Regenerate advice button panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.WEST);

        regenerateButton = new JButton("Regenerate Financial Advice");
        regenerateButton.setName("regenerateButton");
        regenerateButton.setFocusPainted(false);
        regenerateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        regenerateButton.addActionListener(e -> viewModel.regenerateAdvice());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(regenerateButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Chat area (scrollable)
        chatArea = new JTextArea();
        chatArea.setName("chatArea");
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(chatScrollPane, BorderLayout.CENTER);

        // Input panel (text field + send button)
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputField = new JTextField();
        inputField.setName("inputField");
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setMargin(new Insets(5, 10, 5, 10));
        inputPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.setName("sendButton");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Add action listeners for user interactions
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    /**
     * Sends the user's message to the ViewModel and clears the input field.
     */
    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (!userInput.isEmpty()) {
            // Clear input field immediately for better UX
            inputField.setText("");

            // Forward to ViewModel
            viewModel.sendMessage(userInput);
        }
    }

    /**
     * Appends a message to the chat area.
     *
     * @param message the message to append
     */
    private void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    /**
     * Called when a new chat message is added in the ViewModel.
     *
     * @param message the new chat message
     */
    @Override
    public void onMessageAdded(ChatMessage message) {
        // Ensure UI updates happen on the EDT
        SwingUtilities.invokeLater(() -> appendMessage(message.getFormattedMessage()));
    }

    /**
     * Called when an error occurs in the ViewModel.
     *
     * @param errorMessage the error message to display
     */
    @Override
    public void onErrorOccurred(String errorMessage) {
        SwingUtilities.invokeLater(() -> {
            appendMessage("Error: " + errorMessage);
            JOptionPane.showMessageDialog(this, errorMessage, "AI Communication Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Called when financial advice is updated in the ViewModel.
     */
    @Override
    public void onAdviceUpdated() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                this,
                "Financial advice has been successfully updated!",
                "Advice Updated",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    /**
     * Called when this panel is removed from its container.
     * Cleans up listeners and resources.
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        // Clean up when panel is removed from UI
        viewModel.removeListener(this);
        viewModel.cleanup();
    }
}