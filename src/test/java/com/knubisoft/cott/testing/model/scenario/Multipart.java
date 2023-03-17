
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for multipart complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="multipart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="part" type="{http://www.knubisoft.com/cott/testing/model/scenario}part" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="boundary" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multipart", propOrder = {
    "part"
})
public class Multipart {

    @XmlElement(required = true)
    protected List<Part> part;
    @XmlAttribute(name = "boundary", required = true)
    protected String boundary;

    /**
     * Gets the value of the part property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the part property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPart().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Part }
     * 
     * 
     */
    public List<Part> getPart() {
        if (part == null) {
            part = new ArrayList<Part>();
        }
        return this.part;
    }

    /**
     * Gets the value of the boundary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBoundary() {
        return boundary;
    }

    /**
     * Sets the value of the boundary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBoundary(String value) {
        this.boundary = value;
    }

}
