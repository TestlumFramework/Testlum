package com.testlum.testing.model.global_config;

/**
 * <p>Java class for matcher complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="matcher"&gt;
 *   &lt;choice&gt;
 *     &lt;element name="byHeader" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *     &lt;element name="byQueryParam" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *     &lt;element name="byBodyKey" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *     &lt;element name="byPathVar" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *   &lt;/choice&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
public class Matcher {

    private String byHeader;
    private String byQueryParam;
    private String byBodyKey;
    private String byPathVar;

    /**
     * Gets the value of the byHeader property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getByHeader() {
        return byHeader;
    }

    /**
     * Sets the value of the byHeader property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setByHeader(String value) {
        this.byHeader = value;
    }

    /**
     * Gets the value of the byQueryParam property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getByQueryParam() {
        return byQueryParam;
    }

    /**
     * Sets the value of the byQueryParam property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setByQueryParam(String value) {
        this.byQueryParam = value;
    }

    /**
     * Gets the value of the byBodyKey property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getByBodyKey() {
        return byBodyKey;
    }

    /**
     * Sets the value of the byBodyKey property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setByBodyKey(String value) {
        this.byBodyKey = value;
    }

    /**
     * Gets the value of the byPathVar property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getByPathVar() {
        return byPathVar;
    }

    /**
     * Sets the value of the byPathVar property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setByPathVar(String value) {
        this.byPathVar = value;
    }
}
