
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lambda complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="lambda"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="payload" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}aliasPattern" /&gt;
 *       &lt;attribute name="functionName" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="file" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}expectedPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lambda", propOrder = {
    "payload"
})
public class Lambda
    extends AbstractCommand
{

    protected String payload;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "functionName", required = true)
    protected String functionName;
    @XmlAttribute(name = "file", required = true)
    protected String file;

    /**
     * Gets the value of the payload property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Sets the value of the payload property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPayload(String value) {
        this.payload = value;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    /**
     * Gets the value of the functionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Sets the value of the functionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFunctionName(String value) {
        this.functionName = value;
    }

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFile(String value) {
        this.file = value;
    }

}
