package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static java.lang.String.format;

public enum PageScrollScript {

    PAGE_VERTICAL_SCROLL_PIXEL_SCRIPT("window.scrollBy(0, %s)"),
    PAGE_VERTICAL_SCROLL_PERCENT_SCRIPT("document.body.scrollHeight * %s");

    private static final int MAX_PERCENTS_VALUE = 100;

    private final String script;

    PageScrollScript(final String script) {
        this.script = script;
    }

    public static String getPageScrollScript(final Scroll scroll) {
        ScrollDirection direction = scroll.getDirection();
        ScrollMeasure measure = scroll.getMeasure();
        String value = scroll.getValue().toString();
        if (direction.equals(ScrollDirection.UP)) {
            return format(PageScrollScript.PAGE_VERTICAL_SCROLL_PIXEL_SCRIPT.script,
                    pageScrollMeasureFormatter(measure, DelimiterConstant.DASH + value));
        }
        return format(PageScrollScript.PAGE_VERTICAL_SCROLL_PIXEL_SCRIPT.script,
                pageScrollMeasureFormatter(measure, value));
    }

    private static String pageScrollMeasureFormatter(final ScrollMeasure measure, final String value) {
        if (measure.equals(ScrollMeasure.PERCENT)) {
            float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
            if (percent > 1) {
                throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
            }
            return format(PageScrollScript.PAGE_VERTICAL_SCROLL_PERCENT_SCRIPT.script, percent);
        }
        return value;
    }
}
