package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.locator.GlobalLocators;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.pages.Locator;
import com.knubisoft.cott.testing.model.scenario.Assert;
import org.openqa.selenium.WebElement;

import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.*;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_ATTRIBUTE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_LOCATOR;

@ExecutorForClass(Assert.class)
public class AssertExecutor extends AbstractUiExecutor<Assert> {

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Assert aAssert, final CommandResult result) {
        LogUtil.logAssertTag(aAssert);
        result.put(ASSERT_LOCATOR, aAssert.getLocatorId());
        result.put(ASSERT_ATTRIBUTE, aAssert.getAttribute());
        String actual = getActualValue(aAssert);
        String expected = inject(aAssert.getContent()).replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
        result.setActual(actual);
        result.setExpected(expected);
        new CompareBuilder(dependencies.getFile(), dependencies.getPosition())
                .withActual(actual)
                .withExpected(expected)
                .exec();
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private String getActualValue(final Assert aAssert) {
        UiUtil.waitForElementVisibility(dependencies.getDriver(), aAssert.getLocatorId());
        WebElement webElement = UiUtil.findWebElement(dependencies.getDriver(), aAssert.getLocatorId());
        String value = UiUtil.getElementAttribute(webElement, aAssert.getAttribute());
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }
}
