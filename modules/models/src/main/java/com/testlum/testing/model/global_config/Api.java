
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for api complex type&lt;/p&gt;.
 *
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 *
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="api"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}integration"&gt;
 *       &lt;attribute name="url" use="required" type="{http://www.testlum.com/testing/model/global-config}url" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "api")
public class Api
    extends Integration
{

    @XmlAttribute(name = "url", required = true)
    protected String url;

    /**
     * Gets the value of the url property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUrl(String value) {
        this.url = value;
    }

}
