package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.By;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.framework.util.VariableService;
import com.knubisoft.cott.testing.model.scenario.ElementPresent;
import com.knubisoft.cott.testing.model.scenario.WebVar;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.FAILED_VARIABLE_WITH_PATH_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.COOKIES;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ELEMENT_PRESENT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.HTML_DOM;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NO_EXPRESSION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.URL;
import static java.util.Objects.nonNull;


@Slf4j
@ExecutorForClass(WebVar.class)
public class WebVariableExecutor extends AbstractUiExecutor<WebVar> {

    private final Map<WebVarFromPredicate, WebVarFromMethod> webVarToMethodMap;
    @Autowired
    private VariableService variableService;

    public WebVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        Map<WebVarFromPredicate, WebVarFromMethod> webVarMap = new HashMap<>();
        webVarMap.put(var -> nonNull(var.getSql()), this::getSQLResult);
        webVarMap.put(var -> nonNull(var.getFile()), this::getFileResult);
        webVarMap.put(var -> nonNull(var.getExpression()), this::getExpressionResult);
        webVarMap.put(var -> nonNull(var.getPath()), this::getPathResult);
        webVarMap.put(var -> nonNull(var.getCookie()), this::getWebCookiesResult);
        webVarMap.put(var -> nonNull(var.getDom()), this::getDomResult);
        webVarMap.put(var -> nonNull(var.getUrl()), this::getUrlResult);
        webVarMap.put(var -> nonNull(var.getElement()), this::getElementResult);
        webVarToMethodMap = Collections.unmodifiableMap(webVarMap);
    }

    @Override
    public void execute(final WebVar webVar, final CommandResult result) {
        try {
            setContextVariable(webVar, result);
        } catch (Exception e) {
            log.info(FAILED_VARIABLE_WITH_PATH_LOG, webVar.getName(), webVar.getComment());
            throw e;
        }
    }

    private void setContextVariable(final WebVar webVar, final CommandResult result) {
        String value = getValueForContext(webVar, result);
        dependencies.getScenarioContext().set(webVar.getName(), value);
        LogUtil.logVarInfo(webVar.getName(), value);
    }

    private String getValueForContext(final WebVar webVar, final CommandResult result) {
        return webVarToMethodMap.keySet().stream()
                .filter(key -> key.test(webVar))
                .findFirst()
                .map(webVarToMethodMap::get)
                .orElseThrow(() -> new DefaultFrameworkException("Type of 'Var' tag is not supported"))
                .apply(webVar, result);
    }

    private String getElementResult(final WebVar webVar, final CommandResult result) {
        ElementPresent present = webVar.getElement().getPresent();
        try {
            UiUtil.findWebElement(dependencies, inject(present.getLocatorId()));
            ResultUtil.addVariableMetaData(ELEMENT_PRESENT, webVar.getName(), NO_EXPRESSION, "true", result);
            return "true";
        } catch (AssertionError e) {
            ResultUtil.addVariableMetaData(ELEMENT_PRESENT, webVar.getName(), NO_EXPRESSION, "false", result);
            return "false";
        }
    }

    private String getDomResult(final WebVar webVar, final CommandResult result) {
        String xpath = webVar.getDom().getXpath();
        String valueResult = dependencies.getDriver().findElement(By.xpath(xpath)).getText();
        ResultUtil.addVariableMetaData(HTML_DOM, webVar.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getWebCookiesResult(final WebVar webVar, final CommandResult result) {
        Set<Cookie> cookies = dependencies.getDriver().manage().getCookies();
        String valueResult = cookies.stream()
                .map(cookie -> String.join(DelimiterConstant.EQUALS_MARK, cookie.getName(), cookie.getValue()))
                .collect(Collectors.joining(DelimiterConstant.SEMICOLON));
        ResultUtil.addVariableMetaData(COOKIES, webVar.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getUrlResult(final WebVar webVar, final CommandResult result) {
        String valueResult = dependencies.getDriver().getCurrentUrl();
        ResultUtil.addVariableMetaData(URL, webVar.getName(), NO_EXPRESSION, valueResult, result);
        return valueResult;
    }

    private String getPathResult(final WebVar webVar, final CommandResult commandResult) {
        return variableService.getPathResult(webVar.getPath(), webVar.getName(),
                commandResult, dependencies.getScenarioContext());
    }

    private String getExpressionResult(final WebVar webVar, final CommandResult commandResult) {
        return variableService.getExpressionResult(webVar.getExpression(),
                webVar.getName(), commandResult, dependencies.getScenarioContext());
    }

    private String getFileResult(final WebVar webVar, final CommandResult commandResult) {
        return variableService.getFileResult(webVar.getFile(),
                dependencies.getFile(), webVar.getName(), commandResult);
    }

    private String getSQLResult(final WebVar webVar, final CommandResult commandResult) {
        return variableService.getSQLResult(webVar.getSql(),
                webVar.getName(), commandResult, dependencies.getScenarioContext());
    }

    private interface WebVarFromPredicate extends Predicate<WebVar> { }

    private interface WebVarFromMethod extends BiFunction<WebVar, CommandResult, String> { }
}
