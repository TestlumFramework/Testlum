
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for dynamoIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dynamoIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="dynamo" type="{http://www.knubisoft.com/testlum/testing/model/global-config}dynamo"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dynamoIntegration", propOrder = {
    "dynamo"
})
public class DynamoIntegration {

    protected List<Dynamo> dynamo;

    /**
     * Gets the value of the dynamo property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dynamo property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDynamo().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Dynamo }
     * 
     * 
     */
    public List<Dynamo> getDynamo() {
        if (dynamo == null) {
            dynamo = new ArrayList<Dynamo>();
        }
        return this.dynamo;
    }

}
