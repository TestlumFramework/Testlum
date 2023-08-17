package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.Locator;
import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.isNull;

@UtilityClass
public class GlobalLocators {

    private static final Map<String, Locator> LOCATOR_MAP;
    private static final String INCORRECT_NAMING_FOR_LOCATOR_ID =
            "Incorrect naming for %s. Expect format: <pageName.locatorId>";
    private static final String UNABLE_TO_FIND_LOCATOR_BY_PATH = "Unable to find locator [%s] by path";

    static {
        LOCATOR_MAP = Collections.unmodifiableMap(new LocatorCollector().collect());
    }

    public Locator getLocator(final String name) {
        Locator locator = LOCATOR_MAP.get(name);
        if (isNull(locator)) {
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
