package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.pages.ClassName;
import com.knubisoft.testlum.testing.model.pages.CssSelector;
import com.knubisoft.testlum.testing.model.pages.Id;
import com.knubisoft.testlum.testing.model.pages.Text;
import com.knubisoft.testlum.testing.model.pages.Xpath;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@UtilityClass
public class By {

    private static final String XPATH_TEMPLATE_FOR_TEXT_SEARCH = "//*[contains(text(), '%s')]";
    private static final String XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER = "//*[@placeholder='%s']";
    private static final String XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH = "//*[@class='%s']";

    public List<org.openqa.selenium.By> xpath(final List<Xpath> xpathList) {
        return xpathList.stream()
                .map(xpath -> org.openqa.selenium.By.xpath(xpath.getValue()))
                .collect(Collectors.toList());
    }

    public List<org.openqa.selenium.By> id(final List<Id> idList) {
        return idList.stream()
                .map(id -> org.openqa.selenium.By.id(id.getValue()))
                .collect(Collectors.toList());
    }

    public List<org.openqa.selenium.By> cssSelector(final List<CssSelector> cssSelectorList) {
        return cssSelectorList.stream()
                .map(cssSelector -> org.openqa.selenium.By.cssSelector(cssSelector.getValue()))
                .collect(Collectors.toList());
    }

    public List<org.openqa.selenium.By> className(final List<ClassName> classNameList) {
        return classNameList.stream()
                .map(className -> org.openqa.selenium.By.xpath(
                        format(XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH, className.getValue())
                ))
                .collect(Collectors.toList());
    }

    public static List<org.openqa.selenium.By> text(final List<Text> textList) {
        return textList.stream()
                .map(text -> org.openqa.selenium.By.xpath(text.isPlaceholder()
                        ? format(XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER, text.getValue())
                        : format(XPATH_TEMPLATE_FOR_TEXT_SEARCH, text.getValue())))
                .collect(Collectors.toList());
    }
}
