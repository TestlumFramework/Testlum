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

import static java.lang.String.format;

public enum InnerScrollScript {

    INNER_VERTICAL_BY_CSS_SELECTOR(locator -> Objects.nonNull(locator.getCssSelector()),
            Locator::getCssSelector,
            "document.querySelector('%s').scrollBy(0, %s)",
            "document.querySelector('%s').scrollBy(0, document.querySelector('%s')"
                    + ".scrollHeight * %s)"),
    INNER_VERTICAL_BY_ID(locator -> Objects.nonNull(locator.getId()),
            Locator::getId,
            "document.getElementById('%s').scrollBy(0, %s)",
            "document.getElementById('%s').scrollBy(0, document.getElementById('%s')"
                    + ".scrollHeight * %s)"),
    INNER_VERTICAL_BY_CLASS(locator -> Objects.nonNull(locator.getClazz()),
            Locator::getClazz,
            "document.getElementsByClassName('%s').scrollBy(0, %s)",
            "document.getElementsByClassName('%s').scrollBy(0, "
                    + "document.getElementsByClassName('%s').scrollHeight * %s)"),
    INNER_VERTICAL_BY_XPATH(locator -> Objects.nonNull(locator.getXpath()),
            Locator::getXpath,
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, %s)",
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, document.evaluate('%s', document, null, "
                    + "XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.scrollHeight * %s)");

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
        if (ScrollMeasure.PERCENT.equals(measure)) {
            return getInnerPercentScript(scroll, selector, value);
        }
        return getInnerPixelScript(scroll, selector, value);

    }

    private static String getInnerPixelScript(final Scroll scroll, final String selector, final String value) {
        Locator locator = GlobalLocators.getLocator(scroll.getLocator());
        return format(getInnerScrollScript(locator).scrollByPixelScript,
                selector,
                ScrollDirection.UP.equals(scroll.getDirection()) ? DelimiterConstant.DASH + value : value);
    }

    private static String getInnerPercentScript(final Scroll scroll, final String selector, final String value) {
        Locator locator = GlobalLocators.getLocator(scroll.getLocator());
        float percent = UiUtil.calculatePercentageValue(value);
        return format(getInnerScrollScript(locator).scrollByPercentageScript,
                selector,
                selector,
                ScrollDirection.UP.equals(scroll.getDirection()) ? DelimiterConstant.DASH + percent : percent);
    }

    private static InnerScrollScript getInnerScrollScript(final Locator locator) {
        return Arrays.stream(InnerScrollScript.values())
                .filter(l -> l.locatorTypePredicate.test(locator))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(format(ExceptionMessage.NO_SUCH_LOCATOR, locator)));
    }

}
