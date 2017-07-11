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
package ec.tss.tsproviders;

import static _util.SerializableUtil.fromToBytes;
import com.google.common.collect.ImmutableSortedMap;
import ec.tss.tsproviders.utils.IFormatter;
import ec.tss.tsproviders.utils.IParser;
import java.io.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Philippe Charles
 */
public class DataSourceTest {

    static final String PNAME = "SPREADSHEET", VERSION = "20111209",
            K1 = "file", V1 = "c:\\data.txt",
            K2 = "locale", V2 = "fr_BE",
            K3 = "datePattern", V3 = "yyyy-MM-dd";

    static final ImmutableSortedMap<String, String> P0 = ImmutableSortedMap.of();
    static final ImmutableSortedMap<String, String> P1 = ImmutableSortedMap.of(K1, V1);
    static final ImmutableSortedMap<String, String> P3 = ImmutableSortedMap.of(K1, V1, K2, V2, K3, V3);

    static final DataSource ZERO = new DataSource(PNAME, VERSION, P0);
    static final DataSource ONE = new DataSource(PNAME, VERSION, P1);

    static DataSource newSample() {
        return new DataSource(PNAME, VERSION, P3);
    }

    @Test
    public void testConstructor() {
        assertThat(newSample()).satisfies(o -> {
            assertThat(o.getProviderName()).isEqualTo(PNAME);
            assertThat(o.getVersion()).isEqualTo(VERSION);
            assertThat(o.getParams()).containsAllEntriesOf(P3).hasSize(3);
        });
    }

    @Test
    @SuppressWarnings("null")
    public void testDeepCopyOf() {
        assertThatThrownBy(() -> DataSource.deepCopyOf(null, VERSION, P0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSource.deepCopyOf(PNAME, null, P0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSource.deepCopyOf(PNAME, VERSION, null)).isInstanceOf(NullPointerException.class);
        assertThat(DataSource.deepCopyOf(PNAME, VERSION, P3)).isEqualTo(newSample());
    }

    @Test
    @SuppressWarnings("null")
    public void testOf() {
        assertThatThrownBy(() -> DataSource.of(null, VERSION)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSource.of(PNAME, null)).isInstanceOf(NullPointerException.class);
        assertThat(DataSource.of(PNAME, VERSION)).isEqualTo(ZERO);
    }

    @Test
    @SuppressWarnings("null")
    public void testOfKeyValue() {
        assertThatThrownBy(() -> DataSource.of(null, VERSION, K1, V1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSource.of(PNAME, null, K1, V1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSource.of(PNAME, VERSION, null, V1)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DataSource.of(PNAME, VERSION, K1, null)).isInstanceOf(NullPointerException.class);
        assertThat(DataSource.of(PNAME, VERSION, K1, V1)).isEqualTo(ONE);
    }

    @Test
    public void testEquals() {
        assertThat(newSample())
                .isEqualTo(newSample())
                .isNotSameAs(newSample())
                .isNotEqualTo(ZERO)
                .isEqualTo(new DataSource(PNAME, VERSION, ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1)));
    }

    @Test
    public void testToString() {
        assertThat(newSample().toString())
                .isEqualTo(newSample().toString())
                .isEqualTo(new DataSource(PNAME, VERSION, ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1)).toString())
                .isNotEqualTo(ZERO.toString());
    }

    @Test
    public void testHashCode() {
        assertThat(newSample().hashCode())
                .isEqualTo(newSample().hashCode())
                .isEqualTo(new DataSource(PNAME, VERSION, ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1)).hashCode())
                .isNotEqualTo(ZERO.hashCode());
    }

    @Test
    public void testGet() {
        assertThat(newSample()).satisfies(o -> {
            assertThat(o.getParams()).hasSize(3);
            assertThat(o.get(K1)).isEqualTo(V1);
            assertThat(o.get(K2)).isEqualTo(V2);
            assertThat(o.get(K3)).isEqualTo(V3);
            assertThat(o.get("hello")).isNull();
        });
    }

    @Test
    public void testBuilder() {
        assertThat(ZERO.toBuilder().build()).isEqualTo(ZERO);
        assertThat(ONE.toBuilder().build()).isEqualTo(ONE);

        DataSource.Builder builder = newSample().toBuilder();
        assertThat(builder.build())
                .isEqualTo(newSample())
                .isNotSameAs(newSample())
                .isEqualTo(builder.build())
                .isNotSameAs(builder.build());
        assertThat(builder.put(K1, "hello").build().get(K1)).isEqualTo("hello");
        assertThat(builder.put(K1, true).build().get(K1)).isEqualTo("true");
        assertThat(builder.put(K1, 123).build().get(K1)).isEqualTo("123");
        assertThat(builder.clear().build().getParams()).isEmpty();
    }

    @Test
    public void testXmlFormatter() {
        IFormatter<DataSource> formatter = DataSource.xmlFormatter(false);

        assertThat(formatter.format(newSample()))
                .isNotEmpty()
                .isEqualTo(formatter.format(newSample()))
                .isEqualTo(formatter.format(new DataSource(PNAME, VERSION, ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1))))
                .isNotEqualTo(formatter.format(ZERO));
    }

    @Test
    public void testXmlParser() {
        IFormatter<DataSource> formatter = DataSource.xmlFormatter(false);
        IParser<DataSource> parser = DataSource.xmlParser();

        assertThat(parser.parse(formatter.formatValue(newSample()).get())).isEqualTo(newSample());
    }

    @Test
    public void testSerializable() throws IOException, ClassNotFoundException {
        assertThat(fromToBytes(newSample())).isEqualTo(newSample());
    }

    @Test
    public void testUriFormatter() {
        IFormatter<DataSource> formatter = DataSource.uriFormatter();

        assertThat(formatter.format(newSample()))
                .isNotEmpty()
                .isEqualTo(formatter.format(newSample()))
                .isEqualTo(formatter.format(new DataSource(PNAME, VERSION, ImmutableSortedMap.of(K3, V3, K2, V2, K1, V1))))
                .isNotEqualTo(formatter.format(ZERO))
                .startsWith("demetra://tsprovider/");
    }

    @Test
    public void testUriParser() {
        IFormatter<DataSource> formatter = DataSource.uriFormatter();
        IParser<DataSource> parser = DataSource.uriParser();

        assertThat(parser.parse(formatter.formatValue(newSample()).get())).isEqualTo(newSample());
    }
}
