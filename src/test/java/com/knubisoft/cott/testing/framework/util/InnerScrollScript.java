package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;

@Getter
public enum InnerScrollScript {

    VERTICAL_BY_CSS_SELECTOR(locator -> Objects.nonNull(locator.getCssSelector()),
            Locator::getCssSelector,
            "document.querySelector('%s').scrollBy(0, %s)",
            "document.querySelector('%s').scrollBy(0, document.querySelector('%s')"
                    + ".scrollHeight * %s)"),
    VERTICAL_BY_ID(locator -> Objects.nonNull(locator.getId()),
            Locator::getId,
            "document.getElementById('%s').scrollBy(0, %s)",
            "document.getElementById('%s').scrollBy(0, document.getElementById('%s')"
                    + ".scrollHeight * %s)"),
    VERTICAL_BY_CLASS(locator -> Objects.nonNull(locator.getClazz()),
            Locator::getClazz,
            "document.getElementsByClassName('%s').scrollBy(0, %s)",
            "document.getElementsByClassName('%s').scrollBy(0, "
                    + "document.getElementsByClassName('%s').scrollHeight * %s)"),
    VERTICAL_BY_XPATH(locator -> Objects.nonNull(locator.getXpath()),
            Locator::getXpath,
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, %s)",
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, document.evaluate('%s', document, null, "
                    + "XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.scrollHeight * %s)");

    private final Predicate<Locator> locatorTypePredicate;
    private final Function<Locator, String> locatorValue;
    private final String pixelScript;
    private final String percentageScript;


    InnerScrollScript(final Predicate<Locator> locatorTypePredicate,
                      final Function<Locator, String> locatorValue,
                      final String pixelScript,
                      final String percentageScript) {
        this.locatorTypePredicate = locatorTypePredicate;
        this.locatorValue = locatorValue;
        this.pixelScript = pixelScript;
        this.percentageScript = percentageScript;
    }

    public static String getInnerScrollScript(final Scroll scroll) {
        Locator locator = GlobalLocators.getLocator(scroll.getLocator());
        return Arrays.stream(InnerScrollScript.values())
                .filter(e -> e.getLocatorTypePredicate().test(locator))
                .findFirst()
                .map(e -> {
                    String selector = e.locatorValue.apply(locator);
                    String value = scroll.getValue().toString();
                    ScrollDirection scrollDirection = scroll.getDirection();
                    return ScrollMeasure.PERCENT.equals(scroll.getMeasure())
                            ? formatInnerPercentScript(e.getPercentageScript(), selector, value, scrollDirection)
                            : formatInnerPixelScript(e.getPixelScript(), selector, value, scrollDirection);
                })
                .orElseThrow(() -> new DefaultFrameworkException(format(ExceptionMessage.NO_SUCH_LOCATOR, locator)));
    }

    private static String formatInnerPixelScript(final String script,
                                                 final String selector,
                                                 final String value,
                                                 final ScrollDirection scrollDirection) {
        return format(script,
                selector,
                ScrollDirection.UP.equals(scrollDirection) ? DelimiterConstant.DASH + value : value);
    }

    private static String formatInnerPercentScript(final String script,
                                                   final String selector,
                                                   final String value,
                                                   final ScrollDirection scrollDirection) {
        float percent = UiUtil.calculatePercentageValue(value);
        return format(script,
                selector,
                selector,
                ScrollDirection.UP.equals(scrollDirection) ? DelimiterConstant.DASH + percent : percent);
    }
}
