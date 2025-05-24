package com.example.app.viewmodel;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AIViewModelTest {
    private static final String TEST_USERNAME = "testuser_ai";
    private AIViewModel viewModel;

    @BeforeEach
    void setUp() {
        viewModel = new AIViewModel(TEST_USERNAME);
    }

    @AfterEach
    void tearDown() {
        File dir = new File(".\\user_data\\" + TEST_USERNAME);
        if (dir.exists()) {
            for (File file : dir.listFiles()) file.delete();
            dir.delete();
        }
    }

    @Test
    void testInitialMessage() {
        List<AIViewModel.ChatMessage> messages = viewModel.getMessages();
        assertFalse(messages.isEmpty());
        assertFalse(messages.get(0).isFromUser());
    }

    @Test
    void testSendMessageAddsUserMessage() {
        int before = viewModel.getMessages().size();
        viewModel.sendMessage("How is my spending?");
        int after = viewModel.getMessages().size();
        assertTrue(after > before);
    }

    @Test
    void testGetFinancialAdviceNotNull() {
        assertNotNull(viewModel.getFinancialAdvice());
    }

    @Test
    void testRegenerateAdviceAddsMessage() {
        int before = viewModel.getMessages().size();
        viewModel.regenerateAdvice();
        int after = viewModel.getMessages().size();
        assertTrue(after > before);
    }
}