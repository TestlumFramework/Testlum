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


    private final Predicate<Locator> locatorPredicate;
    private final Function<Locator, String> selector;
    private final String byPixel;
    private final String byPercentage;

    InnerScrollScript(final Predicate<Locator> locatorPredicate,
                      final Function<Locator, String> selector,
                      final String byPixel,
                      final String byPercentage) {
        this.locatorPredicate = locatorPredicate;
        this.selector = selector;
        this.byPixel = byPixel;
        this.byPercentage = byPercentage;
    }

    private static final int MAX_PERCENTS_VALUE = 100;

    public static String getScrollScript(final Scroll scroll) {
        Locator locator = GlobalLocators.getLocator(scroll.getLocator());
        String value = scroll.getValue().toString();
        ScrollMeasure measure = scroll.getMeasure();
        InnerScrollScript innerScrollScript = getScrollScript(locator);
        String selector = innerScrollScript.selector.apply(locator);
        if (ScrollDirection.UP.equals(scroll.getDirection())) {
            return format(innerScrollScript.byPixel, selector,
                    innerScrollMeasureFormater(measure, DelimiterConstant.DASH + value, selector, locator));
        }
        return format(innerScrollScript.byPixel, selector,
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
            return format(getScrollScript(locator).byPercentage, selector, percent);
        }
        return value;
    }

    private static InnerScrollScript getScrollScript(Locator locator) {
        return Arrays.stream(InnerScrollScript.values())
                .filter(l -> l.locatorPredicate.test(locator))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(format(ExceptionMessage.NO_SUCH_LOCATOR), locator));
    }

}
