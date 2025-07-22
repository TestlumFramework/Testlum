
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for elasticSearchRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="elasticSearchRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="header" type="{http://www.knubisoft.com/testlum/testing/model/scenario}header" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="param" type="{http://www.knubisoft.com/testlum/testing/model/scenario}param" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="response" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticSearchResponse" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="endpoint" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}endpointPattern" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elasticSearchRequest", propOrder = {
    "header",
    "param",
    "response"
})
@XmlSeeAlso({
    ElasticsearchGetRequest.class,
    ElasticSearchRequestWithBody.class,
    ElasticsearchDeleteRequest.class,
    ElasticsearchHeadRequest.class
})
public abstract class ElasticSearchRequest {

    protected List<Header> header;
    protected List<Param> param;
    protected ElasticSearchResponse response;
    @XmlAttribute(name = "endpoint", required = true)
    protected String endpoint;

    /**
     * Gets the value of the header property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the header property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHeader().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Header }
     * 
     * 
     */
    public List<Header> getHeader() {
        if (header == null) {
            header = new ArrayList<Header>();
        }
        return this.header;
    }

    /**
     * Gets the value of the param property.
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
     *    getParam().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Param }
     * 
     * 
     */
    public List<Param> getParam() {
        if (param == null) {
            param = new ArrayList<Param>();
        }
        return this.param;
    }

    /**
     * Gets the value of the response property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticSearchResponse }
     *     
     */
    public ElasticSearchResponse getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticSearchResponse }
     *     
     */
    public void setResponse(ElasticSearchResponse value) {
        this.response = value;
    }

    /**
     * Gets the value of the endpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the value of the endpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndpoint(String value) {
        this.endpoint = value;
    }

}
