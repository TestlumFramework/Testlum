package com.knubisoft.testlum.testing.framework.util.check;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class InteractabilityCheck extends AbstractElementCheck {

    private static final String COVERED_CHECK_SCRIPT =
            "var e = arguments[0];"
            + "e.scrollIntoView({block: 'center'});"
            + "var r = e.getBoundingClientRect();"
            + "return e.contains(document.elementFromPoint(r.left + r.width / 2, r.top + r.height / 2));";

    @Override
    protected void doCheck(final WebDriver driver, final WebElement element) {
        Boolean isTopElement = (Boolean) ((JavascriptExecutor) driver)
                .executeScript(COVERED_CHECK_SCRIPT, element);
        if (!Boolean.TRUE.equals(isTopElement)) {
            throw new DefaultFrameworkException(LogMessage.UI_ELEMENT_IS_NOT_INTERACTABLE_EXCEPTION_MESSAGE);
        }
    }
}
