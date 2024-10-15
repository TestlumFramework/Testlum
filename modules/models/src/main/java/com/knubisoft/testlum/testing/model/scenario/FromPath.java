
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fromPath complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fromPath"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="value" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}xjpathPattern" /&gt;
 *       &lt;attribute name="context" type="{http://www.knubisoft.com/testlum/testing/model/scenario}xjpathPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromPath")
public class FromPath {

    @XmlAttribute(name = "value", required = true)
    protected String value;

    @XmlAttribute(name = "from")
    protected String from;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the from property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     *
     * @param from
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFrom(String from) {
        this.from = from;
    }

}
