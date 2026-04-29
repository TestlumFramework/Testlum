package com.knubisoft.testlum.testing.framework.autohealing;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.locator.LocatorData;
import com.knubisoft.testlum.testing.model.global_config.AutoHealingMode;
import com.knubisoft.testlum.testing.model.pages.Locator;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.util.Optional;

public interface AutoHealer {

    Optional<WebElement> heal(Locator locator);

    File generateNewLocators(WebElement healedElement,
                             AutoHealingMode mode,
                             ExecutorDependencies dependencies,
                             LocatorData locatorData);
}
