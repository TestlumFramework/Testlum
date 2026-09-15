
package com.testlum.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * &lt;p&gt;Java class for auth complex type&lt;/p&gt;.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.&lt;/p&gt;
 * 
 * &lt;pre&gt;{&#064;code
 * &lt;complexType name="auth"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.testlum.com/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="include" type="{http://www.testlum.com/testing/model/scenario}include"/&gt;
 *         &lt;element name="repeat" type="{http://www.testlum.com/testing/model/scenario}repeat"/&gt;
 *         &lt;element name="web" type="{http://www.testlum.com/testing/model/scenario}web"/&gt;
 *         &lt;element name="http" type="{http://www.testlum.com/testing/model/scenario}http"/&gt;
 *         &lt;element name="migrate" type="{http://www.testlum.com/testing/model/scenario}migrate"/&gt;
 *         &lt;element name="postgres" type="{http://www.testlum.com/testing/model/scenario}postgres"/&gt;
 *         &lt;element name="sqlDatabase" type="{http://www.testlum.com/testing/model/scenario}sqlDatabase"/&gt;
 *         &lt;element name="mysql" type="{http://www.testlum.com/testing/model/scenario}mysql"/&gt;
 *         &lt;element name="oracle" type="{http://www.testlum.com/testing/model/scenario}oracle"/&gt;
 *         &lt;element name="mongo" type="{http://www.testlum.com/testing/model/scenario}mongo"/&gt;
 *         &lt;element name="redis" type="{http://www.testlum.com/testing/model/scenario}redis"/&gt;
 *         &lt;element name="rabbit" type="{http://www.testlum.com/testing/model/scenario}rabbit"/&gt;
 *         &lt;element name="kafka" type="{http://www.testlum.com/testing/model/scenario}kafka"/&gt;
 *         &lt;element name="s3" type="{http://www.testlum.com/testing/model/scenario}s3"/&gt;
 *         &lt;element name="sqs" type="{http://www.testlum.com/testing/model/scenario}sqs"/&gt;
 *         &lt;element name="clickhouse" type="{http://www.testlum.com/testing/model/scenario}clickhouse"/&gt;
 *         &lt;element name="elasticsearch" type="{http://www.testlum.com/testing/model/scenario}elasticsearch"/&gt;
 *         &lt;element name="sendgrid" type="{http://www.testlum.com/testing/model/scenario}sendgrid"/&gt;
 *         &lt;element name="ses" type="{http://www.testlum.com/testing/model/scenario}ses"/&gt;
 *         &lt;element name="dynamo" type="{http://www.testlum.com/testing/model/scenario}dynamo"/&gt;
 *         &lt;element name="graphql" type="{http://www.testlum.com/testing/model/scenario}graphql"/&gt;
 *         &lt;element name="smtp" type="{http://www.testlum.com/testing/model/scenario}smtp"/&gt;
 *         &lt;element name="email" type="{http://www.testlum.com/testing/model/scenario}email"/&gt;
 *         &lt;element name="twilio" type="{http://www.testlum.com/testing/model/scenario}twilio"/&gt;
 *         &lt;element name="var" type="{http://www.testlum.com/testing/model/scenario}var"/&gt;
 *         &lt;element name="wait" type="{http://www.testlum.com/testing/model/scenario}wait"/&gt;
 *         &lt;element name="ai" type="{http://www.testlum.com/testing/model/scenario}ai"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="apiAlias" type="{http://www.testlum.com/testing/model/scenario}nonEmptyString" default="DEFAULT" /&gt;
 *       &lt;attribute name="credentials" use="required" type="{http://www.testlum.com/testing/model/scenario}jsonFileExtension" /&gt;
 *       &lt;attribute name="loginEndpoint" use="required" type="{http://www.testlum.com/testing/model/scenario}endpointPattern" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * }&lt;/pre&gt;
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
        @XmlElement(name = "email", type = Email.class),
        @XmlElement(name = "twilio", type = Twilio.class),
        @XmlElement(name = "var", type = Var.class),
        @XmlElement(name = "wait", type = Wait.class),
        @XmlElement(name = "ai", type = Ai.class)
    })
    protected List<AbstractCommand> commands;
    @XmlAttribute(name = "apiAlias")
    protected String apiAlias;
    @XmlAttribute(name = "credentials", required = true)
    protected String credentials;
    @XmlAttribute(name = "loginEndpoint", required = true)
    protected String loginEndpoint;

    /**
     * Gets the value of the commands property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the commands property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getCommands().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Ai }
     * {@link Clickhouse }
     * {@link Dynamo }
     * {@link Elasticsearch }
     * {@link Email }
     * {@link Graphql }
     * {@link Http }
     * {@link Include }
     * {@link Kafka }
     * {@link Migrate }
     * {@link Mongo }
     * {@link Mysql }
     * {@link Oracle }
     * {@link Postgres }
     * {@link Rabbit }
     * {@link Redis }
     * {@link Repeat }
     * {@link S3 }
     * {@link Sendgrid }
     * {@link Ses }
     * {@link Smtp }
     * {@link SqlDatabase }
     * {@link Sqs }
     * {@link Twilio }
     * {@link Var }
     * {@link Wait }
     * {@link Web }
     * </p>
     * 
     * 
     * @return
     *     The value of the commands property.
     */
    public List<AbstractCommand> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
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
        if (apiAlias == null) {
            return "DEFAULT";
        } else {
            return apiAlias;
        }
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
