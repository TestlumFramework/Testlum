package com.knubisoft.testlum.testing.framework.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScreenshotUtilTest {

    @InjectMocks
    private ScreenshotUtil screenshotUtil;

    @Nested
    class TakeScreenshot {

        private interface ScreenshotDriver extends WebDriver, TakesScreenshot {
        }

        @Test
        void takeScreenshotFromDriver() {
            ScreenshotDriver driver = mock(ScreenshotDriver.class);
            File screenshotFile = new File("/tmp/screenshot.png");
            when(driver.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);
            File result = screenshotUtil.takeScreenshot((WebDriver) driver);
            assertEquals(screenshotFile, result);
        }

        @Test
        void takeScreenshotFromElement() {
            WebElement element = mock(WebElement.class);
            File screenshotFile = new File("/tmp/element-screenshot.png");
            when(element.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);
            File result = screenshotUtil.takeScreenshot(element);
            assertEquals(screenshotFile, result);
        }
    }

}
