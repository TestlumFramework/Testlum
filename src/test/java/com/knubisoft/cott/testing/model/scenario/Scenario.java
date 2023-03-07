
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="overview" type="{http://www.knubisoft.com/cott/testing/model/scenario}overview"/&gt;
 *         &lt;element name="tags" type="{http://www.knubisoft.com/cott/testing/model/scenario}tags"/&gt;
 *         &lt;choice maxOccurs="unbounded"&gt;
 *           &lt;element name="include" type="{http://www.knubisoft.com/cott/testing/model/scenario}include"/&gt;
 *           &lt;element name="auth" type="{http://www.knubisoft.com/cott/testing/model/scenario}auth"/&gt;
 *           &lt;element name="repeat" type="{http://www.knubisoft.com/cott/testing/model/scenario}repeat"/&gt;
 *           &lt;element name="mobilebrowser" type="{http://www.knubisoft.com/cott/testing/model/scenario}mobilebrowser"/&gt;
 *           &lt;element name="web" type="{http://www.knubisoft.com/cott/testing/model/scenario}web"/&gt;
 *           &lt;element name="native" type="{http://www.knubisoft.com/cott/testing/model/scenario}native"/&gt;
 *           &lt;element name="http" type="{http://www.knubisoft.com/cott/testing/model/scenario}http"/&gt;
 *           &lt;element name="migrate" type="{http://www.knubisoft.com/cott/testing/model/scenario}migrate"/&gt;
 *           &lt;element name="postgres" type="{http://www.knubisoft.com/cott/testing/model/scenario}postgres"/&gt;
 *           &lt;element name="mysql" type="{http://www.knubisoft.com/cott/testing/model/scenario}mysql"/&gt;
 *           &lt;element name="oracle" type="{http://www.knubisoft.com/cott/testing/model/scenario}oracle"/&gt;
 *           &lt;element name="mongo" type="{http://www.knubisoft.com/cott/testing/model/scenario}mongo"/&gt;
 *           &lt;element name="redis" type="{http://www.knubisoft.com/cott/testing/model/scenario}redis"/&gt;
 *           &lt;element name="rabbit" type="{http://www.knubisoft.com/cott/testing/model/scenario}rabbit"/&gt;
 *           &lt;element name="kafka" type="{http://www.knubisoft.com/cott/testing/model/scenario}kafka"/&gt;
 *           &lt;element name="s3" type="{http://www.knubisoft.com/cott/testing/model/scenario}s3"/&gt;
 *           &lt;element name="sqs" type="{http://www.knubisoft.com/cott/testing/model/scenario}sqs"/&gt;
 *           &lt;element name="clickhouse" type="{http://www.knubisoft.com/cott/testing/model/scenario}clickhouse"/&gt;
 *           &lt;element name="elasticsearch" type="{http://www.knubisoft.com/cott/testing/model/scenario}elasticsearch"/&gt;
 *           &lt;element name="lambda" type="{http://www.knubisoft.com/cott/testing/model/scenario}lambda"/&gt;
 *           &lt;element name="sendgrid" type="{http://www.knubisoft.com/cott/testing/model/scenario}sendgrid"/&gt;
 *           &lt;element name="ses" type="{http://www.knubisoft.com/cott/testing/model/scenario}ses"/&gt;
 *           &lt;element name="dynamo" type="{http://www.knubisoft.com/cott/testing/model/scenario}dynamo"/&gt;
 *           &lt;element name="graphql" type="{http://www.knubisoft.com/cott/testing/model/scenario}graphql"/&gt;
 *           &lt;element name="websocket" type="{http://www.knubisoft.com/cott/testing/model/scenario}websocket"/&gt;
 *           &lt;element name="var" type="{http://www.knubisoft.com/cott/testing/model/scenario}generalVar"/&gt;
 *           &lt;element name="wait" type="{http://www.knubisoft.com/cott/testing/model/scenario}wait"/&gt;
 *           &lt;element name="shell" type="{http://www.knubisoft.com/cott/testing/model/scenario}shell"/&gt;
 *           &lt;element name="smtp" type="{http://www.knubisoft.com/cott/testing/model/scenario}smtp"/&gt;
 *           &lt;element name="twilio" type="{http://www.knubisoft.com/cott/testing/model/scenario}twilio"/&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="onlyThis" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="active" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *       &lt;attribute name="variations" type="{http://www.knubisoft.com/cott/testing/model/scenario}csv" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "overview",
    "tags",
    "commands"
})
@XmlRootElement(name = "scenario")
public class Scenario {

    @XmlElement(required = true)
    protected Overview overview;
    @XmlElement(required = true)
    protected Tags tags;
    @XmlElements({
        @XmlElement(name = "include", type = Include.class),
        @XmlElement(name = "auth", type = Auth.class),
        @XmlElement(name = "repeat", type = Repeat.class),
        @XmlElement(name = "mobilebrowser", type = Mobilebrowser.class),
        @XmlElement(name = "web", type = Web.class),
        @XmlElement(name = "native", type = Native.class),
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
        @XmlElement(name = "websocket", type = Websocket.class),
        @XmlElement(name = "var", type = GeneralVar.class),
        @XmlElement(name = "wait", type = Wait.class),
        @XmlElement(name = "shell", type = Shell.class),
        @XmlElement(name = "smtp", type = Smtp.class),
        @XmlElement(name = "twilio", type = Twilio.class)
    })
    protected List<AbstractCommand> commands;
    @XmlAttribute(name = "onlyThis")
    protected Boolean onlyThis;
    @XmlAttribute(name = "active")
    protected Boolean active;
    @XmlAttribute(name = "variations")
    protected String variations;

    /**
     * Gets the value of the overview property.
     * 
     * @return
     *     possible object is
     *     {@link Overview }
     *     
     */
    public Overview getOverview() {
        return overview;
    }

    /**
     * Sets the value of the overview property.
     * 
     * @param value
     *     allowed object is
     *     {@link Overview }
     *     
     */
    public void setOverview(Overview value) {
        this.overview = value;
    }

    /**
     * Gets the value of the tags property.
     * 
     * @return
     *     possible object is
     *     {@link Tags }
     *     
     */
    public Tags getTags() {
        return tags;
    }

    /**
     * Sets the value of the tags property.
     * 
     * @param value
     *     allowed object is
     *     {@link Tags }
     *     
     */
    public void setTags(Tags value) {
        this.tags = value;
    }

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
     * {@link Auth }
     * {@link Repeat }
     * {@link Mobilebrowser }
     * {@link Web }
     * {@link Native }
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
     * {@link Websocket }
     * {@link GeneralVar }
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
     * Gets the value of the onlyThis property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isOnlyThis() {
        if (onlyThis == null) {
            return false;
        } else {
            return onlyThis;
        }
    }

    /**
     * Sets the value of the onlyThis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOnlyThis(Boolean value) {
        this.onlyThis = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isActive() {
        if (active == null) {
            return true;
        } else {
            return active;
        }
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setActive(Boolean value) {
        this.active = value;
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
