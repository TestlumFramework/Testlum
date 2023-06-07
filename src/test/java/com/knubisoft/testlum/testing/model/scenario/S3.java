
package com.knubisoft.testlum.testing.model.scenario;

import org.springframework.stereotype.Component;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for s3 complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="s3"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/testlum/testing/model/scenario}abstractCommand"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="upload" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="download" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *       &lt;attribute name="key" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="bucket" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s3", propOrder = {
    "upload",
    "download"
})
public class S3
    extends AbstractCommand
{

    protected String upload;
    protected String download;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "key", required = true)
    protected String key;
    @XmlAttribute(name = "bucket", required = true)
    protected String bucket;

    /**
     * Gets the value of the upload property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUpload() {
        return upload;
    }

    /**
     * Sets the value of the upload property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUpload(String value) {
        this.upload = value;
    }

    /**
     * Gets the value of the download property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDownload() {
        return download;
    }

    /**
     * Sets the value of the download property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDownload(String value) {
        this.download = value;
    }

    /**
     * Gets the value of the alias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlias(String value) {
        this.alias = value;
    }

    /**
     * Gets the value of the key property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the value of the key property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKey(String value) {
        this.key = value;
    }

    /**
     * Gets the value of the bucket property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Sets the value of the bucket property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBucket(String value) {
        this.bucket = value;
    }

}
