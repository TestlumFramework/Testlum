
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for ses complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ses"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="destination" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="source" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="message" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sesMessage"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ses", propOrder = {
    "destination",
    "source",
    "message"
})
public class Ses
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected String destination;
    @XmlElement(required = true)
    protected String source;
    @XmlElement(required = true)
    protected SesMessage message;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the destination property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets the value of the destination property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestination(String value) {
        this.destination = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSource(String value) {
        this.source = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link SesMessage }
     *     
     */
    public SesMessage getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link SesMessage }
     *     
     */
    public void setMessage(SesMessage value) {
        this.message = value;
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

}
