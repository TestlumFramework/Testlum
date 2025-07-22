
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for connectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="connectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="appiumServer" type="{http://www.knubisoft.com/testlum/testing/model/global-config}appiumServer"/&gt;
 *         &lt;element name="browserStack" type="{http://www.knubisoft.com/testlum/testing/model/global-config}browserStackServer"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connectionType", propOrder = {
    "appiumServer",
    "browserStack"
})
public class ConnectionType {

    protected AppiumServer appiumServer;
    protected BrowserStackServer browserStack;

    /**
     * Gets the value of the appiumServer property.
     * 
     * @return
     *     possible object is
     *     {@link AppiumServer }
     *     
     */
    public AppiumServer getAppiumServer() {
        return appiumServer;
    }

    /**
     * Sets the value of the appiumServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link AppiumServer }
     *     
     */
    public void setAppiumServer(AppiumServer value) {
        this.appiumServer = value;
    }

    /**
     * Gets the value of the browserStack property.
     * 
     * @return
     *     possible object is
     *     {@link BrowserStackServer }
     *     
     */
    public BrowserStackServer getBrowserStack() {
        return browserStack;
    }

    /**
     * Sets the value of the browserStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link BrowserStackServer }
     *     
     */
    public void setBrowserStack(BrowserStackServer value) {
        this.browserStack = value;
    }

}
