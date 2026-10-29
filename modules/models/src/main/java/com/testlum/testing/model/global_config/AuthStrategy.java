
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for authStrategy complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="authStrategy"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authStrategy")
@XmlSeeAlso({
    JwtAuthStrategy.class,
    BasicAuthStrategy.class,
    CustomAuthStrategy.class
})
public abstract class AuthStrategy {

    @XmlAttribute(name = "alias", required = true)
    protected String alias;

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
