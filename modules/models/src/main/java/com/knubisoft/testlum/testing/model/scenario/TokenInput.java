
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for tokenInput complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tokenInput"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="tokenInputPlace" type="{http://www.knubisoft.com/testlum/testing/model/scenario}tokenInputPlace" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="locatorStrategy" type="{http://www.knubisoft.com/testlum/testing/model/scenario}locatorStrategy" default="locatorId" /&gt;
 *       &lt;attribute name="parentLocator" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="highlight" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="token" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tokenInput", propOrder = {
    "tokenInputPlace"
})
public class TokenInput
    extends AbstractUiCommand
{

    protected List<TokenInputPlace> tokenInputPlace;
    @XmlAttribute(name = "locatorStrategy")
    protected LocatorStrategy locatorStrategy;
    @XmlAttribute(name = "parentLocator", required = true)
    protected String parentLocator;
    @XmlAttribute(name = "highlight")
    protected Boolean highlight;
    @XmlAttribute(name = "token", required = true)
    protected String token;

    /**
     * Gets the value of the tokenInputPlace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tokenInputPlace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTokenInputPlace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TokenInputPlace }
     * 
     * 
     */
    public List<TokenInputPlace> getTokenInputPlace() {
        if (tokenInputPlace == null) {
            tokenInputPlace = new ArrayList<TokenInputPlace>();
        }
        return this.tokenInputPlace;
    }

    /**
     * Gets the value of the locatorStrategy property.
     * 
     * @return
     *     possible object is
     *     {@link LocatorStrategy }
     *     
     */
    public LocatorStrategy getLocatorStrategy() {
        if (locatorStrategy == null) {
            return LocatorStrategy.LOCATOR_ID;
        } else {
            return locatorStrategy;
        }
    }

    /**
     * Sets the value of the locatorStrategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocatorStrategy }
     *     
     */
    public void setLocatorStrategy(LocatorStrategy value) {
        this.locatorStrategy = value;
    }

    /**
     * Gets the value of the parentLocator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParentLocator() {
        return parentLocator;
    }

    /**
     * Sets the value of the parentLocator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParentLocator(String value) {
        this.parentLocator = value;
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
     * Gets the value of the token property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the value of the token property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

}
