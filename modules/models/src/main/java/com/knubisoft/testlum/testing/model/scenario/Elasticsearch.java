
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for elasticsearch complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="elasticsearch"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="get" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticsearchGetRequest"/&gt;
 *         &lt;element name="post" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticsearchPostRequest"/&gt;
 *         &lt;element name="put" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticsearchPutRequest"/&gt;
 *         &lt;element name="delete" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticsearchDeleteRequest"/&gt;
 *         &lt;element name="head" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticsearchHeadRequest"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elasticsearch", propOrder = {
    "get",
    "post",
    "put",
    "delete",
    "head"
})
public class Elasticsearch
    extends AbstractCommand
{

    protected ElasticsearchGetRequest get;
    protected ElasticsearchPostRequest post;
    protected ElasticsearchPutRequest put;
    protected ElasticsearchDeleteRequest delete;
    protected ElasticsearchHeadRequest head;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the get property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchGetRequest }
     *     
     */
    public ElasticsearchGetRequest getGet() {
        return get;
    }

    /**
     * Sets the value of the get property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchGetRequest }
     *     
     */
    public void setGet(ElasticsearchGetRequest value) {
        this.get = value;
    }

    /**
     * Gets the value of the post property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchPostRequest }
     *     
     */
    public ElasticsearchPostRequest getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchPostRequest }
     *     
     */
    public void setPost(ElasticsearchPostRequest value) {
        this.post = value;
    }

    /**
     * Gets the value of the put property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchPutRequest }
     *     
     */
    public ElasticsearchPutRequest getPut() {
        return put;
    }

    /**
     * Sets the value of the put property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchPutRequest }
     *     
     */
    public void setPut(ElasticsearchPutRequest value) {
        this.put = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchDeleteRequest }
     *     
     */
    public ElasticsearchDeleteRequest getDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchDeleteRequest }
     *     
     */
    public void setDelete(ElasticsearchDeleteRequest value) {
        this.delete = value;
    }

    /**
     * Gets the value of the head property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchHeadRequest }
     *     
     */
    public ElasticsearchHeadRequest getHead() {
        return head;
    }

    /**
     * Sets the value of the head property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchHeadRequest }
     *     
     */
    public void setHead(ElasticsearchHeadRequest value) {
        this.head = value;
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
