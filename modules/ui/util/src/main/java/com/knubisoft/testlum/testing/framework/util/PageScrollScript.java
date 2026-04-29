package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PageScrollScript {

    VERTICAL_BY_PIXEL("window.scrollBy(0, %s)"),
    VERTICAL_BY_PERCENT("window.scrollBy(0, document.body.scrollHeight * %s)");

    private final String script;

    public static String getPageScrollScript(final Scroll scroll, final UiUtil uiUtil) {
        int value = scroll.getValue();
        boolean isUpDirection = ScrollDirection.UP == scroll.getDirection();
        if (ScrollMeasure.PERCENT == scroll.getMeasure()) {
            float percent = uiUtil.calculatePercentageValue(value);
            Object o = isUpDirection ? DelimiterConstant.DASH + percent : percent;
            return String.format(VERTICAL_BY_PERCENT.getScript(), o);
        }
        Object o = isUpDirection ? DelimiterConstant.DASH + value : value;
        return String.format(VERTICAL_BY_PIXEL.getScript(), o);
    }
}
