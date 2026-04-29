package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.pages.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ByService {

    private static final String XPATH_TEMPLATE_FOR_TEXT_SEARCH = "//*[contains(text(), '%s')]";
    private static final String XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER = "//*[@placeholder='%s']";
    private static final String XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH = "//*[@class='%s']";

    public List<org.openqa.selenium.By> xpath(final List<Xpath> xpathList) {
        return xpathList.stream()
                .map(xpath -> org.openqa.selenium.By.xpath(xpath.getValue()))
                .toList();
    }

    public List<org.openqa.selenium.By> id(final List<Id> idList) {
        return idList.stream()
                .map(id -> org.openqa.selenium.By.id(id.getValue()))
                .toList();
    }

    public List<org.openqa.selenium.By> cssSelector(final List<CssSelector> cssSelectorList) {
        return cssSelectorList.stream()
                .map(cssSelector -> org.openqa.selenium.By.cssSelector(cssSelector.getValue()))
                .toList();
    }

    public List<org.openqa.selenium.By> className(final List<ClassName> classNameList) {
        return classNameList.stream()
                .map(className -> org.openqa.selenium.By.xpath(
                        String.format(XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH, className.getValue())
                ))
                .toList();
    }

    public List<org.openqa.selenium.By> text(final List<Text> textList) {
        return textList.stream()
                .map(text -> org.openqa.selenium.By.xpath(text.isPlaceholder()
                        ? String.format(XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER, text.getValue())
                        : String.format(XPATH_TEMPLATE_FOR_TEXT_SEARCH, text.getValue())))
                .toList();
    }
}
