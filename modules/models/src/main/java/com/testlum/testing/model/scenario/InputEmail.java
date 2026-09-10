
package com.testlum.testing.model.scenario;

import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for inputEmail complex type&lt;/p&gt;.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="inputEmail"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}commandWithLocator"&gt;
 *       &lt;attribute name="alias" type="{http://www.testlum.com/testing/model/scenario}aliasPattern" default="DEFAULT" /&gt;
 *       &lt;attribute name="pattern" use="required" type="{http://www.testlum.com/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="timeout" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" default="10000" /&gt;
 *       &lt;attribute name="highlight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="targetVariable" type="{http://www.testlum.com/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inputEmail")
public class InputEmail
    extends CommandWithLocator
{

    @XmlAttribute(name = "alias")
    protected String alias;
    @XmlAttribute(name = "pattern", required = true)
    protected String pattern;
    @XmlAttribute(name = "timeout")
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger timeout;
    @XmlAttribute(name = "highlight")
    protected Boolean highlight;
    @XmlAttribute(name = "targetVariable")
    protected String targetVariable;

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        if (alias == null) {
            return "DEFAULT";
        } else {
            return alias;
        }
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
     * Gets the value of the pattern property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the value of the pattern property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPattern(String value) {
        this.pattern = value;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimeout() {
        if (timeout == null) {
            return new BigInteger("10000");
        } else {
            return timeout;
        }
    }

    /**
     * Sets the value of the timeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimeout(BigInteger value) {
        this.timeout = value;
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
     * Gets the value of the targetVariable property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetVariable() {
        return targetVariable;
    }

    /**
     * Sets the value of the targetVariable property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetVariable(String value) {
        this.targetVariable = value;
    }

}
