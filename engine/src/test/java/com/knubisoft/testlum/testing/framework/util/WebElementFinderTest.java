package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.EnvironmentLoader;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebElementFinderTest {

    @Mock
    private EnvironmentLoader environmentLoader;

    @Mock
    private ByService byService;

    @InjectMocks
    private WebElementFinder webElementFinder;

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
    }

    @Nested
    class GetLocatorsByType {
        @Test
        void returnsCorrectTypeList() {
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
        void throwsWhenTypeNotFound() {
            Locator locator = new Locator();
            Xpath xpath = new Xpath();
            xpath.setValue("//div");
            locator.getXpathOrIdOrClassName().add(xpath);
            assertThrows(DefaultFrameworkException.class,
                    () -> webElementFinder.getLocatorsByType(locator, CssSelector.class));
        }
    }

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
    }
}
