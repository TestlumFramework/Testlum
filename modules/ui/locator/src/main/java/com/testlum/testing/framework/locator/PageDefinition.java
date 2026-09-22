package com.testlum.testing.framework.locator;

import com.testlum.testing.model.pages.Locator;
import com.testlum.testing.model.pages.Page;

import java.util.List;

public record PageDefinition(String name, Page page, List<Locator> ownLocators) {
}
