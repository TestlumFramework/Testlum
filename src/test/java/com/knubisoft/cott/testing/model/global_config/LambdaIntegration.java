
package com.knubisoft.cott.testing.model.global_config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lambdaIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="lambdaIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="lambda" type="{http://www.knubisoft.com/cott/testing/model/global-config}lambda"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lambdaIntegration", propOrder = {
    "lambda"
})
public class LambdaIntegration {

    protected List<Lambda> lambda;

    /**
     * Gets the value of the lambda property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the lambda property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLambda().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Lambda }
     * 
     * 
     */
    public List<Lambda> getLambda() {
        if (lambda == null) {
            lambda = new ArrayList<Lambda>();
        }
        return this.lambda;
    }

}
