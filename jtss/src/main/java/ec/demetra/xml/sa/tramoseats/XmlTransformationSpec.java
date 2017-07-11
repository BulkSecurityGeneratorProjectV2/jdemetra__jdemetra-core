/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.demetra.xml.sa.tramoseats;

import ec.demetra.xml.XmlEmptyElement;
import ec.tss.xml.IXmlMarshaller;
import ec.tss.xml.InPlaceXmlMarshaller;
import ec.tss.xml.InPlaceXmlUnmarshaller;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for TransformationSpecType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransformationSpecType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/modelling}TransformationSpecType"&gt;
 *       &lt;sequence&gt;
 *         &lt;group ref="{ec/eurostat/jdemetra/sa/tramoseats}TransformationSpecGroup" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name="TransformationSpec")
@XmlType(name = "TransformationSpecType", propOrder = {
    "log",
    "auto"
})
public class XmlTransformationSpec
{

    @XmlElement(name = "Log")
    protected XmlEmptyElement log;
    @XmlElement(name = "Auto")
    protected XmlAutoTransformationSpec auto;

    /**
     * Gets the value of the log property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public XmlEmptyElement getLog() {
        return log;
    }

    /**
     * Sets the value of the log property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setLog(XmlEmptyElement value) {
        this.log = value;
    }

    /**
     * Gets the value of the auto property.
     * 
     * @return
     *     possible object is
     *     {@link AutoTransformationSpecType }
     *     
     */
    public XmlAutoTransformationSpec getAuto() {
        return auto;
    }

    /**
     * Sets the value of the auto property.
     * 
     * @param value
     *     allowed object is
     *     {@link AutoTransformationSpecType }
     *     
     */
    public void setAuto(XmlAutoTransformationSpec value) {
        this.auto = value;
    }
    
    public static final InPlaceXmlUnmarshaller<XmlTransformationSpec, TransformSpec> UNMARSHALLER=(XmlTransformationSpec xml, TransformSpec v) -> {
        if (xml.log != null){
            v.setFunction(DefaultTransformationType.Log);
        }else if (xml.auto != null){
            XmlAutoTransformationSpec.UNMARSHALLER.unmarshal(xml.auto, v);
        }else{
            v.setFunction(DefaultTransformationType.None);
         }
        return true;
    };

    public static final IXmlMarshaller<XmlTransformationSpec, TransformSpec> MARSHALLER=(TransformSpec v) -> {
        if (v.getFunction() ==  DefaultTransformationType.None)
            return null;
        XmlTransformationSpec xml=new XmlTransformationSpec();
        switch (v.getFunction()){
            case Log:
                xml.setLog(new XmlEmptyElement());
                break;
            case Auto:
                XmlAutoTransformationSpec xauto=new XmlAutoTransformationSpec();
                XmlAutoTransformationSpec.MARSHALLER.marshal(v, xauto);
                xml.setAuto(xauto);
                break;
        }
        return xml;
    };
}
