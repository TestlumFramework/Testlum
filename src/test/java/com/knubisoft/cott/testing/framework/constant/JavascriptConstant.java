package com.knubisoft.cott.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavascriptConstant {
    public static final String CLICK_SCRIPT = "arguments[0].click();";
    public static final String HIGHLIGHT_SCRIPT = "arguments[0].setAttribute('style', "
            + "'background: grey; border: 3px solid yellow;');";
    public static final String SCROLL_TO_ELEMENT_SCRIPT =
            "arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});";
}
