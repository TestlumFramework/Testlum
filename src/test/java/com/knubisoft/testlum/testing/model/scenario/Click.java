
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for click complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="click"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="method" type="{http://www.knubisoft.com/testlum/testing/model/scenario}clickMethod" /&gt;
 *       &lt;attribute name="highlight" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "click")
public class Click
    extends CommandWithLocator
{

    @XmlAttribute(name = "method")
    protected ClickMethod method;
    @XmlAttribute(name = "highlight")
    protected Boolean highlight;

    /**
     * Gets the value of the method property.
     * 
     * @return
     *     possible object is
     *     {@link ClickMethod }
     *     
     */
    public ClickMethod getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClickMethod }
     *     
     */
    public void setMethod(ClickMethod value) {
        this.method = value;
    }

    /**
     * Gets the value of the highlight property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHighlight() {
        return highlight;
    }

    /**
     * Sets the value of the highlight property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHighlight(Boolean value) {
        this.highlight = value;
    }

}
