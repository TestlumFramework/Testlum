package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.model.pages.*;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollDirection;
import com.knubisoft.testlum.testing.model.scenario.ScrollMeasure;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;


@Getter
@Component
public class InnerScrollScript {

    private final InnerScrollScriptItem verticalByCssSelector;
    private final InnerScrollScriptItem verticalById;
    private final InnerScrollScriptItem verticalByClass;
    private final InnerScrollScriptItem verticalByXpath;

    public InnerScrollScript(final WebElementFinder webElementFinder) {
        this.verticalByCssSelector = new InnerScrollScriptItem(locator ->
                Objects.nonNull(webElementFinder.getLocatorsByType(locator, CssSelector.class)),
                locator -> webElementFinder.getLocatorsByType(locator, CssSelector.class)
                        .stream().map(CssSelector::getValue).toList(),
                "document.querySelector('%s').scrollBy(0, %s)",
                "document.querySelector('%s').scrollBy(0, document.querySelector('%s')"
                        + ".scrollHeight * %s)");
        this.verticalById = new InnerScrollScriptItem(locator ->
                Objects.nonNull(webElementFinder.getLocatorsByType(locator, Id.class)),
                locator -> webElementFinder.getLocatorsByType(locator, Id.class)
                        .stream().map(Id::getValue).toList(),
                "document.getElementById('%s').scrollBy(0, %s)",
                "document.getElementById('%s').scrollBy(0, document.getElementById('%s')"
                        + ".scrollHeight * %s)");
        this.verticalByClass = new InnerScrollScriptItem(locator ->
                Objects.nonNull(webElementFinder.getLocatorsByType(locator, ClassName.class)),
                locator -> webElementFinder.getLocatorsByType(locator, ClassName.class)
                        .stream().map(ClassName::getValue).toList(),
                "document.getElementsByClassName('%s').scrollBy(0, %s)",
                "document.getElementsByClassName('%s').scrollBy(0, "
                        + "document.getElementsByClassName('%s').scrollHeight * %s)");
        this.verticalByXpath = new InnerScrollScriptItem(locator ->
                Objects.nonNull(webElementFinder.getLocatorsByType(locator, Xpath.class)),
                locator -> webElementFinder.getLocatorsByType(locator, Xpath.class)
                        .stream().map(Xpath::getValue).toList(),
                "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                        + ".singleNodeValue.scrollBy(0, %s)",
                "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                        + ".singleNodeValue.scrollBy(0, document.evaluate('%s', document, null, "
                        + "XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue.scrollHeight * %s)");
    }

    public List<String> getInnerScrollScript(final Scroll scroll, final UiUtil uiUtil) {
        LocatorData locatorData = uiUtil.getLocatorByStrategy(scroll.getLocator(), scroll.getLocatorStrategy());
        Locator locator = locatorData.getLocator();
        InnerScrollScriptItem[] items = new InnerScrollScriptItem[]{
                verticalByCssSelector, verticalById, verticalByClass, verticalByXpath};
        return Arrays.stream(items)
                .filter(e -> e.getLocatorTypePredicate().test(locator))
                .findFirst()
                .map(e -> {
                    List<String> selectors = e.locatorValue.apply(locator);
                    return getFormattedScript(e, scroll, selectors, uiUtil);
                })
                .orElseThrow(() -> new DefaultFrameworkException(ExceptionMessage.INVALID_LOCATOR, locator));
    }

    private List<String> getFormattedScript(final InnerScrollScriptItem e,
                                            final Scroll scroll,
                                            final List<String> selectorList,
                                            final UiUtil uiUtil) {
        int value = scroll.getValue();
        ScrollDirection scrollDirection = scroll.getDirection();
        return selectorList.stream()
                .map(selector -> ScrollMeasure.PERCENT == scroll.getMeasure()
                        ? formatInnerPercentScript(e.getPercentageScript(), selector, value, scrollDirection, uiUtil)
                        : formatInnerPixelScript(e.getPixelScript(), selector, value, scrollDirection))
                .toList();
    }

    private String formatInnerPixelScript(final String script,
                                          final String selector,
                                          final int value,
                                          final ScrollDirection scrollDirection) {
        return String.format(script,
                selector,
                ScrollDirection.UP == scrollDirection ? DelimiterConstant.DASH + value : value);
    }

    private String formatInnerPercentScript(final String script,
                                            final String selector,
                                            final int value,
                                            final ScrollDirection scrollDirection,
                                            final UiUtil uiUtil) {
        float percent = uiUtil.calculatePercentageValue(value);
        return String.format(script,
                selector,
                selector,
                ScrollDirection.UP == scrollDirection ? DelimiterConstant.DASH + percent : percent);
    }

    @Getter
    @RequiredArgsConstructor
    private static class InnerScrollScriptItem {
        private final Predicate<Locator> locatorTypePredicate;
        private final Function<Locator, List<String>> locatorValue;
        private final String pixelScript;
        private final String percentageScript;
    }
}
