package com.example.app.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterEach;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for DataRefreshManager.
 * Tests the singleton pattern implementation, listener management, refresh notifications,
 * error handling, and concurrency behavior of the DataRefreshManager class.
 */
class DataRefreshManagerTest {
    
    /**
     * Instance of DataRefreshManager to be tested.
     */
    private DataRefreshManager refreshManager;
    
    /**
     * Test listener instance used across multiple test methods.
     */
    private TestDataRefreshListener testListener;
    
    /**
     * Sets up the test environment before each test method.
     * Resets the DataRefreshManager singleton, gets a fresh instance,
     * and creates a clean test listener.
     */
    @BeforeEach
    void setUp() {
        DataRefreshManager._resetForTests(); // Reset singleton for isolated tests
        refreshManager = DataRefreshManager.getInstance();
        testListener = new TestDataRefreshListener();
        
        // Clean up any existing listeners to ensure isolated tests
        refreshManager.removeListener(testListener);
    }
    
    /**
     * Cleans up after each test by removing any registered test listeners.
     */
    @AfterEach
    void tearDown() {
        // Clean up listeners after each test
        if (testListener != null) {
            refreshManager.removeListener(testListener);
        }
    }
    
    /**
     * Tests that verify the singleton pattern implementation of DataRefreshManager.
     */
    @Nested
    @DisplayName("Singleton Pattern Tests")
    class SingletonPatternTests {
        
        /**
         * Tests that multiple calls to getInstance() return the same instance.
         */
        @Test
        @DisplayName("Should return same instance for multiple getInstance calls")
        void shouldReturnSameInstanceForMultipleGetInstanceCalls() {
            DataRefreshManager instance1 = DataRefreshManager.getInstance();
            DataRefreshManager instance2 = DataRefreshManager.getInstance();
            DataRefreshManager instance3 = DataRefreshManager.getInstance();
            
            assertSame(instance1, instance2, "getInstance should return the same instance");
            assertSame(instance2, instance3, "getInstance should return the same instance");
            assertSame(instance1, instance3, "getInstance should return the same instance");
        }
        
        /**
         * Tests that the singleton pattern is maintained across multiple threads.
         * 
         * @throws InterruptedException If thread execution is interrupted
         */
        @Test
        @DisplayName("Should maintain singleton across different threads")
        void shouldMaintainSingletonAcrossDifferentThreads() throws InterruptedException {
            final int threadCount = 10;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            List<DataRefreshManager> instances = Collections.synchronizedList(new ArrayList<>());
            
            // Create multiple threads that get the instance
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    try {
                        startLatch.await();
                        instances.add(DataRefreshManager.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        doneLatch.countDown();
                    }
                });
                thread.start();
            }
            
            startLatch.countDown();
            assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "All threads should complete");
            
            assertEquals(threadCount, instances.size(), "Should have instances from all threads");
            
            // Verify all instances are the same
            DataRefreshManager firstInstance = instances.get(0);
            for (DataRefreshManager instance : instances) {
                assertSame(firstInstance, instance, "All instances should be the same singleton");
            }
        }
    }
    
    /**
     * Tests that verify the listener management functionality of DataRefreshManager.
     */
    @Nested
    @DisplayName("Listener Management Tests")
    class ListenerManagementTests {
        
        /**
         * Tests that listeners can be successfully added to the manager.
         */
        @Test
        @DisplayName("Should add listener successfully")
        void shouldAddListenerSuccessfully() {
            refreshManager.addListener(testListener);
            
            // Trigger a refresh to verify listener was added
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.ALL);
            
            assertEquals(1, testListener.getRefreshCount(), "Listener should receive refresh notification");
            assertEquals(DataRefreshManager.RefreshType.ALL, testListener.getLastRefreshType(), 
                "Listener should receive correct refresh type");
        }
        
        /**
         * Tests that duplicate listeners are not added multiple times.
         */
        @Test
        @DisplayName("Should not add duplicate listeners")
        void shouldNotAddDuplicateListeners() {
            refreshManager.addListener(testListener);
            refreshManager.addListener(testListener); // Add same listener again
            refreshManager.addListener(testListener); // Add same listener again
            
            // Trigger a refresh
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
            
            assertEquals(1, testListener.getRefreshCount(), 
                "Listener should only receive one notification despite being added multiple times");
        }
        
        /**
         * Tests that listeners can be successfully removed from the manager.
         */
        @Test
        @DisplayName("Should remove listener successfully")
        void shouldRemoveListenerSuccessfully() {
            refreshManager.addListener(testListener);
            refreshManager.removeListener(testListener);
            
            // Trigger a refresh
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
            
            assertEquals(0, testListener.getRefreshCount(), 
                "Removed listener should not receive refresh notifications");
        }
        
        /**
         * Tests that removing a non-existent listener does not cause errors.
         */
        @Test
        @DisplayName("Should handle removing non-existent listener gracefully")
        void shouldHandleRemovingNonExistentListenerGracefully() {
            assertDoesNotThrow(() -> {
                refreshManager.removeListener(testListener);
            }, "Removing non-existent listener should not throw exception");
        }
        
        /**
         * Tests that multiple listeners can be managed simultaneously.
         */
        @Test
        @DisplayName("Should handle multiple listeners")
        void shouldHandleMultipleListeners() {
            TestDataRefreshListener listener1 = new TestDataRefreshListener();
            TestDataRefreshListener listener2 = new TestDataRefreshListener();
            TestDataRefreshListener listener3 = new TestDataRefreshListener();
            
            try {
                refreshManager.addListener(listener1);
                refreshManager.addListener(listener2);
                refreshManager.addListener(listener3);
                
                // Trigger a refresh
                refreshManager.notifyRefresh(DataRefreshManager.RefreshType.CURRENCY);
                
                assertEquals(1, listener1.getRefreshCount(), "Listener 1 should receive notification");
                assertEquals(1, listener2.getRefreshCount(), "Listener 2 should receive notification");
                assertEquals(1, listener3.getRefreshCount(), "Listener 3 should receive notification");
                
                assertEquals(DataRefreshManager.RefreshType.CURRENCY, listener1.getLastRefreshType());
                assertEquals(DataRefreshManager.RefreshType.CURRENCY, listener2.getLastRefreshType());
                assertEquals(DataRefreshManager.RefreshType.CURRENCY, listener3.getLastRefreshType());
                
            } finally {
                refreshManager.removeListener(listener1);
                refreshManager.removeListener(listener2);
                refreshManager.removeListener(listener3);
            }
        }
    }
    
    /**
     * Tests that verify the refresh notification functionality of DataRefreshManager.
     */
    @Nested
    @DisplayName("Refresh Notification Tests")
    class RefreshNotificationTests {
        
        /**
         * Tests notification of TRANSACTIONS refresh type.
         */
        @Test
        @DisplayName("Should notify listeners of TRANSACTIONS refresh")
        void shouldNotifyListenersOfTransactionsRefresh() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.TRANSACTIONS, testListener.getLastRefreshType());
        }
        
        /**
         * Tests notification of BUDGETS refresh type.
         */
        @Test
        @DisplayName("Should notify listeners of BUDGETS refresh")
        void shouldNotifyListenersOfBudgetsRefresh() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.BUDGETS, testListener.getLastRefreshType());
        }
        
        /**
         * Tests notification of CURRENCY refresh type.
         */
        @Test
        @DisplayName("Should notify listeners of CURRENCY refresh")
        void shouldNotifyListenersOfCurrencyRefresh() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.CURRENCY);
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.CURRENCY, testListener.getLastRefreshType());
        }
        
        /**
         * Tests notification of SETTINGS refresh type.
         */
        @Test
        @DisplayName("Should notify listeners of SETTINGS refresh")
        void shouldNotifyListenersOfSettingsRefresh() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.SETTINGS);
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.SETTINGS, testListener.getLastRefreshType());
        }
        
        /**
         * Tests notification of ADVICE refresh type.
         */
        @Test
        @DisplayName("Should notify listeners of ADVICE refresh")
        void shouldNotifyListenersOfAdviceRefresh() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.ADVICE);
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.ADVICE, testListener.getLastRefreshType());
        }
        
        /**
         * Tests notification of ALL refresh type.
         */
        @Test
        @DisplayName("Should notify listeners of ALL refresh")
        void shouldNotifyListenersOfAllRefresh() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.ALL);
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.ALL, testListener.getLastRefreshType());
        }
        
        /**
         * Tests that multiple sequential refresh notifications are handled correctly.
         */
        @Test
        @DisplayName("Should handle multiple sequential refresh notifications")
        void shouldHandleMultipleSequentialRefreshNotifications() {
            refreshManager.addListener(testListener);
            
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.ALL);
            
            assertEquals(3, testListener.getRefreshCount(), "Should receive all three notifications");
            assertEquals(DataRefreshManager.RefreshType.ALL, testListener.getLastRefreshType(), 
                "Should remember the last refresh type");
        }
    }
    
    /**
     * Tests that verify the convenience methods for triggering specific refresh types.
     */
    @Nested
    @DisplayName("Convenience Method Tests")
    class ConvenienceMethodTests {
        
        /**
         * Tests the refreshTransactions() convenience method.
         */
        @Test
        @DisplayName("Should trigger TRANSACTIONS refresh via convenience method")
        void shouldTriggerTransactionsRefreshViaConvenienceMethod() {
            refreshManager.addListener(testListener);
            
            refreshManager.refreshTransactions();
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.TRANSACTIONS, testListener.getLastRefreshType());
        }
        
        /**
         * Tests the refreshBudgets() convenience method.
         */
        @Test
        @DisplayName("Should trigger BUDGETS refresh via convenience method")
        void shouldTriggerBudgetsRefreshViaConvenienceMethod() {
            refreshManager.addListener(testListener);
            
            refreshManager.refreshBudgets();
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.BUDGETS, testListener.getLastRefreshType());
        }
        
        /**
         * Tests the refreshAll() convenience method.
         */
        @Test
        @DisplayName("Should trigger ALL refresh via convenience method")
        void shouldTriggerAllRefreshViaConvenienceMethod() {
            refreshManager.addListener(testListener);
            
            refreshManager.refreshAll();
            
            assertEquals(1, testListener.getRefreshCount());
            assertEquals(DataRefreshManager.RefreshType.ALL, testListener.getLastRefreshType());
        }
    }
    
    /**
     * Tests that verify the error handling capabilities of DataRefreshManager.
     */
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        /**
         * Tests that exceptions thrown by listeners are handled gracefully.
         */
        @Test
        @DisplayName("Should handle listener exceptions gracefully")
        void shouldHandleListenerExceptionsGracefully() {
            ExceptionThrowingListener exceptionListener = new ExceptionThrowingListener();
            TestDataRefreshListener normalListener = new TestDataRefreshListener();
            
            try {
                refreshManager.addListener(exceptionListener);
                refreshManager.addListener(normalListener);
                
                // This should not throw an exception even though one listener throws
                assertDoesNotThrow(() -> {
                    refreshManager.notifyRefresh(DataRefreshManager.RefreshType.ALL);
                }, "Should handle listener exceptions gracefully");
                
                // Normal listener should still receive the notification
                assertEquals(1, normalListener.getRefreshCount(), 
                    "Normal listener should receive notification despite other listener throwing exception");
                
            } finally {
                refreshManager.removeListener(exceptionListener);
                refreshManager.removeListener(normalListener);
            }
        }
        
        /**
         * Tests that recursive refresh calls are prevented to avoid infinite loops.
         */
        @Test
        @DisplayName("Should prevent recursive refresh calls")
        void shouldPreventRecursiveRefreshCalls() {
            RecursiveRefreshListener recursiveListener = new RecursiveRefreshListener(refreshManager);
            
            try {
                refreshManager.addListener(recursiveListener);
                
                // This should not cause infinite recursion
                assertDoesNotThrow(() -> {
                    refreshManager.notifyRefresh(DataRefreshManager.RefreshType.ALL);
                }, "Should prevent recursive refresh calls");
                
                // Should only be called once (recursive call should be prevented)
                assertEquals(1, recursiveListener.getCallCount(), 
                    "Recursive calls should be prevented");
                
            } finally {
                refreshManager.removeListener(recursiveListener);
            }
        }
    }
    
    /**
     * Tests that verify the concurrency safety of DataRefreshManager operations.
     */
    @Nested
    @DisplayName("Concurrency Tests")
    class ConcurrencyTests {
        
        /**
         * Tests that concurrent listener addition and removal operations are thread-safe.
         * 
         * @throws InterruptedException If thread execution is interrupted
         */
        @Test
        @DisplayName("Should handle concurrent listener addition and removal")
        void shouldHandleConcurrentListenerAdditionAndRemoval() throws InterruptedException {
            final int threadCount = 5;
            final int operationsPerThread = 100;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            for (int i = 0; i < threadCount; i++) {
                Thread thread = new Thread(() -> {
                    try {
                        startLatch.await();
                        
                        for (int j = 0; j < operationsPerThread; j++) {
                            TestDataRefreshListener listener = new TestDataRefreshListener();
                            refreshManager.addListener(listener);
                            refreshManager.removeListener(listener);
                        }
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
                thread.start();
            }
            
            startLatch.countDown();
            assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "All threads should complete");
            assertEquals(0, errorCount.get(), "No errors should occur during concurrent operations");
        }
        
        /**
         * Tests that concurrent refresh notifications are thread-safe.
         * 
         * @throws InterruptedException If thread execution is interrupted
         */
        @Test
        @DisplayName("Should handle concurrent refresh notifications")
        void shouldHandleConcurrentRefreshNotifications() throws InterruptedException {
            final int threadCount = 5;
            final int refreshesPerThread = 50;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger totalRefreshes = new AtomicInteger(0);
            
            TestDataRefreshListener concurrentListener = new TestDataRefreshListener();
            refreshManager.addListener(concurrentListener);
            
            try {
                for (int i = 0; i < threadCount; i++) {
                    final int threadIndex = i;
                    Thread thread = new Thread(() -> {
                        try {
                            startLatch.await();
                            
                            for (int j = 0; j < refreshesPerThread; j++) {
                                DataRefreshManager.RefreshType type = 
                                    DataRefreshManager.RefreshType.values()[(threadIndex + j) % DataRefreshManager.RefreshType.values().length];
                                refreshManager.notifyRefresh(type);
                                totalRefreshes.incrementAndGet();
                            }
                        } catch (Exception e) {
                            // Error handling if needed
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                    thread.start();
                }
                
                startLatch.countDown();
                assertTrue(doneLatch.await(10, TimeUnit.SECONDS), "All threads should complete");
                
                // Give some time for all notifications to be processed
                Thread.sleep(100);
                
                assertEquals(threadCount * refreshesPerThread, totalRefreshes.get(), 
                    "All refresh notifications should have been sent");
                assertTrue(concurrentListener.getRefreshCount() > 0, 
                    "Listener should have received some notifications");
                
            } finally {
                refreshManager.removeListener(concurrentListener);
            }
        }
    }
    
    /**
     * Tests that verify the RefreshType enum functionality.
     */
    @Nested
    @DisplayName("RefreshType Enum Tests")
    class RefreshTypeEnumTests {
        
        /**
         * Tests that all expected RefreshType enum values exist.
         */
        @Test
        @DisplayName("Should have all expected RefreshType values")
        void shouldHaveAllExpectedRefreshTypeValues() {
            DataRefreshManager.RefreshType[] types = DataRefreshManager.RefreshType.values();
            
            assertEquals(6, types.length, "Should have 6 RefreshType values");
            
            // Verify all expected types exist
            assertTrue(containsType(types, DataRefreshManager.RefreshType.TRANSACTIONS));
            assertTrue(containsType(types, DataRefreshManager.RefreshType.BUDGETS));
            assertTrue(containsType(types, DataRefreshManager.RefreshType.CURRENCY));
            assertTrue(containsType(types, DataRefreshManager.RefreshType.SETTINGS));
            assertTrue(containsType(types, DataRefreshManager.RefreshType.ADVICE));
            assertTrue(containsType(types, DataRefreshManager.RefreshType.ALL));
        }
        
        /**
         * Tests that all RefreshType enum values can be handled correctly.
         */
        @Test
        @DisplayName("Should handle all RefreshType enum values")
        void shouldHandleAllRefreshTypeEnumValues() {
            refreshManager.addListener(testListener);
            
            for (DataRefreshManager.RefreshType type : DataRefreshManager.RefreshType.values()) {
                testListener.reset();
                
                refreshManager.notifyRefresh(type);
                
                assertEquals(1, testListener.getRefreshCount(), 
                    "Should handle RefreshType: " + type);
                assertEquals(type, testListener.getLastRefreshType(), 
                    "Should pass correct RefreshType: " + type);
            }
        }
        
        /**
         * Helper method to check if a specific RefreshType exists in an array.
         * 
         * @param types Array of RefreshType values to search
         * @param target The RefreshType to find
         * @return true if target is found in types, false otherwise
         */
        private boolean containsType(DataRefreshManager.RefreshType[] types, DataRefreshManager.RefreshType target) {
            for (DataRefreshManager.RefreshType type : types) {
                if (type == target) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Test implementation of DataRefreshListener for testing purposes.
     * Tracks refresh count and the last refresh type received.
     */
    private static class TestDataRefreshListener implements DataRefreshListener {
        private final AtomicInteger refreshCount = new AtomicInteger(0);
        private final AtomicReference<DataRefreshManager.RefreshType> lastRefreshType = new AtomicReference<>();
        
        /**
         * Called when a data refresh occurs.
         * 
         * @param type The type of refresh that occurred
         */
        @Override
        public void onDataRefresh(DataRefreshManager.RefreshType type) {
            refreshCount.incrementAndGet();
            lastRefreshType.set(type);
        }
        
        /**
         * Gets the number of refresh notifications received.
         * 
         * @return The refresh count
         */
        public int getRefreshCount() {
            return refreshCount.get();
        }
        
        /**
         * Gets the last refresh type received.
         * 
         * @return The last refresh type
         */
        public DataRefreshManager.RefreshType getLastRefreshType() {
            return lastRefreshType.get();
        }
        
        /**
         * Resets the refresh count and last refresh type.
         */
        public void reset() {
            refreshCount.set(0);
            lastRefreshType.set(null);
        }
    }
    
    /**
     * Test implementation of DataRefreshListener that throws an exception
     * when a refresh notification is received.
     */
    private static class ExceptionThrowingListener implements DataRefreshListener {
        /**
         * Called when a data refresh occurs. Always throws a RuntimeException.
         * 
         * @param type The type of refresh that occurred
         * @throws RuntimeException Always thrown for testing exception handling
         */
        @Override
        public void onDataRefresh(DataRefreshManager.RefreshType type) {
            throw new RuntimeException("Test exception from listener");
        }
    }
    
    /**
     * Test implementation of DataRefreshListener that attempts to create
     * a recursive refresh call when a notification is received.
     */
    private static class RecursiveRefreshListener implements DataRefreshListener {
        private final DataRefreshManager refreshManager;
        private final AtomicInteger callCount = new AtomicInteger(0);
        
        /**
         * Creates a new recursive refresh listener.
         * 
         * @param refreshManager The refresh manager to trigger recursive notifications on
         */
        public RecursiveRefreshListener(DataRefreshManager refreshManager) {
            this.refreshManager = refreshManager;
        }
        
        /**
         * Called when a data refresh occurs. Attempts to trigger another refresh.
         * 
         * @param type The type of refresh that occurred
         */
        @Override
        public void onDataRefresh(DataRefreshManager.RefreshType type) {
            callCount.incrementAndGet();
            // Try to trigger another refresh (should be prevented)
            refreshManager.notifyRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
        }
        
        /**
         * Gets the number of times this listener has been called.
         * 
         * @return The call count
         */
        public int getCallCount() {
            return callCount.get();
        }
    }
}