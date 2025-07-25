
package com.knubisoft.testlum.testing.model.pages;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * HTML DOM element
 * 
 * <p>Java class for locator complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="locator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="5"&gt;
 *         &lt;element name="xpath" type="{http://www.knubisoft.com/testlum/testing/model/pages}xpath" maxOccurs="unbounded"/&gt;
 *         &lt;element name="id" type="{http://www.knubisoft.com/testlum/testing/model/pages}id" maxOccurs="unbounded"/&gt;
 *         &lt;element name="className" type="{http://www.knubisoft.com/testlum/testing/model/pages}className" maxOccurs="unbounded"/&gt;
 *         &lt;element name="cssSelector" type="{http://www.knubisoft.com/testlum/testing/model/pages}cssSelector" maxOccurs="unbounded"/&gt;
 *         &lt;element name="text" type="{http://www.knubisoft.com/testlum/testing/model/pages}text" maxOccurs="unbounded"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="locatorId" use="required" type="{http://www.knubisoft.com/testlum/testing/model/pages}pagesLocator" /&gt;
 *       &lt;attribute name="comment" type="{http://www.knubisoft.com/testlum/testing/model/pages}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "locator", propOrder = {
    "xpathOrIdOrClassName"
})
public class Locator {

    @XmlElements({
        @XmlElement(name = "xpath", type = Xpath.class),
        @XmlElement(name = "id", type = Id.class),
        @XmlElement(name = "className", type = ClassName.class),
        @XmlElement(name = "cssSelector", type = CssSelector.class),
        @XmlElement(name = "text", type = Text.class)
    })
    protected List<Object> xpathOrIdOrClassName;
    @XmlAttribute(name = "locatorId", required = true)
    protected String locatorId;
    @XmlAttribute(name = "comment")
    protected String comment;

    /**
     * Gets the value of the xpathOrIdOrClassName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xpathOrIdOrClassName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXpathOrIdOrClassName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Xpath }
     * {@link Id }
     * {@link ClassName }
     * {@link CssSelector }
     * {@link Text }
     * 
     * 
     */
    public List<Object> getXpathOrIdOrClassName() {
        if (xpathOrIdOrClassName == null) {
            xpathOrIdOrClassName = new ArrayList<Object>();
        }
        return this.xpathOrIdOrClassName;
    }

    /**
     * Gets the value of the locatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocatorId() {
        return locatorId;
    }

    /**
     * Sets the value of the locatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocatorId(String value) {
        this.locatorId = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
