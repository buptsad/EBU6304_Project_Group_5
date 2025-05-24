package com.example.app.ui.dialogs;

import com.example.app.ui.pages.TransactionsPanel;
import com.example.app.model.FinanceData;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CSVImportDialogTest {
    private CSVImportDialog dialog;
    private TestFrame frame;
    private static final String TEST_USERNAME = "testuser_csvimport";

    // Simple test implementations of dependencies
    static class MockTransactionsPanel extends TransactionsPanel {
        public MockTransactionsPanel() {
            super(TEST_USERNAME); // Use a specific test username
        }
        
        @Override
        public void addTransactionsFromCSV(List<Object[]> transactions) {
            // Test implementation that does nothing
        }
    }

    // Simple JFrame to own the dialog
    static class TestFrame extends JFrame {
        public TestFrame() {
            super("Test Frame");
            setSize(400, 300);
        }
    }

    @BeforeEach
    void setUp() {
        frame = new TestFrame();
        dialog = new CSVImportDialog(
            frame, 
            new MockTransactionsPanel(), 
            new FinanceData()
        );
        
        // Make dialog non-modal for testing
        dialog.setModal(false);
    }

    @AfterEach
    void tearDown() {
        dialog.dispose();
        frame.dispose();
    }
    
    @AfterAll
    static void cleanupTestData() {
        // Delete the test user directory
        Path userDir = Paths.get(".", "user_data", TEST_USERNAME);
        File userDirFile = userDir.toFile();
        
        if (userDirFile.exists() && userDirFile.isDirectory()) {
            deleteDirectory(userDirFile);
            System.out.println("Cleaned up test directory: " + userDir);
        }
    }
    
    // Helper method to recursively delete a directory
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        if (!file.delete()) {
                            System.err.println("Failed to delete file: " + file);
                        }
                    }
                }
            }
            return directory.delete();
        }
        return false;
    }

    @Test
    void testDialogComponentsExist() {
        assertNotNull(dialog);
        
        // Check for combo boxes
        int comboBoxCount = findAll(dialog, JComboBox.class).size();
        assertTrue(comboBoxCount >= 6, "Expected at least 6 combo boxes, found: " + comboBoxCount);
        
        // Check that the preview table exists
        boolean foundTable = !findAll(dialog, JTable.class).isEmpty();
        assertTrue(foundTable, "Preview table not found");
        
        // Check that text fields for identifiers exist
        int textFieldCount = findAll(dialog, JTextField.class).size();
        assertTrue(textFieldCount >= 2, "Expected at least 2 text fields, found: " + textFieldCount);
        
        // Check that action buttons exist
        List<JButton> buttons = findAll(dialog, JButton.class);
        boolean foundImportButton = false;
        boolean foundCancelButton = false;
        boolean foundBrowseButton = false;
        
        for (JButton button : buttons) {
            String text = button.getText();
            if ("Import".equals(text)) foundImportButton = true;
            if ("Cancel".equals(text)) foundCancelButton = true;
            if ("Browse...".equals(text)) foundBrowseButton = true;
        }
        
        assertTrue(foundImportButton, "Import button not found");
        assertTrue(foundCancelButton, "Cancel button not found");
        assertTrue(foundBrowseButton, "Browse button not found");
    }

    @Test
    void testTypeColumnCheckBoxToggling() {
        // Find the "Use Type Column" checkbox
        JCheckBox useTypeColumnCheckBox = null;
        for (JCheckBox cb : findAll(dialog, JCheckBox.class)) {
            if (cb.getText() != null && cb.getText().contains("All amounts are positive")) {
                useTypeColumnCheckBox = cb;
                break;
            }
        }
        
        assertNotNull(useTypeColumnCheckBox, "Use Type Column checkbox not found");
        
        // First verify the initial state
        assertFalse(useTypeColumnCheckBox.isSelected(), "Checkbox should be unchecked initially");
        
        // Toggle the checkbox
        useTypeColumnCheckBox.setSelected(true);
        
        // Check that it's now selected
        assertTrue(useTypeColumnCheckBox.isSelected(), "Checkbox should be checked after selection");
    }

    // Helper method to find all components of a given type
    private <T extends Component> List<T> findAll(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) found.add(type.cast(c));
            if (c instanceof Container) found.addAll(findAll((Container) c, type));
        }
        return found;
    }
}