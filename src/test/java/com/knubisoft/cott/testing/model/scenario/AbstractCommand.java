
package com.knubisoft.cott.testing.model.scenario;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for abstractCommand complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="abstractCommand"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="comment" use="required" type="{http://www.knubisoft.com/cott/testing/model/scenario}stringMin10" /&gt;
 *       &lt;attribute name="threshold" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="condition" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstractCommand")
@XmlSeeAlso({
    Include.class,
    Auth.class,
    Repeat.class,
    Http.class,
    Migrate.class,
    Postgres.class,
    Mysql.class,
    Oracle.class,
    Mongo.class,
    Redis.class,
    Rabbit.class,
    Kafka.class,
    S3 .class,
    Sqs.class,
    Clickhouse.class,
    Elasticsearch.class,
    Lambda.class,
    Sendgrid.class,
    Ses.class,
    Dynamo.class,
    Graphql.class,
    Websocket.class,
    Var.class,
    Shell.class,
    Smtp.class,
    Twilio.class,
    Ui.class,
    AbstractUiCommand.class,
    Logout.class
})
public abstract class AbstractCommand {

    @XmlAttribute(name = "comment", required = true)
    protected String comment;
    @XmlAttribute(name = "threshold")
    protected Integer threshold;
    @XmlAttribute(name = "condition")
    protected String condition;

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the threshold property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Sets the value of the threshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setThreshold(Integer value) {
        this.threshold = value;
    }

    /**
     * Gets the value of the condition property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Sets the value of the condition property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCondition(String value) {
        this.condition = value;
    }

}
