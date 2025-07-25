
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sqlDatabase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sqlDatabase"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}databaseConfig"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="customTruncate" type="{http://www.knubisoft.com/testlum/testing/model/global-config}customTruncateConfig" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sqlDatabase", propOrder = {
    "customTruncate"
})
public class SqlDatabase
    extends DatabaseConfig
{

    protected CustomTruncateConfig customTruncate;

    /**
     * Gets the value of the customTruncate property.
     * 
     * @return
     *     possible object is
     *     {@link CustomTruncateConfig }
     *     
     */
    public CustomTruncateConfig getCustomTruncate() {
        return customTruncate;
    }

    /**
     * Sets the value of the customTruncate property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomTruncateConfig }
     *     
     */
    public void setCustomTruncate(CustomTruncateConfig value) {
        this.customTruncate = value;
    }

}
