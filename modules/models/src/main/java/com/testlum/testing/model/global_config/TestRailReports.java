package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;

/**
 * <p>Java class for testRailReports complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="testRailReports"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="username" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="apiKey" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="url" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="projectId" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="caseMatchKey" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="defaultRunName" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="defaultRunDescription" type="{http://www.knubisoft.com/testlum/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" use="required"/&gt;
 *       &lt;attribute name="addScreenshotForFailure" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testRailReports", propOrder = {
        "username",
        "apiKey",
        "url",
        "projectId",
        "caseMatchKey",
        "defaultRunName",
        "defaultRunDescription"
})
public class TestRailReports {

    @XmlElement(required = true)
    protected String username;
    @XmlElement(required = true)
    protected String apiKey;
    @XmlElement(required = true)
    protected String url;
    @XmlElement(required = true)
    protected String projectId;
    protected String caseMatchKey;
    protected String defaultRunName;
    protected String defaultRunDescription;
    @XmlAttribute(name = "enabled", required = true)
    protected Boolean enabled;
    @XmlAttribute(name = "addScreenshotForFailure")
    protected Boolean addScreenshotForFailure;

    /**
     * Gets the value of the username property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the apiKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the value of the apiKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setApiKey(String value) {
        this.apiKey = value;
    }

    /**
     * Gets the value of the url property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUrl(String value) {
        this.url = value;
    }

    /**
     * Gets the value of the projectId property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Sets the value of the projectId property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setProjectId(String value) {
        this.projectId = value;
    }

    /**
     * Gets the value of the caseMatchKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getCaseMatchKey() {
        return caseMatchKey;
    }

    /**
     * Sets the value of the caseMatchKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setCaseMatchKey(String value) {
        this.caseMatchKey = value;
    }

    /**
     * Gets the value of the defaultRunName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultRunName() {
        return defaultRunName;
    }

    /**
     * Sets the value of the defaultRunName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultRunName(String value) {
        this.defaultRunName = value;
    }

    /**
     * Gets the value of the defaultRunDescription property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDefaultRunDescription() {
        return defaultRunDescription;
    }

    /**
     * Sets the value of the defaultRunDescription property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDefaultRunDescription(String value) {
        this.defaultRunDescription = value;
    }

    /**
     * Gets the value of the enabled property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setEnabled(Boolean value) {
        this.enabled = value;
    }

    /**
     * Gets the value of the addScreenshotForFailure property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public boolean isAddScreenshotForFailure() {
        if (addScreenshotForFailure == null) {
            return false;
        } else {
            return addScreenshotForFailure;
        }
    }

    /**
     * Sets the value of the addScreenshotForFailure property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAddScreenshotForFailure(Boolean value) {
        this.addScreenshotForFailure = value;
    }

}
