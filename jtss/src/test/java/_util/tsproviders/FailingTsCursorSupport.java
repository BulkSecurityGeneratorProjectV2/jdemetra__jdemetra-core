/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package _util.tsproviders;

import ec.tss.TsInformationType;
import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.cursor.HasTsCursor;
import ec.tss.tsproviders.cursor.TsCursor;
import ec.tss.tsproviders.utils.DataSourcePreconditions;
import java.io.IOException;

/**
 *
 * @author Philippe Charles
 */
public final class FailingTsCursorSupport implements HasTsCursor {

    private final String providerName;
    private final String message;

    public FailingTsCursorSupport(String providerName, String message) {
        this.providerName = providerName;
        this.message = message;
    }

    @Override
    public TsCursor<DataSet> getData(DataSource dataSource, TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);
        throw new IOException(message);
    }

    @Override
    public TsCursor<DataSet> getData(DataSet dataSet, TsInformationType type) throws IllegalArgumentException, IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);
        throw new IOException(message);
    }
}
