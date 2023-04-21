
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
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
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="param" type="{http://www.knubisoft.com/testlum/testing/model/scenario}param" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="from" type="{http://www.knubisoft.com/testlum/testing/model/scenario}partFile" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="boundary" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "multipart", propOrder = {
    "paramOrFrom"
})
public class Multipart {

    @XmlElements({
        @XmlElement(name = "param", type = Param.class),
        @XmlElement(name = "from", type = PartFile.class)
    })
    protected List<Object> paramOrFrom;
    @XmlAttribute(name = "boundary")
    protected String boundary;

    /**
     * Gets the value of the paramOrFrom property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the paramOrFrom property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParamOrFrom().add(newItem);
     * </pre>
     * 
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * {@link PartFile }
     * 
     *
     */
    public List<Object> getParamOrFrom() {
        if (paramOrFrom == null) {
            paramOrFrom = new ArrayList<Object>();
        }
        return this.paramOrFrom;
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
