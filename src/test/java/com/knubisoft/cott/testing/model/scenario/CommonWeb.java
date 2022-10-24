
package com.knubisoft.cott.testing.model.scenario;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for common-web complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="common-web"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.knubisoft.com/cott/testing/model/scenario}ui"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="javascript" type="{http://www.knubisoft.com/cott/testing/model/scenario}javascript"/&gt;
 *         &lt;element name="navigate" type="{http://www.knubisoft.com/cott/testing/model/scenario}navigate"/&gt;
 *         &lt;element name="hovers" type="{http://www.knubisoft.com/cott/testing/model/scenario}hovers"/&gt;
 *         &lt;element name="closeSecondTab" type="{http://www.knubisoft.com/cott/testing/model/scenario}closeSecondTab"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "common-web", propOrder = {
    "javascriptOrNavigateOrHovers"
})
@XmlSeeAlso({
    Mobilebrowser.class,
    Web.class
})
public abstract class CommonWeb
    extends Ui
{

    @XmlElements({
        @XmlElement(name = "javascript", type = Javascript.class),
        @XmlElement(name = "navigate", type = Navigate.class),
        @XmlElement(name = "hovers", type = Hovers.class),
        @XmlElement(name = "closeSecondTab", type = CloseSecondTab.class)
    })
    protected List<AbstractCommand> javascriptOrNavigateOrHovers;

    /**
     * Gets the value of the javascriptOrNavigateOrHovers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the javascriptOrNavigateOrHovers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJavascriptOrNavigateOrHovers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Javascript }
     * {@link Navigate }
     * {@link Hovers }
     * {@link CloseSecondTab }
     * 
     * 
     */
    public List<AbstractCommand> getJavascriptOrNavigateOrHovers() {
        if (javascriptOrNavigateOrHovers == null) {
            javascriptOrNavigateOrHovers = new ArrayList<AbstractCommand>();
        }
        return this.javascriptOrNavigateOrHovers;
    }

}
