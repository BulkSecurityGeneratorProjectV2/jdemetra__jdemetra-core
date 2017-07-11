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
package ec.tstoolkit.timeseries.simplets;

import ec.tstoolkit.design.Internal;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.utilities.ObjLongToIntFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Package private supporting class for {@link ObsList}.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Internal
final class ObsLists {

    private ObsLists() {
        // static class
    }

    static final class SortableLongObsList implements ObsList.LongObsList {

        private final ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc;
        private final List<LongObs> list = new ArrayList<>();
        private boolean sorted = true;
        private long latestPeriod = Long.MIN_VALUE;

        @VisibleForTesting
        SortableLongObsList(ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc) {
            this.tsPeriodIdFunc = tsPeriodIdFunc;
        }

        @VisibleForTesting
        boolean isSorted() {
            return sorted;
        }

        @Override
        public void clear() {
            list.clear();
            sorted = true;
            latestPeriod = Long.MIN_VALUE;
        }

        @Override
        public void add(long period, double value) {
            list.add(new LongObs(period, value));
            sorted = sorted && latestPeriod <= period;
            latestPeriod = period;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public double getValue(int index) {
            return list.get(index).value;
        }

        @Override
        public int getPeriodId(TsFrequency frequency, int index) {
            return tsPeriodIdFunc.applyAsInt(frequency, list.get(index).period);
        }

        @Override
        public void sortByPeriod() {
            if (!sorted) {
                list.sort((l, r) -> Long.compare(l.period, r.period));
                sorted = true;
                latestPeriod = list.get(list.size() - 1).period;
            }
        }

        private static final class LongObs {

            final long period;
            final double value;

            private LongObs(long period, double value) {
                this.period = period;
                this.value = value;
            }
        }
    }

    static final class PreSortedLongObsList implements ObsList.LongObsList {

        private final ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc;
        private long[] periods;
        private double[] values;
        private int size;

        @VisibleForTesting
        PreSortedLongObsList(ObjLongToIntFunction<TsFrequency> tsPeriodIdFunc, int initialCapacity) {
            this.tsPeriodIdFunc = tsPeriodIdFunc;
            this.periods = new long[initialCapacity];
            this.values = new double[initialCapacity];
            this.size = 0;
        }

        private void grow() {
            int oldCapacity = periods.length;
            int newCapacity = Math.min(oldCapacity * 2, Integer.MAX_VALUE);
            periods = Arrays.copyOf(periods, newCapacity);
            values = Arrays.copyOf(values, newCapacity);
        }

        @Override
        public void clear() {
            size = 0;
        }

        @Override
        public void add(long period, double value) {
            if (size + 1 == periods.length) {
                grow();
            }
            periods[size] = period;
            values[size] = value;
            size++;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public double getValue(int index) {
            return values[index];
        }

        @Override
        public int getPeriodId(TsFrequency frequency, int index) {
            return tsPeriodIdFunc.applyAsInt(frequency, periods[index]);
        }

        @Override
        public void sortByPeriod() {
            // do nothing
        }

        @Override
        public double[] getValues() {
            return Arrays.copyOf(values, size);
        }
    }
}
