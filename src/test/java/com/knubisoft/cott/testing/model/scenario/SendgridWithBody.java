
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendgridWithBody complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendgridWithBody"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}sendgridInfo"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="body" type="{http://www.knubisoft.com/cott/testing/model/scenario}body" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendgridWithBody", propOrder = {
    "body"
})
@XmlSeeAlso({
    SendgridPost.class,
    SendgridPut.class,
    SendgridPatch.class
})
public abstract class SendgridWithBody
    extends SendgridInfo
{

    protected Body body;

    /**
     * Gets the value of the body property.
     * 
     * @return
     *     possible object is
     *     {@link Body }
     *     
     */
    public Body getBody() {
        return body;
    }

    /**
     * Sets the value of the body property.
     * 
     * @param value
     *     allowed object is
     *     {@link Body }
     *     
     */
    public void setBody(Body value) {
        this.body = value;
    }

}
