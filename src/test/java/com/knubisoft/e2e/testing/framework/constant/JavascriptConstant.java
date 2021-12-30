package com.knubisoft.e2e.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavascriptConstant {
    public static final String CLICK_SCRIPT = "arguments[0].click();";
    public static final String HIGHLIGHT_SCRIPT = "arguments[0].setAttribute('style', "
            + "'background: grey; border: 3px solid yellow;');";
}
