package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "cryptography", propOrder = {
        "secret"
})
public class Cryptography
        extends Integration {

    @XmlElement(required = true)
    protected String secret;

    @XmlAttribute(name = "method", required = true)
    protected CryptoMethods method;

    /**
     * Gets the value of the secret property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the value of the secret property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSecret(String value) {
        this.secret = value;
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
     * Gets the value of the method property.
     *
     * @return
     *     possible object is
     *     {@link CryptoMethods }
     *
     */
    public CryptoMethods getMethod() {
        return method;
    }

    /**
     * Sets the value of the method property.
     *
     * @param value
     *     allowed object is
     *     {@link CryptoMethods }
     *
     */
    public void setMethod(CryptoMethods value) {
        this.method = value;
    }

}
