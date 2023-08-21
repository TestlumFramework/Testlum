package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.WebVar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.FAILED_VARIABLE_LOG;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.COOKIES;
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
    @Autowired
    private VariableHelper variableHelper;

    public WebVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<VarPredicate<WebVar>, VarMethod<WebVar>> webVarMap = new HashMap<>();
        webVarMap.put(var -> nonNull(var.getElement()), this::getElementResult);
        webVarMap.put(var -> nonNull(var.getDom()), this::getDomResult);
        webVarMap.put(var -> nonNull(var.getCookie()), this::getWebCookiesResult);
        webVarMap.put(var -> nonNull(var.getUrl()), this::getUrlResult);
        webVarMap.put(var -> nonNull(var.getPath()), this::getPathResult);
        webVarMap.put(var -> nonNull(var.getConstant()), this::getConstantResult);
        webVarMap.put(var -> nonNull(var.getExpression()), this::getExpressionResult);
        webVarMap.put(var -> nonNull(var.getFile()), this::getFileResult);
        webVarMap.put(var -> nonNull(var.getSql()), this::getSQLResult);
        webVarMap.put(var -> nonNull(var.getGenerate()), this::getRandomGenerateResult);
        varToMethodMap = Collections.unmodifiableMap(webVarMap);
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
        String valueResult;
        String locatorId = webVar.getElement().getPresent().getLocatorId();
        try {
            uiUtil.findWebElement(dependencies, locatorId);
            valueResult = String.valueOf(true);
        } catch (NoSuchElementException e) {
            valueResult = String.valueOf(false);
        }
        ResultUtil.addVariableMetaData(ELEMENT_PRESENT, webVar.getName(), LOCATOR_FORM, locatorId, valueResult, result);
        return valueResult;
    }

    private String getDomResult(final WebVar webVar, final CommandResult result) {
        String locatorId = webVar.getDom().getLocatorId();
        if (StringUtils.isNotBlank(locatorId)) {
            String valueResult = uiUtil.findWebElement(dependencies, locatorId).getAttribute("outerHTML");
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
        Function<String, String> fileToString = this::getContentIfFile;
        return variableHelper.getFileResult(var.getFile(), var.getName(), fileToString, result);
    }

    private String getSQLResult(final WebVar var, final CommandResult result) {
        return variableHelper.getSQLResult(var.getSql(), var.getName(), result);
    }

    private String getRandomGenerateResult(final WebVar var, final CommandResult result) {
        return variableHelper.getRandomGenerateResult(var.getGenerate(), var.getName(), result);
    }
}
