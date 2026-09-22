package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.model.locator.*;
import com.testlum.testing.framework.locator.LocatorCollector;
import com.testlum.testing.framework.locator.PageDefinition;
import com.testlum.testing.model.pages.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;

@Lazy
@org.springframework.stereotype.Component
@RequiredArgsConstructor
public class LocatorSnapshotMapper {

    private final LocatorCollector locatorCollector;

    public List<PageLocators> pages() {
        return locatorCollector.getPageDefinitions().stream().map(this::map).toList();
    }

    public List<ComponentLocators> components() {
        return locatorCollector.getComponents().entrySet().stream().map(this::map).toList();
    }

    private PageLocators map(final PageDefinition definition) {
        Details details = definition.page().getDetails();
        return PageLocators.builder()
                .pageName(definition.name())
                .url(details == null ? null : details.getUrl())
                .description(details == null ? null : details.getDescription())
                .locators(definition.ownLocators().stream().map(this::map).toList())
                .includedComponents(definition.page().getInclude().stream().map(Include::getComponent).toList())
                .build();
    }

    private ComponentLocators map(final Map.Entry<String, Component> component) {
        return ComponentLocators.builder()
                .componentName(component.getKey())
                .locators(map(component.getValue().getLocators()))
                .build();
    }

    private List<LocatorEntry> map(final Locators locators) {
        return locators == null ? List.of() : locators.getLocator().stream().map(this::map).toList();
    }

    private LocatorEntry map(final Locator locator) {
        return LocatorEntry.builder()
                .locatorId(locator.getLocatorId())
                .comment(locator.getComment())
                .strategies(locator.getXpathOrIdOrClassName().stream().map(this::strategy).toList())
                .build();
    }

    private LocatorStrategyValue strategy(final Object strategy) {
        if (strategy instanceof Xpath xpath) {
            return strategyValue(LocatorStrategy.XPATH, xpath.getValue(), null);
        }
        if (strategy instanceof Id id) {
            return strategyValue(LocatorStrategy.ID, id.getValue(), null);
        }
        if (strategy instanceof ClassName className) {
            return strategyValue(LocatorStrategy.CLASS_NAME, className.getValue(), null);
        }
        return textualStrategy(strategy);
    }

    private LocatorStrategyValue textualStrategy(final Object strategy) {
        if (strategy instanceof CssSelector cssSelector) {
            return strategyValue(LocatorStrategy.CSS_SELECTOR, cssSelector.getValue(), null);
        }
        Text text = (Text) strategy;
        return strategyValue(LocatorStrategy.TEXT, text.getValue(), text.isPlaceholder());
    }

    private LocatorStrategyValue strategyValue(final LocatorStrategy type, final String value,
                                               final Boolean placeholder) {
        return LocatorStrategyValue.builder().type(type).value(value).placeholder(placeholder).build();
    }
}
