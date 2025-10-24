
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for swipeNative complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="swipeNative"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="element" type="{http://www.knubisoft.com/testlum/testing/model/scenario}swipeElement"/&gt;
 *         &lt;element name="page" type="{http://www.knubisoft.com/testlum/testing/model/scenario}swipePage"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "swipeNative", propOrder = {
    "element",
    "page"
})
public class SwipeNative
    extends AbstractUiCommand
{

    protected SwipeElement element;
    protected SwipePage page;

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link SwipeElement }
     *     
     */
    public SwipeElement getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwipeElement }
     *     
     */
    public void setElement(SwipeElement value) {
        this.element = value;
    }

    /**
     * Gets the value of the page property.
     * 
     * @return
     *     possible object is
     *     {@link SwipePage }
     *     
     */
    public SwipePage getPage() {
        return page;
    }

    /**
     * Sets the value of the page property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwipePage }
     *     
     */
    public void setPage(SwipePage value) {
        this.page = value;
    }

}
