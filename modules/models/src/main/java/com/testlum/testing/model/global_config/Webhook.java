
package com.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webhook complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webhook"&gt;
 *   &lt;complexContent&gt;
 *     &lt;choice&gt;
 *        &lt;element name="matcher" type="{http://www.testlum.com/testing/model/global-config}matcher" min="0"/&gt;
 *     &lt;/choice&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/global-config}integration"&gt;
 *       &lt;attribute name="url" use="required" type="{http://www.testlum.com/testing/model/global-config}url" /&gt;
 *       &lt;attribute name="responseStatusCode" type="{http://www.w3.org/2001/XMLSchema}int" default="200"/&gt;
 *       &lt;attribute name="holdTimeout" type="{http://www.w3.org/2001/XMLSchema}int" default="5"/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhook", propOrder = {
        "matcher"
})
public class Webhook
    extends Integration
{

    protected Matcher matcher;
    @XmlAttribute(name = "url", required = true)
    protected String url;
    @XmlAttribute(name = "responseStatusCode")
    protected Integer responseStatusCode;
    @XmlAttribute(name = "holdTimeout")
    protected Integer holdTimeout;

    /**
     * Gets the value of the matcher property.
     *
     * @return
     *     possible object is
     *     {@link Matcher }
     *
     */
    public Matcher getMatcher() {
        return matcher;
    }

    /**
     * Sets the value of the matcher property.
     *
     * @param value
     *     allowed object is
     *     {@link Matcher }
     *
     */
    public void setMatcher(Matcher value) {
        this.matcher = value;
    }

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
     * Gets the value of the responseStatusCode property.
     *
     * @return
     *     possible object is
     *     {@link Integer }
     *
     */
    public int getResponseStatusCode() {
        return responseStatusCode == null ? 200 : responseStatusCode;
    }

    /**
     * Sets the value of the responseStatusCode property.
     *
     * @param value
     *     allowed object is
     *     {@link Integer }
     *
     */
    public void setResponseStatusCode(Integer value) {
        this.responseStatusCode = value;
    }

    /**
     * Gets the value of the holdTimeout property.
     *
     *
     */
    public int getHoldTimeout() {
        return holdTimeout == null ? 5 : holdTimeout;
    }

    /**
     * Sets the value of the holdTimeout property.
     *
     */
    public void setHoldTimeout(Integer value) {
        this.holdTimeout = value;
    }

}
