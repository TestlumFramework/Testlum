package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.log.LogFormat;
import com.testlum.testing.framework.interpreter.OttUtil;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.OttInput;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import static java.lang.String.format;

@Slf4j
@ExecutorForClass(OttInput.class)
public class OttInputExecutor extends AbstractUiExecutor<OttInput> {

    private static final String TABLE_FORMAT = "%-23s|%-70s";
    private static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    private static final String CODE_LOG = LogFormat.table("Generated code");
    private static final String ALIAS = "Alias";
    private static final String DEFAULT_ALIAS_VALUE = "DEFAULT";

    private final OttUtil ottUtil;

    public OttInputExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.ottUtil = dependencies.getContext().getBean(OttUtil.class);
    }

    @Override
    protected void execute(final OttInput ottInput, final CommandResult result) {
        checkAlias(ottInput);
        String code = ottUtil.generateCode(ottInput.getAlias());
        dependencies.getScenarioContext().set(ottInput.getName(), code);
        inputCode(ottInput, code, result);
        addOttMetaData(ottInput.getAlias(), code, result);
    }

    private void inputCode(final OttInput ottInput, final String code, final CommandResult result) {
        result.put(ResultUtil.INPUT_LOCATOR, ottInput.getLocator());
        WebElement webElement = uiUtil.findWebElement(
                dependencies,
                ottInput.getLocator(),
                ottInput.getLocatorStrategy(),
                ElementChecks.FOR_WRITING
        );
        uiUtil.highlightElementIfRequired(ottInput.isHighlight(), webElement, dependencies.getDriver());
        webElement.sendKeys(code);
        result.put(ResultUtil.INPUT_VALUE, code);
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void checkAlias(final OttInput ottInput) {
        if (ottInput.getAlias() == null) {
            ottInput.setAlias(DEFAULT_ALIAS_VALUE);
        }
    }

    private void addOttMetaData(final String alias, final String code, final CommandResult result) {
        result.put(ALIAS, alias);
        log.info(ALIAS_LOG, alias);
        log.info(CODE_LOG, code);
    }
}
