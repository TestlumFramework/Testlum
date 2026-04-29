package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarMethod;
import com.knubisoft.testlum.testing.framework.variable.util.VariableHelper.VarPredicate;
import com.knubisoft.testlum.testing.model.scenario.ElementAttribute;
import com.knubisoft.testlum.testing.model.scenario.ElementPresent;
import com.knubisoft.testlum.testing.model.scenario.WebVar;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@ExecutorForClass(WebVar.class)
public class WebVariableExecutor extends AbstractVariableExecutor<WebVar> {

    public WebVariableExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected Map<VarPredicate<WebVar>, VarMethod<WebVar>> buildVarToMethodMap() {
        Map<VarPredicate<WebVar>, VarMethod<WebVar>> map = new HashMap<>();
        map.put(var -> Objects.nonNull(var.getElement()), this::getElementResult);
        map.put(var -> Objects.nonNull(var.getDom()), this::getDomResult);
        map.put(var -> Objects.nonNull(var.getCookie()), this::getWebCookiesResult);
        map.put(var -> Objects.nonNull(var.getUrl()), this::getUrlResult);
        map.put(var -> Objects.nonNull(var.getAlert()), this::getAlertResult);
        addCommonVarMethods(map);
        return map;
    }

    private void addCommonVarMethods(final Map<VarPredicate<WebVar>, VarMethod<WebVar>> map) {
        map.put(var -> Objects.nonNull(var.getPath()), (v, r) -> getPathResult(v, r, v.getPath()));
        map.put(var -> Objects.nonNull(var.getConstant()), (v, r) -> getConstantResult(v, r, v.getConstant()));
        map.put(var -> Objects.nonNull(var.getExpression()), (v, r) -> getExpressionResult(v, r, v.getExpression()));
        map.put(var -> Objects.nonNull(var.getFile()), (v, r) -> getFileResult(v, r, v.getFile()));
        map.put(var -> Objects.nonNull(var.getSql()), (v, r) -> getSQLResult(v, r, v.getSql()));
        map.put(var -> Objects.nonNull(var.getGenerate()), (v, r) -> getRandomGenerateResult(v, r, v.getGenerate()));
        map.put(var -> Objects.nonNull(var.getDate()), (v, r) -> getDateResult(v, r, v.getDate()));
    }

    @Override
    protected String getVarName(final WebVar var) {
        return var.getName();
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

    private String getAlertResult(final WebVar var, final CommandResult result) {
        Alert browserAlert = dependencies.getDriver().switchTo().alert();
        return variableHelper.getAlertResult(var.getAlert(), var.getName(), browserAlert, result);
    }
}