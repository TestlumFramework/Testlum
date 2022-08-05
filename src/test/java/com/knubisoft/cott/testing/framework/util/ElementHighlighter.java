package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.JavascriptConstant;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@UtilityClass
public final class ElementHighlighter {
    public void highlight(final WebElement element, final WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
                JavascriptConstant.HIGHLIGHT_SCRIPT, element);
    }
}
