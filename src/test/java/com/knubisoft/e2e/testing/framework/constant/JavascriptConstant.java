package com.knubisoft.e2e.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavascriptConstant {
    public static final String CLICK_SCRIPT = "arguments[0].click();";
    public static final String HIGHLIGHT_SCRIPT = "arguments[0].setAttribute('style', "
            + "'background: grey; border: 3px solid yellow;');";
    public static final String SCROLL_VERTICAL_SCRIPT_FORMAT = "window.scrollBy(0, %s)";
    public static final String SCROLL_HORIZONTAL_SCRIPT_FORMAT = "window.scrollBy(%s,0)";
    public static final String SCROLL_TO_ELEMENT_SCRIPT = "arguments[0].scrollIntoView();";
    public static final String SCROLL_TO_TOP_SCRIPT = "window.scrollTo(0, 0);";
    public static final String SCROLL_TO_BOTTOM_SCRIPT = "window.scrollBy(0,document.body.scrollHeight)";
    public static final String SCROLL_VERTICAL_AND_HORIZONTAL_FORMAT = "window.scrollBy(%s, %s)";
    public static final String SCROLL_VERTICAL_PERCENT = "document.body.scrollHeight * %s";
    public static final String SCROLL_HORIZONTAL_PERCENT = "document.body.scrollWidth * %s";
}
