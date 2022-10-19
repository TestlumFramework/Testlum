package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;

import static java.lang.String.format;

public enum PageScrollScript {

    PAGE_VERTICAL_SCROLL_PIXEL_SCRIPT("window.scrollBy(0, %s)"),
    PAGE_VERTICAL_SCROLL_PERCENT_SCRIPT("window.scrollBy(0, document.body.scrollHeight * %s)");

    private final String script;

    PageScrollScript(final String script) {
        this.script = script;
    }

    public static String getPageScrollScript(final Scroll scroll) {
        ScrollMeasure measure = scroll.getMeasure();
        String value = scroll.getValue().toString();
        if (ScrollMeasure.PERCENT.equals(measure)) {
            return getPageScrollByPercentScript(scroll, value);
        }
        return getPageScrollByPixelScript(scroll, value);
    }

    private static String getPageScrollByPixelScript(final Scroll scroll, final String value) {
        return format(PageScrollScript.PAGE_VERTICAL_SCROLL_PIXEL_SCRIPT.script,
                ScrollDirection.UP.equals(scroll.getDirection()) ? DelimiterConstant.DASH + value : value);
    }

    private static String getPageScrollByPercentScript(final Scroll scroll, final String value) {
        float percent = UiUtil.calculatePercentageValue(value);
        return format(PageScrollScript.PAGE_VERTICAL_SCROLL_PERCENT_SCRIPT.script,
                ScrollDirection.UP.equals(scroll.getDirection()) ? DelimiterConstant.DASH + percent : percent);
    }
}
