
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element name="appiumServer" type="{http://www.knubisoft.com/cott/testing/model/global-config}appiumServer"/&gt;
 *         &lt;element name="browserStack" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
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
    protected Object browserStack;

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
     *     {@link Object }
     *     
     */
    public Object getBrowserStack() {
        return browserStack;
    }

    /**
     * Sets the value of the browserStack property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setBrowserStack(Object value) {
        this.browserStack = value;
    }

}
