package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import lombok.Getter;

import static java.lang.String.format;

@Getter
public enum PageScrollScript {

    VERTICAL_BY_PIXEL("window.scrollBy(0, %s)"),
    VERTICAL_BY_PERCENT("window.scrollBy(0, document.body.scrollHeight * %s)");

    private final String script;

    PageScrollScript(final String script) {
        this.script = script;
    }

    public static String getPageScrollScript(final Scroll scroll, final UiUtil uiUtil) {
        int value = scroll.getValue();
        boolean isUpDirection = ScrollDirection.UP == scroll.getDirection();
        if (ScrollMeasure.PERCENT == scroll.getMeasure()) {
            float percent = uiUtil.calculatePercentageValue(value);
            return format(VERTICAL_BY_PERCENT.getScript(), isUpDirection ? DelimiterConstant.DASH + percent : percent);
        }
        return format(VERTICAL_BY_PIXEL.getScript(), isUpDirection ? DelimiterConstant.DASH + value : value);
    }
}
