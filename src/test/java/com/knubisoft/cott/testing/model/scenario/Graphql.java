
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *       &lt;choice&gt;
 *         &lt;element name="post" type="{http://www.knubisoft.com/cott/testing/model/scenario}graphqlPost"/&gt;
 *         &lt;element name="get" type="{http://www.knubisoft.com/cott/testing/model/scenario}graphqlGet"/&gt;
 *       &lt;/choice&gt;
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
    "post",
    "get"
})
public class Graphql
    extends AbstractCommand
{

    protected GraphqlPost post;
    protected GraphqlGet get;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the post property.
     * 
     * @return
     *     possible object is
     *     {@link GraphqlPost }
     *     
     */
    public GraphqlPost getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphqlPost }
     *     
     */
    public void setPost(GraphqlPost value) {
        this.post = value;
    }

    /**
     * Gets the value of the get property.
     * 
     * @return
     *     possible object is
     *     {@link GraphqlGet }
     *     
     */
    public GraphqlGet getGet() {
        return get;
    }

    /**
     * Sets the value of the get property.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphqlGet }
     *     
     */
    public void setGet(GraphqlGet value) {
        this.get = value;
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
