package com.knubisoft.cott.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavascriptConstant {
    public static final String CLICK_SCRIPT = "arguments[0].click();";
    public static final String HIGHLIGHT_SCRIPT = "arguments[0].setAttribute('style', "
            + "'background: grey; border: 3px solid yellow;');";
    public static final String PAGE_SCROLL_VERTICAL_SCRIPT_FORMAT = "window.scrollBy(0, %s)";
    public static final String INNER_SCROLL_VERTICAL_SCRIPT_BY_CSS_SELECTOR =
            "document.querySelector('%s').scrollBy(0, %s)";
    public static final String INNER_SCROLL_VERTICAL_SCRIPT_BY_ID = "document.getElementById('%s').scrollBy(0, %s)";
    public static final String INNER_SCROLL_VERTICAL_SCRIPT_BY_CLASS =
            "document.getElementsByClassName('%s').scrollBy(0, %s)";
    public static final String INNER_SCROLL_VERTICAL_SCRIPT_BY_XPATH =
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollBy(0, %s)";
    public static final String SCROLL_TO_ELEMENT_SCRIPT =
            "arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});";
    public static final String PAGE_SCROLL_VERTICAL_PERCENT = "document.body.scrollHeight * %s";
    public static final String INNER_SCROLL_VERTICAL_PERCENT_BY_CSS_SELECTOR =
            "document.querySelector('%s').scrollHeight * %s";
    public static final String INNER_SCROLL_VERTICAL_PERCENT_BY_ID = "document.getElementById('%s').scrollHeight * %s";
    public static final String INNER_SCROLL_VERTICAL_PERCENT_BY_CLASS =
            "document.getElementsByClassName('%s').scrollHeight * %s";
    public static final String INNER_SCROLL_VERTICAL_PERCENT_BY_XPATH =
            "document.evaluate('%s', document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null)"
                    + ".singleNodeValue.scrollHeight * %s";
}
