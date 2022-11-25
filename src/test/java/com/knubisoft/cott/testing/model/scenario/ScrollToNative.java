
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for scrollToNative complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="scrollToNative"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;attribute name="toLocatorId" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "scrollToNative")
public class ScrollToNative
    extends AbstractUiCommand
{

    @XmlAttribute(name = "toLocatorId", required = true)
    protected String toLocatorId;

    /**
     * Gets the value of the toLocatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToLocatorId() {
        return toLocatorId;
    }

    /**
     * Sets the value of the toLocatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToLocatorId(String value) {
        this.toLocatorId = value;
    }

}
