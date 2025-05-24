package com.example.app.user_data;

import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the UserAuthService class.
 * Tests user registration, authentication, and username availability functionality,
 * verifying file creation, duplicate user handling, and password verification.
 */
class UserAuthServiceTest {

    /**
     * Test username used for creating isolated test environment.
     */
    private static final String TEST_USERNAME = "testuser_auth";
    
    /**
     * Test password used for authentication tests.
     */
    private static final String TEST_PASSWORD = "testpass123";
    
    /**
     * Test email used for user registration.
     */
    private static final String TEST_EMAIL = "testuser@example.com";
    
    /**
     * Path to the test user's directory.
     */
    private static final String USER_DIR_PATH = ".\\user_data\\" + TEST_USERNAME;
    
    /**
     * File object representing the test user's directory.
     */
    private static final File USER_DIR = new File(USER_DIR_PATH);

    /**
     * Sets up the test environment before each test.
     * Cleans up any existing test user directory and files to ensure test isolation.
     */
    @BeforeEach
    void setUp() {
        // Clean up before test
        if (USER_DIR.exists()) {
            for (File file : USER_DIR.listFiles()) {
                file.delete();
            }
            USER_DIR.delete();
        }
    }

    /**
     * Cleans up test files and directories after all tests are complete.
     * Removes the test user directory and its contents, and the user_data directory
     * if it's empty to avoid leaving test artifacts.
     */
    @AfterAll
    static void cleanUp() {
        if (USER_DIR.exists()) {
            for (File file : USER_DIR.listFiles()) {
                file.delete();
            }
            USER_DIR.delete();
        }
        // Do NOT delete user_data directory!
    }

    /**
     * Tests that user registration creates all necessary user files.
     * Verifies that the user directory and required files (settings, bill, budgets) are created.
     */
    @Test
    @DisplayName("Should register user and create files")
    void testRegisterUserCreatesFiles() {
        assertTrue(UserAuthService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL), "Registration should succeed");
        assertTrue(USER_DIR.exists() && USER_DIR.isDirectory(), "User directory should exist");
        File settingsFile = new File(USER_DIR, "user_settings.properties");
        File billFile = new File(USER_DIR, "user_bill.csv");
        File budgetFile = new File(USER_DIR, "user_budgets.csv");
        assertTrue(settingsFile.exists(), "Settings file should exist");
        assertTrue(billFile.exists(), "Bill file should exist");
        assertTrue(budgetFile.exists(), "Budget file should exist");
    }

    /**
     * Tests that duplicate user registration is prevented.
     * Verifies that attempting to register a username that already exists fails.
     */
    @Test
    @DisplayName("Should not register duplicate user")
    void testRegisterDuplicateUser() {
        assertTrue(UserAuthService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertFalse(UserAuthService.registerUser(TEST_USERNAME, "otherpass", "other@example.com"), "Duplicate registration should fail");
    }

    /**
     * Tests that user authentication succeeds with the correct password.
     * Verifies that a registered user can be authenticated with their password.
     */
    @Test
    @DisplayName("Should authenticate user with correct password")
    void testAuthenticateUserSuccess() {
        assertTrue(UserAuthService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertTrue(UserAuthService.authenticateUser(TEST_USERNAME, TEST_PASSWORD), "Authentication should succeed");
    }

    /**
     * Tests that authentication fails with an incorrect password.
     * Verifies that authentication is rejected when the wrong password is provided.
     */
    @Test
    @DisplayName("Should not authenticate with wrong password")
    void testAuthenticateUserWrongPassword() {
        assertTrue(UserAuthService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertFalse(UserAuthService.authenticateUser(TEST_USERNAME, "wrongpass"), "Authentication should fail with wrong password");
    }

    /**
     * Tests that authentication fails for non-existent users.
     * Verifies that authentication is rejected for usernames that aren't registered.
     */
    @Test
    @DisplayName("Should not authenticate non-existent user")
    void testAuthenticateNonExistentUser() {
        assertFalse(UserAuthService.authenticateUser("nonexistent", "nopass"), "Authentication should fail for non-existent user");
    }

    /**
     * Tests username availability checking functionality.
     * Verifies that usernames are reported as available before registration and
     * unavailable after registration.
     */
    @Test
    @DisplayName("Should check username availability")
    void testIsUsernameAvailable() {
        assertTrue(UserAuthService.isUsernameAvailable(TEST_USERNAME), "Username should be available before registration");
        assertTrue(UserAuthService.registerUser(TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL));
        assertFalse(UserAuthService.isUsernameAvailable(TEST_USERNAME), "Username should not be available after registration");
    }
}