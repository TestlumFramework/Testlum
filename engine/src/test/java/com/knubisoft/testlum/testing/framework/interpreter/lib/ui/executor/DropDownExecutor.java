package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.AllValues;
import com.knubisoft.testlum.testing.model.scenario.DropDown;
import com.knubisoft.testlum.testing.model.scenario.OneValue;
import com.knubisoft.testlum.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.testlum.testing.model.scenario.TypeForAllValues;
import com.knubisoft.testlum.testing.model.scenario.TypeForOneValue;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.ui.Select;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DROP_DOWN_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.*;

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
        Select select = new Select(UiUtil.findWebElement(dependencies, locatorId, dropDown.getLocatorStrategy()));
        OneValue oneValue = dropDown.getOneValue();
        if (Objects.nonNull(oneValue)) {
            processOneValueFromDropDown(oneValue, select, result);
        } else {
            AllValues allValues = dropDown.getAllValues();
            processAllValuesFromDropDown(allValues, select, result);
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
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void processAllValuesFromDropDown(final AllValues allValues, final Select select,
                                              final CommandResult result) {
        TypeForAllValues type = allValues.getType();
        if (type == TypeForAllValues.DESELECT) {
            deselectAll(select, result);
        } else {
            selectAll(select, result);
        }
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

    private void selectAll(final Select select, final CommandResult result) {
        for (int i = 0; i < select.getOptions().size(); i++) {
            select.selectByIndex(i);
        }
        log.info(COMMAND_TYPE_LOG, ALL_VALUES_SELECT);
        result.put(DROP_DOWN_FOR, ALL_VALUES_SELECT);
    }

    private void deselectAll(final Select select, final CommandResult result) {
        log.info(COMMAND_TYPE_LOG, ALL_VALUES_DESELECT);
        result.put(DROP_DOWN_FOR, ALL_VALUES_DESELECT);
        select.deselectAll();
    }
}
