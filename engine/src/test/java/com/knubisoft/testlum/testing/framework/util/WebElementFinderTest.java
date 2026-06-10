package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.framework.util.check.ElementCheckChain;
import com.knubisoft.testlum.testing.framework.util.check.PageLoadCheck;
import com.knubisoft.testlum.testing.model.global_config.BrowserSettings;
import com.knubisoft.testlum.testing.model.global_config.ElementAutowait;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.Web;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebElementFinderTest {

    @Mock
    private EnvironmentLoader environmentLoader;

    @Mock
    private ByService byService;

    @Mock
    private PageLoadCheck pageLoadCheck;

    @Mock
    private ElementCheckChain elementCheckChain;

    @InjectMocks
    private WebElementFinder webElementFinder;

    // --- Helper to set up Web settings mocks ---
    private void stubWebSettings(final int seconds) {
        Web web = mock(Web.class);
        BrowserSettings browserSettings = mock(BrowserSettings.class);
        ElementAutowait autowait = mock(ElementAutowait.class);
        when(environmentLoader.getWebSettings(any())).thenReturn(Optional.of(web));
        when(web.getBrowserSettings()).thenReturn(browserSettings);
        when(browserSettings.getElementAutowait()).thenReturn(autowait);
        when(autowait.getSeconds()).thenReturn(seconds);
    }

    private void stubNativeSettings(final int seconds) {
        Native nativeSettings = mock(Native.class);
        ElementAutowait autowait = mock(ElementAutowait.class);
        when(environmentLoader.getNativeSettings(any())).thenReturn(Optional.of(nativeSettings));
        when(nativeSettings.getElementAutowait()).thenReturn(autowait);
        when(autowait.getSeconds()).thenReturn(seconds);
    }

    private interface JsWebDriver extends WebDriver, JavascriptExecutor {
    }

    private JsWebDriver createJsDriver() {
        return mock(JsWebDriver.class);
    }

    // ================================================================
    // ExtractLocatorValue
    // ================================================================

    @Nested
    class ExtractLocatorValue {

        @Test
        void extractsFromXpath() {
            By by = By.xpath("//div[@id='test']");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("//div[@id='test']"));
        }

        @Test
        void extractsFromId() {
            By by = By.id("myElement");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("myElement"));
        }

        @Test
        void extractsFromCss() {
            By by = By.cssSelector("div.class");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("div.class"));
        }

        @Test
        void extractsFromClassName() {
            By by = By.className("my-class");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("my-class"));
        }

        @Test
        void extractsFromName() {
            By by = By.name("fieldName");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("fieldName"));
        }

        @Test
        void extractsFromTagName() {
            By by = By.tagName("input");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("input"));
        }

        @Test
        void extractsFromLinkText() {
            By by = By.linkText("Click here");
            String value = webElementFinder.extractLocatorValue(by);
            assertNotNull(value);
            assertTrue(value.contains("Click here"));
        }
    }

    // ================================================================
    // GetLocatorsByType
    // ================================================================

    @Nested
    class GetLocatorsByType {

        @Test
        void returnsCorrectXpathTypeList() {
            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            Id id = new Id();
            id.setValue("test");
            locator.getXpathOrIdOrClassName().add(xpath);
            locator.getXpathOrIdOrClassName().add(id);
            List<Xpath> xpaths = webElementFinder.getLocatorsByType(locator, Xpath.class);
            assertEquals(1, xpaths.size());
            assertEquals("//div", xpaths.get(0).getValue());
        }

        @Test
        void returnsMultipleOfSameType() {
            Locator locator = new Locator();
            Xpath xpath1 = new Xpath();
            xpath1.setValue("//div");
            Xpath xpath2 = new Xpath();
            xpath2.setValue("//span");
            locator.getXpathOrIdOrClassName().add(xpath1);
            locator.getXpathOrIdOrClassName().add(xpath2);
            List<Xpath> xpaths = webElementFinder.getLocatorsByType(locator, Xpath.class);
            assertEquals(2, xpaths.size());
        }

        @Test
        void returnsIdType() {
            Locator locator = new Locator();
            Id id = new Id();
            id.setValue("myId");
            Xpath xpath = new Xpath();
            xpath.setValue("//a");
            locator.getXpathOrIdOrClassName().add(id);
            locator.getXpathOrIdOrClassName().add(xpath);
            List<Id> ids = webElementFinder.getLocatorsByType(locator, Id.class);
            assertEquals(1, ids.size());
            assertEquals("myId", ids.get(0).getValue());
        }

        @Test
        void throwsWhenCssSelectorTypeNotFound() {
            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);
            assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.getLocatorsByType(locator, CssSelector.class));
        }

        @Test
        void throwsWhenTextTypeNotFound() {
            Locator locator = new Locator();
            Id id = new Id();
            id.setValue("someId");
            locator.getXpathOrIdOrClassName().add(id);
            assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.getLocatorsByType(locator, Text.class));
        }

        @Test
        void throwsWhenClassNameTypeNotFound() {
            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);
            assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.getLocatorsByType(locator, ClassName.class));
        }
    }

    // ================================================================
    // SplitByLocatorType
    // ================================================================

    @Nested
    class SplitByLocatorType {

        @Test
        void groupsLocatorsByClass() {
            Xpath xpath1 = new Xpath();
            xpath1.setValue("//a");
            Xpath xpath2 = new Xpath();
            xpath2.setValue("//b");
            Id id = new Id();
            id.setValue("myId");
            Locator locator = new Locator();
            locator.getXpathOrIdOrClassName().add(xpath1);
            locator.getXpathOrIdOrClassName().add(xpath2);
            locator.getXpathOrIdOrClassName().add(id);
            Map<Class<?>, List<Object>> result = webElementFinder.splitByLocatorType(locator);
            assertEquals(2, result.size());
            assertEquals(2, result.get(Xpath.class).size());
            assertEquals(1, result.get(Id.class).size());
        }

        @Test
        void groupsMixedLocatorTypes() {
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            Id id = new Id();
            id.setValue("elem");
            CssSelector css = new CssSelector();
            css.setValue("div.cls");
            ClassName className = new ClassName();
            className.setValue("myClass");
            Text text = new Text();
            text.setValue("some text");

            Locator locator = new Locator();
            locator.getXpathOrIdOrClassName().add(xpath);
            locator.getXpathOrIdOrClassName().add(id);
            locator.getXpathOrIdOrClassName().add(css);
            locator.getXpathOrIdOrClassName().add(className);
            locator.getXpathOrIdOrClassName().add(text);

            Map<Class<?>, List<Object>> result = webElementFinder.splitByLocatorType(locator);
            assertEquals(5, result.size());
            assertTrue(result.containsKey(Xpath.class));
            assertTrue(result.containsKey(Id.class));
            assertTrue(result.containsKey(CssSelector.class));
            assertTrue(result.containsKey(ClassName.class));
            assertTrue(result.containsKey(Text.class));
        }

        @Test
        void singleLocatorProducesSingleGroup() {
            Locator locator = new Locator();
            Id id = new Id();
            id.setValue("onlyOne");
            locator.getXpathOrIdOrClassName().add(id);
            Map<Class<?>, List<Object>> result = webElementFinder.splitByLocatorType(locator);
            assertEquals(1, result.size());
            assertEquals(1, result.get(Id.class).size());
        }

        @Test
        void emptyLocatorReturnsEmptyMap() {
            Locator locator = new Locator();
            Map<Class<?>, List<Object>> result = webElementFinder.splitByLocatorType(locator);
            assertTrue(result.isEmpty());
        }
    }

    // ================================================================
    // Init / CreateClassToType
    // ================================================================

    @Nested
    class InitAndCreateClassToType {

        @Test
        void initDoesNotThrow() {
            assertDoesNotThrow(() -> webElementFinder.init());
        }

        @Test
        void initCalledTwiceDoesNotThrow() {
            webElementFinder.init();
            assertDoesNotThrow(() -> webElementFinder.init());
        }

        @Test
        void afterInitFindWorksForXpathType() {
            webElementFinder.init();

            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);

            when(byService.xpath(anyList())).thenReturn(List.of(By.xpath("//div")));

            WebElement mockElement = mock(WebElement.class);
            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(any(By.class))).thenReturn(mockElement);
            stubWebSettings(5);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            WebElement result = webElementFinder.find(new LocatorData(null, locator), deps);
            assertNotNull(result);
            assertEquals(mockElement, result);
        }
    }

    // ================================================================
    // Find - success path
    // ================================================================

    @Nested
    class Find {

        @Test
        void findSuccessWithXpathLocator() {
            webElementFinder.init();

            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//button[@id='submit']");
            locator.getXpathOrIdOrClassName().add(xpath);

            By expectedBy = By.xpath("//button[@id='submit']");
            when(byService.xpath(anyList())).thenReturn(List.of(expectedBy));

            WebElement mockElement = mock(WebElement.class);
            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(expectedBy)).thenReturn(mockElement);
            stubWebSettings(10);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            WebElement result = webElementFinder.find(new LocatorData(null, locator), deps);
            assertEquals(mockElement, result);
        }

        @Test
        void findSuccessWithIdLocator() {
            webElementFinder.init();

            Locator locator = new Locator();
            Id id = new Id();
            id.setValue("submitBtn");
            locator.getXpathOrIdOrClassName().add(id);

            By expectedBy = By.id("submitBtn");
            when(byService.id(anyList())).thenReturn(List.of(expectedBy));

            WebElement mockElement = mock(WebElement.class);
            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(expectedBy)).thenReturn(mockElement);
            stubWebSettings(10);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            WebElement result = webElementFinder.find(new LocatorData(null, locator), deps);
            assertEquals(mockElement, result);
        }

        @Test
        void findWithNativeTypeSkipsDomWait() {
            webElementFinder.init();

            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//button");
            locator.getXpathOrIdOrClassName().add(xpath);

            By expectedBy = By.xpath("//button");
            when(byService.xpath(anyList())).thenReturn(List.of(expectedBy));

            WebElement mockElement = mock(WebElement.class);
            WebDriver mockDriver = mock(WebDriver.class);
            when(mockDriver.findElement(expectedBy)).thenReturn(mockElement);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.NATIVE).build();

            WebElement result = webElementFinder.find(new LocatorData(null, locator), deps);
            assertEquals(mockElement, result);
            verify(pageLoadCheck).waitUntilDomReady(deps);
        }
    }

    // ================================================================
    // FindElement - found on first try vs empty
    // ================================================================

    @Nested
    class FindElement {

        @Test
        void elementFoundOnFirstTry() {
            webElementFinder.init();

            Locator locator = new Locator();
            Id id = new Id();
            id.setValue("firstTryId");
            locator.getXpathOrIdOrClassName().add(id);

            By expectedBy = By.id("firstTryId");
            when(byService.id(anyList())).thenReturn(List.of(expectedBy));

            WebElement mockElement = mock(WebElement.class);
            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(expectedBy)).thenReturn(mockElement);
            stubWebSettings(5);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            WebElement result = webElementFinder.find(new LocatorData(null, locator), deps);
            assertEquals(mockElement, result);
            verify(mockDriver, times(1)).findElement(expectedBy);
        }

        @Test
        void elementFoundOnSecondLocator() {
            webElementFinder.init();

            Locator locator = new Locator();
            Xpath xpath1 = new Xpath();
            xpath1.setValue("//missing");
            Xpath xpath2 = new Xpath();
            xpath2.setValue("//found");
            locator.getXpathOrIdOrClassName().add(xpath1);
            locator.getXpathOrIdOrClassName().add(xpath2);

            By by1 = By.xpath("//missing");
            By by2 = By.xpath("//found");
            when(byService.xpath(anyList())).thenReturn(List.of(by1, by2));

            WebElement mockElement = mock(WebElement.class);
            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(by1)).thenThrow(new NoSuchElementException("not found"));
            when(mockDriver.findElement(by2)).thenReturn(mockElement);
            stubWebSettings(5);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            WebElement result = webElementFinder.find(new LocatorData(null, locator), deps);
            assertEquals(mockElement, result);
        }
    }

    // ================================================================
    // WaitForDomToComplete
    // ================================================================

    @Nested
    class WaitForDomToComplete {

        @Test
        void nativeTypeSkipsDomCheck() {
            webElementFinder.init();

            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);

            when(byService.xpath(anyList())).thenReturn(List.of(By.xpath("//div")));

            WebElement mockElement = mock(WebElement.class);
            WebDriver mockDriver = mock(WebDriver.class);
            when(mockDriver.findElement(any(By.class))).thenReturn(mockElement);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.NATIVE).build();

            webElementFinder.find(new LocatorData(null, locator), deps);
            verify(pageLoadCheck).waitUntilDomReady(deps);
        }

        @Test
        void webTypeChecksDom() {
            webElementFinder.init();

            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);

            when(byService.xpath(anyList())).thenReturn(List.of(By.xpath("//div")));

            WebElement mockElement = mock(WebElement.class);
            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(any(By.class))).thenReturn(mockElement);
            stubWebSettings(5);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            webElementFinder.find(new LocatorData(null, locator), deps);
            verify(pageLoadCheck).waitUntilDomReady(deps);
        }
    }

    // ================================================================
    // TryToFindElementIfNotFoundBeforeAfterAutoWait - timeout
    // ================================================================

    @Nested
    class TryToFindAfterAutoWait {

        @Test
        void throwsWhenElementNotFoundAfterAutowait() {
            webElementFinder.init();

            Locator locator = new Locator();
            locator.setLocatorId("testLocator");
            Xpath xpath = new Xpath();
            xpath.setValue("//nonexistent");
            locator.getXpathOrIdOrClassName().add(xpath);

            By expectedBy = By.xpath("//nonexistent");
            when(byService.xpath(anyList())).thenReturn(List.of(expectedBy));

            JsWebDriver mockDriver = createJsDriver();
            when(mockDriver.findElement(expectedBy)).thenThrow(new NoSuchElementException("not found"));
            stubWebSettings(1);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.WEB).build();

            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.find(new LocatorData(null, locator), deps));
            assertTrue(ex.getMessage().contains("testLocator"));
        }

        @Test
        void throwsWhenElementNotFoundNativeType() {
            webElementFinder.init();

            Locator locator = new Locator();
            locator.setLocatorId("nativeLocator");
            Xpath xpath = new Xpath();
            xpath.setValue("//missing");
            locator.getXpathOrIdOrClassName().add(xpath);

            By expectedBy = By.xpath("//missing");
            when(byService.xpath(anyList())).thenReturn(List.of(expectedBy));

            WebDriver mockDriver = mock(WebDriver.class);
            when(mockDriver.findElement(expectedBy)).thenThrow(new NoSuchElementException("not found"));
            stubNativeSettings(1);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.NATIVE).build();

            assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.find(new LocatorData(null, locator), deps));
        }
    }

    // ================================================================
    // GetAutowaitSeconds
    // ================================================================

    @Nested
    class GetAutowaitSeconds {

        @Test
        void throwsWhenWebSettingsNotConfigured() {
            webElementFinder.init();

            Locator locator = new Locator();
            locator.setLocatorId("missingSettings");
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);

            when(byService.xpath(anyList())).thenReturn(List.of(By.xpath("//div")));

            WebDriver mockDriver = mock(WebDriver.class);
            when(mockDriver.findElement(any(By.class))).thenThrow(new NoSuchElementException("not found"));
            stubNativeSettings(1);

            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .driver(mockDriver).uiType(UiType.NATIVE).build();

            assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.find(new LocatorData(null, locator), deps));
        }
    }
}
