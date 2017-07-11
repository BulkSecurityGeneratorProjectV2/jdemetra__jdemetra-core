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
package ec.demetra.xml.regression;

import com.google.common.base.Strings;
import ec.demetra.xml.core.XmlTsData;
import ec.tstoolkit.timeseries.regression.TsVariable;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * Default static regression variable. Data must be available. The name of the
 * tsdata sub-item should be considered as a description, while the "name"
 * attribute is the actual name of the variable
 *
 *
 * <p>
 * Java class for StaticTsVariableType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 *
 * <pre>
 * &lt;complexType name="StaticTsVariableType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{ec/eurostat/jdemetra/core}TsVariableType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TsData" type="{ec/eurostat/jdemetra/core}TsDataType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaticTsVariableType", propOrder = {
    "tsData"
})
public class XmlStaticTsVariable
        extends XmlTsVariable {

    @XmlElement(name = "TsData", required = true)
    protected XmlTsData tsData;

    /**
     * Gets the value of the tsData property.
     *
     * @return possible object is {@link TsDataType }
     *
     */
    public XmlTsData getTsData() {
        return tsData;
    }

    /**
     * Sets the value of the tsData property.
     *
     * @param value allowed object is {@link TsDataType }
     *
     */
    public void setTsData(XmlTsData value) {
        this.tsData = value;
    }

    public static class Adapter extends XmlAdapter<XmlStaticTsVariable, TsVariable> {

        @Override
        public TsVariable unmarshal(XmlStaticTsVariable v) {

            return new TsVariable(v.tsData.getName(), XmlTsData.UNMARSHALLER.unmarshal(v.tsData));
        }

        @Override
        public XmlStaticTsVariable marshal(TsVariable v) {
            XmlStaticTsVariable x = new XmlStaticTsVariable();
            x.tsData = new XmlTsData();
            XmlTsData.MARSHALLER.marshal(v.getTsData(), x.tsData);
            String desc = v.getDescription(TsFrequency.Undefined);
            if (!Strings.isNullOrEmpty(desc)) {
                x.tsData.setName(desc);
            }
            return x;
        }
    }

    private static final Adapter ADAPTER = new Adapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }
}
