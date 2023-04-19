
package com.knubisoft.testlum.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rabbitmqIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="rabbitmqIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="rabbitmq" type="{http://www.knubisoft.com/testlum/testing/model/global-config}rabbitmq"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "rabbitmqIntegration", propOrder = {
    "rabbitmq"
})
public class RabbitmqIntegration {

    protected List<Rabbitmq> rabbitmq;

    /**
     * Gets the value of the rabbitmq property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rabbitmq property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRabbitmq().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Rabbitmq }
     * 
     * 
     */
    public List<Rabbitmq> getRabbitmq() {
        if (rabbitmq == null) {
            rabbitmq = new ArrayList<Rabbitmq>();
        }
        return this.rabbitmq;
    }

}
