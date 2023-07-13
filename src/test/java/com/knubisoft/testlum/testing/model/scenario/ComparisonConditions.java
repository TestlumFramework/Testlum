
package com.knubisoft.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for comparisonConditions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="comparisonConditions"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="exclude" type="{http://www.knubisoft.com/testlum/testing/model/scenario}exclude"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="percentage" type="{http://www.knubisoft.com/testlum/testing/model/scenario}positiveDoubleMin0Max100" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "comparisonConditions", propOrder = {
    "exclude"
})
@XmlSeeAlso({
    CompareWithFullScreen.class,
    CompareWithPart.class,
    FindPart.class
})
public class ComparisonConditions {

    protected List<Exclude> exclude;
    @XmlAttribute(name = "percentage")
    protected Double percentage;

    /**
     * Gets the value of the exclude property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exclude property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Exclude }
     * 
     * 
     */
    public List<Exclude> getExclude() {
        if (exclude == null) {
            exclude = new ArrayList<Exclude>();
        }
        return this.exclude;
    }

    /**
     * Gets the value of the percentage property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getPercentage() {
        return percentage;
    }

    /**
     * Sets the value of the percentage property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setPercentage(Double value) {
        this.percentage = value;
    }

}
