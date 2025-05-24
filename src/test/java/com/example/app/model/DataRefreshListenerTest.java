package com.example.app.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DataRefreshListener interface and its implementations.
 * Verifies the contract, behavior, and integration scenarios of the DataRefreshListener
 * functional interface with various implementation approaches.
 */
class DataRefreshListenerTest {
    
    /**
     * Tests that verify the contract requirements of the DataRefreshListener interface.
     * These tests ensure that the interface can be implemented in various ways and
     * handles all expected input types correctly.
     */
    @Nested
    @DisplayName("Interface Contract Tests")
    class InterfaceContractTests {
        
        /**
         * Tests that DataRefreshListener can be implemented as a lambda expression.
         * Verifies that a lambda implementation can be created and called without errors.
         */
        @Test
        @DisplayName("Should be a functional interface")
        void shouldBeFunctionalInterface() {
            // Verify that DataRefreshListener can be implemented as a lambda
            DataRefreshListener listener = (type) -> {
                // Simple lambda implementation
            };
            
            assertNotNull(listener, "Should be able to create listener with lambda");
            
            // Test that it can be called
            assertDoesNotThrow(() -> {
                listener.onDataRefresh(DataRefreshManager.RefreshType.ALL);
            }, "Lambda implementation should be callable");
        }
        
        /**
         * Tests that the listener implementation can handle all possible RefreshType enum values.
         * Verifies that each enum value is correctly processed and stored by the listener.
         */
        @Test
        @DisplayName("Should handle all RefreshType enum values")
        void shouldHandleAllRefreshTypeEnumValues() {
            TestDataRefreshListener listener = new TestDataRefreshListener();
            
            for (DataRefreshManager.RefreshType type : DataRefreshManager.RefreshType.values()) {
                listener.reset();
                
                assertDoesNotThrow(() -> {
                    listener.onDataRefresh(type);
                }, "Should handle RefreshType: " + type);
                
                assertEquals(type, listener.getLastRefreshType(), 
                    "Should receive correct RefreshType: " + type);
            }
        }
        
        /**
         * Tests that the listener implementation handles null RefreshType values gracefully.
         * Verifies that no exceptions are thrown and the null value is correctly stored.
         */
        @Test
        @DisplayName("Should handle null RefreshType gracefully")
        void shouldHandleNullRefreshTypeGracefully() {
            TestDataRefreshListener listener = new TestDataRefreshListener();
            
            assertDoesNotThrow(() -> {
                listener.onDataRefresh(null);
            }, "Should handle null RefreshType without throwing exception");
            
            assertNull(listener.getLastRefreshType(), "Should store null RefreshType");
        }
    }
    
    /**
     * Tests that verify the behavior of specific DataRefreshListener implementations.
     * These tests ensure that implementations correctly track state and handle various
     * call patterns as expected.
     */
    @Nested
    @DisplayName("Implementation Behavior Tests")
    class ImplementationBehaviorTests {
        
        /**
         * Tests that the listener implementation correctly tracks refresh calls.
         * Verifies that call count and last refresh type are accurately maintained.
         */
        @Test
        @DisplayName("Should track refresh calls correctly")
        void shouldTrackRefreshCallsCorrectly() {
            TestDataRefreshListener listener = new TestDataRefreshListener();
            
            // Initial state
            assertEquals(0, listener.getCallCount(), "Initial call count should be 0");
            assertNull(listener.getLastRefreshType(), "Initial refresh type should be null");
            
            // First call
            listener.onDataRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
            assertEquals(1, listener.getCallCount(), "Call count should be 1 after first call");
            assertEquals(DataRefreshManager.RefreshType.TRANSACTIONS, listener.getLastRefreshType());
            
            // Second call
            listener.onDataRefresh(DataRefreshManager.RefreshType.BUDGETS);
            assertEquals(2, listener.getCallCount(), "Call count should be 2 after second call");
            assertEquals(DataRefreshManager.RefreshType.BUDGETS, listener.getLastRefreshType());
            
            // Third call with same type
            listener.onDataRefresh(DataRefreshManager.RefreshType.BUDGETS);
            assertEquals(3, listener.getCallCount(), "Call count should be 3 after third call");
            assertEquals(DataRefreshManager.RefreshType.BUDGETS, listener.getLastRefreshType());
        }
        
        /**
         * Tests that the listener implementation can handle a large number of rapid calls.
         * Verifies that all calls are accurately counted without errors.
         */
        @Test
        @DisplayName("Should handle rapid successive calls")
        void shouldHandleRapidSuccessiveCalls() {
            TestDataRefreshListener listener = new TestDataRefreshListener();
            final int callCount = 1000;
            
            for (int i = 0; i < callCount; i++) {
                DataRefreshManager.RefreshType type = 
                    DataRefreshManager.RefreshType.values()[i % DataRefreshManager.RefreshType.values().length];
                listener.onDataRefresh(type);
            }
            
            assertEquals(callCount, listener.getCallCount(), 
                "Should handle " + callCount + " rapid successive calls");
        }
        
        /**
         * Tests that the listener implementation can reset its state correctly.
         * Verifies that after reset, call count returns to zero and last refresh type to null.
         */
        @Test
        @DisplayName("Should reset state correctly")
        void shouldResetStateCorrectly() {
            TestDataRefreshListener listener = new TestDataRefreshListener();
            
            // Make some calls
            listener.onDataRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
            listener.onDataRefresh(DataRefreshManager.RefreshType.BUDGETS);
            listener.onDataRefresh(DataRefreshManager.RefreshType.ALL);
            
            assertEquals(3, listener.getCallCount(), "Should have 3 calls before reset");
            assertEquals(DataRefreshManager.RefreshType.ALL, listener.getLastRefreshType());
            
            // Reset
            listener.reset();
            
            assertEquals(0, listener.getCallCount(), "Call count should be 0 after reset");
            assertNull(listener.getLastRefreshType(), "Last refresh type should be null after reset");
        }
    }
    
    /**
     * Tests that verify support for multiple different implementations of DataRefreshListener.
     * These tests ensure that the interface can be implemented in various ways and
     * that all implementations function correctly.
     */
    @Nested
    @DisplayName("Multiple Implementation Tests")
    class MultipleImplementationTests {
        
        /**
         * Tests that DataRefreshListener can be implemented in multiple different ways.
         * Verifies that lambda, anonymous class, and concrete class implementations all work correctly.
         */
        @Test
        @DisplayName("Should support multiple different implementations")
        void shouldSupportMultipleDifferentImplementations() {
            // Lambda implementation
            AtomicInteger lambdaCallCount = new AtomicInteger(0);
            DataRefreshListener lambdaListener = (type) -> lambdaCallCount.incrementAndGet();
            
            // Anonymous class implementation
            AtomicInteger anonymousCallCount = new AtomicInteger(0);
            DataRefreshListener anonymousListener = new DataRefreshListener() {
                @Override
                public void onDataRefresh(DataRefreshManager.RefreshType type) {
                    anonymousCallCount.incrementAndGet();
                }
            };
            
            // Custom class implementation
            TestDataRefreshListener customListener = new TestDataRefreshListener();
            
            // Test all implementations
            DataRefreshManager.RefreshType testType = DataRefreshManager.RefreshType.TRANSACTIONS;
            
            lambdaListener.onDataRefresh(testType);
            anonymousListener.onDataRefresh(testType);
            customListener.onDataRefresh(testType);
            
            assertEquals(1, lambdaCallCount.get(), "Lambda listener should be called");
            assertEquals(1, anonymousCallCount.get(), "Anonymous listener should be called");
            assertEquals(1, customListener.getCallCount(), "Custom listener should be called");
            assertEquals(testType, customListener.getLastRefreshType(), "Custom listener should receive correct type");
        }
        
        /**
         * Tests the behavior when a listener implementation throws an exception.
         * Verifies that exceptions are propagated correctly when called directly.
         */
        @Test
        @DisplayName("Should handle exception in listener implementations")
        void shouldHandleExceptionInListenerImplementations() {
            // Listener that throws exception
            DataRefreshListener exceptionListener = (type) -> {
                throw new RuntimeException("Test exception");
            };
            
            // This should throw an exception (since we're calling directly)
            assertThrows(RuntimeException.class, () -> {
                exceptionListener.onDataRefresh(DataRefreshManager.RefreshType.ALL);
            }, "Exception listener should throw exception when called directly");
        }
    }
    
    /**
     * Tests that verify the integration of DataRefreshListener with the DataRefreshManager.
     * These tests ensure that listeners work correctly when registered with the manager
     * and receive notifications as expected.
     */
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        /**
         * Tests that DataRefreshListener works correctly when integrated with DataRefreshManager.
         * Verifies that the listener receives all notifications from the manager.
         */
        @Test
        @DisplayName("Should work correctly with DataRefreshManager")
        void shouldWorkCorrectlyWithDataRefreshManager() {
            DataRefreshManager manager = DataRefreshManager.getInstance();
            TestDataRefreshListener listener = new TestDataRefreshListener();
            
            try {
                // Register listener
                manager.addListener(listener);
                
                // Trigger various refresh types
                manager.refreshTransactions();
                manager.refreshBudgets();
                manager.refreshAll();
                
                assertEquals(3, listener.getCallCount(), "Should receive all refresh notifications");
                assertEquals(DataRefreshManager.RefreshType.ALL, listener.getLastRefreshType(), 
                    "Should receive the last refresh type");
                
            } finally {
                manager.removeListener(listener);
            }
        }
        
        /**
         * Tests that the listener maintains its state across multiple manager operations.
         * Verifies that the listener's state persists even when removed and re-added to the manager.
         */
        @Test
        @DisplayName("Should maintain state across manager operations")
        void shouldMaintainStateAcrossManagerOperations() {
            DataRefreshManager manager = DataRefreshManager.getInstance();
            TestDataRefreshListener listener = new TestDataRefreshListener();
            
            try {
                manager.addListener(listener);
                
                // First batch of operations
                manager.notifyRefresh(DataRefreshManager.RefreshType.TRANSACTIONS);
                manager.notifyRefresh(DataRefreshManager.RefreshType.BUDGETS);
                
                assertEquals(2, listener.getCallCount(), "Should have 2 calls after first batch");
                
                // Remove and re-add listener
                manager.removeListener(listener);
                manager.addListener(listener);
                
                // Second batch of operations (call count should continue from previous state)
                manager.notifyRefresh(DataRefreshManager.RefreshType.CURRENCY);
                manager.notifyRefresh(DataRefreshManager.RefreshType.ALL);
                
                assertEquals(4, listener.getCallCount(), "Should have 4 total calls");
                assertEquals(DataRefreshManager.RefreshType.ALL, listener.getLastRefreshType());
                
            } finally {
                manager.removeListener(listener);
            }
        }
        
        /**
         * Tests that multiple listeners of the same implementation type can be registered.
         * Verifies that all listeners receive notifications correctly.
         */
        @Test
        @DisplayName("Should handle multiple listeners with same implementation")
        void shouldHandleMultipleListenersWithSameImplementation() {
            DataRefreshManager manager = DataRefreshManager.getInstance();
            TestDataRefreshListener listener1 = new TestDataRefreshListener();
            TestDataRefreshListener listener2 = new TestDataRefreshListener();
            TestDataRefreshListener listener3 = new TestDataRefreshListener();
            
            try {
                manager.addListener(listener1);
                manager.addListener(listener2);
                manager.addListener(listener3);
                
                manager.notifyRefresh(DataRefreshManager.RefreshType.SETTINGS);
                
                // All listeners should receive the notification
                assertEquals(1, listener1.getCallCount(), "Listener 1 should receive notification");
                assertEquals(1, listener2.getCallCount(), "Listener 2 should receive notification");
                assertEquals(1, listener3.getCallCount(), "Listener 3 should receive notification");
                
                assertEquals(DataRefreshManager.RefreshType.SETTINGS, listener1.getLastRefreshType());
                assertEquals(DataRefreshManager.RefreshType.SETTINGS, listener2.getLastRefreshType());
                assertEquals(DataRefreshManager.RefreshType.SETTINGS, listener3.getLastRefreshType());
                
            } finally {
                manager.removeListener(listener1);
                manager.removeListener(listener2);
                manager.removeListener(listener3);
            }
        }
    }
    
    /**
     * Helper class that implements DataRefreshListener for testing purposes.
     * Tracks call count and last refresh type for verification in tests.
     */
    private static class TestDataRefreshListener implements DataRefreshListener {
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final AtomicReference<DataRefreshManager.RefreshType> lastRefreshType = new AtomicReference<>();
        
        /**
         * Implements the onDataRefresh method from the DataRefreshListener interface.
         * Increments the call count and stores the refresh type for later verification.
         *
         * @param type The type of refresh that occurred
         */
        @Override
        public void onDataRefresh(DataRefreshManager.RefreshType type) {
            callCount.incrementAndGet();
            lastRefreshType.set(type);
        }
        
        /**
         * Gets the current call count.
         *
         * @return The number of times onDataRefresh has been called
         */
        public int getCallCount() {
            return callCount.get();
        }
        
        /**
         * Gets the last refresh type that was received.
         *
         * @return The last DataRefreshManager.RefreshType passed to onDataRefresh
         */
        public DataRefreshManager.RefreshType getLastRefreshType() {
            return lastRefreshType.get();
        }
        
        /**
         * Resets the state of this listener.
         * Sets call count to 0 and last refresh type to null.
         */
        public void reset() {
            callCount.set(0);
            lastRefreshType.set(null);
        }
    }
}