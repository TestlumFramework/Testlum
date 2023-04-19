
package com.knubisoft.testlum.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for graphqlPost complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="graphqlPost"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}httpInfo"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="body" type="{http://www.knubisoft.com/testlum/testing/model/scenario}graphqlBody"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "graphqlPost", propOrder = {
    "body"
})
public class GraphqlPost
    extends HttpInfo
{

    @XmlElement(required = true)
    protected GraphqlBody body;

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

}
