package com.knubisoft.testlum.testing.framework.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavascriptConstantTest {

    @Test
    void clickScriptShouldNotBeNullOrBlank() {
        assertNotNull(JavascriptConstant.CLICK_SCRIPT);
        assertFalse(JavascriptConstant.CLICK_SCRIPT.isBlank());
    }

    @Test
    void clickScriptShouldContainExpectedContent() {
        assertTrue(JavascriptConstant.CLICK_SCRIPT.contains("arguments[0].click"));
    }

    @Test
    void elementArgumentsScriptShouldNotBeNullOrBlank() {
        assertNotNull(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT);
        assertFalse(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT.isBlank());
    }

    @Test
    void elementArgumentsScriptShouldContainExpectedContent() {
        assertTrue(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT.contains("arguments[0]"));
        assertTrue(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT.contains("arguments[1]"));
        assertTrue(JavascriptConstant.ELEMENT_ARGUMENTS_SCRIPT.contains("getAttribute"));
    }

    @Test
    void highlightScriptShouldNotBeNullOrBlank() {
        assertNotNull(JavascriptConstant.HIGHLIGHT_SCRIPT);
        assertFalse(JavascriptConstant.HIGHLIGHT_SCRIPT.isBlank());
    }

    @Test
    void highlightScriptShouldContainExpectedContent() {
        assertTrue(JavascriptConstant.HIGHLIGHT_SCRIPT.contains("arguments[0]"));
        assertTrue(JavascriptConstant.HIGHLIGHT_SCRIPT.contains("setAttribute"));
        assertTrue(JavascriptConstant.HIGHLIGHT_SCRIPT.contains("style"));
    }

    @Test
    void scrollToElementScriptShouldNotBeNullOrBlank() {
        assertNotNull(JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT);
        assertFalse(JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT.isBlank());
    }

    @Test
    void scrollToElementScriptShouldContainExpectedContent() {
        assertTrue(JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT.contains("arguments[0]"));
        assertTrue(JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT.contains("scrollIntoView"));
    }

    @Test
    void queryForDragAndDropShouldNotBeNullOrBlank() {
        assertNotNull(JavascriptConstant.QUERY_FOR_DRAG_AND_DROP);
        assertFalse(JavascriptConstant.QUERY_FOR_DRAG_AND_DROP.isBlank());
    }

    @Test
    void queryForDragAndDropShouldContainDragAndDropEvents() {
        assertTrue(JavascriptConstant.QUERY_FOR_DRAG_AND_DROP.contains("dragenter"));
        assertTrue(JavascriptConstant.QUERY_FOR_DRAG_AND_DROP.contains("dragover"));
        assertTrue(JavascriptConstant.QUERY_FOR_DRAG_AND_DROP.contains("drop"));
    }
}
