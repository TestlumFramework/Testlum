package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PageScrollScriptTest {

    private UiUtil uiUtil;

    @BeforeEach
    void setUp() {
        uiUtil = mock(UiUtil.class);
    }

    @Nested
    class VerticalByPixel {
        @Test
        void scrollDownByPixel() {
            final Scroll scroll = new Scroll();
            scroll.setValue(300);
            scroll.setDirection(ScrollDirection.DOWN);
            scroll.setMeasure(ScrollMeasure.PIXEL);

            final String script = PageScrollScript.getPageScrollScript(scroll, uiUtil);
            assertEquals("window.scrollBy(0, 300)", script);
        }

        @Test
        void scrollUpByPixel() {
            final Scroll scroll = new Scroll();
            scroll.setValue(200);
            scroll.setDirection(ScrollDirection.UP);
            scroll.setMeasure(ScrollMeasure.PIXEL);

            final String script = PageScrollScript.getPageScrollScript(scroll, uiUtil);
            assertEquals("window.scrollBy(0, -200)", script);
        }
    }

    @Nested
    class VerticalByPercent {
        @Test
        void scrollDownByPercent() {
            final Scroll scroll = new Scroll();
            scroll.setValue(50);
            scroll.setDirection(ScrollDirection.DOWN);
            scroll.setMeasure(ScrollMeasure.PERCENT);
            when(uiUtil.calculatePercentageValue(50)).thenReturn(0.5f);

            final String script = PageScrollScript.getPageScrollScript(scroll, uiUtil);
            assertEquals("window.scrollBy(0, document.body.scrollHeight * 0.5)", script);
        }

        @Test
        void scrollUpByPercent() {
            final Scroll scroll = new Scroll();
            scroll.setValue(25);
            scroll.setDirection(ScrollDirection.UP);
            scroll.setMeasure(ScrollMeasure.PERCENT);
            when(uiUtil.calculatePercentageValue(25)).thenReturn(0.25f);

            final String script = PageScrollScript.getPageScrollScript(scroll, uiUtil);
            assertEquals("window.scrollBy(0, document.body.scrollHeight * -0.25)", script);
        }
    }

    @Nested
    class DefaultValues {
        @Test
        void defaultDirectionIsDown() {
            final Scroll scroll = new Scroll();
            scroll.setValue(100);
            scroll.setMeasure(ScrollMeasure.PIXEL);

            final String script = PageScrollScript.getPageScrollScript(scroll, uiUtil);
            assertEquals("window.scrollBy(0, 100)", script);
        }

        @Test
        void defaultMeasureIsPixel() {
            final Scroll scroll = new Scroll();
            scroll.setValue(150);
            scroll.setDirection(ScrollDirection.DOWN);

            final String script = PageScrollScript.getPageScrollScript(scroll, uiUtil);
            assertEquals("window.scrollBy(0, 150)", script);
        }
    }

    @Nested
    class EnumValues {
        @Test
        void verticalByPixelScript() {
            assertTrue(PageScrollScript.VERTICAL_BY_PIXEL.getScript().contains("window.scrollBy"));
        }

        @Test
        void verticalByPercentScript() {
            assertTrue(PageScrollScript.VERTICAL_BY_PERCENT.getScript().contains("scrollHeight"));
        }
    }
}
