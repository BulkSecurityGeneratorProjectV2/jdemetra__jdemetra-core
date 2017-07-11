/*
 * Copyright 2017 National Bank of Belgium
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
package ec.tss.tsproviders.cursor;

import _util.IOExceptionUtil.FirstIO;
import _util.IOExceptionUtil.SecondIO;
import static _util.IOExceptionUtil.asCloseable;
import _util.tsproviders.ResourceWatcher;
import static _util.tsproviders.TsCursorUtil.forEachId;
import static _util.tsproviders.TsCursorUtil.readAll;
import static _util.tsproviders.TsCursorUtil.readAllAndClose;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterators.singletonIterator;
import com.google.common.collect.Maps;
import ec.tss.tsproviders.cursor.TsCursors.EmptyCursor;
import ec.tss.tsproviders.cursor.TsCursors.FilteringCursor;
import ec.tss.tsproviders.cursor.TsCursors.InMemoryCursor;
import ec.tss.tsproviders.cursor.TsCursors.IteratingCursor;
import ec.tss.tsproviders.cursor.TsCursors.OnCloseCursor;
import ec.tss.tsproviders.cursor.TsCursors.SingletonCursor;
import ec.tss.tsproviders.cursor.TsCursors.TransformingCursor;
import ec.tss.tsproviders.cursor.TsCursors.WithMetaDataCursor;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import static java.util.function.Function.identity;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import ec.tss.tsproviders.cursor.TsCursors.CachingCursor;
import ec.tss.tsproviders.cursor.TsCursors.XCollection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static ec.tss.tsproviders.cursor.TsCursors.CLOSE_ISE;
import static ec.tss.tsproviders.cursor.TsCursors.ID_FILTER_NPE;
import static ec.tss.tsproviders.cursor.TsCursors.NEXT_ISE;
import static com.google.common.collect.Iterators.forArray;
import static ec.tss.tsproviders.cursor.TsCursors.CLOSE_HANDLER_NPE;
import static ec.tss.tsproviders.cursor.TsCursors.ID_TRANSFORMER_NPE;
import static ec.tss.tsproviders.cursor.TsCursors.META_DATA_NPE;

/**
 *
 * @author Philippe Charles
 */
public class TsCursorsTest {

    private final String someKey = "hello";
    private final OptionalTsData someData = OptionalTsData.present(TsData.random(TsFrequency.Monthly, 1));
    private final Map<String, String> someMeta = ImmutableMap.of("key", "value");

    private final Function<String, String> goodIdFunc = String::toUpperCase;
    private final Function<String, OptionalTsData> goodDataFunc = o -> someData;
    private final Function<String, Map<String, String>> goodMetaFunc = o -> someMeta;
    private final Function<String, String> badIdFunc = o -> null;
    private final Function<String, OptionalTsData> badDataFunc = o -> null;
    private final Function<String, Map<String, String>> badMetaFunc = o -> null;

    @Test
    public void testEmptyCursor() throws IOException {
        try (EmptyCursor cursor = new EmptyCursor()) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
            assertApi(cursor);
        }

        try (EmptyCursor cursor = new EmptyCursor().filter(o -> false)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (EmptyCursor cursor = new EmptyCursor().filter(o -> true)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (EmptyCursor cursor = new EmptyCursor().transform(identity())) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (InMemoryCursor cursor = new EmptyCursor().withMetaData(someMeta)) {
            assertThat(cursor.getMetaData()).isEqualTo(someMeta);
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testSingletonCursor() throws IOException {
        Supplier<SingletonCursor<String>> example = () -> new SingletonCursor<>(someKey, someData, someMeta, "LABEL");

        try (SingletonCursor<String> cursor = example.get()) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, someKey, someData, someMeta, "LABEL");
            assertThat(cursor.nextSeries()).isFalse();
            assertApi(cursor);
        }

        try (SingletonCursor<String> cursor = example.get().filter(o -> false)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (SingletonCursor<String> cursor = example.get().filter(o -> true)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isTrue();
        }

        try (SingletonCursor<String> cursor = example.get().transform(String::toUpperCase)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "HELLO", someData, someMeta, "LABEL");
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (InMemoryCursor cursor = example.get().withMetaData(someMeta)) {
            assertThat(cursor.getMetaData()).isEqualTo(someMeta);
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testIteratingCursor() throws IOException {
        Supplier<IteratingCursor<String, String>> example = () -> new IteratingCursor<>(forArray("hello", "world"), goodIdFunc, goodDataFunc, goodMetaFunc, TsCursorsTest::quote);

        try (IteratingCursor<String, String> cursor = example.get()) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "HELLO", someData, someMeta, "'hello'");
            assertNextSeries(cursor, "WORLD", someData, someMeta, "'world'");
            assertThat(cursor.nextSeries()).isFalse();
            assertApi(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().filter(o -> false)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertThat(cursor.nextSeries()).isFalse();
            assertApi(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().filter(o -> o.startsWith("HE"))) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "HELLO", someData, someMeta, "'hello'");
            assertThat(cursor.nextSeries()).isFalse();
            assertApi(cursor);
        }

        try (IteratingCursor<String, String> cursor = example.get().transform(String::toLowerCase)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "hello", someData, someMeta, "'hello'");
            assertNextSeries(cursor, "world", someData, someMeta, "'world'");
            assertThat(cursor.nextSeries()).isFalse();
            assertApi(cursor);
        }

        try (InMemoryCursor cursor = example.get().withMetaData(someMeta)) {
            assertThat(cursor.getMetaData()).isEqualTo(someMeta);
        }

        try (IteratingCursor<String, String> cursor = example.get().filter(o -> o.startsWith("HE")).transform(String::toLowerCase)) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "hello", someData, someMeta, "'hello'");
            assertThat(cursor.nextSeries()).isFalse();
        }

        try (IteratingCursor<String, String> cursor = example.get().transform(String::toLowerCase).filter(o -> o.startsWith("he"))) {
            assertThat(cursor.getMetaData()).isEmpty();
            assertNextSeries(cursor, "hello", someData, someMeta, "'hello'");
            assertThat(cursor.nextSeries()).isFalse();
        }

        assertThatThrownBy(() -> readAllAndClose(new IteratingCursor<>(singletonIterator(someKey), badIdFunc, goodDataFunc, goodMetaFunc, Object::toString)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("id");
        assertThatThrownBy(() -> readAllAndClose(new IteratingCursor<>(singletonIterator(someKey), goodIdFunc, badDataFunc, goodMetaFunc, Object::toString)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("data");
        assertThatThrownBy(() -> readAllAndClose(new IteratingCursor<>(singletonIterator(someKey), goodIdFunc, goodDataFunc, badMetaFunc, Object::toString)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("meta");
    }

    @Test
    @SuppressWarnings("null")
    public void testTransformingCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().transform(null)).isInstanceOf(NullPointerException.class).hasMessage(ID_TRANSFORMER_NPE);

        try (TransformingCursor<String, String> cursor = new TransformingCursor<>(delegateFactory.get(), goodIdFunc)) {
            List<String> ids = new ArrayList<>();
            forEachId(cursor, ids::add);
            assertThat(ids).containsExactly("HELLO", "WORLD");
        }

        assertThatThrownBy(() -> readAllAndClose(new TransformingCursor<>(delegateFactory.get(), badIdFunc)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("id");
    }

    @Test
    @SuppressWarnings("null")
    public void testFilteringCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().filter(null)).isInstanceOf(NullPointerException.class).hasMessage(ID_FILTER_NPE);

        try (FilteringCursor<String> cursor = new FilteringCursor<>(delegateFactory.get(), o -> o.startsWith("w"))) {
            List<String> ids = new ArrayList<>();
            forEachId(cursor, ids::add);
            assertThat(ids).containsExactly("world");
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testMetaDataCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().withMetaData(null)).isInstanceOf(NullPointerException.class).hasMessage(META_DATA_NPE);

        try (WithMetaDataCursor<String> cursor = new WithMetaDataCursor<>(delegateFactory.get(), Collections.singletonMap("key", "value"))) {
            assertThat(cursor.getMetaData()).containsExactly(Maps.immutableEntry("key", "value"));
        }
    }

    @Test
    @SuppressWarnings("null")
    public void testOnCloseCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        assertThatThrownBy(() -> delegateFactory.get().onClose(null)).isInstanceOf(NullPointerException.class).hasMessage(CLOSE_HANDLER_NPE);

        ResourceWatcher<?> watcher = ResourceWatcher.usingId();
        try (OnCloseCursor<?> cursor = new OnCloseCursor<>(delegateFactory.get(), watcher.watchAsCloseable("test"))) {
            assertThat(watcher.isLeakingResources()).isTrue();
        }
        assertThat(watcher.isLeakingResources()).isFalse();

        assertThatThrownBy(() -> TsCursor.empty().onClose(asCloseable(FirstIO::new)).onClose(asCloseable(SecondIO::new)).close())
                .isInstanceOf(FirstIO.class)
                .satisfies(o -> {
                    assertThat(o.getSuppressed())
                            .hasSize(1)
                            .hasAtLeastOneElementOfType(SecondIO.class);
                });
    }

    @Test
    public void testCachingCursor() throws IOException {
        Supplier<TsCursor<String>> delegateFactory = () -> TsCursor.from(forArray("hello", "world"));

        ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();
        try (CachingCursor<String, ?> cursor = new CachingCursor<>(delegateFactory.get(), "key", cache)) {
            assertApi(cursor);
        }
        assertThat(cache).containsOnlyKeys("key");
        assertThat(cache.get("key")).isInstanceOf(XCollection.class);
    }

    private static <ID> void assertNextSeries(InMemoryCursor<ID> cursor, ID id, OptionalTsData data, Map<String, String> meta, String label) {
        assertThat(cursor.nextSeries()).isTrue();
        assertThat(cursor.getSeriesId()).isEqualTo(id);
        assertThat(cursor.getSeriesData()).isEqualTo(data);
        assertThat(cursor.getSeriesMetaData()).isEqualTo(meta);
        assertThat(cursor.getSeriesLabel()).isEqualTo(label);
    }

    private static void assertApi(TsCursor<?> cursor) throws IOException {
        assertInputNotNull(cursor);
        assertNoMoreSeriesState(cursor);
        assertCloseState(cursor);
    }

    @SuppressWarnings("null")
    private static void assertInputNotNull(TsCursor<?> cursor) {
        assertThatThrownBy(() -> cursor.filter(null)).isInstanceOf(NullPointerException.class).hasMessage(ID_FILTER_NPE);
        assertThatThrownBy(() -> cursor.onClose(null)).isInstanceOf(NullPointerException.class).hasMessage(CLOSE_HANDLER_NPE);
        assertThatThrownBy(() -> cursor.transform(null)).isInstanceOf(NullPointerException.class).hasMessage(ID_TRANSFORMER_NPE);
        assertThatThrownBy(() -> cursor.withMetaData(null)).isInstanceOf(NullPointerException.class).hasMessage(META_DATA_NPE);
    }

    private static void assertNoMoreSeriesState(TsCursor<?> cursor) throws IOException {
        assertThat(cursor.isClosed()).isFalse();
        readAll(cursor);
        cursor.nextSeries(); // subsequent calls must have no effects
        assertThatThrownBy(() -> cursor.getSeriesId()).isInstanceOf(IllegalStateException.class).hasMessage(NEXT_ISE);
        assertThatThrownBy(() -> cursor.getSeriesData()).isInstanceOf(IllegalStateException.class).hasMessage(NEXT_ISE);
        assertThatThrownBy(() -> cursor.getSeriesMetaData()).isInstanceOf(IllegalStateException.class).hasMessage(NEXT_ISE);
        assertThatThrownBy(() -> cursor.getSeriesLabel()).isInstanceOf(IllegalStateException.class).hasMessage(NEXT_ISE);
    }

    private static void assertCloseState(TsCursor<?> cursor) throws IOException {
        assertThat(cursor.isClosed()).isFalse();
        cursor.close();
        cursor.close(); // subsequent calls must have no effects
        assertThat(cursor.isClosed()).isTrue();
        assertThatThrownBy(() -> cursor.getMetaData()).isInstanceOf(IllegalStateException.class).hasMessage(CLOSE_ISE);
        assertThatThrownBy(() -> cursor.nextSeries()).isInstanceOf(IllegalStateException.class).hasMessage(CLOSE_ISE);
        assertThatThrownBy(() -> cursor.getSeriesId()).isInstanceOf(IllegalStateException.class).hasMessage(CLOSE_ISE);
        assertThatThrownBy(() -> cursor.getSeriesData()).isInstanceOf(IllegalStateException.class).hasMessage(CLOSE_ISE);
        assertThatThrownBy(() -> cursor.getSeriesMetaData()).isInstanceOf(IllegalStateException.class).hasMessage(CLOSE_ISE);
        assertThatThrownBy(() -> cursor.getSeriesLabel()).isInstanceOf(IllegalStateException.class).hasMessage(CLOSE_ISE);
        assertThat(cursor.filter(o -> true)).isNotNull();
        assertThat(cursor.onClose(() -> {
        })).isNotNull();
        assertThat(cursor.transform(identity())).isNotNull();
        assertThat(cursor.withMetaData(Collections.emptyMap())).isNotNull();
    }

    private static <X> String quote(X object) {
        return "'" + object + "'";
    }
}
