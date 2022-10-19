package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;
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

    public static String getPageScrollScript(final Scroll scroll) {
        String value = scroll.getValue().toString();
        boolean isUpDirection = ScrollDirection.UP.equals(scroll.getDirection());
        if (ScrollMeasure.PERCENT.equals(scroll.getMeasure())) {
            float percent = UiUtil.calculatePercentageValue(value);
            return format(VERTICAL_BY_PERCENT.getScript(), isUpDirection ? DelimiterConstant.DASH + percent : percent);
        }
        return format(VERTICAL_BY_PIXEL.getScript(), isUpDirection ? DelimiterConstant.DASH + value : value);
    }
}
