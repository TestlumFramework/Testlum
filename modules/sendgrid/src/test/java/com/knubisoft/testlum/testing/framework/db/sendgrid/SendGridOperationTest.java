package com.knubisoft.testlum.testing.framework.db.sendgrid;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SendGridOperationTest {

    private final SendGridOperation operation = new SendGridOperation();

    @Nested
    class Apply {

        @Test
        void returnsNullForAnyInput() {
            final Source source = mock(Source.class);

            final AbstractStorageOperation.StorageOperationResult result = operation.apply(source, "alias");

            assertNull(result);
        }

        @Test
        void returnsNullForNullAlias() {
            final Source source = mock(Source.class);

            final AbstractStorageOperation.StorageOperationResult result = operation.apply(source, null);

            assertNull(result);
        }

        @Test
        void returnsNullForNullSource() {
            final Source source = null;
            final AbstractStorageOperation.StorageOperationResult result = operation.apply(source, "alias");

            assertNull(result);
        }

        @Test
        void returnsNullForEmptyAlias() {
            final Source source = mock(Source.class);

            final AbstractStorageOperation.StorageOperationResult result = operation.apply(source, "");

            assertNull(result);
        }
    }

    @Nested
    class ClearSystem {

        @Test
        void doesNotThrow() {
            assertDoesNotThrow(operation::clearSystem);
        }

        @Test
        void canBeCalledMultipleTimes() {
            assertDoesNotThrow(() -> {
                operation.clearSystem();
                operation.clearSystem();
                operation.clearSystem();
            });
        }
    }
}
