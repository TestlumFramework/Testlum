package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static java.lang.String.format;

public enum InnerScrollScript {

    INNER_VERTICAL_BY_CSS_SELECTOR(locator -> Objects.nonNull(locator.getCssSelector()),
            Locator::getCssSelector,
            "document.querySelector('%s').scrollBy(0, %s)",
            "document.querySelector('%s').scrollHeight * %s"),
    INNER_VERTICAL_BY_ID(locator -> Objects.nonNull(locator.getId()),
            Locator::getId,
            "document.getElementById('%s').scrollBy(0, %s)",
            "document.getElementById('%s').scrollHeight * %s"),
    INNER_VERTICAL_BY_CLASS(locator -> Objects.nonNull(locator.getClazz()),
            Locator::getClazz,
            "document.getElementsByClassName('%s').scrollBy(0, %s)",
            "document.getElementsByClassName('%s').scrollHeight * %s"),
    INNER_VERTICAL_BY_XPATH(locator -> Objects.nonNull(locator.getXpath()),
            Locator::getXpath,
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, %s)",
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollHeight * %s");

    private static final int MAX_PERCENTS_VALUE = 100;

    private final Predicate<Locator> locatorTypePredicate;
    private final Function<Locator, String> locatorValue;
    private final String scrollByPixelScript;
    private final String scrollByPercentageScript;


    InnerScrollScript(final Predicate<Locator> locatorTypePredicate,
                      final Function<Locator, String> locatorValue,
                      final String scrollByPixelScript,
                      final String scrollByPercentageScript) {
        this.locatorTypePredicate = locatorTypePredicate;
        this.locatorValue = locatorValue;
        this.scrollByPixelScript = scrollByPixelScript;
        this.scrollByPercentageScript = scrollByPercentageScript;
    }

    public static String getScrollScript(final Scroll scroll) {
        Locator locator = GlobalLocators.getLocator(scroll.getLocator());
        String value = scroll.getValue().toString();
        ScrollMeasure measure = scroll.getMeasure();
        InnerScrollScript innerScrollScript = getInnerScrollScript(locator);
        String selector = innerScrollScript.locatorValue.apply(locator);
        if (ScrollDirection.UP.equals(scroll.getDirection())) {
            return format(innerScrollScript.scrollByPixelScript, selector,
                    innerScrollMeasureFormater(measure, DelimiterConstant.DASH + value, selector, locator));
        }
        return format(innerScrollScript.scrollByPixelScript, selector,
                innerScrollMeasureFormater(measure, value, selector, locator));
    }

    private static String innerScrollMeasureFormater(final ScrollMeasure measure,
                                                     final String value,
                                                     final String selector,
                                                     final Locator locator) {
        if (ScrollMeasure.PERCENT.equals(measure)) {
            float percent = Float.parseFloat(value) / MAX_PERCENTS_VALUE;
            if (percent > 1) {
                throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, value));
            }
            return format(getInnerScrollScript(locator).scrollByPercentageScript, selector, percent);
        }
        return value;
    }

    private static InnerScrollScript getInnerScrollScript(final Locator locator) {
        return Arrays.stream(InnerScrollScript.values())
                .filter(l -> l.locatorTypePredicate.test(locator))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(format(ExceptionMessage.NO_SUCH_LOCATOR, locator)));
    }

}
