package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.pages.Text;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

import static java.lang.String.format;

@UtilityClass
public class By {

    private static final String XPATH_TEMPLATE_FOR_TEXT_SEARCH = "//*[contains(text(), '%s')]";
    private static final String XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER = "//*[@placeholder='%s']";
    private static final String XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH = "//*[@class='%s']";

    public org.openqa.selenium.By xpath(final String xpath) {
        return org.openqa.selenium.By.xpath(xpath);
    }

    public org.openqa.selenium.By id(final String id) {
        return org.openqa.selenium.By.id(id);
    }

    public org.openqa.selenium.By cssSelector(final String cssSelector) {
        return org.openqa.selenium.By.cssSelector(cssSelector);
    }

    public org.openqa.selenium.By className(final String className) {
        return new org.openqa.selenium.By() {
            @Override
            public List<WebElement> findElements(final SearchContext context) {
                String xpathForSearch = format(XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH, className);
                return findElementsByCustomXpath(xpathForSearch, context, className);
            }
        };
    }

    public static org.openqa.selenium.By text(final Text text) {
        return new org.openqa.selenium.By() {
            @Override
            public List<WebElement> findElements(final SearchContext context) {
                String textValue = text.getValue();
                String xpathForSearch = text.isPlaceholder()
                        ? format(XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER, textValue)
                        : format(XPATH_TEMPLATE_FOR_TEXT_SEARCH, textValue);
                return findElementsByCustomXpath(xpathForSearch, context, textValue);
            }
        };
    }

    /*TESTLUM restricts the user and prevents the use of locator
        that is present in more than one element. In this case the exception is thrown.*/

    private List<WebElement> findElementsByCustomXpath(final String xpath,
                                                       final SearchContext context,
                                                       final String locator) {
        List<WebElement> elements = context.findElements(By.xpath(xpath));
        if (elements.size() > 1) {
            throw new DefaultFrameworkException(ExceptionMessage.FOUND_MORE_THEN_ONE_ELEMENT, locator);
        }
        return elements;
    }
}
