package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;

import com.knubisoft.testlum.testing.model.scenario.DropDown;
import com.knubisoft.testlum.testing.model.scenario.OneValue;
import com.knubisoft.testlum.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.testlum.testing.model.scenario.TypeForOneValue;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.ui.Select;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DROP_DOWN_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALL_VALUES_DESELECT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.DROP_DOWN_FOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.DROP_DOWN_LOCATOR;

@Slf4j
@ExecutorForClass(DropDown.class)
public class DropDownExecutor extends AbstractUiExecutor<DropDown> {

    public DropDownExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final DropDown dropDown, final CommandResult result) {
        String locatorId = dropDown.getLocatorId();
        result.put(DROP_DOWN_LOCATOR, locatorId);
        Select select = new Select(uiUtil.findWebElement(dependencies, locatorId));
        OneValue oneValue = dropDown.getOneValue();
        if (Objects.nonNull(oneValue)) {
            processOneValueFromDropDown(oneValue, select, result);
        } else {
            log.info(COMMAND_TYPE_LOG, ALL_VALUES_DESELECT);
            result.put(DROP_DOWN_FOR, ALL_VALUES_DESELECT);
            select.deselectAll();
        }
    }

    private void processOneValueFromDropDown(final OneValue oneValue, final Select select, final CommandResult result) {
        TypeForOneValue type = oneValue.getType();
        SelectOrDeselectBy method = oneValue.getBy();
        String value = oneValue.getValue();
        ResultUtil.addDropDownForOneValueMetaData(type.value(), method.value(), value, result);
        log.info(COMMAND_TYPE_LOG, type.value());
        log.info(VALUE_LOG, value);
        if (type == TypeForOneValue.SELECT) {
            selectByMethod(select, method, value);
        } else {
            deselectByMethod(select, method, value);
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void selectByMethod(final Select select, final SelectOrDeselectBy method, final String value) {
        switch (method) {
            case INDEX: select.selectByIndex(Integer.parseInt(value));
                break;
            case TEXT: select.selectByVisibleText(value);
                break;
            case VALUE: select.selectByValue(value);
                break;
            default: throw new DefaultFrameworkException(DROP_DOWN_NOT_SUPPORTED, method.value());
        }
    }

    private void deselectByMethod(final Select select, final SelectOrDeselectBy method, final String value) {
        switch (method) {
            case INDEX: select.deselectByIndex(Integer.parseInt(value));
                break;
            case TEXT: select.deselectByVisibleText(value);
                break;
            case VALUE: select.deselectByValue(value);
                break;
            default: throw new DefaultFrameworkException(DROP_DOWN_NOT_SUPPORTED, method.value());
        }
    }
}
