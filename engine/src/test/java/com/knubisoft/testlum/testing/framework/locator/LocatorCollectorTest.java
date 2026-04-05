package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.framework.xml.XMLParser;
import com.knubisoft.testlum.testing.model.pages.Component;
import com.knubisoft.testlum.testing.model.pages.Include;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Locators;
import com.knubisoft.testlum.testing.model.pages.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LocatorCollector} verifying locator retrieval,
 * error handling for missing or incorrectly named locators, and
 * internal collection logic.
 */
class LocatorCollectorTest {

    private LocatorCollector collector;

    @BeforeEach
    void setUp() {
        collector = createCollectorWithLocators(buildTestLocatorMap());
    }

    private Map<String, LocatorData> buildTestLocatorMap() {
        final Map<String, LocatorData> map = new LinkedHashMap<>();
        map.put("login.username", createLocatorData("username"));
        map.put("login.password", createLocatorData("password"));
        map.put("home.title", createLocatorData("title"));
        map.put("dashboard.widget", createLocatorData("widget"));
        return map;
    }

    private LocatorData createLocatorData(final String id) {
        final Locator locator = new Locator();
        locator.setLocatorId(id);
        return new LocatorData(null, locator);
    }

    @SuppressWarnings("unchecked")
    private LocatorCollector createCollectorWithLocators(final Map<String, LocatorData> locators) {
        final XMLParsers xmlParsers = mock(XMLParsers.class);
        final PageValidator pageValidator = mock(PageValidator.class);
        final TestResourceSettings testResourceSettings = mock(TestResourceSettings.class);
        final FileSearcher fileSearcher = mock(FileSearcher.class);

        when(testResourceSettings.getPagesFolder()).thenReturn(new File("/pages"));
        when(testResourceSettings.getComponentsFolder()).thenReturn(new File("/components"));

        final Map<String, File> pageFileMap = new LinkedHashMap<>();
        final Map<File, Page> fileToPage = new LinkedHashMap<>();

        for (Map.Entry<String, LocatorData> entry : locators.entrySet()) {
            String pageName = entry.getKey().split("\\.")[0];
            File pageFile = new File(pageName + ".xml");
            pageFileMap.putIfAbsent(pageName, pageFile);
            fileToPage.computeIfAbsent(pageFile, f -> {
                Page page = new Page();
                page.setLocators(new Locators());
                return page;
            });
            fileToPage.get(pageFile).getLocators().getLocator().add(entry.getValue().getLocator());
        }

        when(fileSearcher.collectFilesFromFolder(new File("/pages"))).thenReturn(pageFileMap);
        when(fileSearcher.collectFilesFromFolder(new File("/components"))).thenReturn(new LinkedHashMap<>());

        final XMLParser<Page> pageParser = mock(XMLParser.class);
        when(xmlParsers.forPageLocator()).thenReturn(pageParser);
        for (Map.Entry<File, Page> entry : fileToPage.entrySet()) {
            when(pageParser.process(entry.getKey())).thenReturn(entry.getValue());
        }

        return new LocatorCollector(xmlParsers, pageValidator, testResourceSettings, fileSearcher);
    }

    @Nested
    class GetLocator {
        @Test
        void returnsLocatorByFullName() {
            final LocatorData result = collector.getLocator("login.username");
            assertNotNull(result);
            assertEquals("username", result.getLocator().getLocatorId());
        }

        @Test
        void returnsLocatorFromDifferentPage() {
            final LocatorData result = collector.getLocator("home.title");
            assertNotNull(result);
            assertEquals("title", result.getLocator().getLocatorId());
        }

        @Test
        void returnsPasswordLocator() {
            final LocatorData result = collector.getLocator("login.password");
            assertNotNull(result);
            assertEquals("password", result.getLocator().getLocatorId());
        }

        @Test
        void returnsDashboardWidgetLocator() {
            final LocatorData result = collector.getLocator("dashboard.widget");
            assertNotNull(result);
            assertEquals("widget", result.getLocator().getLocatorId());
        }

        @Test
        void throwsForMissingLocator() {
            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("login.nonexistent"));
            assertTrue(ex.getMessage().contains("login.nonexistent"));
        }

        @Test
        void throwsForIncorrectNamingFormat() {
            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("noDotInName"));
            assertNotNull(ex.getMessage());
        }

        @Test
        void throwsForTooManyDots() {
            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("a.b.c"));
            assertNotNull(ex.getMessage());
        }

        @Test
        void throwsForEmptyName() {
            assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator(""));
        }

        @Test
        void throwsForSingleDotName() {
            assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("."));
        }

        @Test
        void throwsForNonexistentPage() {
            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("nonexistent.field"));
            assertTrue(ex.getMessage().contains("nonexistent.field"));
        }
    }

    @Nested
    class DefaultFrameworkExceptionMessage {

        @Test
        void incorrectNamingMessageForSingleSegment() throws Exception {
            Method method = LocatorCollector.class.getDeclaredMethod("defaultFrameworkException", String.class);
            method.setAccessible(true);
            DefaultFrameworkException ex = (DefaultFrameworkException) method.invoke(collector, "noDotsHere");
            assertNotNull(ex);
            // Single segment -> incorrect naming message
            assertTrue(ex.getMessage().contains("noDotsHere"));
        }

        @Test
        void unableToFindMessageForTwoSegments() throws Exception {
            Method method = LocatorCollector.class.getDeclaredMethod("defaultFrameworkException", String.class);
            method.setAccessible(true);
            DefaultFrameworkException ex = (DefaultFrameworkException) method.invoke(collector, "page.missing");
            assertNotNull(ex);
            assertTrue(ex.getMessage().contains("page.missing"));
        }

        @Test
        void incorrectNamingMessageForThreeSegments() throws Exception {
            Method method = LocatorCollector.class.getDeclaredMethod("defaultFrameworkException", String.class);
            method.setAccessible(true);
            DefaultFrameworkException ex = (DefaultFrameworkException) method.invoke(collector, "a.b.c");
            assertNotNull(ex);
        }
    }

    @Nested
    class ConstructorAndCollection {

        @SuppressWarnings("unchecked")
        @Test
        void constructorCollectsLocatorsFromPages() {
            final XMLParsers xmlParsers = mock(XMLParsers.class);
            final PageValidator pageValidator = mock(PageValidator.class);
            final TestResourceSettings testResourceSettings = mock(TestResourceSettings.class);
            final FileSearcher fileSearcher = mock(FileSearcher.class);

            when(testResourceSettings.getPagesFolder()).thenReturn(new File("/pages"));
            when(testResourceSettings.getComponentsFolder()).thenReturn(new File("/components"));

            Page page = new Page();
            Locators locators = new Locators();
            Locator loc = new Locator();
            loc.setLocatorId("field1");
            locators.getLocator().add(loc);
            page.setLocators(locators);

            Map<String, File> pageFiles = new LinkedHashMap<>();
            pageFiles.put("myPage", new File("myPage.xml"));
            Map<String, File> componentFiles = new LinkedHashMap<>();

            when(fileSearcher.collectFilesFromFolder(new File("/pages"))).thenReturn(pageFiles);
            when(fileSearcher.collectFilesFromFolder(new File("/components"))).thenReturn(componentFiles);

            XMLParser<Page> pageParser = mock(XMLParser.class);
            when(xmlParsers.forPageLocator()).thenReturn(pageParser);
            when(pageParser.process(any(File.class))).thenReturn(page);

            LocatorCollector lc = new LocatorCollector(xmlParsers, pageValidator, testResourceSettings, fileSearcher);

            LocatorData result = lc.getLocator("myPage.field1");
            assertNotNull(result);
            assertEquals("field1", result.getLocator().getLocatorId());
        }

        @SuppressWarnings("unchecked")
        @Test
        void constructorThrowsWhenPageParsingFails() {
            final XMLParsers xmlParsers = mock(XMLParsers.class);
            final PageValidator pageValidator = mock(PageValidator.class);
            final TestResourceSettings testResourceSettings = mock(TestResourceSettings.class);
            final FileSearcher fileSearcher = mock(FileSearcher.class);

            when(testResourceSettings.getPagesFolder()).thenReturn(new File("/pages"));
            when(testResourceSettings.getComponentsFolder()).thenReturn(new File("/components"));

            Map<String, File> pageFiles = new LinkedHashMap<>();
            pageFiles.put("broken", new File("broken.xml"));
            Map<String, File> componentFiles = new LinkedHashMap<>();

            when(fileSearcher.collectFilesFromFolder(new File("/pages"))).thenReturn(pageFiles);
            when(fileSearcher.collectFilesFromFolder(new File("/components"))).thenReturn(componentFiles);

            XMLParser<Page> pageParser = mock(XMLParser.class);
            when(xmlParsers.forPageLocator()).thenReturn(pageParser);
            when(pageParser.process(any(File.class))).thenThrow(new RuntimeException("parse error"));

            assertThrows(DefaultFrameworkException.class,
                    () -> new LocatorCollector(xmlParsers, pageValidator, testResourceSettings, fileSearcher));
        }

        @SuppressWarnings("unchecked")
        @Test
        void constructorIncludesComponentLocators() {
            final XMLParsers xmlParsers = mock(XMLParsers.class);
            final PageValidator pageValidator = mock(PageValidator.class);
            final TestResourceSettings testResourceSettings = mock(TestResourceSettings.class);
            final FileSearcher fileSearcher = mock(FileSearcher.class);

            when(testResourceSettings.getPagesFolder()).thenReturn(new File("/pages"));
            when(testResourceSettings.getComponentsFolder()).thenReturn(new File("/components"));

            // Page with include
            Page page = new Page();
            Locators locators = new Locators();
            Locator pageLoc = new Locator();
            pageLoc.setLocatorId("pageField");
            locators.getLocator().add(pageLoc);
            page.setLocators(locators);
            Include include = new Include();
            include.setComponent("myComponent");
            page.getInclude().add(include);

            // Component
            Component component = new Component();
            Locators compLocators = new Locators();
            Locator compLoc = new Locator();
            compLoc.setLocatorId("compField");
            compLocators.getLocator().add(compLoc);
            component.setLocators(compLocators);

            Map<String, File> pageFiles = new LinkedHashMap<>();
            pageFiles.put("testPage", new File("testPage.xml"));
            Map<String, File> componentFiles = new LinkedHashMap<>();
            componentFiles.put("myComponent", new File("myComponent.xml"));

            when(fileSearcher.collectFilesFromFolder(new File("/pages"))).thenReturn(pageFiles);
            when(fileSearcher.collectFilesFromFolder(new File("/components"))).thenReturn(componentFiles);

            XMLParser<Page> pageParser = mock(XMLParser.class);
            XMLParser<Component> compParser = mock(XMLParser.class);
            when(xmlParsers.forPageLocator()).thenReturn(pageParser);
            when(xmlParsers.forComponentLocator()).thenReturn(compParser);
            when(pageParser.process(any(File.class))).thenReturn(page);
            when(compParser.process(any(File.class))).thenReturn(component);

            LocatorCollector lc = new LocatorCollector(xmlParsers, pageValidator, testResourceSettings, fileSearcher);

            assertNotNull(lc.getLocator("testPage.pageField"));
            assertNotNull(lc.getLocator("testPage.compField"));
        }
    }
}
