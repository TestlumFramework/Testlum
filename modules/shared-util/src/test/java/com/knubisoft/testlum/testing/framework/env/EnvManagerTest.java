package com.knubisoft.testlum.testing.framework.env;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvManagerTest {

    @AfterEach
    void tearDown() {
        EnvManager.clearCurrentEnv();
    }

    @Nested
    class CurrentEnv {
        @Test
        void throwsWhenNotSet() {
            EnvManager.clearCurrentEnv();
            assertThrows(DefaultFrameworkException.class, EnvManager::currentEnv);
        }

        @Test
        void returnsSetValue() {
            EnvManager.setCurrentEnv("staging");
            assertEquals("staging", EnvManager.currentEnv());
        }
    }

    @Nested
    class SetCurrentEnv {
        @Test
        void overwritesPreviousValue() {
            EnvManager.setCurrentEnv("dev");
            EnvManager.setCurrentEnv("prod");
            assertEquals("prod", EnvManager.currentEnv());
        }
    }

    @Nested
    class ClearCurrentEnv {
        @Test
        void clearedEnvThrows() {
            EnvManager.setCurrentEnv("test");
            EnvManager.clearCurrentEnv();
            assertThrows(DefaultFrameworkException.class, EnvManager::currentEnv);
        }
    }

    @Nested
    class ThreadIsolation {
        @Test
        void differentThreadsHaveDifferentEnvs() throws InterruptedException {
            EnvManager.setCurrentEnv("main-env");
            final AtomicReference<String> otherThreadEnv = new AtomicReference<>();
            final AtomicReference<Throwable> otherThreadError = new AtomicReference<>();

            final Thread thread = new Thread(() -> {
                try {
                    EnvManager.setCurrentEnv("other-env");
                    otherThreadEnv.set(EnvManager.currentEnv());
                } catch (Throwable t) {
                    otherThreadError.set(t);
                } finally {
                    EnvManager.clearCurrentEnv();
                }
            });
            thread.start();
            thread.join();

            assertEquals("main-env", EnvManager.currentEnv());
            assertEquals("other-env", otherThreadEnv.get());
        }
    }
}
