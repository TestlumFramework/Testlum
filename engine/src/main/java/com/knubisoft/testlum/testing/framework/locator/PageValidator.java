package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.validator.XMLValidator;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Page;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.LOCATOR_ID_HAS_DUPLICATE_LOCATOR;

public class PageValidator implements XMLValidator<Page> {

    @Override
    public void validate(final Page page, final File xmlFile) {
        Set<String> ids = new HashSet<>();
        for (Locator each : page.getLocators().getLocator()) {
            String locatorId = each.getLocatorId();
            throwIfDuplicateLocators(ids.contains(locatorId), page, locatorId);
            ids.add(locatorId);
        }
    }

    private void throwIfDuplicateLocators(final boolean contains, final Page page, final String locatorId) {
        if (contains) {
            throw new DefaultFrameworkException(LOCATOR_ID_HAS_DUPLICATE_LOCATOR,
                    page.getDetails().getName(), locatorId);
        }
    }
}
