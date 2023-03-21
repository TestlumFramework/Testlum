
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for desktop complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="desktop"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}settings"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="connection" type="{http://www.knubisoft.com/cott/testing/model/global-config}desktopConnectionType"/&gt;
 *         &lt;element name="types" type="{http://www.knubisoft.com/cott/testing/model/global-config}desktopTypes"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "desktop", propOrder = {
    "connection",
    "types"
})
public class Desktop
    extends Settings
{

    @XmlElement(required = true)
    protected DesktopConnectionType connection;
    @XmlElement(required = true)
    protected DesktopTypes types;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;

    /**
     * Gets the value of the connection property.
     * 
     * @return
     *     possible object is
     *     {@link DesktopConnectionType }
     *     
     */
    public DesktopConnectionType getConnection() {
        return connection;
    }

    /**
     * Sets the value of the connection property.
     * 
     * @param value
     *     allowed object is
     *     {@link DesktopConnectionType }
     *     
     */
    public void setConnection(DesktopConnectionType value) {
        this.connection = value;
    }

    /**
     * Gets the value of the types property.
     * 
     * @return
     *     possible object is
     *     {@link DesktopTypes }
     *     
     */
    public DesktopTypes getTypes() {
        return types;
    }

    /**
     * Sets the value of the types property.
     * 
     * @param value
     *     allowed object is
     *     {@link DesktopTypes }
     *     
     */
    public void setTypes(DesktopTypes value) {
        this.types = value;
    }

    /**
     * Gets the value of the enabled property.
     * 
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the value of the enabled property.
     * 
     */
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

}
