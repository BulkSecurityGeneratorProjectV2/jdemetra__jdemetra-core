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
package demetra.timeseries;

import static demetra.timeseries.TsUnit.*;
import static demetra.timeseries.TsPeriod.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class TsPeriodTest {

    @Test
    public void testFactories() {
        assertThatThrownBy(() -> of(null, d2011_02_01_0000)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(MONTH, (LocalDate) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(null, d2011_02_01)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(MONTH, (LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(null, 0)).isInstanceOf(NullPointerException.class);

        assertThat(monthly(2011, 2))
                .extracting("reference", "unit", "id")
                .containsExactly(EPOCH, MONTH, 493L);

        assertThat(quarterly(2011, 2))
                .extracting("reference", "unit", "id")
                .containsExactly(EPOCH, QUARTER, 165L);
    }

    @Test
    public void testBuilder() {
        assertThat(builder().build()).isNotNull();

        assertThat(builder().unit(MONTH).build())
                .isEqualTo(new TsPeriod(EPOCH, MONTH, 0))
                .extracting("reference", "unit", "id")
                .containsExactly(EPOCH, MONTH, 0L);

        assertThat(builder().unit(MONTH).reference(someReference).build())
                .isEqualTo(new TsPeriod(someReference, MONTH, -135L))
                .extracting("reference", "unit", "id")
                .containsExactly(someReference, MONTH, -135L);
        assertThat(builder().unit(of(2, ChronoUnit.DECADES)).reference(someReference).plus(2).build())
                .isEqualTo(new TsPeriod(someReference, of(2, ChronoUnit.DECADES), 1))
                .extracting("reference", "unit", "id")
                .containsExactly(someReference, of(2, ChronoUnit.DECADES), 1L);

        assertThat(builder()).satisfies(o -> {
            assertThat(o.toShortString()).isEqualTo("P1M#0");
            assertThat(o.plus(1).toShortString()).isEqualTo("P1M#1");
            assertThat(o.plus(-2).toShortString()).isEqualTo("P1M#-1");
        });

        assertThat(TsPeriod.builder().unit(DAY).reference(someReference).id(2).build())
                .isEqualTo(TsPeriod.monthly(1970, 1).withUnit(DAY).withReference(someReference).withId(2))
                .isNotEqualTo(TsPeriod.builder().unit(DAY).id(2).reference(someReference).build());
    }

    @Test
    public void testEquals() {
        assertThat(of(YEAR, d2011_02_01))
                .isEqualTo(of(YEAR, d2011_02_01))
                .isNotEqualTo(of(YEAR, d2011_02_01).next())
                .isNotEqualTo(of(YEAR, d2011_02_01).withReference(someReference))
                .isNotEqualTo(of(YEAR, d2011_02_01).withUnit(HOUR))
                .isNotEqualTo(of(YEAR, d2011_02_01).withDate(d2011_02_01.plusYears(1)))
                .isEqualTo(of(YEAR, d2011_02_01).withDate(d2011_02_01_1337));
    }

    @Test
    public void testRange() {
        assertThat(monthly(2011, 2)).satisfies(o -> {
            assertThat(o.start()).isEqualTo(d2011_02_01_0000);
            assertThat(o.end()).isEqualTo(d2011_02_01_0000.plusMonths(1));
            assertThat(o.contains(d2011_02_01_0000)).isTrue();
            assertThat(o.contains(d2011_02_01_0000.plusDays(1))).isTrue();
            assertThat(o.contains(d2011_02_01_0000.plusMonths(1))).isFalse();
        });

        assertThat(minutely(2011, 2, 1, 13, 37)).satisfies(o -> {
            assertThat(o.start()).isEqualTo(d2011_02_01_1337);
            assertThat(o.end()).isEqualTo(d2011_02_01_1337.plusMinutes(1));
            assertThat(o.contains(d2011_02_01_1337)).isTrue();
            assertThat(o.contains(d2011_02_01_1337.plusSeconds(1))).isTrue();
            assertThat(o.contains(d2011_02_01_1337.plusMinutes(1))).isFalse();
        });
    }

    @Test
    public void testComparable() {
        assertThat(monthly(2011, 2))
                .isEqualByComparingTo(monthly(2011, 2))
                .isLessThan(monthly(2011, 3))
                .isGreaterThan(monthly(2011, 1));

        assertThat(monthly(2011, 2).withReference(someReference))
                .isEqualByComparingTo(monthly(2011, 2))
                .isLessThan(monthly(2011, 3))
                .isGreaterThan(monthly(2011, 1));

        assertThatThrownBy(() -> monthly(2011, 2).compareTo(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsAfter() {
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isAfter(monthly(2011, 1))).isTrue();

        assertThat(monthly(2011, 2).withReference(someReference).isAfter(monthly(2011, 3))).isFalse();
        assertThat(monthly(2011, 2).withReference(someReference).isAfter(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withReference(someReference).isAfter(monthly(2011, 1))).isTrue();

        assertThatThrownBy(() -> monthly(2011, 2).isAfter(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIsBefore() {
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).isBefore(monthly(2011, 1))).isFalse();

        assertThat(monthly(2011, 2).withReference(someReference).isBefore(monthly(2011, 3))).isTrue();
        assertThat(monthly(2011, 2).withReference(someReference).isBefore(monthly(2011, 2))).isFalse();
        assertThat(monthly(2011, 2).withReference(someReference).isBefore(monthly(2011, 1))).isFalse();

        assertThatThrownBy(() -> monthly(2011, 2).isBefore(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testNext() {
        assertThat(of(YEAR, d2011_02_01).next()).isEqualTo(of(YEAR, d2011_02_01.plusYears(1)));
    }

    @Test
    public void testPlus() {
        assertThat(of(YEAR, d2011_02_01).plus(1)).isEqualTo(of(YEAR, d2011_02_01).next());
        assertThat(of(YEAR, d2011_02_01).plus(2)).isEqualTo(of(YEAR, d2011_02_01.plusYears(2)));
        assertThat(of(YEAR, d2011_02_01).plus(-1)).isEqualTo(of(YEAR, d2011_02_01.plusYears(-1)));
        assertThat(of(HOUR, d2011_02_01).plus(11)).isEqualTo(of(HOUR, d2011_02_01_0000.plus(11, ChronoUnit.HOURS)));
    }

    @Test
    public void testWithFreq() {
        assertThatThrownBy(() -> of(YEAR, d2011_02_01).withUnit(null)).isInstanceOf(NullPointerException.class);
        assertThat(of(YEAR, d2011_02_01).withUnit(MONTH)).isEqualTo(of(MONTH, LocalDate.of(2011, 1, 1)));
        assertThat(of(MONTH, d2011_02_01).withUnit(YEAR)).isEqualTo(of(YEAR, d2011_02_01));
    }

    @Test
    public void testWithReference() {
        assertThat(of(DAY, d2011_02_01).withReference(someReference)).isEqualTo(new TsPeriod(someReference, DAY, 10898));
        assertThat(of(DAY, d2011_02_01.plusDays(1)).withReference(someReference)).isEqualTo(new TsPeriod(someReference, DAY, 10899));
    }

    @Test
    public void testWithDate() {
        assertThatThrownBy(() -> of(YEAR, d2011_02_01).withDate((LocalDate) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> of(YEAR, d2011_02_01).withDate((LocalDateTime) null)).isInstanceOf(NullPointerException.class);
        assertThat(of(DAY, d2011_02_01).withDate(d2011_02_01.plusDays(3))).isEqualTo(of(DAY, d2011_02_01.plusDays(3)));
    }

    @Test
    public void testToShortString() {
        assertThat(of(YEAR, d2011_02_01).toShortString()).isEqualTo("P1Y#41");
        assertThat(of(YEAR, d2011_02_01).withReference(someReference).next().toShortString()).isEqualTo("P1Y#30@1981-04-01");
        assertThat(of(DAY, d2011_02_01).toShortString()).isEqualTo("P1D#15006");
    }

    @Test
    public void testToBuilder() {
        assertThat(of(DAY, d2011_02_01).toBuilder().build()).isEqualTo(of(DAY, d2011_02_01));
    }

    @Test
    public void testParse() {
        assertThatThrownBy(() -> TsPeriod.parse("hello")).isInstanceOf(DateTimeParseException.class);
        assertThat(TsPeriod.parse("P1M#2")).isEqualTo(TsPeriod.builder().unit(MONTH).id(2).build());
        assertThat(TsPeriod.parse("P1M#-1")).isEqualTo(TsPeriod.builder().unit(MONTH).id(-1).build());
        assertThat(TsPeriod.parse("P1M#2@" + someReference.toString())).isEqualTo(TsPeriod.builder().unit(MONTH).reference(someReference).id(2).build());
    }

    @Test
    public void testUntil() {
        assertThat(yearly(2010).until(yearly(2012))).isEqualTo(2);
        assertThat(yearly(2010).until(yearly(2010))).isEqualTo(0);
        assertThat(yearly(2010).until(yearly(2009))).isEqualTo(-1);

        assertThatThrownBy(() -> monthly(2011, 2).until(yearly(2011)))
                .isInstanceOf(TsException.class)
                .hasMessage(TsException.INCOMPATIBLE_FREQ);
    }

    @Test
    public void testIdAt() {
        assertThat(idAt(EPOCH, MONTH, EPOCH)).isEqualTo(0);
        assertThat(idAt(EPOCH, MONTH, EPOCH.plusNanos(1))).isEqualTo(0);
        assertThat(idAt(EPOCH, MONTH, EPOCH.plusMonths(1))).isEqualTo(1);
        assertThat(idAt(EPOCH, MONTH, EPOCH.minusNanos(1))).isEqualTo(-1);
        assertThat(idAt(EPOCH, MONTH, EPOCH.minusMonths(1))).isEqualTo(-1);

        assertThat(idAt(EPOCH, YEAR, EPOCH)).isEqualTo(0);
        assertThat(idAt(EPOCH, YEAR, EPOCH.plusNanos(1))).isEqualTo(0);
        assertThat(idAt(EPOCH, YEAR, EPOCH.plusYears(1))).isEqualTo(1);
        assertThat(idAt(EPOCH, YEAR, EPOCH.minusNanos(1))).isEqualTo(-1);
        assertThat(idAt(EPOCH, YEAR, EPOCH.minusYears(1))).isEqualTo(-1);

        assertThat(idAt(EPOCH, DAY, EPOCH)).isEqualTo(0);
        assertThat(idAt(EPOCH.plusDays(4), DAY, EPOCH.plusDays(4))).isEqualTo(0);
        assertThat(idAt(EPOCH.plusDays(4), DAY, EPOCH.plusDays(5))).isEqualTo(1);
    }

    @Test
    public void testDateAt() {
        assertThat(dateAt(EPOCH, MONTH, 0)).isEqualTo(EPOCH);
        assertThat(dateAt(EPOCH, MONTH, 1)).isEqualTo(EPOCH.plusMonths(1));
        assertThat(dateAt(EPOCH, MONTH, -1)).isEqualTo(EPOCH.minusMonths(1));

        assertThat(dateAt(EPOCH, YEAR, 0)).isEqualTo(EPOCH);
        assertThat(dateAt(EPOCH, YEAR, 1)).isEqualTo(EPOCH.plusYears(1));
        assertThat(dateAt(EPOCH, YEAR, -1)).isEqualTo(EPOCH.minusYears(1));

        assertThat(dateAt(EPOCH, DAY, 0)).isEqualTo(EPOCH);
        assertThat(dateAt(EPOCH.plusDays(4), DAY, 0)).isEqualTo(EPOCH.plusDays(4));
        assertThat(dateAt(EPOCH.plusDays(4), DAY, 1)).isEqualTo(EPOCH.plusDays(5));
    }

//    @Test
//    public void testGetPosition() {
//
//        assertThat(monthly(2010, 1).getPosition(YEAR)).isEqualTo(position(monthly(2010, 1), YEAR));
//        assertThat(monthly(2010, 2).getPosition(YEAR)).isEqualTo(1);
//        assertThat(monthly(2010, 12).getPosition(YEAR)).isEqualTo(11);
//
//        assertThat(monthly(2010, 1).getPosition(QUARTER)).isEqualTo(0);
//        assertThat(monthly(2010, 2).getPosition(QUARTER)).isEqualTo(1);
//        assertThat(monthly(2010, 3).getPosition(QUARTER)).isEqualTo(2);
//        assertThat(monthly(2010, 4).getPosition(QUARTER)).isEqualTo(0);
//
//        assertThat(monthly(2010, 1).getPosition(MONTH)).isEqualTo(0);
//        assertThat(monthly(2010, 2).getPosition(MONTH)).isEqualTo(0);
//
//        assertThat(quarterly(2010, 1).getPosition(YEAR)).isEqualTo(0);
//        assertThat(quarterly(2010, 2).getPosition(YEAR)).isEqualTo(1);
//        assertThat(quarterly(2010, 4).getPosition(YEAR)).isEqualTo(3);
//
//        assertThat(monthly(2010, 1).getPosition(HOUR)).isEqualTo(0);
//        assertThat(monthly(2010, 2).getPosition(HOUR)).isEqualTo(0);
//    }
//
//    private static int position(TsPeriod p, TsUnit lfreq) {
//        TsPeriod lp = p.withUnit(lfreq);
//        RegularDomain hdom = RegularDomain.splitOf(lp, p.getUnit(), true);
//        return hdom.indexOf(p);
//    }

//    @Test
//    public void stressTestPosition() {
//        TsPeriod p = TsPeriod.of(MONTH, d2011_02_01);
//        long t0 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; ++i) {
//            int pos = position(p, YEAR);
//        }
//        long t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//        t0 = System.currentTimeMillis();
//        for (int i = 0; i < 10000000; ++i) {
//            int pos = p.getPosition(YEAR);
//        }
//        t1 = System.currentTimeMillis();
//        System.out.println(t1 - t0);
//    }

    @Test
    public void testPoint() {
        LocalDateTime x = d2011_02_01_0000.plus(0, ChronoUnit.SECONDS);
        assertEquals(x, d2011_02_01_0000);
        LocalDate y = d2011_02_01.plus(0, ChronoUnit.DAYS);
        assertEquals(y, d2011_02_01);
    }

    private final LocalDate d2011_02_01 = LocalDate.of(2011, 2, 1);
    private final LocalDateTime someReference = TsPeriod.EPOCH.plusMonths(135);
    private final LocalDateTime d2011_02_01_0000 = d2011_02_01.atStartOfDay();
    private final LocalDateTime d2011_02_01_1337 = d2011_02_01.atTime(13, 37);
}
