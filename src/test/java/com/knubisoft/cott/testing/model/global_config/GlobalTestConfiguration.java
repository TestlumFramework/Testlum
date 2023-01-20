
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
 *         &lt;element name="subscription" type="{http://www.knubisoft.com/cott/testing/model/global-config}subscription"/&gt;
 *         &lt;element name="stopScenarioOnFailure" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="stopIfInvalidScenario" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="delayBetweenScenariosRuns" type="{http://www.knubisoft.com/cott/testing/model/global-config}delayBetweenScenariosRuns" minOccurs="0"/&gt;
 *         &lt;element name="runScenariosByTag" type="{http://www.knubisoft.com/cott/testing/model/global-config}runScenariosByTag"/&gt;
 *         &lt;element name="report" type="{http://www.knubisoft.com/cott/testing/model/global-config}report"/&gt;
 *         &lt;element name="auth" type="{http://www.knubisoft.com/cott/testing/model/global-config}auth"/&gt;
 *         &lt;element name="uiConfigurations" type="{http://www.knubisoft.com/cott/testing/model/global-config}configFiles"/&gt;
 *         &lt;element name="integrationsConfigurations" type="{http://www.knubisoft.com/cott/testing/model/global-config}configFiles"/&gt;
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
    "subscription",
    "stopScenarioOnFailure",
    "stopIfInvalidScenario",
    "delayBetweenScenariosRuns",
    "runScenariosByTag",
    "report",
    "auth",
    "uiConfigurations",
    "integrationsConfigurations"
})
@XmlRootElement(name = "globalTestConfiguration")
public class GlobalTestConfiguration {

    @XmlElement(required = true)
    protected Subscription subscription;
    protected boolean stopScenarioOnFailure;
    protected boolean stopIfInvalidScenario;
    protected DelayBetweenScenariosRuns delayBetweenScenariosRuns;
    @XmlElement(required = true)
    protected RunScenariosByTag runScenariosByTag;
    @XmlElement(required = true)
    protected Report report;
    @XmlElement(required = true)
    protected Auth auth;
    @XmlElement(required = true)
    protected ConfigFiles uiConfigurations;
    @XmlElement(required = true)
    protected ConfigFiles integrationsConfigurations;

    /**
     * Gets the value of the subscription property.
     * 
     * @return
     *     possible object is
     *     {@link Subscription }
     *     
     */
    public Subscription getSubscription() {
        return subscription;
    }

    /**
     * Sets the value of the subscription property.
     * 
     * @param value
     *     allowed object is
     *     {@link Subscription }
     *     
     */
    public void setSubscription(Subscription value) {
        this.subscription = value;
    }

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
     * Gets the value of the stopIfInvalidScenario property.
     * 
     */
    public boolean isStopIfInvalidScenario() {
        return stopIfInvalidScenario;
    }

    /**
     * Sets the value of the stopIfInvalidScenario property.
     * 
     */
    public void setStopIfInvalidScenario(boolean value) {
        this.stopIfInvalidScenario = value;
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
     * Gets the value of the uiConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigFiles }
     *     
     */
    public ConfigFiles getUiConfigurations() {
        return uiConfigurations;
    }

    /**
     * Sets the value of the uiConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigFiles }
     *     
     */
    public void setUiConfigurations(ConfigFiles value) {
        this.uiConfigurations = value;
    }

    /**
     * Gets the value of the integrationsConfigurations property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigFiles }
     *     
     */
    public ConfigFiles getIntegrationsConfigurations() {
        return integrationsConfigurations;
    }

    /**
     * Sets the value of the integrationsConfigurations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigFiles }
     *     
     */
    public void setIntegrationsConfigurations(ConfigFiles value) {
        this.integrationsConfigurations = value;
    }

}
