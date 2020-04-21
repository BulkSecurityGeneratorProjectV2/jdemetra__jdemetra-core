/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.sa.r;

import jdplus.data.DataBlock;
import jdplus.linearmodel.LeastSquaresResults;
import jdplus.linearmodel.LinearModel;
import jdplus.linearmodel.Ols;
import jdplus.math.matrices.Matrix;
import demetra.timeseries.regression.PeriodicContrasts;
import demetra.stats.TestResult;
import jdplus.stats.tests.LjungBox;
import jdplus.stats.tests.StatisticalTest;
import jdplus.stats.tests.seasonal.CanovaHansen;
import jdplus.stats.tests.seasonal.CanovaHansen2;
import jdplus.stats.tests.seasonal.PeriodicLjungBox;
import jdplus.modelling.regression.PeriodicContrastsFactory;
import demetra.data.DoubleSeq;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class SeasonalityTests {

    public TestResult fTest(double[] s, int period, boolean ar, int ny) {

        DoubleSeq y = DoubleSeq.of(s);
        if (ar) {
            if (ny != 0) {
                y = y.drop(Math.max(0, s.length - period * ny - 1), 0);
            }
            return processAr(y, period);
        } else {
            double[] ds = new double[s.length - 1];
            for (int i = 0; i < ds.length; ++i) {
                ds[i] = s[i + 1] - s[i];
            }
            y = DoubleSeq.of(ds);
            if (ny != 0) {
                y = y.drop(Math.max(0, s.length - period * ny), 0);
            }
            return process(y, period);
        }
    }

    public TestResult qsTest(double[] s, int period, int ny) {

        for (int i = s.length - 1; i > 0; --i) {
            s[i] -= s[i - 1];
        }
        DoubleSeq y = DoubleSeq.of(s, 1, s.length - 1);
        if (ny != 0) {
            y = y.drop(Math.max(0, y.length() - period * ny), 0);
        }
        StatisticalTest test = new LjungBox(y)
                .lag(period)
                .autoCorrelationsCount(2)
                .usePositiveAutoCorrelations()
                .build();
        return test.toSummary();
    }

    public TestResult periodicQsTest(double[] s, double[] periods) {
        DoubleSeq y;
        if (periods.length == 1) {
            for (int j = s.length - 1; j > 0; --j) {
                s[j] -= s[j - 1];
            }
            y = DoubleSeq.of(s, 1, s.length - 1);
        } else {
            int del = 0;
            for (int i = 1; i < periods.length; ++i) {
                int p = (int) periods[i];
                del += p;
                for (int j = s.length - 1; j >= del; --j) {
                    s[j] -= s[j - p];
                }
            }
            y = DoubleSeq.of(s, del, s.length - del);
        }
        StatisticalTest test = new PeriodicLjungBox(y, 0)
                .lags(periods[0], 2)
                .usePositiveAutocorrelations()
                .build();
        return test.toSummary();
    }

    public double[] canovaHansenTest(double[] s, int start, int end, boolean original) {
        double[] rslt = new double[end - start];
        DoubleSeq x = DoubleSeq.of(s);
        for (int i = start; i < end; ++i) {
            if (original){
                rslt[i - start] = CanovaHansen.test(x)
                        .specific(i, 1)
                        .build()
                        .testAll();
            }else{
                rslt[i - start] = CanovaHansen2.of(x)
                        .periodicity(i)
                        .compute();
            }
        }
        return rslt;
    }

    private TestResult process(DoubleSeq s, int freq) {
        try {
            DataBlock y = DataBlock.of(s);
            y.sub(y.average());
            PeriodicContrasts var = new PeriodicContrasts(freq);
            Matrix sd = PeriodicContrastsFactory.matrix(var, s.length(), 0);
            LinearModel reg = new LinearModel(y.getStorage(), false, sd);
            LeastSquaresResults rslt = Ols.compute(reg);

            StatisticalTest ftest = rslt.Ftest();
            return ftest.toSummary();

        } catch (Exception err) {
            return null;
        }
    }

    private TestResult processAr(DoubleSeq s, int freq) {
        try {
            PeriodicContrasts var = new PeriodicContrasts(freq);

            Matrix sd = PeriodicContrastsFactory.matrix(var, s.length() - 1, 0);

            LinearModel reg = LinearModel.builder()
                    .y(s.drop(1, 0))
                    .addX(s.drop(0, 1))
                    .addX(sd)
                    .meanCorrection(true)
                    .build();

            LeastSquaresResults rslt = Ols.compute(reg);
            StatisticalTest ftest = rslt.Ftest(2, sd.getColumnsCount());
            return ftest.toSummary();
        } catch (Exception err) {
            return null;
        }
    }

}