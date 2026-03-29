package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.Details;
import com.knubisoft.testlum.testing.model.pages.Locator;
import com.knubisoft.testlum.testing.model.pages.Locators;
import com.knubisoft.testlum.testing.model.pages.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageValidatorTest {

    @InjectMocks
    private PageValidator pageValidator;

    @Test
    void noDuplicateLocatorsDoesNotThrow() {
        Page page = createPageWithLocatorIds("locator1", "locator2", "locator3");
        File xmlFile = new File("/test/page.xml");

        assertDoesNotThrow(() -> pageValidator.validate(page, xmlFile));
    }

    @Test
    void duplicateLocatorIdsThrowsException() {
        Page page = createPageWithLocatorIds("locator1", "locator2", "locator1");
        File xmlFile = new File("/test/page.xml");

        assertThrows(DefaultFrameworkException.class,
                () -> pageValidator.validate(page, xmlFile));
    }

    @Test
    void emptyLocatorsListDoesNotThrow() {
        Page page = createPageWithLocatorIds();
        File xmlFile = new File("/test/page.xml");

        assertDoesNotThrow(() -> pageValidator.validate(page, xmlFile));
    }

    @Test
    void singleLocatorDoesNotThrow() {
        Page page = createPageWithLocatorIds("onlyLocator");
        File xmlFile = new File("/test/page.xml");

        assertDoesNotThrow(() -> pageValidator.validate(page, xmlFile));
    }

    private Page createPageWithLocatorIds(final String... ids) {
        final Page page = mock(Page.class);
        final Locators locators = mock(Locators.class);
        final List<Locator> locatorList = new ArrayList<>();

        for (String id : ids) {
            Locator locator = mock(Locator.class);
            when(locator.getLocatorId()).thenReturn(id);
            locatorList.add(locator);
        }

        when(page.getLocators()).thenReturn(locators);
        when(locators.getLocator()).thenReturn(locatorList);
        final Details details = mock(Details.class);
        lenient().when(page.getDetails()).thenReturn(details);
        lenient().when(details.getName()).thenReturn("TestPage");

        return page;
    }
}
