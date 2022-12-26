
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
 *         &lt;element name="web" type="{http://www.knubisoft.com/cott/testing/model/global-config}web" minOccurs="0"/&gt;
 *         &lt;element name="mobilebrowser" type="{http://www.knubisoft.com/cott/testing/model/global-config}mobilebrowser" minOccurs="0"/&gt;
 *         &lt;element name="native" type="{http://www.knubisoft.com/cott/testing/model/global-config}native" minOccurs="0"/&gt;
 *         &lt;element name="browserStackLogin" type="{http://www.knubisoft.com/cott/testing/model/global-config}browserStackLogin" minOccurs="0"/&gt;
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
    "web",
    "mobilebrowser",
    "_native",
    "browserStackLogin",
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
    protected Web web;
    protected Mobilebrowser mobilebrowser;
    @XmlElement(name = "native")
    protected Native _native;
    protected BrowserStackLogin browserStackLogin;
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
     * Gets the value of the web property.
     * 
     * @return
     *     possible object is
     *     {@link Web }
     *     
     */
    public Web getWeb() {
        return web;
    }

    /**
     * Sets the value of the web property.
     * 
     * @param value
     *     allowed object is
     *     {@link Web }
     *     
     */
    public void setWeb(Web value) {
        this.web = value;
    }

    /**
     * Gets the value of the mobilebrowser property.
     * 
     * @return
     *     possible object is
     *     {@link Mobilebrowser }
     *     
     */
    public Mobilebrowser getMobilebrowser() {
        return mobilebrowser;
    }

    /**
     * Sets the value of the mobilebrowser property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mobilebrowser }
     *     
     */
    public void setMobilebrowser(Mobilebrowser value) {
        this.mobilebrowser = value;
    }

    /**
     * Gets the value of the native property.
     * 
     * @return
     *     possible object is
     *     {@link Native }
     *     
     */
    public Native getNative() {
        return _native;
    }

    /**
     * Sets the value of the native property.
     * 
     * @param value
     *     allowed object is
     *     {@link Native }
     *     
     */
    public void setNative(Native value) {
        this._native = value;
    }

    /**
     * Gets the value of the browserStackLogin property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserStackLogin }
     *     
     */
    public BrowserStackLogin getBrowserStackLogin() {
        return browserStackLogin;
    }

    /**
     * Sets the value of the browserStackLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserStackLogin }
     *     
     */
    public void setBrowserStackLogin(BrowserStackLogin value) {
        this.browserStackLogin = value;
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
