package com.knubisoft.testlum.testing.framework.db.ses;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class SESOperationTest {

    private final SESOperation operation = new SESOperation();

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
