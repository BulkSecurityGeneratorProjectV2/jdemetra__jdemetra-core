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
package ec.tss.tsproviders;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Defines the ability to browse a DataSource or a DataSet as a hierarchy. Note
 * that the implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
public interface HasDataHierarchy {

    /**
     * Gets the children of the specified DataSource.
     *
     * @param dataSource
     * @return a list a DataSet; might be empty but never null.
     * @throws IllegalArgumentException if the DataSource doesn't belong to this
     * provider.
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @Nonnull
    List<DataSet> children(@Nonnull DataSource dataSource) throws IllegalArgumentException, IOException;

    /**
     * Gets the children of the specified DataSet.
     *
     * @param parent
     * @return a list of DataSet; might be empty but never null.
     * @throws IllegalArgumentException if the DataSet doesn't belong to this
     * provider.
     * @throws IOException if an internal exception prevented data retrieval.
     */
    @Nonnull
    List<DataSet> children(@Nonnull DataSet parent) throws IllegalArgumentException, IOException;

    @Nonnull
    static HasDataHierarchy noOp(@Nonnull String providerName) {
        return new Util.NoOpDataHierarchy(providerName);
    }
}
