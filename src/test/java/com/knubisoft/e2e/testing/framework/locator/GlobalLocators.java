package com.knubisoft.e2e.testing.framework.locator;

import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.pages.Locator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.INCORRECT_NAMING_FOR_LOCATOR_ID;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.UNABLE_TO_FIND_LOCATOR_BY_PATH;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalLocators {

    private static final GlobalLocators GLOBAL_LOCATORS =
            new GlobalLocators();
    private final Map<String, Locator> locatorMap =
            new LocatorCollector().collect();

    public static GlobalLocators getInstance() {
        return GLOBAL_LOCATORS;
    }

    public Locator getLocator(final String name) {
        Locator locator = locatorMap.get(name);
        if (locator == null) {
            throw defaultFrameworkException(name);
        }
        return locator;
    }

    private DefaultFrameworkException defaultFrameworkException(final String name) {
        if (name.split(DelimiterConstant.DOT_REGEX).length != 2) {
            return new DefaultFrameworkException(INCORRECT_NAMING_FOR_LOCATOR_ID, name);
        }
        return new DefaultFrameworkException(UNABLE_TO_FIND_LOCATOR_BY_PATH, name);
    }
}
