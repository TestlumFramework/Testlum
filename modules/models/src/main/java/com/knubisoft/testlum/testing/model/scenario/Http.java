
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for http complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="http"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="get" type="{http://www.knubisoft.com/testlum/testing/model/scenario}get"/&gt;
 *         &lt;element name="post" type="{http://www.knubisoft.com/testlum/testing/model/scenario}post"/&gt;
 *         &lt;element name="put" type="{http://www.knubisoft.com/testlum/testing/model/scenario}put"/&gt;
 *         &lt;element name="patch" type="{http://www.knubisoft.com/testlum/testing/model/scenario}patch"/&gt;
 *         &lt;element name="delete" type="{http://www.knubisoft.com/testlum/testing/model/scenario}delete"/&gt;
 *         &lt;element name="options" type="{http://www.knubisoft.com/testlum/testing/model/scenario}options"/&gt;
 *         &lt;element name="head" type="{http://www.knubisoft.com/testlum/testing/model/scenario}head"/&gt;
 *         &lt;element name="trace" type="{http://www.knubisoft.com/testlum/testing/model/scenario}trace"/&gt;
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
@XmlType(name = "http", propOrder = {
    "get",
    "post",
    "put",
    "patch",
    "delete",
    "options",
    "head",
    "trace"
})
public class Http
    extends AbstractCommand
{

    protected Get get;
    protected Post post;
    protected Put put;
    protected Patch patch;
    protected Delete delete;
    protected Options options;
    protected Head head;
    protected Trace trace;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;

    /**
     * Gets the value of the get property.
     * 
     * @return
     *     possible object is
     *     {@link Get }
     *     
     */
    public Get getGet() {
        return get;
    }

    /**
     * Sets the value of the get property.
     * 
     * @param value
     *     allowed object is
     *     {@link Get }
     *     
     */
    public void setGet(Get value) {
        this.get = value;
    }

    /**
     * Gets the value of the post property.
     * 
     * @return
     *     possible object is
     *     {@link Post }
     *     
     */
    public Post getPost() {
        return post;
    }

    /**
     * Sets the value of the post property.
     * 
     * @param value
     *     allowed object is
     *     {@link Post }
     *     
     */
    public void setPost(Post value) {
        this.post = value;
    }

    /**
     * Gets the value of the put property.
     * 
     * @return
     *     possible object is
     *     {@link Put }
     *     
     */
    public Put getPut() {
        return put;
    }

    /**
     * Sets the value of the put property.
     * 
     * @param value
     *     allowed object is
     *     {@link Put }
     *     
     */
    public void setPut(Put value) {
        this.put = value;
    }

    /**
     * Gets the value of the patch property.
     * 
     * @return
     *     possible object is
     *     {@link Patch }
     *     
     */
    public Patch getPatch() {
        return patch;
    }

    /**
     * Sets the value of the patch property.
     * 
     * @param value
     *     allowed object is
     *     {@link Patch }
     *     
     */
    public void setPatch(Patch value) {
        this.patch = value;
    }

    /**
     * Gets the value of the delete property.
     * 
     * @return
     *     possible object is
     *     {@link Delete }
     *     
     */
    public Delete getDelete() {
        return delete;
    }

    /**
     * Sets the value of the delete property.
     * 
     * @param value
     *     allowed object is
     *     {@link Delete }
     *     
     */
    public void setDelete(Delete value) {
        this.delete = value;
    }

    /**
     * Gets the value of the options property.
     * 
     * @return
     *     possible object is
     *     {@link Options }
     *     
     */
    public Options getOptions() {
        return options;
    }

    /**
     * Sets the value of the options property.
     * 
     * @param value
     *     allowed object is
     *     {@link Options }
     *     
     */
    public void setOptions(Options value) {
        this.options = value;
    }

    /**
     * Gets the value of the head property.
     * 
     * @return
     *     possible object is
     *     {@link Head }
     *     
     */
    public Head getHead() {
        return head;
    }

    /**
     * Sets the value of the head property.
     * 
     * @param value
     *     allowed object is
     *     {@link Head }
     *     
     */
    public void setHead(Head value) {
        this.head = value;
    }

    /**
     * Gets the value of the trace property.
     * 
     * @return
     *     possible object is
     *     {@link Trace }
     *     
     */
    public Trace getTrace() {
        return trace;
    }

    /**
     * Sets the value of the trace property.
     * 
     * @param value
     *     allowed object is
     *     {@link Trace }
     *     
     */
    public void setTrace(Trace value) {
        this.trace = value;
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
