
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendgrid complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendgrid"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice&gt;
 *           &lt;element name="get" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgridGet"/&gt;
 *           &lt;element name="post" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgridPost"/&gt;
 *           &lt;element name="put" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgridPut"/&gt;
 *           &lt;element name="patch" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgridPatch"/&gt;
 *           &lt;element name="delete" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgridDelete"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendgrid", propOrder = {
    "get",
    "post",
    "put",
    "patch",
    "delete"
})
public class Sendgrid
    extends AbstractCommand
{

    protected SendgridGet get;
    protected SendgridPost post;
    protected SendgridPut put;
    protected SendgridPatch patch;
    protected SendgridDelete delete;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the get property.
     * 
     * @return
     *     possible object is
     *     {@link SendgridGet }
     *     
     */
    public SendgridGet getGet() {
        return get;
    }

    /**
     * Sets the value of the get property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendgridGet }
     *     
     */
    public void setGet(SendgridGet value) {
        this.get = value;
    }

    /**
     * Gets the value of the post property.
     * 
     * @return
     *     possible object is
     *     {@link SendgridPost }
     *     
     */
    public SendgridPost getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendgridPost }
     *     
     */
    public void setPost(SendgridPost value) {
        this.post = value;
    }

    /**
     * Gets the value of the put property.
     * 
     * @return
     *     possible object is
     *     {@link SendgridPut }
     *     
     */
    public SendgridPut getPut() {
        return put;
    }

    /**
     * Sets the value of the put property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendgridPut }
     *     
     */
    public void setPut(SendgridPut value) {
        this.put = value;
    }

    /**
     * Gets the value of the patch property.
     * 
     * @return
     *     possible object is
     *     {@link SendgridPatch }
     *     
     */
    public SendgridPatch getPatch() {
        return patch;
    }

    /**
     * Sets the value of the patch property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendgridPatch }
     *     
     */
    public void setPatch(SendgridPatch value) {
        this.patch = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link SendgridDelete }
     *     
     */
    public SendgridDelete getDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendgridDelete }
     *     
     */
    public void setDelete(SendgridDelete value) {
        this.delete = value;
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
