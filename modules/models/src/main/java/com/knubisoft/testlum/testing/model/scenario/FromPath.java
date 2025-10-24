
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


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

    @XmlAttribute(name = "fromFile")
    protected String fromFile;

    @XmlAttribute(name = "fromVar")
    protected String fromVar;

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
     * Gets the value of the fromFile property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFromFile() {
        return fromFile;
    }

    /**
     * Sets the value of the fromFile property.
     *
     * @param fromFile
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFromFile(String fromFile) {
        this.fromFile = fromFile;
    }

    /**
     * Gets the value of the fromVar property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getFromVar() {
        return fromVar;
    }

    /**
     * Sets the value of the fromVar property.
     *
     * @param fromVar
     *     allowed object is
     *     {@link String }
     *
     */
    public void setFromVar(String fromVar) {
        this.fromVar = fromVar;
    }

}
