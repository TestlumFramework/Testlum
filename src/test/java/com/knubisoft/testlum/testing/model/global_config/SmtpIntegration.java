
package com.knubisoft.testlum.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for smtpIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="smtpIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="smtp" type="{http://www.knubisoft.com/testlum/testing/model/global-config}smtp"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "smtpIntegration", propOrder = {
    "smtp"
})
public class SmtpIntegration {

    protected List<Smtp> smtp;

    /**
     * Gets the value of the smtp property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the smtp property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSmtp().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Smtp }
     * 
     * 
     */
    public List<Smtp> getSmtp() {
        if (smtp == null) {
            smtp = new ArrayList<Smtp>();
        }
        return this.smtp;
    }

}
