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
package jdplus.timeseries.calendars;

import nbbrd.design.Development;
import demetra.math.matrices.MatrixType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.calendars.Easter;
import demetra.timeseries.calendars.EasterRelatedDay;
import demetra.timeseries.calendars.FixedDay;
import demetra.timeseries.calendars.Holiday;
import demetra.timeseries.calendars.PrespecifiedHoliday;
import java.time.DayOfWeek;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
@lombok.experimental.UtilityClass
public class HolidaysUtility {

    public void fillDays(Holiday[] holidays, final Matrix D, final LocalDate start, final boolean skipSundays) {
        LocalDate end = start.plusDays(D.getRowsCount());
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = HolidayInfo.iterable(item, start, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay();
                if (!skipSundays || date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    long pos = start.until(date, DAYS);
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillPreviousWorkingDays(Holiday[] holidays, final Matrix D, final LocalDate start, final int del) {
        int n = D.getRowsCount();
        LocalDate nstart = start.plusDays(del);
        LocalDate end = start.plusDays(n);
        int col = 0;
        for (Holiday item : holidays) {
            Iterator<HolidayInfo> iter = HolidayInfo.iterable(item, nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay().minusDays(del);
                date = HolidayInfo.getPreviousWorkingDate(date);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    public void fillNextWorkingDays(Holiday[] holidays, final Matrix D, final LocalDate start, final int del) {
        int n = D.getRowsCount();
        LocalDate nstart = start.minusDays(del);
        LocalDate end = nstart.plusDays(n);
        int col = 0;
        for (Holiday item : holidays) {

            Iterator<HolidayInfo> iter = HolidayInfo.iterable(item, nstart, end).iterator();
            while (iter.hasNext()) {
                LocalDate date = iter.next().getDay().plusDays(del);
                date = HolidayInfo.getNextWorkingDate(date);
                long pos = start.until(date, DAYS);
                if (pos >= 0 && pos < n) {
                    D.set((int) pos, col, 1);
                }
            }
            if (D.getColumnsCount() > 1) {
                ++col;
            }
        }
    }

    private double probEaster(int del, boolean julian) {
        return julian ? Easter.probJulianEaster(del)
                : Easter.probEaster(del);
    }

    static final int START = 80, JSTART = 90, DEL = 35, JDEL = 43;
    // 31+28+21=80, 31+28+31=90

    /*
     * Raw estimation of the probability to get Easter at a specific date is defined below:
     * 22/3 (1/7)*1/LUNARY
     * 23/3 (2/7)*1/LUNARY
     * ...
     * 27/3 (6/7)*1/LUNARY
     * 28/3 1/LUNARY
     * ...
     * 18/4 1/LUNARY
     * 19/4 1/LUNARY + (1/7) * DEC_LUNARY/LUNARY = (7 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 20/4 (6/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY= (6 + 1 * DEC_LUNARY)/(7 * LUNARY)
     * 21/4 (5/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 22/4 (4/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 23/4 (3/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 24/4 (2/7)*1/LUNARY + (1/7) * DEC_LUNARY/LUNARY
     * 25/4 (1/7)*1/LUNARY + (1/7) *DEC_LUNARY/LUNARY
     */
    public double[][] longTermMean(EasterRelatedDay eday, int freq) {
        // week day

        int w = eday.getOffset() % 7;
        if (w == 0) {
            w = 7; // Sunday
        }
        if (w < 0) {
            w += 7;
        }
        // monday must be 0...
        --w;

        // Easter always falls between March, 22 and April, 25 (inclusive). The probability to get a specific day is defined by probEaster.
        // We don't take into account leap year. So, the solution is slightly wrong for offset
        // <= -50.
        // The considered day falls between ...
        int d0, d1;
        if (eday.isJulian()) {
            d0 = JSTART + eday.getOffset();
            d1 = d0 + JDEL;
        } else {
            d0 = START + eday.getOffset();
            d1 = d0 + DEL;
        }
        // d1 excluded

        int ifreq = (int) freq;
        int c = 12 / ifreq;

        int c0 = 0, c1 = 0;
        for (int i = 0; i < c; ++i) {
            c1 += MDAYS[i];
        }

        double[][] rslt = new double[ifreq][];
        for (int i = 0; i < ifreq;) {
            if (d0 < c1 && d1 > c0) {
                double[] m = new double[7];
                double x = 0;
                for (int j = Math.max(d0, c0); j < Math.min(d1, c1); ++j) {
                    x += probEaster(j - d0, eday.isJulian());
                }
                m[w] = x * eday.getWeight();
                rslt[i] = m;
            }
            // update c0, c1;
            c0 = c1;
            if (++i < ifreq) {
                for (int j = 0; j < c; ++j) {
                    c1 += MDAYS[i * c + j];
                }
            }
        }
        return rslt;
    }

    public double[][] longTermMean(FixedDay fday, int freq) {
        int c = 12 / freq;
        int p = (fday.getMonth() - 1) / c;
        double[] m = new double[7];

        for (int i = 0; i < 7; ++i) {
            m[i] = fday.getWeight() / 7;
        }

        double[][] rslt = new double[freq][];
        rslt[p] = m;
        return rslt;
    }

    private final int[] MDAYS = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    /**
     * Gets the number of days corresponding to the holidays
     *
     * @param holidays
     * @param domain
     * @return The (weighted) number of holidays for each period of the domain.
     * The different columns of the matrix correspond to Mondays...Sundays
     */
    public MatrixType holidays(Holiday[] holidays, TsDomain domain) {
        int n = domain.getLength();
        double[] h = new double[7 * n];

        LocalDate dstart = domain.start().toLocalDate(), dend = domain.end().toLocalDate();
        Map<LocalDate, Double> used = new HashMap<>();
        for (int i = 0; i < holidays.length; ++i) {
            Holiday cur = holidays[i];
            LocalDate start = cur.getValidityPeriod().getStart(), end = cur.getValidityPeriod().getEnd();
            if (start.isBefore(dstart)) {
                start = dstart;
            }
            if (end.isAfter(dend)) {
                end = dend;
            }
            if (start.isBefore(end)) {
                Iterator<HolidayInfo> iter = HolidayInfo.iterable(cur, start, end).iterator();
                while (iter.hasNext()) {
                    HolidayInfo info = iter.next();
                    LocalDate curday = info.getDay();
                    Double Weight = used.get(curday);
                    double weight = cur.getWeight();
                    if (Weight == null || weight > Weight) {
                        used.put(curday, weight);
                        DayOfWeek w = info.getDayOfWeek();
                        int pos = domain.indexOf(curday.atStartOfDay());
                        if (pos >= 0) {
                            int col = w.getValue() - 1;
                            h[n * col + pos] += Weight == null ? weight : weight - Weight;
                        }
                    }
                }
            }
        }
        return MatrixType.of(h, n, 7);
    }

    public static double[][] longTermMean(Holiday holiday, int freq) {
        if (holiday instanceof FixedDay) {
            return HolidaysUtility.longTermMean((FixedDay) holiday, freq);
        } else if (holiday instanceof EasterRelatedDay) {
            return HolidaysUtility.longTermMean((EasterRelatedDay) holiday, freq);
        } else if (holiday instanceof PrespecifiedHoliday) {
            PrespecifiedHoliday ph = (PrespecifiedHoliday) holiday;
            return longTermMean(ph.rawHoliday(), freq);
        }
        throw new IllegalArgumentException();

    }

    /**
     * Computes the long term mean effects
     *
     * @param holidays
     * @param freq
     * @return Returns an array of "annualFrequency" length, corresponding to
     * each period in one year (for instance, Jan, Feb..., Dec).
     * Each item of the result will contain 7 elements, corresponding to the
     * long term average for Mondays...Sundays
     * The sum of the longTermMean must be equal to the sum of the weights of
     * the different holidays.
     * Some element of the array can be null, which means that there are no
     * effect for the considered period.
     */
    public double[][] longTermMean(Holiday[] holidays, int freq) {
        double[][] rslt = null;
        for (int k = 0; k < holidays.length; ++k) {
            double[][] cur = longTermMean(holidays[k], freq);
            if (cur != null) {
                if (rslt == null) {
                    rslt = cur;
                } else {
                    for (int i = 0; i < cur.length; ++i) {
                        if (cur[i] != null) {
                            if (rslt[i] == null) {
                                rslt[i] = cur[i];
                            } else {
                                for (int j = 0; j < 7; ++j) {
                                    rslt[i][j] += cur[i][j];
                                }
                            }
                        }
                    }
                }
            }
        }
        return rslt != null ? rslt : new double[freq][];
    }

}