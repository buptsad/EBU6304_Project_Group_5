package com.example.app.ui.pages;

import com.example.app.ui.dashboard.OverviewPanel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class AIPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;

    public AIPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title label
        JLabel titleLabel = new JLabel("AI Assistant", JLabel.LEFT);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 22));
        add(titleLabel, BorderLayout.NORTH);

        // Chat area (scrollable)
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(chatScrollPane, BorderLayout.CENTER);

        // Add regenerate advice button panel
        JPanel regeneratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton regenerateButton = new JButton("Regenerate Financial Advice");
        regenerateButton.setFocusPainted(false);
        regenerateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        regenerateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Regenerate the shared advice
                OverviewPanel.sharedAdvice.regenerate();
                
                // Notify user in the chat area
                appendMessage("System: Financial advice has been updated with new AI insights.");
                
                // In a real application, we would find and notify the OverviewPanel to update
                // For a demo this is sufficient
            }
        });
        
        regeneratePanel.add(regenerateButton);
        add(regeneratePanel, BorderLayout.NORTH);

        // Input panel (text field + send button)
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setMargin(new Insets(5, 10, 5, 10));
        inputPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Add action listener for the send button
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Add action listener for pressing Enter in the input field
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        
        // Add initial welcome message
        appendMessage("AI: Hello! I can help analyze your finances and provide personalized advice. Ask me anything about your financial data.");
    }

    private void sendMessage() {
        String userInput = inputField.getText().trim();
        if (!userInput.isEmpty()) {
            appendMessage("You: " + userInput);
            inputField.setText("");

            // Fetch AI response in a separate thread to avoid blocking the UI
            new Thread(() -> {
                String aiResponse = getAIResponse(userInput);
                SwingUtilities.invokeLater(() -> appendMessage("AI: " + aiResponse));
            }).start();
        }
    }

    private void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private String getAIResponse(String userInput) {
        try {
            // Replace with your API endpoint
            URL url = new URL("https://api.example.com/v0/chat");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer YOUR_API_KEY");
            connection.setDoOutput(true);

            // JSON payload
            String payload = String.format("{\"message\": \"%s\"}", userInput);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    // Parse and return the AI response (adjust based on API response format)
                    return parseAIResponse(response.toString());
                }
            } else {
                return "Error: Unable to fetch AI response (HTTP " + responseCode + ")";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to connect to AI service.";
        }
    }

    private String parseAIResponse(String jsonResponse) {
        // Parse the JSON response to extract the AI's reply
        // Adjust this based on the API's response format
        // Example: {"reply": "Hello, how can I help you?"}
        try {
            // Assuming a simple JSON structure
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);
            return jsonObject.getString("reply");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Unable to parse AI response.";
        }
    }
}