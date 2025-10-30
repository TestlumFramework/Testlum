
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for overview complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="overview"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="description" type="{http://www.knubisoft.com/testlum/testing/model/scenario}stringMin10"/&gt;
 *         &lt;element name="name" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="jira" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="developer" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="link" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="testRails" type="{http://www.knubisoft.com/testlum/testing/model/scenario}testRails" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "overview", propOrder = {

})
public class Overview {

    @XmlElement(required = true)
    protected String description;
    @XmlElement(required = true)
    protected String name;
    protected String jira;
    protected String developer;
    protected String link;
    protected TestRails testRails;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the jira property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJira() {
        return jira;
    }

    /**
     * Sets the value of the jira property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJira(String value) {
        this.jira = value;
    }

    /**
     * Gets the value of the developer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeveloper() {
        return developer;
    }

    /**
     * Sets the value of the developer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeveloper(String value) {
        this.developer = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }

    /**
     * Gets the value of the testRails property.
     * 
     * @return
     *     possible object is
     *     {@link TestRails }
     *     
     */
    public TestRails getTestRails() {
        return testRails;
    }

    /**
     * Sets the value of the testRails property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestRails }
     *     
     */
    public void setTestRails(TestRails value) {
        this.testRails = value;
    }

}
