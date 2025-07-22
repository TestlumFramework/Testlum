
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for klovServerReportGenerator complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="klovServerReportGenerator"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="mongoDB" type="{http://www.knubisoft.com/testlum/testing/model/global-config}mongodb"/&gt;
 *         &lt;element name="klovServer" type="{http://www.knubisoft.com/testlum/testing/model/global-config}klovServer"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "klovServerReportGenerator", propOrder = {
    "mongoDB",
    "klovServer"
})
public class KlovServerReportGenerator {

    @XmlElement(required = true)
    protected Mongodb mongoDB;
    @XmlElement(required = true)
    protected KlovServer klovServer;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;

    /**
     * Gets the value of the mongoDB property.
     * 
     * @return
     *     possible object is
     *     {@link Mongodb }
     *     
     */
    public Mongodb getMongoDB() {
        return mongoDB;
    }

    /**
     * Sets the value of the mongoDB property.
     * 
     * @param value
     *     allowed object is
     *     {@link Mongodb }
     *     
     */
    public void setMongoDB(Mongodb value) {
        this.mongoDB = value;
    }

    /**
     * Gets the value of the klovServer property.
     * 
     * @return
     *     possible object is
     *     {@link KlovServer }
     *     
     */
    public KlovServer getKlovServer() {
        return klovServer;
    }

    /**
     * Sets the value of the klovServer property.
     * 
     * @param value
     *     allowed object is
     *     {@link KlovServer }
     *     
     */
    public void setKlovServer(KlovServer value) {
        this.klovServer = value;
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
