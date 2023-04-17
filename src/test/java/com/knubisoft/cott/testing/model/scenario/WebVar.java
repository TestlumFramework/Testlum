
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for webVar complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="webVar"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}abstractUiCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;group ref="{http://www.knubisoft.com/cott/testing/model/scenario}generalVarType"/&gt;
 *         &lt;element name="cookie" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromCookie"/&gt;
 *         &lt;element name="dom" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromDom"/&gt;
 *         &lt;element name="url" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromUrl"/&gt;
 *         &lt;element name="element" type="{http://www.knubisoft.com/cott/testing/model/scenario}fromElement"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webVar", propOrder = {
    "file",
    "path",
    "expression",
    "sql",
    "generate",
    "cookie",
    "dom",
    "url",
    "element"
})
public class WebVar
    extends AbstractUiCommand
{

    protected FromFile file;
    protected FromPath path;
    protected FromExpression expression;
    protected FromSQL sql;
    protected FromRandomGenerated generate;
    protected FromCookie cookie;
    protected FromDom dom;
    protected FromUrl url;
    protected FromElement element;
    @XmlAttribute(name = "name", required = true)
    protected String name;

    /**
     * Gets the value of the file property.
     * 
     * @return
     *     possible object is
     *     {@link FromFile }
     *     
     */
    public FromFile getFile() {
        return file;
    }

    /**
     * Sets the value of the file property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromFile }
     *     
     */
    public void setFile(FromFile value) {
        this.file = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link FromPath }
     *     
     */
    public FromPath getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromPath }
     *     
     */
    public void setPath(FromPath value) {
        this.path = value;
    }

    /**
     * Gets the value of the expression property.
     * 
     * @return
     *     possible object is
     *     {@link FromExpression }
     *     
     */
    public FromExpression getExpression() {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromExpression }
     *     
     */
    public void setExpression(FromExpression value) {
        this.expression = value;
    }

    /**
     * Gets the value of the sql property.
     * 
     * @return
     *     possible object is
     *     {@link FromSQL }
     *     
     */
    public FromSQL getSql() {
        return sql;
    }

    /**
     * Sets the value of the sql property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromSQL }
     *     
     */
    public void setSql(FromSQL value) {
        this.sql = value;
    }

    /**
     * Gets the value of the generate property.
     * 
     * @return
     *     possible object is
     *     {@link FromRandomGenerated }
     *     
     */
    public FromRandomGenerated getGenerate() {
        return generate;
    }

    /**
     * Sets the value of the generate property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromRandomGenerated }
     *     
     */
    public void setGenerate(FromRandomGenerated value) {
        this.generate = value;
    }

    /**
     * Gets the value of the cookie property.
     * 
     * @return
     *     possible object is
     *     {@link FromCookie }
     *     
     */
    public FromCookie getCookie() {
        return cookie;
    }

    /**
     * Sets the value of the cookie property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromCookie }
     *     
     */
    public void setCookie(FromCookie value) {
        this.cookie = value;
    }

    /**
     * Gets the value of the dom property.
     * 
     * @return
     *     possible object is
     *     {@link FromDom }
     *     
     */
    public FromDom getDom() {
        return dom;
    }

    /**
     * Sets the value of the dom property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromDom }
     *     
     */
    public void setDom(FromDom value) {
        this.dom = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link FromUrl }
     *     
     */
    public FromUrl getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromUrl }
     *     
     */
    public void setUrl(FromUrl value) {
        this.url = value;
    }

    /**
     * Gets the value of the element property.
     * 
     * @return
     *     possible object is
     *     {@link FromElement }
     *     
     */
    public FromElement getElement() {
        return element;
    }

    /**
     * Sets the value of the element property.
     * 
     * @param value
     *     allowed object is
     *     {@link FromElement }
     *     
     */
    public void setElement(FromElement value) {
        this.element = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

}
