
package com.knubisoft.testlum.testing.model.scenario;

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
 *         &lt;element name="createBucket" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="removeBucket" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="uploadFile" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="downloadFile" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *         &lt;element name="removeFile" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="alias" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}aliasPattern" /&gt;
 *       &lt;attribute name="bucket" use="required" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *       &lt;attribute name="key" type="{http://www.knubisoft.com/testlum/testing/model/scenario}nonEmptyString" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "s3", propOrder = {
    "createBucket",
    "removeBucket",
    "uploadFile",
    "downloadFile",
    "removeFile"
})
public class S3
    extends AbstractCommand
{

    protected String createBucket;
    protected String removeBucket;
    protected String uploadFile;
    protected String downloadFile;
    protected String removeFile;
    @XmlAttribute(name = "alias", required = true)
    protected String alias;
    @XmlAttribute(name = "bucket", required = true)
    protected String bucket;
    @XmlAttribute(name = "key")
    protected String key;

    /**
     * Gets the value of the createBucket property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreateBucket() {
        return createBucket;
    }

    /**
     * Sets the value of the createBucket property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateBucket(String value) {
        this.createBucket = value;
    }

    /**
     * Gets the value of the removeBucket property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoveBucket() {
        return removeBucket;
    }

    /**
     * Sets the value of the removeBucket property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoveBucket(String value) {
        this.removeBucket = value;
    }

    /**
     * Gets the value of the uploadFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUploadFile() {
        return uploadFile;
    }

    /**
     * Sets the value of the uploadFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUploadFile(String value) {
        this.uploadFile = value;
    }

    /**
     * Gets the value of the downloadFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDownloadFile() {
        return downloadFile;
    }

    /**
     * Sets the value of the downloadFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDownloadFile(String value) {
        this.downloadFile = value;
    }

    /**
     * Gets the value of the removeFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoveFile() {
        return removeFile;
    }

    /**
     * Sets the value of the removeFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoveFile(String value) {
        this.removeFile = value;
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

}
