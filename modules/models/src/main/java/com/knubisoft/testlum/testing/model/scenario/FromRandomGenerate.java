
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fromRandomGenerate complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="fromRandomGenerate"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="numeric" type="{http://www.knubisoft.com/testlum/testing/model/scenario}randomNumeric"/&gt;
 *         &lt;element name="alphabetic" type="{http://www.knubisoft.com/testlum/testing/model/scenario}randomAlphabetic"/&gt;
 *         &lt;element name="alphanumeric" type="{http://www.knubisoft.com/testlum/testing/model/scenario}randomAlphanumeric"/&gt;
 *         &lt;element name="randomRegexp" type="{http://www.knubisoft.com/testlum/testing/model/scenario}randomRegexp"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="length" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}generateStringLengthPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "fromRandomGenerate", propOrder = {
    "numeric",
    "alphabetic",
    "alphanumeric",
    "randomRegexp"
})
public class FromRandomGenerate {

    protected RandomNumeric numeric;
    protected RandomAlphabetic alphabetic;
    protected RandomAlphanumeric alphanumeric;
    protected RandomRegexp randomRegexp;
    @XmlAttribute(name = "length", required = true)
    protected int length;

    /**
     * Gets the value of the numeric property.
     * 
     * @return
     *     possible object is
     *     {@link RandomNumeric }
     *     
     */
    public RandomNumeric getNumeric() {
        return numeric;
    }

    /**
     * Sets the value of the numeric property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomNumeric }
     *     
     */
    public void setNumeric(RandomNumeric value) {
        this.numeric = value;
    }

    /**
     * Gets the value of the alphabetic property.
     * 
     * @return
     *     possible object is
     *     {@link RandomAlphabetic }
     *     
     */
    public RandomAlphabetic getAlphabetic() {
        return alphabetic;
    }

    /**
     * Sets the value of the alphabetic property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomAlphabetic }
     *     
     */
    public void setAlphabetic(RandomAlphabetic value) {
        this.alphabetic = value;
    }

    /**
     * Gets the value of the alphanumeric property.
     * 
     * @return
     *     possible object is
     *     {@link RandomAlphanumeric }
     *     
     */
    public RandomAlphanumeric getAlphanumeric() {
        return alphanumeric;
    }

    /**
     * Sets the value of the alphanumeric property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomAlphanumeric }
     *     
     */
    public void setAlphanumeric(RandomAlphanumeric value) {
        this.alphanumeric = value;
    }

    /**
     * Gets the value of the randomRegexp property.
     * 
     * @return
     *     possible object is
     *     {@link RandomRegexp }
     *     
     */
    public RandomRegexp getRandomRegexp() {
        return randomRegexp;
    }

    /**
     * Sets the value of the randomRegexp property.
     * 
     * @param value
     *     allowed object is
     *     {@link RandomRegexp }
     *     
     */
    public void setRandomRegexp(RandomRegexp value) {
        this.randomRegexp = value;
    }

    /**
     * Gets the value of the length property.
     * 
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     */
    public void setLength(int value) {
        this.length = value;
    }

}
