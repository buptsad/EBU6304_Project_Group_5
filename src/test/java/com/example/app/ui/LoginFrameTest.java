package com.example.app.ui;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class LoginFrameTest {
    private JFrame frame;

    @BeforeEach
    void setUp() {
        // Only clean up test user folders, not all of user_data!
        File[] testDirs = new File(".\\user_data").listFiles((dir, name) -> name.startsWith("testuser_"));
        if (testDirs != null) {
            for (File userDir : testDirs) {
                File[] files = userDir.listFiles();
                if (files != null) {
                    for (File file : files) file.delete();
                }
                userDir.delete();
            }
        }
    }

    @AfterEach
    void tearDown() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    @AfterAll
    static void cleanUp() {
        // Only clean up test user folders, not all of user_data!
        // Example: for test users named "testuser_*"
        File[] testDirs = new File(".\\user_data").listFiles((dir, name) -> name.startsWith("testuser_"));
        if (testDirs != null) {
            for (File userDir : testDirs) {
                File[] files = userDir.listFiles();
                if (files != null) {
                    for (File file : files) file.delete();
                }
                userDir.delete();
            }
        }
    }

    @Test
    @DisplayName("Should initialize LoginFrame and show login panel")
    void testLoginFrameInitializes() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new LoginFrame();
            assertNotNull(frame);
            assertTrue(frame.isVisible());
            // Check for login panel components
            JTextField usernameField = findTextField(frame.getContentPane());
            assertNotNull(usernameField, "Username field should exist");
        });
    }

    @Test
    @DisplayName("Should switch to register panel when register toggle clicked")
    void testSwitchToRegisterPanel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new LoginFrame();
            JButton registerToggle = findButtonByText(frame.getContentPane(), "Register");
            assertNotNull(registerToggle, "Register toggle button should exist");
            registerToggle.doClick();
            // After click, the register panel should be visible (look for "Create Account" label)
            JLabel label = findLabelByText(frame.getContentPane(), "Create Account");
            assertNotNull(label, "Register panel should be visible after toggle");
        });
    }

    // --- Utility methods ---
    private JTextField findTextField(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JTextField) return (JTextField) comp;
            if (comp instanceof Container) {
                JTextField tf = findTextField((Container) comp);
                if (tf != null) return tf;
            }
        }
        return null;
    }

    private JButton findButtonByText(Container parent, String text) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JButton && text.equals(((JButton) comp).getText())) return (JButton) comp;
            if (comp instanceof Container) {
                JButton btn = findButtonByText((Container) comp, text);
                if (btn != null) return btn;
            }
        }
        return null;
    }

    private JLabel findLabelByText(Container parent, String text) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JLabel && text.equals(((JLabel) comp).getText())) return (JLabel) comp;
            if (comp instanceof Container) {
                JLabel lbl = findLabelByText((Container) comp, text);
                if (lbl != null) return lbl;
            }
        }
        return null;
    }
}