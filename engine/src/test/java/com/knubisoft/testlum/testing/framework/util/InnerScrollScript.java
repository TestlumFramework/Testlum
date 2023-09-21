package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.locator.GlobalLocators;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
public enum InnerScrollScript {

    VERTICAL_BY_CSS_SELECTOR(locator -> isNotBlank(locator.getCssSelector()),
            Locator::getCssSelector,
            "document.querySelector('%s').scrollBy(0, %s)",
            "document.querySelector('%s').scrollBy(0, document.querySelector('%s')"
                    + ".scrollHeight * %s)"),
    VERTICAL_BY_ID(locator -> isNotBlank(locator.getId()),
            Locator::getId,
            "document.getElementById('%s').scrollBy(0, %s)",
            "document.getElementById('%s').scrollBy(0, document.getElementById('%s')"
                    + ".scrollHeight * %s)"),
    VERTICAL_BY_CLASS(locator -> isNotBlank(locator.getClazz()),
            Locator::getClazz,
            "document.getElementsByClassName('%s').scrollBy(0, %s)",
            "document.getElementsByClassName('%s').scrollBy(0, "
                    + "document.getElementsByClassName('%s').scrollHeight * %s)"),
    VERTICAL_BY_XPATH(locator -> isNotBlank(locator.getXpath()),
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
        Locator locator = GlobalLocators.getLocator(scroll.getLocatorId());
        return Arrays.stream(InnerScrollScript.values())
                .filter(e -> e.getLocatorTypePredicate().test(locator))
                .findFirst()
                .map(e -> {
                    String selector = e.locatorValue.apply(locator);
                    int value = scroll.getValue();
                    ScrollDirection scrollDirection = scroll.getDirection();
                    return ScrollMeasure.PERCENT == scroll.getMeasure()
                            ? formatInnerPercentScript(e.getPercentageScript(), selector, value, scrollDirection)
                            : formatInnerPixelScript(e.getPixelScript(), selector, value, scrollDirection);
                })
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.INVALID_LOCATOR, locator));
    }

    private static String formatInnerPixelScript(final String script,
                                                 final String selector,
                                                 final int value,
                                                 final ScrollDirection scrollDirection) {
        return format(script,
                selector,
                ScrollDirection.UP == scrollDirection ? DelimiterConstant.DASH + value : value);
    }

    private static String formatInnerPercentScript(final String script,
                                                   final String selector,
                                                   final int value,
                                                   final ScrollDirection scrollDirection) {
        float percent = UiUtil.calculatePercentageValue(value);
        return format(script,
                selector,
                selector,
                ScrollDirection.UP == scrollDirection ? DelimiterConstant.DASH + percent : percent);
    }
}
