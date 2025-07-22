
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for lambda complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="lambda"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="body" type="{http://www.knubisoft.com/testlum/testing/model/scenario}lambdaBody"/&gt;
 *         &lt;element name="response" type="{http://www.knubisoft.com/testlum/testing/model/scenario}response"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *       &lt;attribute name="functionName" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lambda", propOrder = {
    "body",
    "response"
})
public class Lambda
    extends AbstractCommand
{

    @XmlElement(required = true)
    protected LambdaBody body;
    @XmlElement(required = true)
    protected Response response;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "functionName", required = true)
    protected String functionName;

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link LambdaBody }
     *     
     */
    public LambdaBody getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link LambdaBody }
     *     
     */
    public void setBody(LambdaBody value) {
        this.body = value;
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

    /**
     * Gets the value of the functionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * Sets the value of the functionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFunctionName(String value) {
        this.functionName = value;
    }

}
