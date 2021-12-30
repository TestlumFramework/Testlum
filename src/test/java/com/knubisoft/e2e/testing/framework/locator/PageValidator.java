package com.knubisoft.e2e.testing.framework.locator;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.model.pages.Locator;
import com.knubisoft.e2e.testing.model.pages.Page;
import com.knubisoft.e2e.testing.framework.parser.XMLValidator;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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
            throw new DefaultFrameworkException("Locator id from page %s has duplicate locator with id %s",
                    page.getDetails().getName(), locatorId);
        }
    }
}
