/*
* Copyright 2013 National Bank of Belgium
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
package ec.tss;

import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class TsInformation {

    /**
     * IN
     */
    public TsMoniker moniker;

    /**
     * OUT
     */
    public String name;

    /**
     * OUT
     */
    public MetaData metaData;

    /**
     * OUT
     */
    public TsData data;

    /**
     * IN/OUT
     */
    public TsInformationType type;

    /**
     * OUT
     */
    public String invalidDataCause;

    /**
     *
     */
    public TsInformation() {
        this.moniker = TsMoniker.createAnonymousMoniker();
        type = TsInformationType.UserDefined;
    }

    /**
     *
     * @param ts
     * @param type
     * @deprecated use {@link Ts#toInfo(ec.tss.TsInformationType)} instead
     */
    @Deprecated
    public TsInformation(Ts ts, TsInformationType type) {
        this.name = ts.getRawName();
        this.moniker = ts.getMoniker();
        this.type = type;
        ts.load(type);
        if (hasData()) {
            data = ts.getTsData();
        }
        if (hasMetaData()) {
            metaData = ts.getMetaData();
        }
        this.invalidDataCause = ts.getInvalidDataCause();
    }

    /**
     *
     * @param name
     * @param moniker
     * @param type
     */
    public TsInformation(String name, TsMoniker moniker, TsInformationType type) {
        this.name = name;
        this.moniker = moniker;
        this.type = type;
    }

    /**
     *
     * @return
     */
    public boolean hasData() {
        return type == TsInformationType.All || type == TsInformationType.Data
                || (type == TsInformationType.UserDefined && data != null);
    }

    /**
     *
     * @return
     */
    public boolean hasMetaData() {
        return type == TsInformationType.All
                || type == TsInformationType.MetaData
                || (type == TsInformationType.UserDefined && metaData != null);
    }

    /**
     * Converts this ts information to a Ts.
     *
     * @return a non null Ts
     */
    @Nonnull
    public Ts toTs() {
        Ts result = TsFactory.instance.createTs(name, moniker, metaData, data);
        if (data == null) {
            result.setInvalidDataCause(invalidDataCause);
        }
        return result;
    }
}
