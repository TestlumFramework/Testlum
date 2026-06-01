package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Java class for webhookMatcher complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="webhookMatcher"&gt;
 *   &lt;choice maxOccurs="unbounded"&gt;
 *     &lt;element name="byHeader" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookMatcherNameValue"/&gt;
 *     &lt;element name="byJson" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookMatcherJson"/&gt;
 *     &lt;element name="byQueryParam" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookMatcherNameValue"/&gt;
 *     &lt;element name="byEndpoint" type="{http://www.knubisoft.com/testlum/testing/model/scenario}webhookMatcherEndpoint"/&gt;
 *     &lt;element name="always" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true"/&gt;
 *   &lt;/choice&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webhookMatcher", propOrder = {
        "byHeader",
        "byJson",
        "byQueryParam",
        "byEndpoint",
        "always"
})
public class WebhookMatcher {

    protected List<WebhookMatcherNameValue> byHeader;
    protected List<WebhookMatcherJson> byJson;
    protected List<WebhookMatcherNameValue> byQueryParam;
    protected List<WebhookMatcherEndpoint> byEndpoint;
    protected Boolean always;

    /**
     * Gets the value of the byHeader property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getByHeader().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebhookMatcherNameValue }
     *
     *
     */
    public List<WebhookMatcherNameValue> getByHeader() {
        if (byHeader == null) {
            byHeader = new ArrayList<WebhookMatcherNameValue>();
        }
        return this.byHeader;
    }

    /**
     * Gets the value of the byJson property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getByJson().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebhookMatcherJson }
     *
     *
     */
    public List<WebhookMatcherJson> getByJson() {
        if (byJson == null) {
            byJson = new ArrayList<WebhookMatcherJson>();
        }
        return this.byJson;
    }

    /**
     * Gets the value of the byQueryParam property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getByQueryParam().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebhookMatcherNameValue }
     *
     *
     */
    public List<WebhookMatcherNameValue> getByQueryParam() {
        if (byQueryParam == null) {
            byQueryParam = new ArrayList<WebhookMatcherNameValue>();
        }
        return this.byQueryParam;
    }

    /**
     * Gets the value of the byEndpoint property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the param property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getByEndpoint().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WebhookMatcherEndpoint }
     *
     *
     */
    public List<WebhookMatcherEndpoint> getByEndpoint() {
        if (byEndpoint == null) {
            byEndpoint = new ArrayList<WebhookMatcherEndpoint>();
        }
        return this.byEndpoint;
    }

    /**
     * Gets the value of the always property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isAlways() {
        if (always == null) {
            return true;
        } else {
            return always;
        }
    }

    /**
     * Sets the value of the always property.
     *
     * @param always
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setAlways(Boolean always) {
        this.always = always;
    }

}
