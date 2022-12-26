
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for androidDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="androidDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}nativeDevice"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="appPackage" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="appActivity" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *         &lt;element name="app" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="playMarketLogin" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "androidDevice", propOrder = {
    "appPackage",
    "appActivity",
    "app"
})
public class AndroidDevice
    extends NativeDevice
{

    protected String appPackage;
    protected String appActivity;
    protected String app;
    @XmlAttribute(name = "playMarketLogin")
    protected Boolean playMarketLogin;

    /**
     * Gets the value of the appPackage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppPackage() {
        return appPackage;
    }

    /**
     * Sets the value of the appPackage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppPackage(String value) {
        this.appPackage = value;
    }

    /**
     * Gets the value of the appActivity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAppActivity() {
        return appActivity;
    }

    /**
     * Sets the value of the appActivity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAppActivity(String value) {
        this.appActivity = value;
    }

    /**
     * Gets the value of the app property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApp() {
        return app;
    }

    /**
     * Sets the value of the app property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApp(String value) {
        this.app = value;
    }

    /**
     * Gets the value of the playMarketLogin property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isPlayMarketLogin() {
        if (playMarketLogin == null) {
            return false;
        } else {
            return playMarketLogin;
        }
    }

    /**
     * Sets the value of the playMarketLogin property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPlayMarketLogin(Boolean value) {
        this.playMarketLogin = value;
    }

}
