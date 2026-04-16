package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertNotEqual;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssertEqualityHelperTest {

    @Nested
    class CheckContentIsEqual {

        @Test
        void passesWhenAllContentItemsAreIdentical() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().addAll(List.of("same", "same"));

            assertDoesNotThrow(() -> AssertEqualityHelper.checkContentIsEqual(equal));
        }

        @Test
        void throwsWhenContentItemsDiffer() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().addAll(List.of("one", "two"));

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> AssertEqualityHelper.checkContentIsEqual(equal));
            assertTrue(ex.getMessage().contains("not equal"));
        }

        @Test
        void passesWithThreeIdenticalItems() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().addAll(List.of("abc", "abc", "abc"));

            assertDoesNotThrow(() -> AssertEqualityHelper.checkContentIsEqual(equal));
        }

        @Test
        void throwsWhenOneOfThreeItemsDiffers() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().addAll(List.of("abc", "abc", "xyz"));

            assertThrows(DefaultFrameworkException.class,
                    () -> AssertEqualityHelper.checkContentIsEqual(equal));
        }

        @Test
        void passesWhenContentDiffersOnlyByLineEndings() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().addAll(List.of("line1\nline2", "line1\r\nline2"));

            assertDoesNotThrow(() -> AssertEqualityHelper.checkContentIsEqual(equal));
        }

        @Test
        void passesWhenAllItemsAreNull() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().add(null);
            equal.getContent().add(null);

            assertDoesNotThrow(() -> AssertEqualityHelper.checkContentIsEqual(equal));
        }

        @Test
        void throwsWhenOneItemIsNullAndOtherIsNot() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().add(null);
            equal.getContent().add("something");

            assertThrows(DefaultFrameworkException.class,
                    () -> AssertEqualityHelper.checkContentIsEqual(equal));
        }
    }

    @Nested
    class CheckContentNotEqual {

        @Test
        void passesWhenContentItemsDiffer() {
            AssertNotEqual notEqual = new AssertNotEqual();
            notEqual.getContent().addAll(List.of("one", "two"));

            assertDoesNotThrow(() -> AssertEqualityHelper.checkContentNotEqual(notEqual));
        }

        @Test
        void throwsWhenAllContentItemsAreIdentical() {
            AssertNotEqual notEqual = new AssertNotEqual();
            notEqual.getContent().addAll(List.of("same", "same"));

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> AssertEqualityHelper.checkContentNotEqual(notEqual));
            assertTrue(ex.getMessage().contains("is equal"));
        }

        @Test
        void passesWhenOneOfThreeItemsDiffers() {
            AssertNotEqual notEqual = new AssertNotEqual();
            notEqual.getContent().addAll(List.of("abc", "abc", "xyz"));

            assertDoesNotThrow(() -> AssertEqualityHelper.checkContentNotEqual(notEqual));
        }

        @Test
        void throwsWhenThreeItemsAreAllIdentical() {
            AssertNotEqual notEqual = new AssertNotEqual();
            notEqual.getContent().addAll(List.of("same", "same", "same"));

            assertThrows(DefaultFrameworkException.class,
                    () -> AssertEqualityHelper.checkContentNotEqual(notEqual));
        }

        @Test
        void throwsWhenContentDiffersOnlyByLineEndings() {
            AssertNotEqual notEqual = new AssertNotEqual();
            notEqual.getContent().addAll(List.of("line1\nline2", "line1\r\nline2"));

            assertThrows(DefaultFrameworkException.class,
                    () -> AssertEqualityHelper.checkContentNotEqual(notEqual));
        }
    }

    @Nested
    class FormatContent {

        @Test
        void formatsContentWithComma() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().addAll(List.of("one", "two", "three"));

            String formatted = AssertEqualityHelper.formatContent(equal);
            assertEquals("one,two,three", formatted);
        }

        @Test
        void formatsSingleContent() {
            AssertEqual equal = new AssertEqual();
            equal.getContent().add("only");

            String formatted = AssertEqualityHelper.formatContent(equal);
            assertEquals("only", formatted);
        }
    }

    @Nested
    class PrivateConstructor {

        @Test
        void cannotBeInstantiated() throws Exception {
            var constructor = AssertEqualityHelper.class.getDeclaredConstructor();
            assertFalse(constructor.canAccess(null));
        }
    }
}
