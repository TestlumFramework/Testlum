
package com.knubisoft.testlum.testing.model.scenario;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for repeat complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="repeat"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="include" type="{http://www.knubisoft.com/testlum/testing/model/scenario}include"/&gt;
 *         &lt;element name="repeat" type="{http://www.knubisoft.com/testlum/testing/model/scenario}repeat"/&gt;
 *         &lt;element name="web" type="{http://www.knubisoft.com/testlum/testing/model/scenario}web"/&gt;
 *         &lt;element name="http" type="{http://www.knubisoft.com/testlum/testing/model/scenario}http"/&gt;
 *         &lt;element name="migrate" type="{http://www.knubisoft.com/testlum/testing/model/scenario}migrate"/&gt;
 *         &lt;element name="postgres" type="{http://www.knubisoft.com/testlum/testing/model/scenario}postgres"/&gt;
 *         &lt;element name="mysql" type="{http://www.knubisoft.com/testlum/testing/model/scenario}mysql"/&gt;
 *         &lt;element name="oracle" type="{http://www.knubisoft.com/testlum/testing/model/scenario}oracle"/&gt;
 *         &lt;element name="mongo" type="{http://www.knubisoft.com/testlum/testing/model/scenario}mongo"/&gt;
 *         &lt;element name="redis" type="{http://www.knubisoft.com/testlum/testing/model/scenario}redis"/&gt;
 *         &lt;element name="rabbit" type="{http://www.knubisoft.com/testlum/testing/model/scenario}rabbit"/&gt;
 *         &lt;element name="kafka" type="{http://www.knubisoft.com/testlum/testing/model/scenario}kafka"/&gt;
 *         &lt;element name="s3" type="{http://www.knubisoft.com/testlum/testing/model/scenario}s3"/&gt;
 *         &lt;element name="sqs" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sqs"/&gt;
 *         &lt;element name="clickhouse" type="{http://www.knubisoft.com/testlum/testing/model/scenario}clickhouse"/&gt;
 *         &lt;element name="elasticsearch" type="{http://www.knubisoft.com/testlum/testing/model/scenario}elasticsearch"/&gt;
 *         &lt;element name="lambda" type="{http://www.knubisoft.com/testlum/testing/model/scenario}lambda"/&gt;
 *         &lt;element name="sendgrid" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgrid"/&gt;
 *         &lt;element name="ses" type="{http://www.knubisoft.com/testlum/testing/model/scenario}ses"/&gt;
 *         &lt;element name="dynamo" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dynamo"/&gt;
 *         &lt;element name="graphql" type="{http://www.knubisoft.com/testlum/testing/model/scenario}graphql"/&gt;
 *         &lt;element name="var" type="{http://www.knubisoft.com/testlum/testing/model/scenario}var"/&gt;
 *         &lt;element name="wait" type="{http://www.knubisoft.com/testlum/testing/model/scenario}wait"/&gt;
 *         &lt;element name="shell" type="{http://www.knubisoft.com/testlum/testing/model/scenario}shell"/&gt;
 *         &lt;element name="smtp" type="{http://www.knubisoft.com/testlum/testing/model/scenario}smtp"/&gt;
 *         &lt;element name="twilio" type="{http://www.knubisoft.com/testlum/testing/model/scenario}twilio"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="times" use="required" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="variations" type="{http://www.knubisoft.com/testlum/testing/model/scenario}csv" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "repeat", propOrder = {
    "commands"
})
public class Repeat
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "include", type = Include.class),
        @XmlElement(name = "repeat", type = Repeat.class),
        @XmlElement(name = "web", type = Web.class),
        @XmlElement(name = "http", type = Http.class),
        @XmlElement(name = "migrate", type = Migrate.class),
        @XmlElement(name = "postgres", type = Postgres.class),
        @XmlElement(name = "mysql", type = Mysql.class),
        @XmlElement(name = "oracle", type = Oracle.class),
        @XmlElement(name = "mongo", type = Mongo.class),
        @XmlElement(name = "redis", type = Redis.class),
        @XmlElement(name = "rabbit", type = Rabbit.class),
        @XmlElement(name = "kafka", type = Kafka.class),
        @XmlElement(name = "s3", type = S3 .class),
        @XmlElement(name = "sqs", type = Sqs.class),
        @XmlElement(name = "clickhouse", type = Clickhouse.class),
        @XmlElement(name = "elasticsearch", type = Elasticsearch.class),
        @XmlElement(name = "lambda", type = Lambda.class),
        @XmlElement(name = "sendgrid", type = Sendgrid.class),
        @XmlElement(name = "ses", type = Ses.class),
        @XmlElement(name = "dynamo", type = Dynamo.class),
        @XmlElement(name = "graphql", type = Graphql.class),
        @XmlElement(name = "var", type = Var.class),
        @XmlElement(name = "wait", type = Wait.class),
        @XmlElement(name = "shell", type = Shell.class),
        @XmlElement(name = "smtp", type = Smtp.class),
        @XmlElement(name = "twilio", type = Twilio.class)
    })
    protected List<AbstractCommand> commands;
    @XmlAttribute(name = "times", required = true)
    @XmlSchemaType(name = "positiveInteger")
    protected BigInteger times;
    @XmlAttribute(name = "variations")
    protected String variations;

    /**
     * Gets the value of the commands property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the commands property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCommands().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Include }
     * {@link Repeat }
     * {@link Web }
     * {@link Http }
     * {@link Migrate }
     * {@link Postgres }
     * {@link Mysql }
     * {@link Oracle }
     * {@link Mongo }
     * {@link Redis }
     * {@link Rabbit }
     * {@link Kafka }
     * {@link S3 }
     * {@link Sqs }
     * {@link Clickhouse }
     * {@link Elasticsearch }
     * {@link Lambda }
     * {@link Sendgrid }
     * {@link Ses }
     * {@link Dynamo }
     * {@link Graphql }
     * {@link Var }
     * {@link Wait }
     * {@link Shell }
     * {@link Smtp }
     * {@link Twilio }
     * 
     * 
     */
    public List<AbstractCommand> getCommands() {
        if (commands == null) {
            commands = new ArrayList<AbstractCommand>();
        }
        return this.commands;
    }

    /**
     * Gets the value of the times property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimes() {
        return times;
    }

    /**
     * Sets the value of the times property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTimes(BigInteger value) {
        this.times = value;
    }

    /**
     * Gets the value of the variations property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVariations() {
        return variations;
    }

    /**
     * Sets the value of the variations property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVariations(String value) {
        this.variations = value;
    }

}
