package com.example.app.ui.pages.AI;
import lombok.Data;
import lombok.Builder;
import java.util.List;

/**
 * Request model for DeepSeek AI API.
 * This class represents the structure of a request to the DeepSeek API for
 * chat completion operations.
 */
@Data
@Builder
public class DeepseekRequest {
    /**
     * The model identifier to use for this request.
     * For example: "deepseek-chat"
     */
    private String model;
    
    /**
     * List of message objects that comprise the conversation history.
     * These messages provide context for the AI to generate responses.
     */
    private List<Message> messages;

    /**
     * Represents a message in the conversation with the DeepSeek AI.
     * Each message has a role (e.g., "user", "assistant") and content.
     */
    @Data
    @Builder
    public static class Message {
        /**
         * The role of the message sender.
         * Common values include "user" for user messages and "assistant" for AI responses.
         */
        private String role;
        
        /**
         * The actual content/text of the message.
         */
        private String content;
    }
}