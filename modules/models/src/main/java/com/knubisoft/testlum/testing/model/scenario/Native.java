
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for native complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="native"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}ui"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="click" type="{http://www.knubisoft.com/testlum/testing/model/scenario}click"/&gt;
 *         &lt;element name="input" type="{http://www.knubisoft.com/testlum/testing/model/scenario}input"/&gt;
 *         &lt;element name="assert" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nativeAssert"/&gt;
 *         &lt;element name="wait" type="{http://www.knubisoft.com/testlum/testing/model/scenario}waitNative"/&gt;
 *         &lt;element name="clear" type="{http://www.knubisoft.com/testlum/testing/model/scenario}clear"/&gt;
 *         &lt;element name="image" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nativeImage"/&gt;
 *         &lt;element name="refresh" type="{http://www.knubisoft.com/testlum/testing/model/scenario}refresh"/&gt;
 *         &lt;element name="navigate" type="{http://www.knubisoft.com/testlum/testing/model/scenario}navigateNative"/&gt;
 *         &lt;element name="dragAndDrop" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dragAndDropNative"/&gt;
 *         &lt;element name="swipeNative" type="{http://www.knubisoft.com/testlum/testing/model/scenario}swipeNative"/&gt;
 *         &lt;element name="webView" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webView"/&gt;
 *         &lt;element name="var" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nativeVar"/&gt;
 *         &lt;element name="condition" type="{http://www.knubisoft.com/testlum/testing/model/scenario}uiCondition"/&gt;
 *         &lt;element name="repeat" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nativeRepeat"/&gt;
 *         &lt;element name="ai" type="{http://www.knubisoft.com/testlum/testing/model/scenario}uiAskAi"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
        @XmlElement(name = "swipeNative", type = SwipeNative.class),
        @XmlElement(name = "webView", type = WebView.class),
        @XmlElement(name = "var", type = NativeVar.class),
        @XmlElement(name = "condition", type = UiCondition.class),
        @XmlElement(name = "repeat", type = NativeRepeat.class),
        @XmlElement(name = "ai", type = UiAi.class)
    })
    protected List<AbstractUiCommand> clickOrInputOrAssert;

    /**
     * Gets the value of the clickOrInputOrAssert property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the clickOrInputOrAssert property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClickOrInputOrAssert().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Click }
     * {@link Input }
     * {@link NativeAssert }
     * {@link WaitNative }
     * {@link Clear }
     * {@link NativeImage }
     * {@link Refresh }
     * {@link NavigateNative }
     * {@link DragAndDropNative }
     * {@link SwipeNative }
     * {@link WebView }
     * {@link NativeVar }
     * {@link UiCondition }
     * {@link NativeRepeat }
     * {@link UiAi }
     * 
     * 
     */
    public List<AbstractUiCommand> getClickOrInputOrAssert() {
        if (clickOrInputOrAssert == null) {
            clickOrInputOrAssert = new ArrayList<AbstractUiCommand>();
        }
        return this.clickOrInputOrAssert;
    }

}
