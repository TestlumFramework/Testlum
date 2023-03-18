
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for parallelExecution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="parallelExecution"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="threads" type="{http://www.knubisoft.com/cott/testing/model/global-config}positiveIntegerMin1" default="1" /&gt;
 *       &lt;attribute name="enabled" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parallelExecution")
public class ParallelExecution {

    @XmlAttribute(name = "threads")
    protected Integer threads;
    @XmlAttribute(name = "enabled", required = true)
    protected boolean enabled;

    /**
     * Gets the value of the threads property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public int getThreads() {
        if (threads == null) {
            return  1;
        } else {
            return threads;
        }
    }

    /**
     * Sets the value of the threads property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setThreads(Integer value) {
        this.threads = value;
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
