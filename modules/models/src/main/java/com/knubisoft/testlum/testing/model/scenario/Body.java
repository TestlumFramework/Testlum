
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for body complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="body"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="param" type="{http://www.knubisoft.com/testlum/testing/model/scenario}param" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="multipart" type="{http://www.knubisoft.com/testlum/testing/model/scenario}multipart" minOccurs="0"/&gt;
 *         &lt;element name="from" type="{http://www.knubisoft.com/testlum/testing/model/scenario}file" minOccurs="0"/&gt;
 *         &lt;element name="raw" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "body", propOrder = {
    "param",
    "multipart",
    "from",
    "raw"
})
public class Body {

    protected List<Param> param;
    protected Multipart multipart;
    protected File from;
    protected String raw;

    /**
     * Gets the value of the param property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * 
     * 
     */
    public List<Param> getParam() {
        if (param == null) {
            param = new ArrayList<Param>();
        }
        return this.param;
    }

    /**
     * Gets the value of the multipart property.
     * 
     * @return
     *     possible object is
     *     {@link Multipart }
     *     
     */
    public Multipart getMultipart() {
        return multipart;
    }

    /**
     * Sets the value of the multipart property.
     * 
     * @param value
     *     allowed object is
     *     {@link Multipart }
     *     
     */
    public void setMultipart(Multipart value) {
        this.multipart = value;
    }

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link File }
     *     
     */
    public File getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link File }
     *     
     */
    public void setFrom(File value) {
        this.from = value;
    }

    /**
     * Gets the value of the raw property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRaw() {
        return raw;
    }

    /**
     * Sets the value of the raw property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRaw(String value) {
        this.raw = value;
    }

}
