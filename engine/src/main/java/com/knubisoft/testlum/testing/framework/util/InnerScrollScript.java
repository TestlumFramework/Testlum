package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Getter
public enum InnerScrollScript {

    VERTICAL_BY_CSS_SELECTOR(locator -> nonNull(WebElementFinder.getLocatorsByType(locator, CssSelector.class)),
            locator -> WebElementFinder.getLocatorsByType(locator, CssSelector.class)
                    .stream().map(CssSelector::getValue).collect(Collectors.toList()),
            "document.querySelector('%s').scrollBy(0, %s)",
            "document.querySelector('%s').scrollBy(0, document.querySelector('%s')"
                    + ".scrollHeight * %s)"),
    VERTICAL_BY_ID(locator -> nonNull(WebElementFinder.getLocatorsByType(locator, Id.class)),
            locator -> WebElementFinder.getLocatorsByType(locator, Id.class)
                    .stream().map(Id::getValue).collect(Collectors.toList()),
            "document.getElementById('%s').scrollBy(0, %s)",
            "document.getElementById('%s').scrollBy(0, document.getElementById('%s')"
                    + ".scrollHeight * %s)"),
    VERTICAL_BY_CLASS(locator -> nonNull(WebElementFinder.getLocatorsByType(locator, ClassName.class)),
            locator -> WebElementFinder.getLocatorsByType(locator, ClassName.class)
                    .stream().map(ClassName::getValue).collect(Collectors.toList()),
            "document.getElementsByClassName('%s').scrollBy(0, %s)",
            "document.getElementsByClassName('%s').scrollBy(0, "
                    + "document.getElementsByClassName('%s').scrollHeight * %s)"),
    VERTICAL_BY_XPATH(locator -> nonNull(WebElementFinder.getLocatorsByType(locator, Xpath.class)),
            locator -> WebElementFinder.getLocatorsByType(locator, Xpath.class)
                    .stream().map(Xpath::getValue).collect(Collectors.toList()),
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, %s)",
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, document.evaluate('%s', document, null, "
                    + "XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.scrollHeight * %s)");

    private final Predicate<Locator> locatorTypePredicate;
    private final Function<Locator, List<String>> locatorValue;
    private final String pixelScript;
    private final String percentageScript;


    InnerScrollScript(final Predicate<Locator> locatorTypePredicate,
                      final Function<Locator, List<String>> locatorValue,
                      final String pixelScript,
                      final String percentageScript) {
        this.locatorTypePredicate = locatorTypePredicate;
        this.locatorValue = locatorValue;
        this.pixelScript = pixelScript;
        this.percentageScript = percentageScript;
    }

    public static List<String> getInnerScrollScript(final Scroll scroll) {
        Locator locator = UiUtil.getLocatorByStrategy(scroll.getLocator(), scroll.getLocatorStrategy());
        return Arrays.stream(InnerScrollScript.values())
                .filter(e -> e.getLocatorTypePredicate().test(locator))
                .findFirst()
                .map(e -> {
                    List<String> selectors = e.locatorValue.apply(locator);
                    return getFormattedScript(e, scroll, selectors);
                })
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.INVALID_LOCATOR, locator));
    }

    private static List<String> getFormattedScript(final InnerScrollScript e,
                                                   final Scroll scroll,
                                                   final List<String> selectorList) {
        int value = scroll.getValue();
        ScrollDirection scrollDirection = scroll.getDirection();
        return selectorList.stream()
                .map(selector -> ScrollMeasure.PERCENT == scroll.getMeasure()
                ? formatInnerPercentScript(e.getPercentageScript(), selector, value, scrollDirection)
                : formatInnerPixelScript(e.getPixelScript(), selector, value, scrollDirection))
                .collect(Collectors.toList());
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
