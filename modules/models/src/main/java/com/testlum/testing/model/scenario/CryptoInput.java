package com.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

/**
 * <p>Java class for cryptoInput complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="cryptoInput"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="encrypt" type="{http://www.testlum.com/testing/model/scenario}encrypt" /&gt;
 *           &lt;element name="decrypt" type="{http://www.testlum.com/testing/model/scenario}decrypt" /&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="highlight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cryptoInput", propOrder = {
        "operation"
})
public class CryptoInput
        extends CommandWithLocator
{

    @XmlElements({
            @XmlElement(name = "encrypt", type = Encrypt.class),
            @XmlElement(name = "decrypt", type = Decrypt.class)
    })
    protected CryptoOperation operation;

    @XmlAttribute(name = "highlight")
    protected Boolean highlight;

    /**
     * Gets the value of the operation property.
     *
     * @return
     *     possible object is
     *     {@link CryptoOperation }
     *
     */
    public CryptoOperation getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     *
     * @param operation
     *     allowed object is
     *     {@link CryptoOperation }
     *
     */
    public void setOperation(CryptoOperation operation) {
        this.operation = operation;
    }

    /**
     * Gets the value of the highlight property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isHighlight() {
        if (highlight == null) {
            return true;
        } else {
            return highlight;
        }
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

    /**
     * Gets action name ("ENCRYPT" or "DECRYPT") based on inner operation.
     *
     * @return action string or null
     */
    public String getAction() {
        return operation != null ? operation.getAction() : null;
    }

}