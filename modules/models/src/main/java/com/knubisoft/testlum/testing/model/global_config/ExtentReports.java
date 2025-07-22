
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for extentReports complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="extentReports"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="htmlReportGenerator" type="{http://www.knubisoft.com/testlum/testing/model/global-config}htmlReportGenerator"/&gt;
 *         &lt;element name="klovServerReportGenerator" type="{http://www.knubisoft.com/testlum/testing/model/global-config}klovServerReportGenerator"/&gt;
 *         &lt;element name="testRailsReports" type="{http://www.knubisoft.com/testlum/testing/model/global-config}testRailsReports"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="projectName" use="required" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="onlyFailedScenarios" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "extentReports", propOrder = {
    "htmlReportGenerator",
    "klovServerReportGenerator",
    "testRailsReports"
})
public class ExtentReports {

    protected HtmlReportGenerator htmlReportGenerator;
    protected KlovServerReportGenerator klovServerReportGenerator;
    protected TestRailsReports testRailsReports;
    @XmlAttribute(name = "projectName", required = true)
    protected String projectName;
    @XmlAttribute(name = "onlyFailedScenarios")
    protected Boolean onlyFailedScenarios;

    /**
     * Gets the value of the htmlReportGenerator property.
     * 
     * @return
     *     possible object is
     *     {@link HtmlReportGenerator }
     *     
     */
    public HtmlReportGenerator getHtmlReportGenerator() {
        return htmlReportGenerator;
    }

    /**
     * Sets the value of the htmlReportGenerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link HtmlReportGenerator }
     *     
     */
    public void setHtmlReportGenerator(HtmlReportGenerator value) {
        this.htmlReportGenerator = value;
    }

    /**
     * Gets the value of the klovServerReportGenerator property.
     * 
     * @return
     *     possible object is
     *     {@link KlovServerReportGenerator }
     *     
     */
    public KlovServerReportGenerator getKlovServerReportGenerator() {
        return klovServerReportGenerator;
    }

    /**
     * Sets the value of the klovServerReportGenerator property.
     * 
     * @param value
     *     allowed object is
     *     {@link KlovServerReportGenerator }
     *     
     */
    public void setKlovServerReportGenerator(KlovServerReportGenerator value) {
        this.klovServerReportGenerator = value;
    }

    /**
     * Gets the value of the testRailsReports property.
     * 
     * @return
     *     possible object is
     *     {@link TestRailsReports }
     *     
     */
    public TestRailsReports getTestRailsReports() {
        return testRailsReports;
    }

    /**
     * Sets the value of the testRailsReports property.
     * 
     * @param value
     *     allowed object is
     *     {@link TestRailsReports }
     *     
     */
    public void setTestRailsReports(TestRailsReports value) {
        this.testRailsReports = value;
    }

    /**
     * Gets the value of the projectName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the value of the projectName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProjectName(String value) {
        this.projectName = value;
    }

    /**
     * Gets the value of the onlyFailedScenarios property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOnlyFailedScenarios() {
        return onlyFailedScenarios;
    }

    /**
     * Sets the value of the onlyFailedScenarios property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnlyFailedScenarios(Boolean value) {
        this.onlyFailedScenarios = value;
    }

}
