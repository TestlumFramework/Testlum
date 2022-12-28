
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for graphql complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="graphql"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/global-config}integration"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="url" type="{http://www.knubisoft.com/cott/testing/model/global-config}nonEmptyString"/&gt;
 *         &lt;element name="apiAlias" type="{http://www.knubisoft.com/cott/testing/model/global-config}aliasPattern"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "graphql", propOrder = {
    "url",
    "apiAlias"
})
public class Graphql
    extends Integration
{

    @XmlElement(required = true)
    protected String url;
    @XmlElement(required = true)
    protected String apiAlias;

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

    /**
     * Gets the value of the apiAlias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApiAlias() {
        return apiAlias;
    }

    /**
     * Sets the value of the apiAlias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApiAlias(String value) {
        this.apiAlias = value;
    }

}
