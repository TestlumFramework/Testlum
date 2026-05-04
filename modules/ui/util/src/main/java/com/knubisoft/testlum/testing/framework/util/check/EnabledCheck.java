package com.knubisoft.testlum.testing.framework.util.check;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class EnabledCheck extends AbstractElementCheck {

    private static final String DISABLED_ATTRIBUTE = "disabled";
    private static final String ARIA_DISABLED_ATTRIBUTE = "aria-disabled";

    @Override
    protected void doCheck(final WebDriver driver, final WebElement element) {
        if (!element.isEnabled()) {
            throw new DefaultFrameworkException(
                    String.format(LogMessage.UI_ELEMENT_DISABLED_EXCEPTION_MESSAGE, DISABLED_ATTRIBUTE)
            );
        }
        if (Boolean.parseBoolean(element.getAttribute(ARIA_DISABLED_ATTRIBUTE))) {
            throw new DefaultFrameworkException(
                    String.format(LogMessage.UI_ELEMENT_DISABLED_EXCEPTION_MESSAGE, ARIA_DISABLED_ATTRIBUTE)
            );
        }
    }
}
