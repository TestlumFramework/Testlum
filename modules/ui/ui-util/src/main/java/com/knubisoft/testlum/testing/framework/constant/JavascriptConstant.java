package com.knubisoft.testlum.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavascriptConstant {
    public static final String CLICK_SCRIPT = "arguments[0].click();";
    public static final String ELEMENT_ARGUMENTS_SCRIPT = "return arguments[0].getAttribute(arguments[1])";
    public static final String HIGHLIGHT_SCRIPT = "arguments[0].setAttribute('style', "
            + "'background: grey; border: 3px solid yellow;');";
    public static final String SCROLL_TO_ELEMENT_SCRIPT =
            "arguments[0].scrollIntoView({block: \"center\", inline: \"center\"});";
    public static final String QUERY_FOR_DRAG_AND_DROP = "var target = arguments[0],"
            + "    offsetX = 0,"
            + "    offsetY = 0,"
            + "    document = target.ownerDocument || document,"
            + "    window = document.defaultView || window;"
            + ""
            + "var input = document.createElement('INPUT');"
            + "input.type = 'file';"
            + "input.style.display = 'none';"
            + "input.onchange = function () {"
            + "  var rect = target.getBoundingClientRect(),"
            + "      x = rect.left + (offsetX || (rect.width >> 1)),"
            + "      y = rect.top + (offsetY || (rect.height >> 1)),"
            + "      dataTransfer = { files: this.files };"
            + ""
            + "  ['dragenter', 'dragover', 'drop'].forEach(function (name) {"
            + "    var evt = document.createEvent('MouseEvent');"
            + "    evt.initMouseEvent(name, !0, !0, window, 0, 0, 0, x, y, !1, !1, !1, !1, 0, null);"
            + "    evt.dataTransfer = dataTransfer;"
            + "    target.dispatchEvent(evt);"
            + "  });"
            + ""
            + "  setTimeout(function () { document.body.removeChild(input); }, 25);"
            + "};"
            + "document.body.appendChild(input);"
            + "return input;";
}
