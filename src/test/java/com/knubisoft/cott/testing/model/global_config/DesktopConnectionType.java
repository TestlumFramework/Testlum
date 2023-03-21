
package com.knubisoft.cott.testing.model.global_config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for desktopConnectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="desktopConnectionType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.knubisoft.com/cott/testing/model/global-config}connectionType"&gt;
 *       &lt;choice&gt;
 *         &lt;element name="appiumServer" type="{http://www.knubisoft.com/cott/testing/model/global-config}appiumServer"/&gt;
 *         &lt;element name="browserStack" type="{http://www.w3.org/2001/XMLSchema}anyType" maxOccurs="0" minOccurs="0"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "desktopConnectionType")
public class DesktopConnectionType
    extends ConnectionType
{


}
