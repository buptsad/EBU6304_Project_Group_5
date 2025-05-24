package com.example.app.ui.pages;

import com.example.app.viewmodel.AIViewModel;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class AIPanelTest {

    private static final String TEST_USERNAME = "testuser_aipanel";
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USERNAME;

    private JFrame frame;
    private AIPanel aiPanel;

    @BeforeEach
    void setUp() {
        // Clean up user directory before test
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
        // Create panel on EDT
        SwingUtilities.invokeLater(() -> {
            aiPanel = new AIPanel(TEST_USERNAME);
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(aiPanel);
            frame.setSize(600, 400);
            frame.setVisible(true);
        });
        // Wait for EDT to finish setup
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
    }

    @AfterEach
    void tearDown() {
        // Dispose frame and clean up user directory
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
    }

    @AfterAll
    static void cleanUp() {
        File userDir = new File(USER_DIR_PATH);
        if (userDir.exists()) {
            for (File file : userDir.listFiles()) {
                file.delete();
            }
            userDir.delete();
        }
        // Do NOT delete user_data folder!
        // File userDataDir = new File(".\\user_data");
        // if (userDataDir.exists() && userDataDir.isDirectory() && userDataDir.list().length == 0) {
        //     userDataDir.delete();
        // }
    }

    @Test
    @DisplayName("Should send user message and add to chat")
    void testSendMessageAddsToChat() throws Exception {
        JTextField inputField = (JTextField) TestUtils.getChildNamed(aiPanel, "inputField");
        JButton sendButton = (JButton) TestUtils.getChildNamed(aiPanel, "sendButton");
        JTextArea chatArea = (JTextArea) TestUtils.getChildNamed(aiPanel, "chatArea");

        assertNotNull(inputField);
        assertNotNull(sendButton);
        assertNotNull(chatArea);

        SwingUtilities.invokeAndWait(() -> {
            inputField.setText("Test message");
            sendButton.doClick();
        });

        // Wait for message to be processed
        Thread.sleep(500);

        String text = chatArea.getText();
        assertTrue(text.contains("You: Test message"), "Chat area should contain user message");
    }

    @Test
    @DisplayName("Should call regenerateAdvice and show system message")
    void testRegenerateAdviceAddsSystemMessage() throws Exception {
        JButton regenButton = null;
        JTextArea chatArea = null;

        // Wait for components to be available (up to 5 seconds)
        for (int i = 0; i < 50; i++) {
            regenButton = (JButton) TestUtils.getChildNamed(aiPanel, "regenerateButton");
            chatArea = (JTextArea) TestUtils.getChildNamed(aiPanel, "chatArea");
            if (regenButton != null && chatArea != null) break;
            Thread.sleep(100);
        }

        assertNotNull(regenButton);
        assertNotNull(chatArea);

        SwingUtilities.invokeAndWait(regenButton::doClick);

        // Wait for the message to appear (since regenerateAdvice may be async)
        boolean found = false;
        for (int i = 0; i < 600; i++) { // wait up to 60 second
            Thread.sleep(100);
            String text = chatArea.getText();
            if (text.contains("Financial advice has been updated")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Chat area should contain system message about advice update");
    }

    // --- Utility for finding named components in Swing trees ---
    static class TestUtils {
        static Component getChildNamed(Component parent, String name) {
            if (parent == null) return null;
            if (name.equals(parent.getName())) return parent;
            if (parent instanceof Container) {
                for (Component child : ((Container) parent).getComponents()) {
                    Component found = getChildNamed(child, name);
                    if (found != null) return found;
                }
            }
            return null;
        }
    }
}