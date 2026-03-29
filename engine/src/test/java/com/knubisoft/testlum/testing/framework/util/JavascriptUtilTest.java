package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JavascriptUtilTest {

    @Mock
    private FileSearcher fileSearcher;

    @InjectMocks
    private JavascriptUtil javascriptUtil;

    @Nested
    class ExecuteJsScript {
        @Test
        void delegatesToJavascriptExecutor() {
            WebDriver driver = mock(WebDriver.class, org.mockito.Mockito.withSettings()
                    .extraInterfaces(JavascriptExecutor.class));
            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            when(jsExecutor.executeScript("return 1;")).thenReturn(1L);
            Object result = javascriptUtil.executeJsScript("return 1;", driver);
            assertEquals(1L, result);
            verify(jsExecutor).executeScript("return 1;");
        }
    }

    @Nested
    class ReadCommands {
        @TempDir
        Path tempDir;

        @Test
        void readsAndJoinsFileLines() throws IOException {
            File jsFile = Files.writeString(tempDir.resolve("test.js"), "line1\nline2\nline3").toFile();
            when(fileSearcher.searchFileFromDataFolder("test.js")).thenReturn(jsFile);
            String result = javascriptUtil.readCommands("test.js");
            assertEquals("line1line2line3", result);
        }

        @Test
        void throwsWhenFileNotFound() {
            when(fileSearcher.searchFileFromDataFolder("missing.js"))
                    .thenThrow(new RuntimeException("not found"));
            assertThrows(RuntimeException.class, () -> javascriptUtil.readCommands("missing.js"));
        }
    }
}
