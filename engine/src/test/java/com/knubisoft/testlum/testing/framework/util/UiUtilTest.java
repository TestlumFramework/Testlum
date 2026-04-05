package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.locator.LocatorCollector;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.model.pages.*;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Sequence;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UiUtilTest {

    @Mock
    private LocatorCollector locatorCollector;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private WebElementFinder webElementFinder;
    @Mock
    private FileSearcher fileSearcher;
    @Mock
    private EnvironmentLoader environmentLoader;
    @Mock
    private ImageCompressor imageCompressor;
    @Mock
    private ConfigProvider configProvider;

    @InjectMocks
    private UiUtil uiUtil;

    @Nested
    class ResolveSendKeysType {
        @Test
        void withFilePrefixReturnsFilePath() {
            File dir = new File("/some/dir");
            File file = new File("/some/dir/data.csv");
            WebElement element = mock(WebElement.class);
            when(fileSearcher.searchFileFromDir(dir, "data.csv")).thenReturn(file);
            String result = uiUtil.resolveSendKeysType("file:data.csv", element, dir);
            assertEquals(file.getPath(), result);
        }

        @Test
        void withoutFilePrefixClearsElementAndReturnsValue() {
            WebElement element = mock(WebElement.class);
            String result = uiUtil.resolveSendKeysType("hello", element, new File("/dir"));
            assertEquals("hello", result);
            verify(element).clear();
        }
    }

    @Nested
    class GetLocatorByStrategy {
        @Test
        void locatorIdDelegatesToCollector() {
            Locator locator = new Locator();
            LocatorData expected = new LocatorData(null, locator);
            when(locatorCollector.getLocator("myLocator")).thenReturn(expected);
            LocatorData result = uiUtil.getLocatorByStrategy("myLocator", LocatorStrategy.LOCATOR_ID);
            assertEquals(expected, result);
        }

        @Test
        void xpathCreatesXpathLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("//div", LocatorStrategy.XPATH);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(Xpath.class, result.getLocator().getXpathOrIdOrClassName().get(0));
        }

        @Test
        void idCreatesIdLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("myId", LocatorStrategy.ID);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(Id.class, result.getLocator().getXpathOrIdOrClassName().get(0));
        }

        @Test
        void textCreatesTextLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("some text", LocatorStrategy.TEXT);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(Text.class, result.getLocator().getXpathOrIdOrClassName().get(0));
        }

        @Test
        void classCreatesClassNameLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("myClass", LocatorStrategy.CLASS);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(ClassName.class, result.getLocator().getXpathOrIdOrClassName().get(0));
        }

        @Test
        void cssSelectorCreatesCssSelectorLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("div.cls", LocatorStrategy.CSS_SELECTOR);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(CssSelector.class, result.getLocator().getXpathOrIdOrClassName().get(0));
        }
    }

    @Nested
    class ResolveHostIfNeeded {
        @Test
        void replacesAppiumLocalhost() {
            String result = uiUtil.resolveHostIfNeeded("http://10.0.2.2:8080/api");
            assertEquals("http://localhost:8080/api", result);
        }

        @Test
        void keepsNonAppiumUrl() {
            String result = uiUtil.resolveHostIfNeeded("http://example.com:8080/api");
            assertEquals("http://example.com:8080/api", result);
        }
    }

    @Nested
    class CalculatePercentageValue {
        @Test
        void returnsCorrectPercentage() {
            float result = uiUtil.calculatePercentageValue(50);
            assertEquals(0.5f, result);
        }

        @Test
        void throwsWhenOverHundred() {
            assertThrows(DefaultFrameworkException.class, () -> uiUtil.calculatePercentageValue(150));
        }

        @Test
        void hundredPercentReturnsOne() {
            float result = uiUtil.calculatePercentageValue(100);
            assertEquals(1.0f, result);
        }
    }

    @Nested
    class GetBasePageURL {
        @Test
        void extractsBaseUrl() {
            String result = uiUtil.getBasePageURL("https://example.com/some/path?query=1");
            assertEquals("https://example.com", result);
        }

        @Test
        void throwsForInvalidUrl() {
            assertThrows(DefaultFrameworkException.class, () -> uiUtil.getBasePageURL("not-a-url"));
        }
    }

    @Nested
    class GetUrl {
        @Test
        void returnsHttpUrlDirectly() {
            String result = uiUtil.getUrl("https://example.com/path", "env", null);
            assertEquals("https://example.com/path", result);
        }
    }

    @Nested
    class BuildSequence {
        @Test
        void createsSequence() {
            Sequence seq = uiUtil.buildSequence(new Point(10, 20), new Point(30, 40), 500);
            assertNotNull(seq);
        }
    }

    @Nested
    class GetCenterPoint {
        @Test
        void returnsCenterOfWindow() {
            WebDriver driver = mock(WebDriver.class);
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(driver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);
            when(window.getSize()).thenReturn(new Dimension(800, 600));
            Point center = uiUtil.getCenterPoint(driver);
            assertEquals(400, center.getX());
            assertEquals(300, center.getY());
        }
    }
}
