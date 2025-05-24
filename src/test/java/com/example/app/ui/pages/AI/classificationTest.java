package com.example.app.ui.pages.AI;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the classification class.
 * These tests verify the functionality of parsing AI responses from JSON format,
 * including handling valid responses, invalid JSON, and missing data fields.
 */
class classificationTest {

    /**
     * Tests that the parseAIResponse method correctly extracts content from valid JSON.
     * Verifies that the method returns the expected content string when given a properly formatted JSON response.
     */
    @Test
    @DisplayName("parseAIResponse should extract content from valid JSON")
    void testParseAIResponseValid() {
        String json = "{ \"choices\": [ { \"message\": { \"content\": \"Food,Other\" } } ] }";
        classification ai = new classification();
        String result = ai.parseAIResponse(json);
        assertEquals("Food,Other", result);
    }

    /**
     * Tests that the parseAIResponse method handles invalid JSON input gracefully.
     * Verifies that the method returns an error message when given input that is not valid JSON.
     */
    @Test
    @DisplayName("parseAIResponse should return error string for invalid JSON")
    void testParseAIResponseInvalid() {
        String json = "not a json";
        classification ai = new classification();
        String result = ai.parseAIResponse(json);
        assertTrue(result.startsWith("Error"));
    }

    /**
     * Tests that the parseAIResponse method handles JSON with missing required fields.
     * Verifies that the method returns an appropriate error message when given JSON that lacks expected fields.
     */
    @Test
    @DisplayName("parseAIResponse should return error string for missing fields")
    void testParseAIResponseMissingFields() {
        String json = "{ \"choices\": [] }";
        classification ai = new classification();
        String result = ai.parseAIResponse(json);
        assertTrue(result.startsWith("Error") || result.isEmpty());
    }

    /**
     * Cleans up any resources after all tests have completed.
     * This method is currently empty as the classification class does not create any files or folders that need cleanup.
     */
    @AfterAll
    static void cleanUp() {
        // No files or folders created by classification, nothing to clean up
    }
}