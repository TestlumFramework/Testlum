package com.knubisoft.cott.testing.framework.util;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ByText {

    public static By text(String text){
        return new By() {
            @Override
            public List<WebElement> findElements(SearchContext context) {
                String xpath = "//*[text()='" + text + "']";
                List<WebElement> elements = context.findElements(By.xpath(xpath));
                return elements;
            }
        };
    }
}
