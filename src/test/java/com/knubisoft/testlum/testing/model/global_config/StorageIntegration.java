
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for storageIntegration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="storageIntegration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/global-config}integration"&gt;
 *       &lt;attribute name="truncate" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "storageIntegration")
@XmlSeeAlso({
    Clickhouse.class,
    DatabaseConfig.class,
    Redis.class,
    Mongo.class,
    S3 .class,
    Sqs.class,
    Kafka.class,
    Rabbitmq.class,
    Dynamo.class,
    Elasticsearch.class
})
public class StorageIntegration
    extends Integration
{

    @XmlAttribute(name = "truncate")
    protected Boolean truncate;

    /**
     * Gets the value of the truncate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isTruncate() {
        if (truncate == null) {
            return true;
        } else {
            return truncate;
        }
    }

    /**
     * Sets the value of the truncate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTruncate(Boolean value) {
        this.truncate = value;
    }

}
