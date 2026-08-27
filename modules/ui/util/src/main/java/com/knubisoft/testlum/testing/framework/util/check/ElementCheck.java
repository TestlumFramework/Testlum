package com.knubisoft.testlum.testing.framework.util.check;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * Validations applied to a web element right after it was located.
 * <p>
 * The declaration order of the constants is the execution order: sets of checks are kept as
 * {@link java.util.EnumSet}, which iterates in natural order. Reordering the constants silently
 * reorders every check set, so keep the order intentional.
 */
public enum ElementCheck {

    VISIBILITY {
        @Override
        public void check(final ExecutorDependencies dependencies, final WebElement element) {
            if (!element.isDisplayed()) {
                throw new DefaultFrameworkException(LogMessage.UI_ELEMENT_IS_NOT_VISIBLE_EXCEPTION_MESSAGE);
            }
            Dimension size = element.getSize();
            if (size.getWidth() == 0 || size.getHeight() == 0) {
                throw new DefaultFrameworkException(LogMessage.UI_ELEMENT_HAS_ZERO_SIZE_EXCEPTION_MESSAGE);
            }
        }
    },

    /**
     * Not a pure check: it scrolls the element to the center of the viewport before sampling the
     * point under it. The scroll and the sampling are a single {@code executeScript} on purpose and
     * must not be split - without the scroll {@code elementFromPoint} returns null for everything
     * below the fold, which would be reported as a false "covered by an overlay".
     * <p>
     * Not applicable to {@link UiType#NATIVE}: there is no DOM there.
     */
    SCROLLED_INTO_VIEW_AND_INTERACTABLE {
        @Override
        public boolean supports(final UiType uiType) {
            return uiType != UiType.NATIVE;
        }

        @Override
        public void check(final ExecutorDependencies dependencies, final WebElement element) {
            Boolean isTopElement = (Boolean) ((JavascriptExecutor) dependencies.getDriver())
                    .executeScript(COVERED_CHECK_SCRIPT, element);
            if (!Boolean.TRUE.equals(isTopElement)) {
                throw new DefaultFrameworkException(LogMessage.UI_ELEMENT_IS_NOT_INTERACTABLE_EXCEPTION_MESSAGE);
            }
        }
    },

    ENABLED {
        @Override
        public void check(final ExecutorDependencies dependencies, final WebElement element) {
            if (!element.isEnabled()) {
                throw new DefaultFrameworkException(
                        String.format(LogMessage.UI_ELEMENT_DISABLED_EXCEPTION_MESSAGE, DISABLED_ATTRIBUTE));
            }
            if (Boolean.parseBoolean(element.getAttribute(ARIA_DISABLED_ATTRIBUTE))) {
                throw new DefaultFrameworkException(
                        String.format(LogMessage.UI_ELEMENT_DISABLED_EXCEPTION_MESSAGE, ARIA_DISABLED_ATTRIBUTE));
            }
        }
    },

    /**
     * The element accepts input. A read-only field is not disabled - it is enabled, displayed and not
     * covered - so every other check passes it and the driver then rejects the write itself.
     * <p>
     * Not applicable to {@link UiType#NATIVE}: there is no read-only attribute in Appium, and asking
     * for one is at best null and at worst an unsupported command.
     */
    EDITABLE {
        @Override
        public boolean supports(final UiType uiType) {
            return uiType != UiType.NATIVE;
        }

        @Override
        public void check(final ExecutorDependencies dependencies, final WebElement element) {
            if (isFlagSet(element, READONLY_ATTRIBUTE)) {
                throw new DefaultFrameworkException(
                        String.format(LogMessage.UI_ELEMENT_READONLY_EXCEPTION_MESSAGE, READONLY_ATTRIBUTE));
            }
            if (isFlagSet(element, ARIA_READONLY_ATTRIBUTE)) {
                throw new DefaultFrameworkException(
                        String.format(LogMessage.UI_ELEMENT_READONLY_EXCEPTION_MESSAGE, ARIA_READONLY_ATTRIBUTE));
            }
        }
    };

    private static final String DISABLED_ATTRIBUTE = "disabled";
    private static final String ARIA_DISABLED_ATTRIBUTE = "aria-disabled";
    private static final String READONLY_ATTRIBUTE = "readonly";
    private static final String ARIA_READONLY_ATTRIBUTE = "aria-readonly";
    private static final String COVERED_CHECK_SCRIPT =
            "var e = arguments[0];"
            + "e.scrollIntoView({block: 'center'});"
            + "var r = e.getBoundingClientRect();"
            + "return e.contains(document.elementFromPoint(r.left + r.width / 2, r.top + r.height / 2));";

    /**
     * Reads an attribute that may be either a boolean HTML attribute or a plain "true"/"false" one.
     * Selenium normalises {@code readonly} through the DOM property and answers "true" or null, but
     * other drivers answer "" or the attribute name itself - all of which {@code parseBoolean} would
     * quietly read as false.
     */
    private static boolean isFlagSet(final WebElement element, final String attribute) {
        String value = element.getAttribute(attribute);
        return value != null && !Boolean.FALSE.toString().equalsIgnoreCase(value);
    }

    /**
     * Whether this check is meaningful for the given UI type. Checks that are not supported are
     * skipped instead of failing.
     */
    public boolean supports(final UiType uiType) {
        return true;
    }

    /**
     * Validates the element and throws {@link DefaultFrameworkException} with a user facing message
     * when it does not pass.
     */
    public abstract void check(ExecutorDependencies dependencies, WebElement element);
}
