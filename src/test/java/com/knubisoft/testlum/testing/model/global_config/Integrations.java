
package com.knubisoft.testlum.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *       &lt;all&gt;
 *         &lt;element name="apis" type="{http://www.knubisoft.com/testlum/testing/model/global-config}apis" minOccurs="0"/&gt;
 *         &lt;element name="websockets" type="{http://www.knubisoft.com/testlum/testing/model/global-config}websockets" minOccurs="0"/&gt;
 *         &lt;element name="graphqlIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}graphqlIntegration" minOccurs="0"/&gt;
 *         &lt;element name="postgresIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}postgresIntegration" minOccurs="0"/&gt;
 *         &lt;element name="clickhouseIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}clickhouseIntegration" minOccurs="0"/&gt;
 *         &lt;element name="mysqlIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}mysqlIntegration" minOccurs="0"/&gt;
 *         &lt;element name="oracleIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}oracleIntegration" minOccurs="0"/&gt;
 *         &lt;element name="redisIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}redisIntegration" minOccurs="0"/&gt;
 *         &lt;element name="mongoIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}mongoIntegration" minOccurs="0"/&gt;
 *         &lt;element name="s3Integration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}s3Integration" minOccurs="0"/&gt;
 *         &lt;element name="sqsIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}sqsIntegration" minOccurs="0"/&gt;
 *         &lt;element name="kafkaIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}kafkaIntegration" minOccurs="0"/&gt;
 *         &lt;element name="rabbitmqIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}rabbitmqIntegration" minOccurs="0"/&gt;
 *         &lt;element name="dynamoIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}dynamoIntegration" minOccurs="0"/&gt;
 *         &lt;element name="elasticsearchIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}elasticsearchIntegration" minOccurs="0"/&gt;
 *         &lt;element name="lambdaIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}lambdaIntegration" minOccurs="0"/&gt;
 *         &lt;element name="sendgridIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}sendgridIntegration" minOccurs="0"/&gt;
 *         &lt;element name="sesIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}sesIntegration" minOccurs="0"/&gt;
 *         &lt;element name="smtpIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}smtpIntegration" minOccurs="0"/&gt;
 *         &lt;element name="twilioIntegration" type="{http://www.knubisoft.com/testlum/testing/model/global-config}twilioIntegration" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "integrations")
public class Integrations {

    protected Apis apis;
    protected Websockets websockets;
    protected GraphqlIntegration graphqlIntegration;
    protected PostgresIntegration postgresIntegration;
    protected ClickhouseIntegration clickhouseIntegration;
    protected MysqlIntegration mysqlIntegration;
    protected OracleIntegration oracleIntegration;
    protected RedisIntegration redisIntegration;
    protected MongoIntegration mongoIntegration;
    protected S3Integration s3Integration;
    protected SqsIntegration sqsIntegration;
    protected KafkaIntegration kafkaIntegration;
    protected RabbitmqIntegration rabbitmqIntegration;
    protected DynamoIntegration dynamoIntegration;
    protected ElasticsearchIntegration elasticsearchIntegration;
    protected LambdaIntegration lambdaIntegration;
    protected SendgridIntegration sendgridIntegration;
    protected SesIntegration sesIntegration;
    protected SmtpIntegration smtpIntegration;
    protected TwilioIntegration twilioIntegration;

    /**
     * Gets the value of the apis property.
     * 
     * @return
     *     possible object is
     *     {@link Apis }
     *     
     */
    public Apis getApis() {
        return apis;
    }

    /**
     * Sets the value of the apis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Apis }
     *     
     */
    public void setApis(Apis value) {
        this.apis = value;
    }

    /**
     * Gets the value of the websockets property.
     * 
     * @return
     *     possible object is
     *     {@link Websockets }
     *     
     */
    public Websockets getWebsockets() {
        return websockets;
    }

    /**
     * Sets the value of the websockets property.
     * 
     * @param value
     *     allowed object is
     *     {@link Websockets }
     *     
     */
    public void setWebsockets(Websockets value) {
        this.websockets = value;
    }

    /**
     * Gets the value of the graphqlIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link GraphqlIntegration }
     *     
     */
    public GraphqlIntegration getGraphqlIntegration() {
        return graphqlIntegration;
    }

    /**
     * Sets the value of the graphqlIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphqlIntegration }
     *     
     */
    public void setGraphqlIntegration(GraphqlIntegration value) {
        this.graphqlIntegration = value;
    }

    /**
     * Gets the value of the postgresIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link PostgresIntegration }
     *     
     */
    public PostgresIntegration getPostgresIntegration() {
        return postgresIntegration;
    }

    /**
     * Sets the value of the postgresIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostgresIntegration }
     *     
     */
    public void setPostgresIntegration(PostgresIntegration value) {
        this.postgresIntegration = value;
    }

    /**
     * Gets the value of the clickhouseIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link ClickhouseIntegration }
     *     
     */
    public ClickhouseIntegration getClickhouseIntegration() {
        return clickhouseIntegration;
    }

    /**
     * Sets the value of the clickhouseIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ClickhouseIntegration }
     *     
     */
    public void setClickhouseIntegration(ClickhouseIntegration value) {
        this.clickhouseIntegration = value;
    }

    /**
     * Gets the value of the mysqlIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link MysqlIntegration }
     *     
     */
    public MysqlIntegration getMysqlIntegration() {
        return mysqlIntegration;
    }

    /**
     * Sets the value of the mysqlIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link MysqlIntegration }
     *     
     */
    public void setMysqlIntegration(MysqlIntegration value) {
        this.mysqlIntegration = value;
    }

    /**
     * Gets the value of the oracleIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link OracleIntegration }
     *     
     */
    public OracleIntegration getOracleIntegration() {
        return oracleIntegration;
    }

    /**
     * Sets the value of the oracleIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link OracleIntegration }
     *     
     */
    public void setOracleIntegration(OracleIntegration value) {
        this.oracleIntegration = value;
    }

    /**
     * Gets the value of the redisIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link RedisIntegration }
     *     
     */
    public RedisIntegration getRedisIntegration() {
        return redisIntegration;
    }

    /**
     * Sets the value of the redisIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link RedisIntegration }
     *     
     */
    public void setRedisIntegration(RedisIntegration value) {
        this.redisIntegration = value;
    }

    /**
     * Gets the value of the mongoIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link MongoIntegration }
     *     
     */
    public MongoIntegration getMongoIntegration() {
        return mongoIntegration;
    }

    /**
     * Sets the value of the mongoIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link MongoIntegration }
     *     
     */
    public void setMongoIntegration(MongoIntegration value) {
        this.mongoIntegration = value;
    }

    /**
     * Gets the value of the s3Integration property.
     * 
     * @return
     *     possible object is
     *     {@link S3Integration }
     *     
     */
    public S3Integration getS3Integration() {
        return s3Integration;
    }

    /**
     * Sets the value of the s3Integration property.
     * 
     * @param value
     *     allowed object is
     *     {@link S3Integration }
     *     
     */
    public void setS3Integration(S3Integration value) {
        this.s3Integration = value;
    }

    /**
     * Gets the value of the sqsIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link SqsIntegration }
     *     
     */
    public SqsIntegration getSqsIntegration() {
        return sqsIntegration;
    }

    /**
     * Sets the value of the sqsIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link SqsIntegration }
     *     
     */
    public void setSqsIntegration(SqsIntegration value) {
        this.sqsIntegration = value;
    }

    /**
     * Gets the value of the kafkaIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link KafkaIntegration }
     *     
     */
    public KafkaIntegration getKafkaIntegration() {
        return kafkaIntegration;
    }

    /**
     * Sets the value of the kafkaIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link KafkaIntegration }
     *     
     */
    public void setKafkaIntegration(KafkaIntegration value) {
        this.kafkaIntegration = value;
    }

    /**
     * Gets the value of the rabbitmqIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link RabbitmqIntegration }
     *     
     */
    public RabbitmqIntegration getRabbitmqIntegration() {
        return rabbitmqIntegration;
    }

    /**
     * Sets the value of the rabbitmqIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link RabbitmqIntegration }
     *     
     */
    public void setRabbitmqIntegration(RabbitmqIntegration value) {
        this.rabbitmqIntegration = value;
    }

    /**
     * Gets the value of the dynamoIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link DynamoIntegration }
     *     
     */
    public DynamoIntegration getDynamoIntegration() {
        return dynamoIntegration;
    }

    /**
     * Sets the value of the dynamoIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link DynamoIntegration }
     *     
     */
    public void setDynamoIntegration(DynamoIntegration value) {
        this.dynamoIntegration = value;
    }

    /**
     * Gets the value of the elasticsearchIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link ElasticsearchIntegration }
     *     
     */
    public ElasticsearchIntegration getElasticsearchIntegration() {
        return elasticsearchIntegration;
    }

    /**
     * Sets the value of the elasticsearchIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElasticsearchIntegration }
     *     
     */
    public void setElasticsearchIntegration(ElasticsearchIntegration value) {
        this.elasticsearchIntegration = value;
    }

    /**
     * Gets the value of the lambdaIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link LambdaIntegration }
     *     
     */
    public LambdaIntegration getLambdaIntegration() {
        return lambdaIntegration;
    }

    /**
     * Sets the value of the lambdaIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link LambdaIntegration }
     *     
     */
    public void setLambdaIntegration(LambdaIntegration value) {
        this.lambdaIntegration = value;
    }

    /**
     * Gets the value of the sendgridIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link SendgridIntegration }
     *     
     */
    public SendgridIntegration getSendgridIntegration() {
        return sendgridIntegration;
    }

    /**
     * Sets the value of the sendgridIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link SendgridIntegration }
     *     
     */
    public void setSendgridIntegration(SendgridIntegration value) {
        this.sendgridIntegration = value;
    }

    /**
     * Gets the value of the sesIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link SesIntegration }
     *     
     */
    public SesIntegration getSesIntegration() {
        return sesIntegration;
    }

    /**
     * Sets the value of the sesIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link SesIntegration }
     *     
     */
    public void setSesIntegration(SesIntegration value) {
        this.sesIntegration = value;
    }

    /**
     * Gets the value of the smtpIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link SmtpIntegration }
     *     
     */
    public SmtpIntegration getSmtpIntegration() {
        return smtpIntegration;
    }

    /**
     * Sets the value of the smtpIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link SmtpIntegration }
     *     
     */
    public void setSmtpIntegration(SmtpIntegration value) {
        this.smtpIntegration = value;
    }

    /**
     * Gets the value of the twilioIntegration property.
     * 
     * @return
     *     possible object is
     *     {@link TwilioIntegration }
     *     
     */
    public TwilioIntegration getTwilioIntegration() {
        return twilioIntegration;
    }

    /**
     * Sets the value of the twilioIntegration property.
     * 
     * @param value
     *     allowed object is
     *     {@link TwilioIntegration }
     *     
     */
    public void setTwilioIntegration(TwilioIntegration value) {
        this.twilioIntegration = value;
    }

}
