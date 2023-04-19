
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for browserType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="browserType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="localBrowser" type="{http://www.knubisoft.com/testlum/testing/model/global-config}localBrowser"/&gt;
 *         &lt;element name="browserInDocker" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserInDocker"/&gt;
 *         &lt;element name="remoteBrowser" type="{http://www.knubisoft.com/testlum/testing/model/global-config}remoteBrowser"/&gt;
 *         &lt;element name="browserStack" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserStackWeb"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "browserType", propOrder = {
    "localBrowser",
    "browserInDocker",
    "remoteBrowser",
    "browserStack"
})
public class BrowserType {

    protected LocalBrowser localBrowser;
    protected BrowserInDocker browserInDocker;
    protected RemoteBrowser remoteBrowser;
    protected BrowserStackWeb browserStack;

    /**
     * Gets the value of the localBrowser property.
     * 
     * @return
     *     possible object is
     *     {@link LocalBrowser }
     *     
     */
    public LocalBrowser getLocalBrowser() {
        return localBrowser;
    }

    /**
     * Sets the value of the localBrowser property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocalBrowser }
     *     
     */
    public void setLocalBrowser(LocalBrowser value) {
        this.localBrowser = value;
    }

    /**
     * Gets the value of the browserInDocker property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserInDocker }
     *     
     */
    public BrowserInDocker getBrowserInDocker() {
        return browserInDocker;
    }

    /**
     * Sets the value of the browserInDocker property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserInDocker }
     *     
     */
    public void setBrowserInDocker(BrowserInDocker value) {
        this.browserInDocker = value;
    }

    /**
     * Gets the value of the remoteBrowser property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteBrowser }
     *     
     */
    public RemoteBrowser getRemoteBrowser() {
        return remoteBrowser;
    }

    /**
     * Sets the value of the remoteBrowser property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteBrowser }
     *     
     */
    public void setRemoteBrowser(RemoteBrowser value) {
        this.remoteBrowser = value;
    }

    /**
     * Gets the value of the browserStack property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserStackWeb }
     *     
     */
    public BrowserStackWeb getBrowserStack() {
        return browserStack;
    }

    /**
     * Sets the value of the browserStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserStackWeb }
     *     
     */
    public void setBrowserStack(BrowserStackWeb value) {
        this.browserStack = value;
    }

}
