package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ByText {

    public static By text(String text){
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                String xpath = "//*[contains(text(), '" + text + "')]";
                List<WebElement> elements = context.findElements(By.xpath(xpath));
                if (elements.size() > 1){
                    throw new DefaultFrameworkException(ExceptionMessage.FOUND_MORE_THEN_ONE_ELEMENT, text);
                }
                return elements;
            }
        };
    }
}
