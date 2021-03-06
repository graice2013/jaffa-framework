//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.12 at 05:13:00 PM PDT 
//


package org.jaffa.soa.services.configdomain;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         Describes a domain object to be added as a Fact into the Drools context during the processing of a SOA Event
 *         
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;ul xmlns:jxb="http://java.sun.com/xml/ns/jaxb" xmlns:xsd="http://www.w3.org/2001/XMLSchema"&gt;&lt;li&gt;&lt;b&gt;domain-class&lt;/b&gt;: The domain class&lt;/li&gt;&lt;li&gt;&lt;b&gt;domain-key&lt;/b&gt;: Collection of key fields for the domain class&lt;/li&gt;
 *         &lt;/ul&gt;
 * </pre>
 * 
 *       
 * 
 * <p>Java class for inject-domain-fact complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="inject-domain-fact">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="domain-key" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="domain-class" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "inject-domain-fact", propOrder = {
    "domainKey"
})
public class InjectDomainFact {

    @XmlElement(name = "domain-key", required = true)
    protected List<String> domainKey;
    @XmlAttribute(name = "domain-class", required = true)
    protected String domainClass;

    /**
     * Gets the value of the domainKey property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the domainKey property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDomainKey().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getDomainKey() {
        if (domainKey == null) {
            domainKey = new ArrayList<String>();
        }
        return this.domainKey;
    }

    /**
     * Gets the value of the domainClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDomainClass() {
        return domainClass;
    }

    /**
     * Sets the value of the domainClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDomainClass(String value) {
        this.domainClass = value;
    }

}
