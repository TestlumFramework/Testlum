
package com.knubisoft.testlum.testing.model.scenario;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for auth complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="auth"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="include" type="{http://www.knubisoft.com/testlum/testing/model/scenario}include"/&gt;
 *         &lt;element name="repeat" type="{http://www.knubisoft.com/testlum/testing/model/scenario}repeat"/&gt;
 *         &lt;element name="web" type="{http://www.knubisoft.com/testlum/testing/model/scenario}web"/&gt;
 *         &lt;element name="http" type="{http://www.knubisoft.com/testlum/testing/model/scenario}http"/&gt;
 *         &lt;element name="migrate" type="{http://www.knubisoft.com/testlum/testing/model/scenario}migrate"/&gt;
 *         &lt;element name="postgres" type="{http://www.knubisoft.com/testlum/testing/model/scenario}postgres"/&gt;
 *         &lt;element name="sqlDatabase" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sqlDatabase"/&gt;
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
 *         &lt;element name="sendgrid" type="{http://www.knubisoft.com/testlum/testing/model/scenario}sendgrid"/&gt;
 *         &lt;element name="ses" type="{http://www.knubisoft.com/testlum/testing/model/scenario}ses"/&gt;
 *         &lt;element name="dynamo" type="{http://www.knubisoft.com/testlum/testing/model/scenario}dynamo"/&gt;
 *         &lt;element name="graphql" type="{http://www.knubisoft.com/testlum/testing/model/scenario}graphql"/&gt;
 *         &lt;element name="smtp" type="{http://www.knubisoft.com/testlum/testing/model/scenario}smtp"/&gt;
 *         &lt;element name="twilio" type="{http://www.knubisoft.com/testlum/testing/model/scenario}twilio"/&gt;
 *         &lt;element name="var" type="{http://www.knubisoft.com/testlum/testing/model/scenario}var"/&gt;
 *         &lt;element name="wait" type="{http://www.knubisoft.com/testlum/testing/model/scenario}wait"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="apiAlias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="credentials" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}jsonFileExtension" /&gt;
 *       &lt;attribute name="loginEndpoint" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}endpointPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "auth", propOrder = {
    "commands"
})
public class Auth
    extends AbstractCommand
{

    @XmlElements({
        @XmlElement(name = "include", type = Include.class),
        @XmlElement(name = "repeat", type = Repeat.class),
        @XmlElement(name = "web", type = Web.class),
        @XmlElement(name = "http", type = Http.class),
        @XmlElement(name = "migrate", type = Migrate.class),
        @XmlElement(name = "postgres", type = Postgres.class),
        @XmlElement(name = "sqlDatabase", type = SqlDatabase.class),
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
        @XmlElement(name = "sendgrid", type = Sendgrid.class),
        @XmlElement(name = "ses", type = Ses.class),
        @XmlElement(name = "dynamo", type = Dynamo.class),
        @XmlElement(name = "graphql", type = Graphql.class),
        @XmlElement(name = "smtp", type = Smtp.class),
        @XmlElement(name = "twilio", type = Twilio.class),
        @XmlElement(name = "var", type = Var.class),
        @XmlElement(name = "wait", type = Wait.class)
    })
    protected List<AbstractCommand> commands;
    @XmlAttribute(name = "apiAlias", required = true)
    protected String apiAlias;
    @XmlAttribute(name = "credentials", required = true)
    protected String credentials;
    @XmlAttribute(name = "loginEndpoint", required = true)
    protected String loginEndpoint;

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
     * {@link SqlDatabase }
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
     * {@link Sendgrid }
     * {@link Ses }
     * {@link Dynamo }
     * {@link Graphql }
     * {@link Smtp }
     * {@link Twilio }
     * {@link Var }
     * {@link Wait }
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
     * Gets the value of the apiAlias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApiAlias() {
        return apiAlias;
    }

    /**
     * Sets the value of the apiAlias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApiAlias(String value) {
        this.apiAlias = value;
    }

    /**
     * Gets the value of the credentials property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCredentials() {
        return credentials;
    }

    /**
     * Sets the value of the credentials property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCredentials(String value) {
        this.credentials = value;
    }

    /**
     * Gets the value of the loginEndpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLoginEndpoint() {
        return loginEndpoint;
    }

    /**
     * Sets the value of the loginEndpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLoginEndpoint(String value) {
        this.loginEndpoint = value;
    }

}
