
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for report complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="report"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="extentReports" type="{http://www.testlum.com/testing/model/global-config}extentReports"/&gt;
 *         &lt;element name="junit" type="{http://www.testlum.com/testing/model/global-config}junitReport" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "report", propOrder = {
    "extentReports",
    "junit"
})
public class Report {

    @XmlElement(required = true)
    protected ExtentReports extentReports;
    protected JunitReport junit;

    /**
     * Gets the value of the extentReports property.
     * 
     * @return
     *     possible object is
     *     {@link ExtentReports }
     *     
     */
    public ExtentReports getExtentReports() {
        return extentReports;
    }

    /**
     * Sets the value of the extentReports property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtentReports }
     *     
     */
    public void setExtentReports(ExtentReports value) {
        this.extentReports = value;
    }

    /**
     * Gets the value of the junit property.
     * 
     * @return
     *     possible object is
     *     {@link JunitReport }
     *     
     */
    public JunitReport getJunit() {
        return junit;
    }

    /**
     * Sets the value of the junit property.
     * 
     * @param value
     *     allowed object is
     *     {@link JunitReport }
     *     
     */
    public void setJunit(JunitReport value) {
        this.junit = value;
    }

}
