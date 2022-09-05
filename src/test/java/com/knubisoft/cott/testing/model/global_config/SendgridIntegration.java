
package com.knubisoft.cott.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sendgridIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendgridIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="sendgrid" type="{http://www.knubisoft.com/cott/testing/model/global-config}sendgrid"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendgridIntegration", propOrder = {
    "sendgrid"
})
public class SendgridIntegration {

    protected List<Sendgrid> sendgrid;

    /**
     * Gets the value of the sendgrid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sendgrid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSendgrid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Sendgrid }
     * 
     * 
     */
    public List<Sendgrid> getSendgrid() {
        if (sendgrid == null) {
            sendgrid = new ArrayList<Sendgrid>();
        }
        return this.sendgrid;
    }

}
