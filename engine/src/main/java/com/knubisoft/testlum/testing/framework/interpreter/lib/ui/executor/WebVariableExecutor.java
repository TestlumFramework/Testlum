package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.ElementAttribute;
import com.knubisoft.testlum.testing.model.scenario.ElementPresent;
import com.knubisoft.testlum.testing.model.scenario.WebVar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_VARIABLE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.*;
import static java.util.Objects.nonNull;


@Slf4j
@ExecutorForClass(WebVar.class)
public class WebVariableExecutor extends AbstractUiExecutor<WebVar> {

    private final Map<VarPredicate<WebVar>, VarMethod<WebVar>> varToMethodMap;
    private final VariableHelper variableHelper;

    public WebVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.variableHelper = dependencies.getContext().getBean(VariableHelper.class);
        this.varToMethodMap = Map.ofEntries(
                Map.entry(var -> nonNull(var.getElement()), this::getElementResult),
                Map.entry(var -> nonNull(var.getDom()), this::getDomResult),
                Map.entry(var -> nonNull(var.getCookie()), this::getWebCookiesResult),
                Map.entry(var -> nonNull(var.getUrl()), this::getUrlResult),
                Map.entry(var -> nonNull(var.getPath()), this::getPathResult),
                Map.entry(var -> nonNull(var.getConstant()), this::getConstantResult),
                Map.entry(var -> nonNull(var.getExpression()), this::getExpressionResult),
                Map.entry(var -> nonNull(var.getFile()), this::getFileResult),
                Map.entry(var -> nonNull(var.getSql()), this::getSQLResult),
                Map.entry(var -> nonNull(var.getGenerate()), this::getRandomGenerateResult),
                Map.entry(var -> nonNull(var.getAlert()), this::getAlertResult));
    }

    @Override
    public void execute(final WebVar var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_LOG, var.getName(), var.getComment());
            throw e;
        }
    }

    private void setContextVariable(final WebVar var, final CommandResult result) {
        String value = getValueForContext(var, result);
        dependencies.getScenarioContext().set(var.getName(), value);
        logUtil.logVarInfo(var.getName(), value);
    }

    private String getValueForContext(final WebVar var, final CommandResult result) {
        return variableHelper.lookupVarMethod(varToMethodMap, var)
                .apply(var, result);
    }

    private String getElementResult(final WebVar webVar, final CommandResult result) {
        ElementAttribute attribute = webVar.getElement().getAttribute();
        ElementPresent present = webVar.getElement().getPresent();
        String valueResult;
        if (nonNull(attribute)) {
            valueResult = getAttributeValue(attribute, webVar.getName(), result);
        } else {
            valueResult = getPresentValue(present, webVar.getName(), result);
        }
        return valueResult;
    }

    private String getPresentValue(final ElementPresent present, final String varName, final CommandResult r) {
        String value;
        try {
            uiUtil.findWebElement(dependencies, present.getLocator(), present.getLocatorStrategy());
            value = String.valueOf(true);
        } catch (DefaultFrameworkException e) {
            value = String.valueOf(false);
        }
        resultUtil.addVariableMetaData(ELEMENT_PRESENT, varName, LOCATOR_FORM, present.getLocator(), value, r);
        return value;
    }

    private String getAttributeValue(final ElementAttribute attribute, final String varName, final CommandResult r) {
        WebElement webElement = uiUtil.findWebElement(dependencies, attribute.getLocator(),
                attribute.getLocatorStrategy());
        String value = uiUtil.getElementAttribute(webElement, attribute.getName(), dependencies.getDriver());
        resultUtil.addVariableMetaData(ELEMENT_ATTRIBUTE, varName, LOCATOR_FORM, attribute.getLocator(), value, r);
        return value;
    }

    private String getDomResult(final WebVar webVar, final CommandResult result) {
        String locatorId = webVar.getDom().getLocator();
        if (StringUtils.isNotBlank(locatorId)) {
            String valueResult = uiUtil.findWebElement(dependencies, locatorId, webVar.getDom().getLocatorStrategy())
                    .getAttribute("outerHTML");
            resultUtil.addVariableMetaData(HTML_DOM, webVar.getName(), LOCATOR_FORM, locatorId, valueResult, result);
            return valueResult;
        }
        String valueResult = dependencies.getDriver().getPageSource();
        resultUtil.addVariableMetaData(HTML_DOM, webVar.getName(), FULL_DOM, valueResult, result);
        return valueResult;
    }

    private String getWebCookiesResult(final WebVar var, final CommandResult result) {
        Set<Cookie> cookies = dependencies.getDriver().manage().getCookies();
        String valueResult = cookies.stream()
                .map(cookie -> String.join(DelimiterConstant.EQUALS_MARK, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(DelimiterConstant.SEMICOLON));
        resultUtil.addVariableMetaData(COOKIES, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getUrlResult(final WebVar var, final CommandResult result) {
        String valueResult = dependencies.getDriver().getCurrentUrl();
        resultUtil.addVariableMetaData(URL, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getPathResult(final WebVar var, final CommandResult result) {
        UnaryOperator<String> fileToString = fileName -> {
            String content = fileSearcher.searchFileToString(fileName, dependencies.getFile());
            return inject(content);
        };
        return variableHelper.getPathResult(var.getPath(), var.getName(), dependencies.getScenarioContext(), result,
                fileToString);
    }

    private String getConstantResult(final WebVar var, final CommandResult result) {
        return variableHelper.getConstantResult(var.getConstant(), var.getName(), result);
    }

    private String getExpressionResult(final WebVar var, final CommandResult result) {
        return variableHelper.getExpressionResult(var.getExpression(), var.getName(), result);
    }

    private String getFileResult(final WebVar var, final CommandResult result) {
        UnaryOperator<String> fileToString = this::getContentIfFile;
        return variableHelper.getFileResult(var.getFile(), var.getName(), fileToString, result);
    }

    private String getSQLResult(final WebVar var, final CommandResult result) {
        return variableHelper.getSQLResult(var.getSql(), var.getName(), result);
    }

    private String getRandomGenerateResult(final WebVar var, final CommandResult result) {
        return variableHelper.getRandomGenerateResult(var.getGenerate(), var.getName(), result);
    }

    private String getAlertResult(final WebVar var, final CommandResult result) {
        return dependencies.getDriver().switchTo().alert().getText();
    }
}
