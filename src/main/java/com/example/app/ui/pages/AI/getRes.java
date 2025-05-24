package com.example.app.ui.pages.AI;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for making requests to the DeepSeek AI API.
 * Provides methods to send prompts and parse responses from the API.
 */
public class getRes {
    /**
     * Private constructor to prevent instantiation of this utility class.
     * This class only contains static methods and should not be instantiated.
     */
    public getRes() {
        // Prevent instantiation
    }

    /**
     * DeepSeek API endpoint URL
     */
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    
    /**
     * DeepSeek API Key - Keep this private and secure!
     */
    private static String API_KEY = "sk-fdf26a37926f46ab8d4884c2cd533db8";
    
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends a request to the DeepSeek API with the given prompt.
     * 
     * @param apiKey The DeepSeek API key for authentication
     * @param prompt The text prompt to send to the AI model
     * @return The raw JSON response from the DeepSeek API
     * @throws IOException If there is an error in the API communication
     */
    public String getResponse(String apiKey, String prompt) throws IOException {
        // Build request body
        DeepseekRequest.Message message = DeepseekRequest.Message.builder()
                .role("user")
                .content(prompt)
                .build();
        DeepseekRequest requestBody = DeepseekRequest.builder()
                .model("deepseek-chat")
                .messages(Collections.singletonList(message))
                .build();

        // Serialize request body to JSON string
        String jsonBody = new ObjectMapper().writeValueAsString(requestBody);

        // Create HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey); // Equivalent to headers.add("Authorization", "Bearer " + apiKey)

        // Build HttpEntity object
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

        // Send HTTP POST request and get response
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(API_URL, httpEntity, String.class);

        // Check response status code and response body validity
        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            return responseEntity.getBody();
        }
        throw new IOException("Unexpected status code" );
    }
    
    /**
     * Extracts the content from the DeepSeek API JSON response.
     * 
     * @param jsonResponse The raw JSON response from the DeepSeek API
     * @return The extracted content or an error message if parsing fails
     */
    public String parseAIResponse(String jsonResponse) {
        try {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray choices = root.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject first = choices.getJSONObject(0);
                JSONObject message = first.getJSONObject("message");
                return message.getString("content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error: Unable to parse AI response.";
    }
    
    /**
     * Main method for testing the API functionality.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {

    }
}
