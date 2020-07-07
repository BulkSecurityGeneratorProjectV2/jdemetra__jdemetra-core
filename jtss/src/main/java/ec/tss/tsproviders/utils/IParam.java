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
package ec.tss.tsproviders.utils;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import net.jcip.annotations.ThreadSafe;

/**
 * Tool that loads/stores values from/to a key-value structure. It provides a
 * best-effort retrieval behavior where a failure returns a default value
 * instead of an error. All implementations must be thread-safe.
 *
 * @author Philippe Charles
 * @since 1.0.0
 * @param <S>
 * @param <P>
 */
@ThreadSafe
public interface IParam<S extends IConfig, P> {

    @NonNull
    P defaultValue();

    @NonNull
    P get(@NonNull S config);

    void set(IConfig.@NonNull Builder<?, S> builder, @Nullable P value);
}
