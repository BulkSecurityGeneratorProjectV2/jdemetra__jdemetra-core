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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import static java.util.Locale.*;
import java.util.function.BiConsumer;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.util.DateUtil;
import org.junit.Test;
import static ec.tss.tsproviders.utils.DataFormat.of;

/**
 *
 * @author Philippe Charles
 */
public class DataFormatTest {

    @Test
    public void testFactories() {
        assertThat(of(null, null, null))
                .extracting(DataFormat::getLocale, DataFormat::getDatePattern, DataFormat::getNumberPattern)
                .containsExactly(null, "", "");

        assertThat(of(FRENCH, null, null))
                .extracting(DataFormat::getLocale, DataFormat::getDatePattern, DataFormat::getNumberPattern)
                .containsExactly(FRENCH, "", "");

        assertThat(of(null, "yyyy", null))
                .extracting(DataFormat::getLocale, DataFormat::getDatePattern, DataFormat::getNumberPattern)
                .containsExactly(null, "yyyy", "");

        assertThat(of(null, null, "#0.00"))
                .extracting(DataFormat::getLocale, DataFormat::getDatePattern, DataFormat::getNumberPattern)
                .containsExactly(null, "", "#0.00");
    }

    @Test
    public void testEquals() {
        assertThat(of(null, null, null)).isEqualTo(of(null, null, null));
        assertThat(of(JAPAN, null, null))
                .isEqualTo(of(JAPAN, null, null))
                .isNotEqualTo(of(null, null, null))
                .isNotEqualTo(of(FRANCE, null, null));
        assertThat(of(JAPAN, "MMMdd", "#"))
                .isEqualTo(of(JAPAN, "MMMdd", "#"))
                .isNotEqualTo(of(null, null, null))
                .isNotEqualTo(of(FRANCE, "MMMdd", "#"));
    }

    @Test
    public void testNewNumberFormat() throws ParseException {
        assertThatIllegalArgumentException().isThrownBy(() -> of(FRENCH, null, ",.,.,.").newNumberFormat());
        assertThatIllegalArgumentException().isThrownBy(() -> of(null, null, ",.,.,.").newNumberFormat());

        double value = 1234.5d;

        NumberFormat f1 = of(FRANCE, null, null).newNumberFormat();
        assertThat(f1.parse("1234,5")).isEqualTo(value);
        assertThat(f1.format(value)).isEqualTo("1" + getGroupingSeparator(f1) + "234,5");

        NumberFormat f2 = of(US, null, null).newNumberFormat();
        assertThat(f2.parse("1,234.5")).isEqualTo(value);
        assertThat(f2.format(value)).isEqualTo("1,234.5");

        NumberFormat f3 = of(FRANCE, null, "#0.00").newNumberFormat();
        assertThat(f3.parse("1234,50")).isEqualTo(value);
        assertThat(f3.format(value)).isEqualTo("1234,50");

        NumberFormat f4 = of(US, null, "#0.00 €").newNumberFormat();
        assertThat(f4.parse("1234.50 €")).isEqualTo(value);
        assertThat(f4.format(value)).isEqualTo("1234.50 €");
    }

    @Test
    public void testNewDateFormat() throws ParseException {
        assertThatIllegalArgumentException().isThrownBy(() -> of(FRENCH, "c", null).newDateFormat());
        assertThatIllegalArgumentException().isThrownBy(() -> of(null, "c", null).newDateFormat());

        Date date = DateUtil.parse("2000-01-01");

        DateFormat f1 = of(FRANCE, null, null).newDateFormat();
        assertThat(f1.format(date)).isEqualTo("1 janv. 2000");
        assertThat(f1.parse("1 janv. 2000")).isEqualTo(date);

        DateFormat f2 = of(US, null, null).newDateFormat();
        assertThat(f2.format(date)).isEqualTo("Jan 1, 2000");
        assertThat(f2.parse("Jan 1, 2000")).isEqualTo(date);

        DateFormat f3 = of(FRANCE, "yyyy-MMM", null).newDateFormat();
        assertThat(f3.format(date)).isEqualTo("2000-janv.");
        assertThat(f3.parse("2000-janv.")).isEqualTo(date);

        DateFormat f4 = of(US, "yyyy-MMM", null).newDateFormat();
        assertThat(f4.format(date)).isEqualTo("2000-Jan");
        assertThat(f4.parse("2000-Jan")).isEqualTo(date);

        BiConsumer<DataFormat, DateFormat> equivalence = (o, r) -> {
            try {
                DateFormat l = o.newDateFormat();
                assertThat(l.parseObject(r.format(date))).isEqualTo(date);
                assertThat(r.parseObject(l.format(date))).isEqualTo(date);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        };

        equivalence.accept(of(FRANCE, "yyyy-MMM", null), new SimpleDateFormat("yyyy-MMM", FRANCE));
        equivalence.accept(of(null, "yyyy-MMM", null), new SimpleDateFormat("yyyy-MMM"));
        equivalence.accept(of(FRANCE, null, null), SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, FRANCE));
        equivalence.accept(of(null, null, null), SimpleDateFormat.getDateInstance());
    }

    private static char getGroupingSeparator(NumberFormat format) {
        return (((DecimalFormat) format).getDecimalFormatSymbols()).getGroupingSeparator();
    }
}
