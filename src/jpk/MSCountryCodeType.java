//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.07.04 at 10:07:16 PM CEST 
//


package jpk;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MSCountryCode_Type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MSCountryCode_Type">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="AT"/>
 *     &lt;enumeration value="BE"/>
 *     &lt;enumeration value="BG"/>
 *     &lt;enumeration value="CY"/>
 *     &lt;enumeration value="CZ"/>
 *     &lt;enumeration value="DK"/>
 *     &lt;enumeration value="EE"/>
 *     &lt;enumeration value="FI"/>
 *     &lt;enumeration value="FR"/>
 *     &lt;enumeration value="DE"/>
 *     &lt;enumeration value="EL"/>
 *     &lt;enumeration value="HR"/>
 *     &lt;enumeration value="HU"/>
 *     &lt;enumeration value="IE"/>
 *     &lt;enumeration value="IT"/>
 *     &lt;enumeration value="LV"/>
 *     &lt;enumeration value="LT"/>
 *     &lt;enumeration value="LU"/>
 *     &lt;enumeration value="MT"/>
 *     &lt;enumeration value="NL"/>
 *     &lt;enumeration value="PL"/>
 *     &lt;enumeration value="PT"/>
 *     &lt;enumeration value="RO"/>
 *     &lt;enumeration value="SK"/>
 *     &lt;enumeration value="SI"/>
 *     &lt;enumeration value="ES"/>
 *     &lt;enumeration value="SE"/>
 *     &lt;enumeration value="GB"/>
 *     &lt;enumeration value="IC"/>
 *     &lt;enumeration value="XI"/>
 *     &lt;enumeration value="XJ"/>
 *     &lt;enumeration value="MC"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MSCountryCode_Type", namespace = "http://crd.gov.pl/xml/schematy/dziedzinowe/mf/2013/05/23/eD/KodyCECHKRAJOW/")
@XmlEnum
public enum MSCountryCodeType {

    AT,
    BE,
    BG,
    CY,
    CZ,
    DK,
    EE,
    FI,
    FR,
    DE,
    EL,
    HR,
    HU,
    IE,
    IT,
    LV,
    LT,
    LU,
    MT,
    NL,
    PL,
    PT,
    RO,
    SK,
    SI,
    ES,
    SE,
    GB,
    IC,
    XI,
    XJ,
    MC;

    public String value() {
        return name();
    }

    public static MSCountryCodeType fromValue(String v) {
        return valueOf(v);
    }

}
