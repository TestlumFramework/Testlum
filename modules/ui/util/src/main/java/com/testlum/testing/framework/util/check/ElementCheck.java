package com.testlum.testing.framework.util.check;

import com.testlum.testing.framework.constant.LogMessage;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.UiType;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

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

    SCROLLED_INTO_VIEW_AND_INTERACTABLE {
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

    EDITABLE {
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

    private static boolean isFlagSet(final WebElement element, final String attribute) {
        String value = element.getAttribute(attribute);
        return value != null && !Boolean.FALSE.toString().equalsIgnoreCase(value);
    }

    public boolean supports(final UiType uiType) {
        return uiType != UiType.NATIVE;
    }

    public abstract void check(ExecutorDependencies dependencies, WebElement element);
}
