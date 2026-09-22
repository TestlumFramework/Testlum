
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for report complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="report"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="htmlReport" type="{http://www.testlum.com/testing/model/global-config}htmlReport"/&gt;
 *         &lt;element name="testlumReportServer" type="{http://www.testlum.com/testing/model/global-config}testlumReportServer" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="projectName" use="required" type="{http://www.testlum.com/testing/model/global-config}nonEmptyString" /&gt;
 *       &lt;attribute name="onlyFailedScenarios" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "report", propOrder = {
        "htmlReport",
        "testlumReportServer"
})
public class Report {

    protected HtmlReport htmlReport;
    protected TestlumReportServer testlumReportServer;
    @XmlAttribute(name = "projectName", required = true)
    protected String projectName;
    @XmlAttribute(name = "onlyFailedScenarios")
    protected Boolean onlyFailedScenarios;

    /**
     * Gets the value of the htmlReport property.
     *
     * @return possible object is
     * {@link HtmlReport }
     *
     */
    public HtmlReport getHtmlReport() {
        return htmlReport;
    }

    /**
     * Sets the value of the htmlReport property.
     *
     * @param value allowed object is
     *              {@link HtmlReport }
     *
     */
    public void setHtmlReport(HtmlReport value) {
        this.htmlReport = value;
    }

    /**
     * Gets the value of the testlumReportServer property.
     *
     * @return possible object is
     * {@link TestlumReportServer }
     *
     */
    public TestlumReportServer getTestlumReportServer() {
        return testlumReportServer;
    }

    /**
     * Sets the value of the testlumReportServer property.
     *
     * @param value allowed object is
     *              {@link TestlumReportServer }
     *
     */
    public void setTestlumReportServer(TestlumReportServer value) {
        this.testlumReportServer = value;
    }

    /**
     * Gets the value of the projectName property.
     *
     * @return possible object is
     * {@link String }
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
    public boolean isOnlyFailedScenarios() {
        if (onlyFailedScenarios == null) {
            return false;
        } else {
            return onlyFailedScenarios;
        }
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
