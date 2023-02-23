
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="body" type="{http://www.knubisoft.com/cott/testing/model/scenario}graphqlBody"/&gt;
 *         &lt;element name="header" type="{http://www.knubisoft.com/cott/testing/model/scenario}header" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="response" type="{http://www.knubisoft.com/cott/testing/model/scenario}response" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="endpoint" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}endpointPattern" /&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "graphql", propOrder = {
    "body",
    "header",
    "response"
})
public class Graphql
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected GraphqlBody body;
    protected List<Header> header;
    protected Response response;
    @XmlAttribute(name = "endpoint", required = true)
    protected String endpoint;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link GraphqlBody }
     *     
     */
    public GraphqlBody getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphqlBody }
     *     
     */
    public void setBody(GraphqlBody value) {
        this.body = value;
    }

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
     * Gets the value of the response property.
     * 
     * @return
     *     possible object is
     *     {@link Response }
     *     
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     * 
     * @param value
     *     allowed object is
     *     {@link Response }
     *     
     */
    public void setResponse(Response value) {
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

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

}
