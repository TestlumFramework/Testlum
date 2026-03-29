package com.knubisoft.testlum.testing.framework.locator;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.pages.Locator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LocatorCollector} verifying locator retrieval
 * and error handling for missing or incorrectly named locators.
 */
class LocatorCollectorTest {

    private LocatorCollector collector;

    @BeforeEach
    void setUp() throws Exception {
        collector = createCollectorWithLocators(buildTestLocatorMap());
    }

    private Map<String, Locator> buildTestLocatorMap() {
        final Map<String, Locator> map = new LinkedHashMap<>();
        map.put("login.username", createLocator("username"));
        map.put("login.password", createLocator("password"));
        map.put("home.title", createLocator("title"));
        return map;
    }

    private Locator createLocator(final String id) {
        final Locator locator = new Locator();
        locator.setLocatorId(id);
        return locator;
    }

    private LocatorCollector createCollectorWithLocators(
            final Map<String, Locator> locators) throws Exception {
        final java.lang.reflect.Constructor<LocatorCollector> ctor =
                LocatorCollector.class.getDeclaredConstructor(
                        XMLParsers.class, PageValidator.class,
                        TestResourceSettings.class, FileSearcher.class);
        ctor.setAccessible(true);

        // We cannot call the real constructor, so we use Objenesis-style
        // instantiation via the Unsafe API available in the JDK.
        final Object unsafe = getUnsafe();
        final LocatorCollector instance = (LocatorCollector) unsafe.getClass()
                .getMethod("allocateInstance", Class.class)
                .invoke(unsafe, LocatorCollector.class);

        final Field locatorMapField = LocatorCollector.class.getDeclaredField("locatorMap");
        locatorMapField.setAccessible(true);
        locatorMapField.set(instance, locators);
        return instance;
    }

    private Object getUnsafe() throws Exception {
        final Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        return theUnsafe.get(null);
    }

    @Nested
    class GetLocator {
        @Test
        void returnsLocatorByFullName() {
            final Locator result = collector.getLocator("login.username");
            assertNotNull(result);
            assertEquals("username", result.getLocatorId());
        }

        @Test
        void returnsLocatorFromDifferentPage() {
            final Locator result = collector.getLocator("home.title");
            assertNotNull(result);
            assertEquals("title", result.getLocatorId());
        }

        @Test
        void throwsForMissingLocator() {
            assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("login.nonexistent"));
        }

        @Test
        void throwsForIncorrectNamingFormat() {
            assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("noDotInName"));
        }

        @Test
        void throwsForTooManyDots() {
            assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator("a.b.c"));
        }

        @Test
        void throwsForEmptyName() {
            assertThrows(DefaultFrameworkException.class,
                    () -> collector.getLocator(""));
        }
    }
}
