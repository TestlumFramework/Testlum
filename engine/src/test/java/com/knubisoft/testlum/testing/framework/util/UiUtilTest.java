package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.constant.JavascriptConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.locator.LocatorCollector;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Web;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import com.knubisoft.testlum.testing.model.scenario.LocatorStrategy;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Sequence;

import java.io.File;
import java.util.Optional;

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

    // ================================================================
    // ResolveSendKeysType
    // ================================================================

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
        void withFilePrefixDoesNotClearElement() {
            File dir = new File("/some/dir");
            File file = new File("/some/dir/image.png");
            WebElement element = mock(WebElement.class);
            when(fileSearcher.searchFileFromDir(dir, "image.png")).thenReturn(file);
            uiUtil.resolveSendKeysType("file:image.png", element, dir);
            verify(element, never()).clear();
        }

        @Test
        void withoutFilePrefixClearsElementAndReturnsValue() {
            WebElement element = mock(WebElement.class);
            String result = uiUtil.resolveSendKeysType("hello", element, new File("/dir"));
            assertEquals("hello", result);
            verify(element).clear();
        }

        @Test
        void withoutFilePrefixEmptyValueClearsAndReturns() {
            WebElement element = mock(WebElement.class);
            String result = uiUtil.resolveSendKeysType("", element, new File("/dir"));
            assertEquals("", result);
            verify(element).clear();
        }

        @Test
        void filePrefixWithNestedPath() {
            File dir = new File("/base");
            File file = new File("/base/sub/folder/file.txt");
            WebElement element = mock(WebElement.class);
            when(fileSearcher.searchFileFromDir(dir, "sub/folder/file.txt")).thenReturn(file);
            String result = uiUtil.resolveSendKeysType("file:sub/folder/file.txt", element, dir);
            assertEquals(file.getPath(), result);
        }
    }

    // ================================================================
    // GetLocatorByStrategy
    // ================================================================

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
            assertEquals("//div", ((Xpath) result.getLocator().getXpathOrIdOrClassName().get(0)).getValue());
        }

        @Test
        void idCreatesIdLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("myId", LocatorStrategy.ID);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(Id.class, result.getLocator().getXpathOrIdOrClassName().get(0));
            assertEquals("myId", ((Id) result.getLocator().getXpathOrIdOrClassName().get(0)).getValue());
        }

        @Test
        void textCreatesTextLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("some text", LocatorStrategy.TEXT);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(Text.class, result.getLocator().getXpathOrIdOrClassName().get(0));
            Text text = (Text) result.getLocator().getXpathOrIdOrClassName().get(0);
            assertEquals("some text", text.getValue());
            assertFalse(text.isPlaceholder());
        }

        @Test
        void classCreatesClassNameLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("myClass", LocatorStrategy.CLASS);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(ClassName.class, result.getLocator().getXpathOrIdOrClassName().get(0));
            assertEquals("myClass", ((ClassName) result.getLocator().getXpathOrIdOrClassName().get(0)).getValue());
        }

        @Test
        void cssSelectorCreatesCssSelectorLocator() {
            LocatorData result = uiUtil.getLocatorByStrategy("div.cls", LocatorStrategy.CSS_SELECTOR);
            assertEquals(1, result.getLocator().getXpathOrIdOrClassName().size());
            assertInstanceOf(CssSelector.class, result.getLocator().getXpathOrIdOrClassName().get(0));
            assertEquals("div.cls", ((CssSelector) result.getLocator().getXpathOrIdOrClassName().get(0)).getValue());
        }

        @Test
        void setsLocatorIdForNonLocatorIdStrategies() {
            LocatorData result = uiUtil.getLocatorByStrategy("//xpath-val", LocatorStrategy.XPATH);
            assertEquals("//xpath-val", result.getLocator().getLocatorId());
        }
    }

    // ================================================================
    // HighlightElementIfRequired
    // ================================================================

    @Nested
    class HighlightElementIfRequired {

        @Test
        void highlightsWhenFlagTrueAndNotAppium() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            uiUtil.highlightElementIfRequired(true, element, driver);
            verify(javascriptUtil).executeJsScript(
                    eq(JavascriptConstant.HIGHLIGHT_SCRIPT), eq(driver), eq(element));
        }

        @Test
        void doesNotHighlightWhenFlagFalse() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            uiUtil.highlightElementIfRequired(false, element, driver);
            verifyNoInteractions(javascriptUtil);
        }

        @Test
        void doesNotHighlightForAppiumDriver() {
            WebElement element = mock(WebElement.class);
            AppiumDriver driver = mock(AppiumDriver.class);
            uiUtil.highlightElementIfRequired(true, element, driver);
            verifyNoInteractions(javascriptUtil);
        }

        @Test
        void doesNotHighlightForAppiumDriverWhenFlagFalse() {
            WebElement element = mock(WebElement.class);
            AppiumDriver driver = mock(AppiumDriver.class);
            uiUtil.highlightElementIfRequired(false, element, driver);
            verifyNoInteractions(javascriptUtil);
        }
    }

    // ================================================================
    // CalculatePercentageValue
    // ================================================================

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

        @Test
        void zeroPercentReturnsZero() {
            float result = uiUtil.calculatePercentageValue(0);
            assertEquals(0.0f, result);
        }

        @Test
        void twentyFivePercentReturnsPointTwoFive() {
            float result = uiUtil.calculatePercentageValue(25);
            assertEquals(0.25f, result);
        }

        @Test
        void throwsWhenSlightlyOverHundred() {
            assertThrows(DefaultFrameworkException.class, () -> uiUtil.calculatePercentageValue(100.1f));
        }
    }

    // ================================================================
    // ResolveHostIfNeeded
    // ================================================================

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

        @Test
        void replacesMultipleOccurrences() {
            String result = uiUtil.resolveHostIfNeeded("http://10.0.2.2:8080/redirect?to=http://10.0.2.2:9090");
            assertEquals("http://localhost:8080/redirect?to=http://localhost:9090", result);
        }

        @Test
        void handlesUrlWithoutPort() {
            String result = uiUtil.resolveHostIfNeeded("http://10.0.2.2/path");
            assertEquals("http://localhost/path", result);
        }
    }

    // ================================================================
    // GetUrl
    // ================================================================

    @Nested
    class GetUrl {

        @Test
        void returnsHttpUrlDirectly() {
            String result = uiUtil.getUrl("https://example.com/path", "env", null);
            assertEquals("https://example.com/path", result);
        }

        @Test
        void returnsHttpUrlDirectlyWithHttp() {
            String result = uiUtil.getUrl("http://example.com/page", "env", null);
            assertEquals("http://example.com/page", result);
        }

        @Test
        void appendsRelativePathForWebType() {
            Web web = mock(Web.class);
            when(web.getBaseUrl()).thenReturn("https://base.com");
            when(environmentLoader.getWebSettings("testEnv")).thenReturn(Optional.of(web));
            String result = uiUtil.getUrl("/login", "testEnv", UiType.WEB);
            assertEquals("https://base.com/login", result);
        }

        @Test
        void appendsRelativePathForMobileBrowserType() {
            Mobilebrowser mb = mock(Mobilebrowser.class);
            when(mb.getBaseUrl()).thenReturn("https://mobile.com");
            when(environmentLoader.getMobileBrowserSettings("mobileEnv")).thenReturn(Optional.of(mb));
            String result = uiUtil.getUrl("/home", "mobileEnv", UiType.MOBILE_BROWSER);
            assertEquals("https://mobile.com/home", result);
        }

        @Test
        void httpUrlNotModifiedEvenWithUiType() {
            String result = uiUtil.getUrl("https://absolute.com/page", "env", UiType.WEB);
            assertEquals("https://absolute.com/page", result);
        }
    }

    // ================================================================
    // GetBasePageURL
    // ================================================================

    @Nested
    class GetBasePageURL {

        @Test
        void extractsBaseUrl() {
            String result = uiUtil.getBasePageURL("https://example.com/some/path?query=1");
            assertEquals("https://example.com", result);
        }

        @Test
        void extractsBaseUrlWithPort() {
            String result = uiUtil.getBasePageURL("http://localhost:8080/api/v1");
            assertEquals("http://localhost", result);
        }

        @Test
        void extractsBaseUrlHttps() {
            String result = uiUtil.getBasePageURL("https://secure.site.io/dashboard");
            assertEquals("https://secure.site.io", result);
        }

        @Test
        void throwsForInvalidUrl() {
            assertThrows(DefaultFrameworkException.class, () -> uiUtil.getBasePageURL("not-a-url"));
        }

        @Test
        void throwsForEmptyString() {
            assertThrows(DefaultFrameworkException.class, () -> uiUtil.getBasePageURL(""));
        }

        @Test
        void extractsBaseUrlWithFragment() {
            String result = uiUtil.getBasePageURL("https://example.com/page#section");
            assertEquals("https://example.com", result);
        }
    }

    // ================================================================
    // BuildSequence
    // ================================================================

    @Nested
    class BuildSequence {

        @Test
        void createsSequenceWithValidPoints() {
            Sequence seq = uiUtil.buildSequence(new Point(10, 20), new Point(30, 40), 500);
            assertNotNull(seq);
        }

        @Test
        void createsSequenceWithZeroDuration() {
            Sequence seq = uiUtil.buildSequence(new Point(0, 0), new Point(100, 100), 0);
            assertNotNull(seq);
        }

        @Test
        void createsSequenceWithSameStartEnd() {
            Sequence seq = uiUtil.buildSequence(new Point(50, 50), new Point(50, 50), 200);
            assertNotNull(seq);
        }

        @Test
        void createsSequenceWithLargeDuration() {
            Sequence seq = uiUtil.buildSequence(new Point(0, 0), new Point(500, 800), 5000);
            assertNotNull(seq);
        }
    }

    // ================================================================
    // GetCenterPoint
    // ================================================================

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

        @Test
        void returnsCenterOfOddDimension() {
            WebDriver driver = mock(WebDriver.class);
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(driver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);
            when(window.getSize()).thenReturn(new Dimension(1001, 501));
            Point center = uiUtil.getCenterPoint(driver);
            assertEquals(500, center.getX());
            assertEquals(250, center.getY());
        }

        @Test
        void returnsCenterOfSmallWindow() {
            WebDriver driver = mock(WebDriver.class);
            WebDriver.Options options = mock(WebDriver.Options.class);
            WebDriver.Window window = mock(WebDriver.Window.class);
            when(driver.manage()).thenReturn(options);
            when(options.window()).thenReturn(window);
            when(window.getSize()).thenReturn(new Dimension(2, 2));
            Point center = uiUtil.getCenterPoint(driver);
            assertEquals(1, center.getX());
            assertEquals(1, center.getY());
        }
    }

    // ================================================================
    // TakeScreenshot
    // ================================================================

    @Nested
    class TakeScreenshot {

        private interface ScreenshotDriver extends WebDriver, TakesScreenshot {
        }

        @Test
        void takeScreenshotFromDriver() {
            ScreenshotDriver driver = mock(ScreenshotDriver.class);
            File screenshotFile = new File("/tmp/screenshot.png");
            when(driver.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);
            File result = uiUtil.takeScreenshot((WebDriver) driver);
            assertEquals(screenshotFile, result);
        }

        @Test
        void takeScreenshotFromElement() {
            WebElement element = mock(WebElement.class);
            File screenshotFile = new File("/tmp/element-screenshot.png");
            when(element.getScreenshotAs(OutputType.FILE)).thenReturn(screenshotFile);
            File result = uiUtil.takeScreenshot(element);
            assertEquals(screenshotFile, result);
        }
    }

    // ================================================================
    // GetElementAttribute
    // ================================================================

    @Nested
    class GetElementAttribute {

        @Test
        void returnsValueFromJavascript() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            when(javascriptUtil.executeJsScript(
                    eq(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT), eq(driver), eq(element), eq("href")))
                    .thenReturn("https://example.com");

            String result = uiUtil.getElementAttribute(element, "href", driver);
            assertEquals("https://example.com", result);
        }

        @Test
        void fallsBackToGetAttributeWhenJsReturnsNull() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            when(javascriptUtil.executeJsScript(
                    eq(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT), eq(driver), eq(element), eq("value")))
                    .thenReturn(null);
            when(element.getAttribute("value")).thenReturn("fallback-value");

            String result = uiUtil.getElementAttribute(element, "value", driver);
            assertEquals("fallback-value", result);
        }

        @Test
        void fallsBackToGetAttributeWhenJsReturnsBlank() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            when(javascriptUtil.executeJsScript(
                    eq(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT), eq(driver), eq(element), eq("class")))
                    .thenReturn("   ");
            when(element.getAttribute("class")).thenReturn("my-class");

            String result = uiUtil.getElementAttribute(element, "class", driver);
            assertEquals("my-class", result);
        }

        @Test
        void throwsWhenBothJsAndGetAttributeReturnBlank() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            when(javascriptUtil.executeJsScript(
                    eq(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT), eq(driver), eq(element), eq("data-x")))
                    .thenReturn(null);
            when(element.getAttribute("data-x")).thenReturn(null);

            assertThrows(DefaultFrameworkException.class,
                    () -> uiUtil.getElementAttribute(element, "data-x", driver));
        }

        @Test
        void throwsWhenBothReturnEmptyString() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            when(javascriptUtil.executeJsScript(
                    eq(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT), eq(driver), eq(element), eq("title")))
                    .thenReturn("");
            when(element.getAttribute("title")).thenReturn("");

            assertThrows(DefaultFrameworkException.class,
                    () -> uiUtil.getElementAttribute(element, "title", driver));
        }

        @Test
        void throwsWhenFallbackReturnsBlankString() {
            WebElement element = mock(WebElement.class);
            WebDriver driver = mock(WebDriver.class);
            when(javascriptUtil.executeJsScript(
                    eq(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT), eq(driver), eq(element), eq("id")))
                    .thenReturn(null);
            when(element.getAttribute("id")).thenReturn("   ");

            assertThrows(DefaultFrameworkException.class,
                    () -> uiUtil.getElementAttribute(element, "id", driver));
        }
    }
}
