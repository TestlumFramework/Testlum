
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
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
 *       &lt;choice&gt;
 *         &lt;element name="extentReports" type="{http://www.knubisoft.com/testlum/testing/model/global-config}extentReports"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "report", propOrder = {
    "extentReports"
})
public class Report {

    protected ExtentReports extentReports;

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

}
