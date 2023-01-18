
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
 *         &lt;element name="ui" type="{http://www.knubisoft.com/cott/testing/model/global-config}configFiles"/&gt;
 *         &lt;element name="integrations" type="{http://www.knubisoft.com/cott/testing/model/global-config}configFiles"/&gt;
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
    "ui",
    "integrations"
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
    protected ConfigFiles ui;
    @XmlElement(required = true)
    protected ConfigFiles integrations;

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
     * Gets the value of the ui property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigFiles }
     *     
     */
    public ConfigFiles getUi() {
        return ui;
    }

    /**
     * Sets the value of the ui property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigFiles }
     *     
     */
    public void setUi(ConfigFiles value) {
        this.ui = value;
    }

    /**
     * Gets the value of the integrations property.
     * 
     * @return
     *     possible object is
     *     {@link ConfigFiles }
     *     
     */
    public ConfigFiles getIntegrations() {
        return integrations;
    }

    /**
     * Sets the value of the integrations property.
     * 
     * @param value
     *     allowed object is
     *     {@link ConfigFiles }
     *     
     */
    public void setIntegrations(ConfigFiles value) {
        this.integrations = value;
    }

}
