package com.example.app.viewmodel;

import com.example.app.model.FinancialAdvice;
import com.example.app.ui.dashboard.OverviewPanel;
import com.example.app.ui.pages.AI.getRes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the AI Panel following the MVVM pattern.
 * Handles business logic for AI interactions and chat functionality.
 * <p>
 * Features:
 * <ul>
 *   <li>Manages chat history and AI responses</li>
 *   <li>Provides access to financial advice</li>
 *   <li>Notifies listeners about chat and advice updates</li>
 *   <li>Handles cleanup of listeners when no longer needed</li>
 * </ul>
 
 */
public class AIViewModel {
    // Model references
    private final getRes aiService;
    private final String apiKey;
    private final String username;

    // Chat message history
    private final List<ChatMessage> messages;

    // Listeners for UI updates
    private final List<AIDataChangeListener> listeners;

    /**
     * Represents a chat message with content and sender information.
     */
    public static class ChatMessage {
        private final String content;
        private final boolean isFromUser;

        /**
         * Constructs a ChatMessage.
         * @param content the message content
         * @param isFromUser true if sent by user, false if sent by AI
         */
        public ChatMessage(String content, boolean isFromUser) {
            this.content = content;
            this.isFromUser = isFromUser;
        }

        /**
         * Gets the message content.
         * @return message content
         */
        public String getContent() {
            return content;
        }

        /**
         * Checks if the message is from the user.
         * @return true if from user, false otherwise
         */
        public boolean isFromUser() {
            return isFromUser;
        }

        /**
         * Gets the formatted message with sender prefix.
         * @return formatted message string
         */
        public String getFormattedMessage() {
            return (isFromUser ? "You: " : "AI: ") + content;
        }
    }

    /**
     * Interface for components that need to be notified of AI data changes.
     */
    public interface AIDataChangeListener {
        /**
         * Called when a new chat message is added.
         * @param message the new chat message
         */
        void onMessageAdded(ChatMessage message);

        /**
         * Called when an error occurs during AI interaction.
         * @param errorMessage the error message
         */
        void onErrorOccurred(String errorMessage);

        /**
         * Called when financial advice has been updated.
         */
        void onAdviceUpdated();
    }

    /**
     * Constructs an AIViewModel for the specified user.
     * Initializes the AI service, advice, and chat history.
     * @param username the username for which to manage AI interactions
     */
    public AIViewModel(String username) {
        this.username = username;
        this.aiService = new getRes();
        this.apiKey = "sk-fdf26a37926f46ab8d4884c2cd533db8";
        this.messages = new ArrayList<>();
        this.listeners = new ArrayList<>();

        // Initialize the shared advice with username
        OverviewPanel.sharedAdvice.initialize(username);

        // Add initial welcome message
        addAIMessage("Hello! I can help analyze your finances and provide personalized advice. Ask me anything about your financial data.");
    }

    /**
     * Adds a listener for AI data changes.
     * @param listener the listener to add
     */
    public void addListener(AIDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener.
     * @param listener the listener to remove
     */
    public void removeListener(AIDataChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Sends a user message to the AI and gets a response.
     * @param userInput the user's input message
     */
    public void sendMessage(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return;
        }

        // Add user message to history
        addUserMessage(userInput);

        // Create a new thread to avoid blocking the UI
        new Thread(() -> {
            try {
                // Call the AI service
                String response = aiService.getResponse(apiKey, userInput);
                String parsedResponse = aiService.parseAIResponse(response);

                // Add AI response to history
                addAIMessage(parsedResponse);
            } catch (IOException e) {
                // Notify listeners of error
                notifyError("Error communicating with AI: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Regenerates financial advice using the AI.
     */
    public void regenerateAdvice() {
        // Access shared advice instance and regenerate
        OverviewPanel.sharedAdvice.regenerate();

        // Add system message to chat
        addAIMessage("Financial advice has been updated with new AI insights.");

        // Notify listeners specifically about advice update
        for (AIDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onAdviceUpdated();
        }
    }

    /**
     * Gets access to the financial advice model.
     * @return the FinancialAdvice instance
     */
    public FinancialAdvice getFinancialAdvice() {
        return OverviewPanel.sharedAdvice;
    }

    /**
     * Gets all chat messages.
     * @return a list of chat messages
     */
    public List<ChatMessage> getMessages() {
        return new ArrayList<>(messages); // Return a copy to prevent external modification
    }

    // Private helper methods

    private void addUserMessage(String content) {
        ChatMessage message = new ChatMessage(content, true);
        messages.add(message);
        notifyMessageAdded(message);
    }

    private void addAIMessage(String content) {
        ChatMessage message = new ChatMessage(content, false);
        messages.add(message);
        notifyMessageAdded(message);
    }

    private void notifyMessageAdded(ChatMessage message) {
        for (AIDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onMessageAdded(message);
        }
    }

    private void notifyError(String errorMessage) {
        for (AIDataChangeListener listener : new ArrayList<>(listeners)) {
            listener.onErrorOccurred(errorMessage);
        }
    }

    /**
     * Cleans up resources and listeners.
     */
    public void cleanup() {
        listeners.clear();
    }
}