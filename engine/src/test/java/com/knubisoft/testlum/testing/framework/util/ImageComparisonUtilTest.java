package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageComparisonUtilTest {

    @Mock
    private UiUtil uiUtil;

    @Mock
    private ResultUtil resultUtil;

    @Mock
    private JavascriptUtil javascriptUtil;

    @InjectMocks
    private ImageComparisonUtil imageComparisonUtil;

    @Nested
    class ProcessImageComparisonResult {
        @Test
        void doesNothingOnMatch() {
            ImageComparisonResult comparisonResult = mock(ImageComparisonResult.class);
            when(comparisonResult.getImageComparisonState()).thenReturn(ImageComparisonState.MATCH);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> imageComparisonUtil.processImageComparisonResult(
                    comparisonResult, "expected.png", true, new File("/tmp"), result));
            verifyNoInteractions(uiUtil);
        }
    }

    @Nested
    class GetStatusBarHeight {
        @Test
        void usesCapabilityWhenAvailable() {
            RemoteWebDriver driver = mock(RemoteWebDriver.class);
            Capabilities caps = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(caps);
            when(caps.getCapability("statBarHeight")).thenReturn(48L);
            int height = imageComparisonUtil.getStatusBarHeight(driver);
            assertEquals(48, height);
        }

        @Test
        void calculatesFromScreenAndWindowHeight() {
            RemoteWebDriver driver = mock(RemoteWebDriver.class);
            Capabilities caps = mock(Capabilities.class);
            when(driver.getCapabilities()).thenReturn(caps);
            when(caps.getCapability("statBarHeight")).thenReturn(null);
            when(javascriptUtil.executeJsScript("return screen.height;", driver)).thenReturn(2400L);
            when(javascriptUtil.executeJsScript("return window.innerHeight;", driver)).thenReturn(2300L);
            int height = imageComparisonUtil.getStatusBarHeight(driver);
            assertEquals(100, height);
        }
    }
}
