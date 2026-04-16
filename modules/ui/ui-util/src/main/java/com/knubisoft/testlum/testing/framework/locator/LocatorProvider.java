package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.model.pages.Locator;

public interface LocatorProvider {

    Locator getLocator(String name);
}
