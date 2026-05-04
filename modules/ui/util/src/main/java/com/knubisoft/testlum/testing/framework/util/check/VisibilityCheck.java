package com.knubisoft.testlum.testing.framework.util.check;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VisibilityCheck extends AbstractElementCheck {

    @Override
    protected void doCheck(final WebDriver driver, final WebElement element) {
        if (!element.isDisplayed()) {
            throw new DefaultFrameworkException(LogMessage.UI_ELEMENT_IS_NOT_VISIBLE_EXCEPTION_MESSAGE);
        }
        if (element.getSize().getWidth() == 0 || element.getSize().getHeight() == 0) {
            throw new DefaultFrameworkException(LogMessage.UI_ELEMENT_HAS_ZERO_SIZE_EXCEPTION_MESSAGE);
        }
    }
}
