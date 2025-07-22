
package com.knubisoft.testlum.testing.model.global_config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for elasticsearchIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="elasticsearchIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="elasticsearch" type="{http://www.knubisoft.com/testlum/testing/model/global-config}elasticsearch"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "elasticsearchIntegration", propOrder = {
    "elasticsearch"
})
public class ElasticsearchIntegration {

    protected List<Elasticsearch> elasticsearch;

    /**
     * Gets the value of the elasticsearch property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the elasticsearch property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getElasticsearch().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Elasticsearch }
     * 
     * 
     */
    public List<Elasticsearch> getElasticsearch() {
        if (elasticsearch == null) {
            elasticsearch = new ArrayList<Elasticsearch>();
        }
        return this.elasticsearch;
    }

}
