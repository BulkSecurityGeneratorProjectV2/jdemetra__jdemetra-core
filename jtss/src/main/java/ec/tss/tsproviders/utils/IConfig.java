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

import ec.tstoolkit.design.IBuilder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import net.jcip.annotations.Immutable;

/**
 * Defines an immutable key-value store.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
@Immutable
public interface IConfig {

    /**
     * Returns all the parameters of this config sorted by key.
     *
     * @return a non-null map
     */
    @NonNull
    SortedMap<String, String> getParams();

    /**
     * Returns a parameter by its key.
     *
     * @param key non-null key of the requested parameter
     * @return a parameter if available, null otherwise
     */
    @Nullable
    default String get(@NonNull String key) {
        return getParams().get(key);
    }

    /**
     * Returns an optional parameter by its key.
     *
     * @param key non-null of the requested parameter
     * @return an optional parameter
     * @since 2.2.0
     */
    @NonNull
    default Optional<String> getParam(@NonNull String key) {
        return Optional.of(getParams().get(key));
    }

    /**
     * Performs the given action for each parameter in this config until all
     * parameters have been processed or the action throws an exception.
     *
     * @param action The non-null action to be performed for each entry
     * @since 2.2.0
     */
    default void forEach(@NonNull BiConsumer<? super String, ? super String> action) {
        getParams().forEach(action);
    }

    /**
     * Returns a sequential {@code Stream} with the parameters of this config as
     * its source.
     *
     * @return a non-null sequential {@code Stream} over the parameters in this
     * config
     * @since 2.2.0
     */
    @NonNull
    default Stream<Entry<String, String>> stream() {
        return getParams().entrySet().stream();
    }

    public interface Builder<THIS, T extends IConfig> extends IBuilder<T> {

        /**
         * Put a key-value pair.
         *
         * @param key a non-null key
         * @param value a non-null value
         * @return itself
         */
        @NonNull
        THIS put(@NonNull String key, @NonNull String value);

        @NonNull
        default THIS put(@NonNull String key, int value) {
            return put(key, String.valueOf(value));
        }

        @NonNull
        default THIS put(@NonNull String key, boolean value) {
            return put(key, String.valueOf(value));
        }

        @NonNull
        default THIS put(Map.@NonNull Entry<String, String> entry) {
            return put(entry.getKey(), entry.getValue());
        }

        @NonNull
        default THIS putAll(@NonNull Map<String, String> map) {
            map.forEach(this::put);
            return (THIS) this;
        }

        @NonNull
        default <V> THIS put(@NonNull IParam<T, V> param, V value) {
            param.set(this, value);
            return (THIS) this;
        }
    }
}
