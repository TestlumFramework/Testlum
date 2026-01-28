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
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
import static org.apache.commons.lang3.StringUtils.isBlank;

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
        WebElement dropDownElement = UiUtil.findWebElement(dependencies, locatorId, dropDown.getLocatorStrategy());
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
        if ("mat-select".equalsIgnoreCase(dropDownElement.getTagName())) {
            processMatSelect(dependencies, dropDownElement, value);
            UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
            return;
        }
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

    public void processMatSelect(final ExecutorDependencies dependencies, final WebElement matSelect, final String value) {
        openMatSelect(dependencies, matSelect);
        WebElement panel = getMatSelectPanel(dependencies, matSelect);
        WebElement option = findMatchingOption(panel, value);
        clickMatOption(dependencies, option);
        UiUtil.waitForMatSelectToClose(dependencies, matSelect);
    }

    private void openMatSelect(final ExecutorDependencies dependencies, final WebElement matSelect) {
        UiUtil.waitForElementToBeClickable(dependencies, matSelect);
        matSelect.click();
        UiUtil.waitForMatSelectToOpen(dependencies, matSelect);
    }

    private WebElement getMatSelectPanel(final ExecutorDependencies dependencies, final WebElement matSelect) {
        String panelId = matSelect.getAttribute("aria-controls");
        if (isBlank(panelId)) {
            throw new DefaultFrameworkException("The 'aria-controls' attribute is missing on mat-select. Cannot find dropdown panel.");
        }
        WebElement panel = dependencies.getDriver().findElement(By.id(panelId));
        UiUtil.waitForElementVisibility(dependencies, panel);
        return panel;
    }

    private WebElement findMatchingOption(final WebElement panel, final String value) {
        List<WebElement> options = panel.findElements(By.cssSelector("mat-option, [role='option']"));
        if (options.isEmpty()) {
            throw new DefaultFrameworkException("No options (mat-option) found in the dropdown panel.");
        }

        String normalizedTarget = normalizeText(value);
        return options.stream()
                .filter(o -> !"true".equalsIgnoreCase(o.getAttribute("aria-disabled")))
                .filter(o -> normalizeText(extractOptionText(o)).equalsIgnoreCase(normalizedTarget))
                .findFirst()
                .orElseThrow(() -> new DefaultFrameworkException(
                        format("Option '%s' not found. Available options: %s", value,
                                options.stream().map(o -> normalizeText(extractOptionText(o))).collect(Collectors.toList()))
                ));
    }

    private void clickMatOption(final ExecutorDependencies dependencies, final WebElement option) {
        UiUtil.waitForElementToBeClickable(dependencies, option);
        option.click();
    }

    private String extractOptionText(final WebElement option) {
        List<WebElement> spans = option.findElements(By.cssSelector("span"));
        for (WebElement span : spans) {
            String txt = span.getText();
            if (!isBlank(txt)) {
                return txt;
            }
        }
        return option.getText();
    }

    private String normalizeText(final String s) {
        if (s == null) {
            return "";
        }
        return s.replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}
