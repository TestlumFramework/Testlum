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

    public List<org.openqa.selenium.By> className(final List<ClassName> className) {
        return Collections.singletonList(new org.openqa.selenium.By() {
            @Override
            public List<WebElement> findElements(final SearchContext context) {
                List<String> xpathForSearch = className.stream()
                        .map(c -> format(XPATH_TEMPLATE_FOR_CLASS_NAME_SEARCH, c))
                        .collect(Collectors.toList());
                return findElementsByCustomXpath(transformToXpathList(xpathForSearch), context);
            }
        });
    }

    public static List<org.openqa.selenium.By> text(final List<Text> textList) {
        return Collections.singletonList(new org.openqa.selenium.By() {
            @Override
            public List<WebElement> findElements(final SearchContext context) {
                List<String> xpathForSearch = textList.stream()
                        .map(text -> text.isPlaceholder()
                                ? format(XPATH_TEMPLATE_FOR_TEXT_SEARCH_FROM_PLACEHOLDER, text.getValue())
                                : format(XPATH_TEMPLATE_FOR_TEXT_SEARCH, text.getValue()))
                        .collect(Collectors.toList());
                return findElementsByCustomXpath(transformToXpathList(xpathForSearch), context);
            }
        });
    }

    /*TESTLUM restricts the user and prevents the use of locator
        that is present in more than one element. In this case the exception is thrown.*/

    private List<WebElement> findElementsByCustomXpath(final List<Xpath> xpath,
                                                       final SearchContext context) {
        List<org.openqa.selenium.By> byXpath = By.xpath(xpath);
        return byXpath.stream()
                .map(context::findElement)
                .collect(Collectors.toList());
//        if (elements.size() > 1) {
//            throw new DefaultFrameworkException(ExceptionMessage.FOUND_MORE_THEN_ONE_ELEMENT, locator);
//        }
//        return elements;
    }

    private List<Xpath> transformToXpathList(final List<String> list) {
        List<Xpath> xpathList = new ArrayList<>();
        for (String locator : list) {
            Xpath xpath = new Xpath();
            xpath.setValue(locator);
            xpathList.add(xpath);
        }
        return xpathList;
    }
}
