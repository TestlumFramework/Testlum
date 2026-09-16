
package com.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for native complex type&lt;/p&gt;.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="native"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}ui"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="click" type="{http://www.testlum.com/testing/model/scenario}click"/&gt;
 *         &lt;element name="input" type="{http://www.testlum.com/testing/model/scenario}input"/&gt;
 *         &lt;element name="assert" type="{http://www.testlum.com/testing/model/scenario}nativeAssert"/&gt;
 *         &lt;element name="wait" type="{http://www.testlum.com/testing/model/scenario}waitNative"/&gt;
 *         &lt;element name="clear" type="{http://www.testlum.com/testing/model/scenario}clear"/&gt;
 *         &lt;element name="image" type="{http://www.testlum.com/testing/model/scenario}nativeImage"/&gt;
 *         &lt;element name="refresh" type="{http://www.testlum.com/testing/model/scenario}refresh"/&gt;
 *         &lt;element name="navigate" type="{http://www.testlum.com/testing/model/scenario}navigateNative"/&gt;
 *         &lt;element name="dragAndDrop" type="{http://www.testlum.com/testing/model/scenario}dragAndDropNative"/&gt;
 *         &lt;element name="swipe" type="{http://www.testlum.com/testing/model/scenario}swipeNative"/&gt;
 *         &lt;element name="webView" type="{http://www.testlum.com/testing/model/scenario}webView"/&gt;
 *         &lt;element name="var" type="{http://www.testlum.com/testing/model/scenario}nativeVar"/&gt;
 *         &lt;element name="condition" type="{http://www.testlum.com/testing/model/scenario}uiCondition"/&gt;
 *         &lt;element name="repeat" type="{http://www.testlum.com/testing/model/scenario}nativeRepeat"/&gt;
 *         &lt;element name="ai" type="{http://www.testlum.com/testing/model/scenario}uiAi"/&gt;
 *         &lt;element name="email" type="{http://www.testlum.com/testing/model/scenario}email"/&gt;
 *         &lt;element name="inputEmail" type="{http://www.testlum.com/testing/model/scenario}inputEmail"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "native", propOrder = {
    "clickOrInputOrAssert"
})
public class Native
    extends Ui
{

    @XmlElements({
        @XmlElement(name = "click", type = Click.class),
        @XmlElement(name = "input", type = Input.class),
        @XmlElement(name = "assert", type = NativeAssert.class),
        @XmlElement(name = "wait", type = WaitNative.class),
        @XmlElement(name = "clear", type = Clear.class),
        @XmlElement(name = "image", type = NativeImage.class),
        @XmlElement(name = "refresh", type = Refresh.class),
        @XmlElement(name = "navigate", type = NavigateNative.class),
        @XmlElement(name = "dragAndDrop", type = DragAndDropNative.class),
        @XmlElement(name = "swipe", type = SwipeNative.class),
        @XmlElement(name = "webView", type = WebView.class),
        @XmlElement(name = "var", type = NativeVar.class),
        @XmlElement(name = "condition", type = UiCondition.class),
        @XmlElement(name = "repeat", type = NativeRepeat.class),
        @XmlElement(name = "ai", type = UiAi.class),
        @XmlElement(name = "email", type = Email.class),
        @XmlElement(name = "inputEmail", type = InputEmail.class)
    })
    protected List<AbstractUiCommand> clickOrInputOrAssert;

    /**
     * Gets the value of the clickOrInputOrAssert property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the clickOrInputOrAssert property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getClickOrInputOrAssert().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Clear }
     * {@link Click }
     * {@link DragAndDropNative }
     * {@link Email }
     * {@link Input }
     * {@link InputEmail }
     * {@link NativeAssert }
     * {@link NativeImage }
     * {@link NativeRepeat }
     * {@link NativeVar }
     * {@link NavigateNative }
     * {@link Refresh }
     * {@link SwipeNative }
     * {@link UiAi }
     * {@link UiCondition }
     * {@link WaitNative }
     * {@link WebView }
     * </p>
     * 
     * 
     * @return
     *     The value of the clickOrInputOrAssert property.
     */
    public List<AbstractUiCommand> getClickOrInputOrAssert() {
        if (clickOrInputOrAssert == null) {
            clickOrInputOrAssert = new ArrayList<>();
        }
        return this.clickOrInputOrAssert;
    }

}
