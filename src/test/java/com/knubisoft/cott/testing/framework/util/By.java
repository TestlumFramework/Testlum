package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;


import java.util.List;

@UtilityClass
public class By {

    private static final String XPATH_WITH_TEXT_METHOD = "//*[contains(text(), '%s')]";

    public static org.openqa.selenium.By text(final String text) {
        return new org.openqa.selenium.By() {
            @Override
            public List<WebElement> findElements(final SearchContext context) {
                String xpath = String.format(XPATH_WITH_TEXT_METHOD, text);
                List<WebElement> elements = context.findElements(By.xpath(xpath));
                if (elements.size() > 1) {
                    throw new DefaultFrameworkException(ExceptionMessage.FOUND_MORE_THEN_ONE_ELEMENT, text);
                }
                return elements;
            }
        };
    }
    public org.openqa.selenium.By xpath(final String xpath) {
        return org.openqa.selenium.By.xpath(xpath);
    }

    public org.openqa.selenium.By id(final String id) {
        return org.openqa.selenium.By.id(id);
    }

    public org.openqa.selenium.By linkText(final String linkText) {
        return org.openqa.selenium.By.linkText(linkText);
    }

    public org.openqa.selenium.By partialLinkText(final String partialLinkText) {
        return org.openqa.selenium.By.partialLinkText(partialLinkText);
    }

    public org.openqa.selenium.By name(final String name) {
        return org.openqa.selenium.By.name(name);
    }

    public org.openqa.selenium.By tagName(final String tagName) {
        return org.openqa.selenium.By.tagName(tagName);
    }

    public org.openqa.selenium.By className(final String className) {
        return org.openqa.selenium.By.className(className);
    }

    public org.openqa.selenium.By cssSelector(final String cssSelector) {
        return org.openqa.selenium.By.cssSelector(cssSelector);
    }
}
