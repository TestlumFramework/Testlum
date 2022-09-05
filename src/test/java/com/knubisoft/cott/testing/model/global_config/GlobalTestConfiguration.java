
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="stopScenarioOnFailure" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="delayBetweenScenariosRuns" type="{http://www.knubisoft.com/cott/testing/model/global-config}delayBetweenScenariosRuns" minOccurs="0"/&gt;
 *         &lt;element name="runScenariosByTag" type="{http://www.knubisoft.com/cott/testing/model/global-config}runScenariosByTag"/&gt;
 *         &lt;element name="report" type="{http://www.knubisoft.com/cott/testing/model/global-config}report"/&gt;
 *         &lt;element name="ui" type="{http://www.knubisoft.com/cott/testing/model/global-config}ui" minOccurs="0"/&gt;
 *         &lt;element name="auth" type="{http://www.knubisoft.com/cott/testing/model/global-config}auth"/&gt;
 *         &lt;element name="integrations" type="{http://www.knubisoft.com/cott/testing/model/global-config}integrations" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "stopScenarioOnFailure",
    "delayBetweenScenariosRuns",
    "runScenariosByTag",
    "report",
    "ui",
    "auth",
    "integrations"
})
@XmlRootElement(name = "globalTestConfiguration")
public class GlobalTestConfiguration {

    protected boolean stopScenarioOnFailure;
    protected DelayBetweenScenariosRuns delayBetweenScenariosRuns;
    @XmlElement(required = true)
    protected RunScenariosByTag runScenariosByTag;
    @XmlElement(required = true)
    protected Report report;
    protected Ui ui;
    @XmlElement(required = true)
    protected Auth auth;
    protected Integrations integrations;

    /**
     * Gets the value of the stopScenarioOnFailure property.
     * 
     */
    public boolean isStopScenarioOnFailure() {
        return stopScenarioOnFailure;
    }

    /**
     * Sets the value of the stopScenarioOnFailure property.
     * 
     */
    public void setStopScenarioOnFailure(boolean value) {
        this.stopScenarioOnFailure = value;
    }

    /**
     * Gets the value of the delayBetweenScenariosRuns property.
     * 
     * @return
     *     possible object is
     *     {@link DelayBetweenScenariosRuns }
     *     
     */
    public DelayBetweenScenariosRuns getDelayBetweenScenariosRuns() {
        return delayBetweenScenariosRuns;
    }

    /**
     * Sets the value of the delayBetweenScenariosRuns property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelayBetweenScenariosRuns }
     *     
     */
    public void setDelayBetweenScenariosRuns(DelayBetweenScenariosRuns value) {
        this.delayBetweenScenariosRuns = value;
    }

    /**
     * Gets the value of the runScenariosByTag property.
     * 
     * @return
     *     possible object is
     *     {@link RunScenariosByTag }
     *     
     */
    public RunScenariosByTag getRunScenariosByTag() {
        return runScenariosByTag;
    }

    /**
     * Sets the value of the runScenariosByTag property.
     * 
     * @param value
     *     allowed object is
     *     {@link RunScenariosByTag }
     *     
     */
    public void setRunScenariosByTag(RunScenariosByTag value) {
        this.runScenariosByTag = value;
    }

    /**
     * Gets the value of the report property.
     * 
     * @return
     *     possible object is
     *     {@link Report }
     *     
     */
    public Report getReport() {
        return report;
    }

    /**
     * Sets the value of the report property.
     * 
     * @param value
     *     allowed object is
     *     {@link Report }
     *     
     */
    public void setReport(Report value) {
        this.report = value;
    }

    /**
     * Gets the value of the ui property.
     * 
     * @return
     *     possible object is
     *     {@link Ui }
     *     
     */
    public Ui getUi() {
        return ui;
    }

    /**
     * Sets the value of the ui property.
     * 
     * @param value
     *     allowed object is
     *     {@link Ui }
     *     
     */
    public void setUi(Ui value) {
        this.ui = value;
    }

    /**
     * Gets the value of the auth property.
     * 
     * @return
     *     possible object is
     *     {@link Auth }
     *     
     */
    public Auth getAuth() {
        return auth;
    }

    /**
     * Sets the value of the auth property.
     * 
     * @param value
     *     allowed object is
     *     {@link Auth }
     *     
     */
    public void setAuth(Auth value) {
        this.auth = value;
    }

    /**
     * Gets the value of the integrations property.
     * 
     * @return
     *     possible object is
     *     {@link Integrations }
     *     
     */
    public Integrations getIntegrations() {
        return integrations;
    }

    /**
     * Sets the value of the integrations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integrations }
     *     
     */
    public void setIntegrations(Integrations value) {
        this.integrations = value;
    }

}
