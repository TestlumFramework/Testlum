
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nativeFullScreen complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="nativeFullScreen"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}fullScreen"&gt;
 *       &lt;attribute name="excludeStatusBar" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "nativeFullScreen")
public class NativeFullScreen
    extends FullScreen
{

    @XmlAttribute(name = "excludeStatusBar")
    protected Boolean excludeStatusBar;

    /**
     * Gets the value of the excludeStatusBar property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isExcludeStatusBar() {
        if (excludeStatusBar == null) {
            return false;
        } else {
            return excludeStatusBar;
        }
    }

    /**
     * Sets the value of the excludeStatusBar property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setExcludeStatusBar(Boolean value) {
        this.excludeStatusBar = value;
    }

}
