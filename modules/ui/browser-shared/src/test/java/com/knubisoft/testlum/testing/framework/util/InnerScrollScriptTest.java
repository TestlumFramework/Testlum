package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.*;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InnerScrollScriptTest {

    @Mock
    private WebElementFinder webElementFinder;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private Locator locator;

    private InnerScrollScript innerScrollScript;

    @BeforeEach
    void setUp() {
        innerScrollScript = new InnerScrollScript(webElementFinder);
    }

    @Nested
    class ByCssSelector {

        @Test
        void scrollDownByPixel() {
            CssSelector css = new CssSelector();
            css.setValue(".container");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(List.of(css));
            final Scroll scroll = createScroll(300, ScrollDirection.DOWN, ScrollMeasure.PIXEL);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(1, scripts.size());
            assertEquals("document.querySelector('.container').scrollBy(0, 300)", scripts.get(0));
        }

        @Test
        void scrollUpByPixel() {
            CssSelector css = new CssSelector();
            css.setValue(".panel");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(List.of(css));
            final Scroll scroll = createScroll(200, ScrollDirection.UP, ScrollMeasure.PIXEL);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(1, scripts.size());
            assertEquals("document.querySelector('.panel').scrollBy(0, -200)", scripts.get(0));
        }

        @Test
        void scrollDownByPercent() {
            CssSelector css = new CssSelector();
            css.setValue(".list");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(List.of(css));
            when(uiUtil.calculatePercentageValue(50)).thenReturn(0.5f);
            final Scroll scroll = createScroll(50, ScrollDirection.DOWN, ScrollMeasure.PERCENT);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(1, scripts.size());
            assertEquals("document.querySelector('.list').scrollBy(0, "
                    + "document.querySelector('.list').scrollHeight * 0.5)", scripts.get(0));
        }
    }

    @Nested
    class ById {

        @Test
        void scrollDownByPixel() {
            Id id = new Id();
            id.setValue("main-content");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(Id.class)))
                    .thenReturn(List.of(id));
            final Scroll scroll = createScroll(400, ScrollDirection.DOWN, ScrollMeasure.PIXEL);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(1, scripts.size());
            assertEquals("document.getElementById('main-content').scrollBy(0, 400)", scripts.get(0));
        }

        @Test
        void scrollUpByPercent() {
            Id id = new Id();
            id.setValue("sidebar");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(Id.class)))
                    .thenReturn(List.of(id));
            when(uiUtil.calculatePercentageValue(75)).thenReturn(0.75f);
            final Scroll scroll = createScroll(75, ScrollDirection.UP, ScrollMeasure.PERCENT);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(1, scripts.size());
            assertEquals("document.getElementById('sidebar').scrollBy(0, "
                    + "document.getElementById('sidebar').scrollHeight * -0.75)", scripts.get(0));
        }
    }

    @Nested
    class ByXpath {

        @Test
        void scrollDownByPixel() {
            Xpath xpath = new Xpath();
            xpath.setValue("//div[@id='content']");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(Id.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(ClassName.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(Xpath.class)))
                    .thenReturn(List.of(xpath));
            final Scroll scroll = createScroll(500, ScrollDirection.DOWN, ScrollMeasure.PIXEL);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(1, scripts.size());
            assertTrue(scripts.get(0).contains("document.evaluate"));
            assertTrue(scripts.get(0).contains("//div[@id='content']"));
            assertTrue(scripts.get(0).contains("scrollBy(0, 500)"));
        }
    }

    @Nested
    class MultipleSelectors {

        @Test
        void returnsScriptForEachSelector() {
            CssSelector css1 = new CssSelector();
            css1.setValue(".item-1");
            CssSelector css2 = new CssSelector();
            css2.setValue(".item-2");
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(List.of(css1, css2));
            final Scroll scroll = createScroll(100, ScrollDirection.DOWN, ScrollMeasure.PIXEL);

            List<String> scripts = innerScrollScript.getInnerScrollScript(scroll, uiUtil);

            assertEquals(2, scripts.size());
            assertEquals("document.querySelector('.item-1').scrollBy(0, 100)", scripts.get(0));
            assertEquals("document.querySelector('.item-2').scrollBy(0, 100)", scripts.get(1));
        }
    }

    @Nested
    class InvalidLocator {

        @Test
        void throwsExceptionWhenNoLocatorTypeMatches() {
            when(uiUtil.getLocatorByStrategy(any(), any())).thenReturn(locator);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(CssSelector.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(Id.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(ClassName.class)))
                    .thenReturn(null);
            when(webElementFinder.getLocatorsByType(eq(locator), eq(Xpath.class)))
                    .thenReturn(null);
            final Scroll scroll = createScroll(100, ScrollDirection.DOWN, ScrollMeasure.PIXEL);

            assertThrows(DefaultFrameworkException.class,
                    () -> innerScrollScript.getInnerScrollScript(scroll, uiUtil));
        }
    }

    @Nested
    class GetterAccess {

        @Test
        void allScrollItemGettersReturnNonNull() {
            assertNotNull(innerScrollScript.getVerticalByCssSelector());
            assertNotNull(innerScrollScript.getVerticalById());
            assertNotNull(innerScrollScript.getVerticalByClass());
            assertNotNull(innerScrollScript.getVerticalByXpath());
        }
    }

    private Scroll createScroll(final int value, final ScrollDirection direction, final ScrollMeasure measure) {
        Scroll scroll = new Scroll();
        scroll.setValue(value);
        scroll.setDirection(direction);
        scroll.setMeasure(measure);
        return scroll;
    }
}
