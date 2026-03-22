package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Slf4j
@ExecutorForClass(WebVar.class)
public class WebVariableExecutor extends AbstractUiExecutor<WebVar> {

    private final Map<VarPredicate<WebVar>, VarMethod<WebVar>> varToMethodMap;
    private final VariableHelper variableHelper;

    public WebVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.variableHelper = dependencies.getContext().getBean(VariableHelper.class);
        this.varToMethodMap = Map.of(
                var -> Objects.nonNull(var.getElement()), this::getElementResult,
                var -> Objects.nonNull(var.getDom()), this::getDomResult,
                var -> Objects.nonNull(var.getCookie()), this::getWebCookiesResult,
                var -> Objects.nonNull(var.getUrl()), this::getUrlResult,
                var -> Objects.nonNull(var.getPath()), this::getPathResult,
                var -> Objects.nonNull(var.getConstant()), this::getConstantResult,
                var -> Objects.nonNull(var.getExpression()), this::getExpressionResult,
                var -> Objects.nonNull(var.getFile()), this::getFileResult,
                var -> Objects.nonNull(var.getSql()), this::getSQLResult,
                var -> Objects.nonNull(var.getGenerate()), this::getRandomGenerateResult);
    }

    @Override
    public void execute(final WebVar var, final CommandResult result) {
        try {
            setContextVariable(var, result);
        } catch (Exception e) {
            log.info(LogMessage.FAILED_VARIABLE_LOG, var.getName(), var.getComment());
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
        if (Objects.nonNull(attribute)) {
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
        resultUtil.addVariableMetaData(ResultUtil.ELEMENT_PRESENT, varName,
                ResultUtil.LOCATOR_FORM, present.getLocator(), value, r);
        return value;
    }

    private String getAttributeValue(final ElementAttribute attribute, final String varName, final CommandResult r) {
        WebElement webElement = uiUtil.findWebElement(dependencies, attribute.getLocator(),
                attribute.getLocatorStrategy());
        String value = uiUtil.getElementAttribute(webElement, attribute.getName(), dependencies.getDriver());
        resultUtil.addVariableMetaData(ResultUtil.ELEMENT_ATTRIBUTE, varName,
                ResultUtil.LOCATOR_FORM, attribute.getLocator(), value, r);
        return value;
    }

    private String getDomResult(final WebVar webVar, final CommandResult result) {
        String locatorId = webVar.getDom().getLocator();
        if (StringUtils.isNotBlank(locatorId)) {
            String valueResult = uiUtil.findWebElement(dependencies, locatorId, webVar.getDom().getLocatorStrategy())
                    .getAttribute("outerHTML");
            resultUtil.addVariableMetaData(ResultUtil.HTML_DOM, webVar.getName(),
                    ResultUtil.LOCATOR_FORM, locatorId, valueResult, result);
            return valueResult;
        }
        String valueResult = dependencies.getDriver().getPageSource();
        resultUtil.addVariableMetaData(ResultUtil.HTML_DOM, webVar.getName(),
                ResultUtil.FULL_DOM, valueResult, result);
        return valueResult;
    }

    private String getWebCookiesResult(final WebVar var, final CommandResult result) {
        Set<Cookie> cookies = dependencies.getDriver().manage().getCookies();
        String valueResult = cookies.stream()
                .map(cookie -> String.join(DelimiterConstant.EQUALS_MARK, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(DelimiterConstant.SEMICOLON));
        resultUtil.addVariableMetaData(ResultUtil.COOKIES, var.getName(),
                ResultUtil.NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getUrlResult(final WebVar var, final CommandResult result) {
        String valueResult = dependencies.getDriver().getCurrentUrl();
        resultUtil.addVariableMetaData(ResultUtil.URL, var.getName(), ResultUtil.NO_EXPRESSION, valueResult, result);
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
}
