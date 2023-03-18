
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
 *         &lt;element name="delayBetweenScenarioRuns" type="{http://www.knubisoft.com/cott/testing/model/global-config}delayBetweenScenarioRuns"/&gt;
 *         &lt;element name="parallelExecution" type="{http://www.knubisoft.com/cott/testing/model/global-config}parallelExecution"/&gt;
 *         &lt;element name="runScenariosByTag" type="{http://www.knubisoft.com/cott/testing/model/global-config}runScenariosByTag"/&gt;
 *         &lt;element name="report" type="{http://www.knubisoft.com/cott/testing/model/global-config}report"/&gt;
 *         &lt;element name="environments" type="{http://www.knubisoft.com/cott/testing/model/global-config}environments"/&gt;
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
    "delayBetweenScenarioRuns",
    "parallelExecution",
    "runScenariosByTag",
    "report",
    "environments"
})
@XmlRootElement(name = "globalTestConfiguration")
public class GlobalTestConfiguration {

    @XmlElement(required = true)
    protected Subscription subscription;
    protected boolean stopScenarioOnFailure;
    protected boolean stopIfInvalidScenario;
    @XmlElement(required = true)
    protected DelayBetweenScenarioRuns delayBetweenScenarioRuns;
    @XmlElement(required = true)
    protected ParallelExecution parallelExecution;
    @XmlElement(required = true)
    protected RunScenariosByTag runScenariosByTag;
    @XmlElement(required = true)
    protected Report report;
    @XmlElement(required = true)
    protected Environments environments;

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
     * Gets the value of the delayBetweenScenarioRuns property.
     * 
     * @return
     *     possible object is
     *     {@link DelayBetweenScenarioRuns }
     *     
     */
    public DelayBetweenScenarioRuns getDelayBetweenScenarioRuns() {
        return delayBetweenScenarioRuns;
    }

    /**
     * Sets the value of the delayBetweenScenarioRuns property.
     * 
     * @param value
     *     allowed object is
     *     {@link DelayBetweenScenarioRuns }
     *     
     */
    public void setDelayBetweenScenarioRuns(DelayBetweenScenarioRuns value) {
        this.delayBetweenScenarioRuns = value;
    }

    /**
     * Gets the value of the parallelExecution property.
     * 
     * @return
     *     possible object is
     *     {@link ParallelExecution }
     *     
     */
    public ParallelExecution getParallelExecution() {
        return parallelExecution;
    }

    /**
     * Sets the value of the parallelExecution property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParallelExecution }
     *     
     */
    public void setParallelExecution(ParallelExecution value) {
        this.parallelExecution = value;
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
     * Gets the value of the environments property.
     * 
     * @return
     *     possible object is
     *     {@link Environments }
     *     
     */
    public Environments getEnvironments() {
        return environments;
    }

    /**
     * Sets the value of the environments property.
     * 
     * @param value
     *     allowed object is
     *     {@link Environments }
     *     
     */
    public void setEnvironments(Environments value) {
        this.environments = value;
    }

}
