package com.knubisoft.testlum.testing.framework.exception;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link FileLinkingException}. */
class FileLinkingExceptionTest {

    @Nested
    class ThreeArgConstructor {
        @Test
        void messageContainsFileKey() {
            File start = new File("/project/scenarios");
            File root = new File("/project");
            FileLinkingException ex = new FileLinkingException(start, root, "data.json");
            assertTrue(ex.getMessage().contains("data.json"));
        }

        @Test
        void messageContainsStartFolder() {
            File start = new File("/project/scenarios");
            File root = new File("/project");
            FileLinkingException ex = new FileLinkingException(start, root, "data.json");
            assertTrue(ex.getMessage().contains(start.toString()));
        }

        @Test
        void messageContainsRootFolder() {
            File start = new File("/project/scenarios");
            File root = new File("/project");
            FileLinkingException ex = new FileLinkingException(start, root, "data.json");
            assertTrue(ex.getMessage().contains(root.toString()));
        }

        @Test
        void messageFollowsExpectedFormat() {
            File start = new File("/a/b");
            File root = new File("/a");
            FileLinkingException ex = new FileLinkingException(start, root, "key123");
            String msg = ex.getMessage();
            assertTrue(msg.contains("Unable to find file by key"));
            assertTrue(msg.contains("key123"));
            assertTrue(msg.contains("Initial scan folder"));
        }
    }

    @Nested
    class TwoArgConstructor {
        @Test
        void messageContainsErrorMessage() {
            File start = new File("/project/config");
            FileLinkingException ex = new FileLinkingException("Config missing", start);
            assertTrue(ex.getMessage().contains("Config missing"));
        }

        @Test
        void messageContainsAbsolutePath() {
            File start = new File("/project/config");
            FileLinkingException ex = new FileLinkingException("Config missing", start);
            assertTrue(ex.getMessage().contains(start.getAbsolutePath()));
        }

        @Test
        void messageFollowsExpectedLocationFormat() {
            File start = new File("/x/y");
            FileLinkingException ex = new FileLinkingException("err", start);
            assertTrue(ex.getMessage().contains("Expected location ->"));
        }
    }

    @Nested
    class InheritanceHierarchy {
        @Test
        void isRuntimeException() {
            File start = new File("/a");
            File root = new File("/");
            FileLinkingException ex = new FileLinkingException(start, root, "k");
            assertInstanceOf(RuntimeException.class, ex);
        }
    }

    @Nested
    class Constants {
        @Test
        void folderLocationErrorMessageContainsPlaceholders() {
            String template = FileLinkingException.FOLDER_LOCATION_ERROR_MESSAGE;
            assertTrue(template.contains("%s"));
            assertTrue(template.contains("Expected location"));
        }
    }
}
