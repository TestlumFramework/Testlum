package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.pages.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ByServiceTest {

    private ByService byService;

    @BeforeEach
    void setUp() {
        byService = new ByService();
    }

    @Test
    void xpathReturnsCorrectByXpath() {
        Xpath xpath = mock(Xpath.class);
        when(xpath.getValue()).thenReturn("//div[@id='test']");

        List<By> result = byService.xpath(List.of(xpath));

        assertEquals(1, result.size());
        assertEquals(By.xpath("//div[@id='test']"), result.get(0));
    }

    @Test
    void xpathWithMultipleElements() {
        Xpath xpath1 = mock(Xpath.class);
        Xpath xpath2 = mock(Xpath.class);
        when(xpath1.getValue()).thenReturn("//div");
        when(xpath2.getValue()).thenReturn("//span");

        List<By> result = byService.xpath(List.of(xpath1, xpath2));

        assertEquals(2, result.size());
        assertEquals(By.xpath("//div"), result.get(0));
        assertEquals(By.xpath("//span"), result.get(1));
    }

    @Test
    void xpathWithEmptyListReturnsEmpty() {
        List<By> result = byService.xpath(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void idReturnsCorrectById() {
        Id id = mock(Id.class);
        when(id.getValue()).thenReturn("myElement");

        List<By> result = byService.id(List.of(id));

        assertEquals(1, result.size());
        assertEquals(By.id("myElement"), result.get(0));
    }

    @Test
    void idWithMultipleElements() {
        Id id1 = mock(Id.class);
        Id id2 = mock(Id.class);
        when(id1.getValue()).thenReturn("first");
        when(id2.getValue()).thenReturn("second");

        List<By> result = byService.id(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(By.id("first"), result.get(0));
        assertEquals(By.id("second"), result.get(1));
    }

    @Test
    void idWithEmptyListReturnsEmpty() {
        List<By> result = byService.id(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void cssSelectorReturnsCorrectByCssSelector() {
        CssSelector css = mock(CssSelector.class);
        when(css.getValue()).thenReturn("div.container > span");

        List<By> result = byService.cssSelector(List.of(css));

        assertEquals(1, result.size());
        assertEquals(By.cssSelector("div.container > span"), result.get(0));
    }

    @Test
    void cssSelectorWithMultipleElements() {
        CssSelector css1 = mock(CssSelector.class);
        CssSelector css2 = mock(CssSelector.class);
        when(css1.getValue()).thenReturn(".class1");
        when(css2.getValue()).thenReturn("#id1");

        List<By> result = byService.cssSelector(List.of(css1, css2));

        assertEquals(2, result.size());
        assertEquals(By.cssSelector(".class1"), result.get(0));
        assertEquals(By.cssSelector("#id1"), result.get(1));
    }

    @Test
    void cssSelectorWithEmptyListReturnsEmpty() {
        List<By> result = byService.cssSelector(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void classNameReturnsXpathWithClassTemplate() {
        ClassName className = mock(ClassName.class);
        when(className.getValue()).thenReturn("my-class");

        List<By> result = byService.className(List.of(className));

        assertEquals(1, result.size());
        assertEquals(By.xpath("//*[@class='my-class']"), result.get(0));
    }

    @Test
    void classNameWithEmptyListReturnsEmpty() {
        List<By> result = byService.className(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void textWithPlaceholderFalseUsesContainsTextTemplate() {
        Text text = mock(Text.class);
        when(text.getValue()).thenReturn("Click me");
        when(text.isPlaceholder()).thenReturn(false);

        List<By> result = byService.text(List.of(text));

        assertEquals(1, result.size());
        assertEquals(By.xpath("//*[contains(text(), 'Click me')]"), result.get(0));
    }

    @Test
    void textWithPlaceholderTrueUsesPlaceholderTemplate() {
        Text text = mock(Text.class);
        when(text.getValue()).thenReturn("Enter name");
        when(text.isPlaceholder()).thenReturn(true);

        List<By> result = byService.text(List.of(text));

        assertEquals(1, result.size());
        assertEquals(By.xpath("//*[@placeholder='Enter name']"), result.get(0));
    }

    @Test
    void textWithEmptyListReturnsEmpty() {
        List<By> result = byService.text(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void textWithMultipleElements() {
        Text text1 = mock(Text.class);
        Text text2 = mock(Text.class);
        when(text1.getValue()).thenReturn("Hello");
        when(text1.isPlaceholder()).thenReturn(false);
        when(text2.getValue()).thenReturn("Search");
        when(text2.isPlaceholder()).thenReturn(true);

        List<By> result = byService.text(List.of(text1, text2));

        assertEquals(2, result.size());
        assertEquals(By.xpath("//*[contains(text(), 'Hello')]"), result.get(0));
        assertEquals(By.xpath("//*[@placeholder='Search']"), result.get(1));
    }
}
