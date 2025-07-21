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
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.CUSTOM_DROP_DOWN_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.DROP_DOWN_NOT_SUPPORTED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.BY_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALL_VALUES_DESELECT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ALL_VALUES_SELECT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.DROP_DOWN_FOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.DROP_DOWN_LOCATOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ONE_VALUE_TEMPLATE;
import static java.lang.String.format;

@Slf4j
@ExecutorForClass(DropDown.class)
public class DropDownExecutor extends AbstractUiExecutor<DropDown> {

    private static final String CONTAINS_TEXT_PATTERN = ".//*[contains(text(), '%s')]";

    public DropDownExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final DropDown dropDown, final CommandResult result) {
        String locatorId = dropDown.getLocator();
        result.put(DROP_DOWN_LOCATOR, locatorId);
        WebElement dropDownElement = UiUtil.findWebElement(dependencies, locatorId, dropDown.getLocatorStrategy(), result);
        if (dropDownElement.getTagName().equals("select")) {
            processSelectDropDown(dropDown, result, dropDownElement);
        } else {
            processCustomDropDown(dropDownElement, dropDown, result);
        }
    }

    private void processCustomDropDown(final WebElement dropDownElement, final DropDown dropDown,
                                       final CommandResult result) {
        OneValue oneValue = dropDown.getOneValue();
        if (Objects.nonNull(oneValue)) {
            processOneValueForCustomDropDown(dropDownElement, dropDown, result);
        } else {
            throw new DefaultFrameworkException(CUSTOM_DROP_DOWN_NOT_SUPPORTED,
                    dropDown.getAllValues().getType().value());
        }

    }

    private void processSelectDropDown(final DropDown dropDown, final CommandResult result,
                                       final WebElement dropDownElement) {
        Select select = new Select(dropDownElement);
        OneValue oneValue = dropDown.getOneValue();
        if (Objects.nonNull(oneValue)) {
            processOneValueFromSelectDropDown(oneValue, select, result);
        } else {
            AllValues allValues = dropDown.getAllValues();
            processAllValuesFromSelectDropDown(allValues, select, result);
        }
    }

    private void processOneValueForCustomDropDown(final WebElement dropDownElement, final DropDown dropDown,
                                                  final CommandResult result) {
        OneValue oneValue = dropDown.getOneValue();
        TypeForOneValue type = oneValue.getType();
        String value = oneValue.getValue();
        validateByMethodForCustomDropDown(oneValue.getBy());
        ResultUtil.addDropDownForOneValueMetaData(oneValue.getType().value(), oneValue.getBy().value(), value, result);
        log.info(COMMAND_TYPE_LOG, format(ONE_VALUE_TEMPLATE, type.value()));
        log.info(BY_LOG, oneValue.getBy().value());
        log.info(VALUE_LOG, value);
        dropDownElement.click();
        List<WebElement> dropDownParentElements = dropDownElement.findElements(By.xpath("ancestor::*"));
        selectSearchableOptionForCustomDropDown(dropDownParentElements, value);
    }

    private void selectSearchableOptionForCustomDropDown(final List<WebElement> dropDownParentElements,
                                                         final String value) {
        Collections.reverse(dropDownParentElements);

        for (int i = 0; i < dropDownParentElements.size(); i++) {
            WebElement element = dropDownParentElements.get(i);
            try {
                WebElement searchableOption = element.findElement(By.xpath(format(CONTAINS_TEXT_PATTERN, value)));
                searchableOption.click();
                break;
            } catch (NoSuchElementException e) {
                if (i == dropDownParentElements.size() - 1) {
                    throw e;
                }
            }
        }
    }

    private void processOneValueFromSelectDropDown(final OneValue oneValue, final Select select,
                                                   final CommandResult result) {
        TypeForOneValue type = oneValue.getType();
        SelectOrDeselectBy method = oneValue.getBy();
        String value = oneValue.getValue();
        ResultUtil.addDropDownForOneValueMetaData(type.value(), method.value(), value, result);
        log.info(COMMAND_TYPE_LOG, format(ONE_VALUE_TEMPLATE, type.value()));
        log.info(BY_LOG, oneValue.getBy().value());
        log.info(VALUE_LOG, value);
        if (type == TypeForOneValue.SELECT) {
            selectByMethod(select, method, value);
        } else {
            deselectByMethod(select, method, value);
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private void validateByMethodForCustomDropDown(final SelectOrDeselectBy method) {
        switch (method) {
            case TEXT:
                break;
            case INDEX:
            case VALUE:
                throw new DefaultFrameworkException(CUSTOM_DROP_DOWN_NOT_SUPPORTED, method.value());
            default:
                throw new DefaultFrameworkException(DROP_DOWN_NOT_SUPPORTED, method.value());
        }
    }

    private void processAllValuesFromSelectDropDown(final AllValues allValues, final Select select,
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
            case INDEX:
                select.selectByIndex(Integer.parseInt(value));
                break;
            case TEXT:
                select.selectByVisibleText(value);
                break;
            case VALUE:
                select.selectByValue(value);
                break;
            default:
                throw new DefaultFrameworkException(DROP_DOWN_NOT_SUPPORTED, method.value());
        }
    }

    private void deselectByMethod(final Select select, final SelectOrDeselectBy method, final String value) {
        switch (method) {
            case INDEX:
                select.deselectByIndex(Integer.parseInt(value));
                break;
            case TEXT:
                select.deselectByVisibleText(value);
                break;
            case VALUE:
                select.deselectByValue(value);
                break;
            default:
                throw new DefaultFrameworkException(DROP_DOWN_NOT_SUPPORTED, method.value());
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
