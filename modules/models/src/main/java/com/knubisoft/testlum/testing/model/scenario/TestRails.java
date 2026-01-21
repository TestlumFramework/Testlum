
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for testRails complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="testRails"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="enable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="testRailRunId" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="testCaseId" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testRails")
public class TestRails {

    @XmlAttribute(name = "enable")
    protected Boolean enable;
    @XmlAttribute(name = "testRailRunId")
    protected String testRailRunId;
    @XmlAttribute(name = "testCaseId", required = true)
    protected String testCaseId;

    /**
     * Gets the value of the enable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isEnable() {
        if (enable == null) {
            return false;
        } else {
            return enable;
        }
    }

    /**
     * Sets the value of the enable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnable(Boolean value) {
        this.enable = value;
    }

    /**
     * Gets the value of the testRailRunId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTestRailRunId() {
        return testRailRunId;
    }

    /**
     * Sets the value of the testRailRunId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTestRailRunId(String value) {
        this.testRailRunId = value;
    }

    /**
     * Gets the value of the testCaseId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTestCaseId() {
        return testCaseId;
    }

    /**
     * Sets the value of the testCaseId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTestCaseId(String value) {
        this.testCaseId = value;
    }

}
