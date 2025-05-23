package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
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
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.COOKIES;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ELEMENT_ATTRIBUTE;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.ELEMENT_PRESENT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.FULL_DOM;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.HTML_DOM;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LOCATOR_FORM;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.URL;
import static java.util.Objects.nonNull;


@Slf4j
@ExecutorForClass(WebVar.class)
public class WebVariableExecutor extends AbstractUiExecutor<WebVar> {

    private final Map<VarPredicate<WebVar>, VarMethod<WebVar>> varToMethodMap;
    private final VariableHelper variableHelper;

    public WebVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.variableHelper = dependencies.getContext().getBean(VariableHelper.class);
        varToMethodMap = Map.of(
                var -> nonNull(var.getElement()), this::getElementResult,
                var -> nonNull(var.getDom()), this::getDomResult,
                var -> nonNull(var.getCookie()), this::getWebCookiesResult,
                var -> nonNull(var.getUrl()), this::getUrlResult,
                var -> nonNull(var.getPath()), this::getPathResult,
                var -> nonNull(var.getConstant()), this::getConstantResult,
                var -> nonNull(var.getExpression()), this::getExpressionResult,
                var -> nonNull(var.getFile()), this::getFileResult,
                var -> nonNull(var.getSql()), this::getSQLResult,
                var -> nonNull(var.getGenerate()), this::getRandomGenerateResult);
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
        LogUtil.logVarInfo(var.getName(), value);
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
            UiUtil.findWebElement(dependencies, present.getLocator(), present.getLocatorStrategy());
            value = String.valueOf(true);
        } catch (DefaultFrameworkException e) {
            value = String.valueOf(false);
        }
        ResultUtil.addVariableMetaData(ELEMENT_PRESENT, varName, LOCATOR_FORM, present.getLocator(), value, r);
        return value;
    }

    private String getAttributeValue(final ElementAttribute attribute, final String varName, final CommandResult r) {
        WebElement webElement = UiUtil.findWebElement(dependencies, attribute.getLocator(),
                attribute.getLocatorStrategy());
        String value = UiUtil.getElementAttribute(webElement, attribute.getName(), dependencies.getDriver());
        ResultUtil.addVariableMetaData(ELEMENT_ATTRIBUTE, varName, LOCATOR_FORM, attribute.getLocator(), value, r);
        return value;
    }

    private String getDomResult(final WebVar webVar, final CommandResult result) {
        String locatorId = webVar.getDom().getLocator();
        if (StringUtils.isNotBlank(locatorId)) {
            String valueResult = UiUtil.findWebElement(dependencies, locatorId, webVar.getDom().getLocatorStrategy())
                    .getAttribute("outerHTML");
            ResultUtil.addVariableMetaData(HTML_DOM, webVar.getName(), LOCATOR_FORM, locatorId, valueResult, result);
            return valueResult;
        }
        String valueResult = dependencies.getDriver().getPageSource();
        ResultUtil.addVariableMetaData(HTML_DOM, webVar.getName(), FULL_DOM, valueResult, result);
        return valueResult;
    }

    private String getWebCookiesResult(final WebVar var, final CommandResult result) {
        Set<Cookie> cookies = dependencies.getDriver().manage().getCookies();
        String valueResult = cookies.stream()
                .map(cookie -> String.join(DelimiterConstant.EQUALS_MARK, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(DelimiterConstant.SEMICOLON));
        ResultUtil.addVariableMetaData(COOKIES, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getUrlResult(final WebVar var, final CommandResult result) {
        String valueResult = dependencies.getDriver().getCurrentUrl();
        ResultUtil.addVariableMetaData(URL, var.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getPathResult(final WebVar var, final CommandResult result) {
        return variableHelper.getPathResult(var.getPath(), var.getName(), dependencies.getScenarioContext(), result);
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
