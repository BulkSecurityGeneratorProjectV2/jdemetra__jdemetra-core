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
package ec.tss.sa.diagnostics;

import ec.tstoolkit.modelling.ComponentInformation;
import ec.satoolkit.GenericSaResults;
import ec.satoolkit.ISeriesDecomposition;
import ec.satoolkit.seats.SeatsResults;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import ec.tstoolkit.algorithm.ProcQuality;
import ec.tstoolkit.arima.ArimaModel;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovDiagnostics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Kristof Bayens
 */
public class SeatsDiagnostics implements IDiagnostics {

//    private UcarimaModel ucm_;
//    private ArimaModel[] models_, stmodels_;
//    private LinearModel[] emodels_;
//    private WienerKolmogorovEstimators wk_;
    private WienerKolmogorovDiagnostics diags_;
//    private ComponentDescriptor[] descs_;
//    private DataBlock[] data_;
    private double bad_ = SeatsDiagnosticsConfiguration.BAD, uncertain_ = SeatsDiagnosticsConfiguration.UNC;
    private boolean same_, cutoff_;

    public static SeatsDiagnostics create(SeatsDiagnosticsConfiguration config, CompositeResults rslts) {
        try {
            SeatsResults srslts = GenericSaResults.getDecomposition(rslts, SeatsResults.class);
            if (srslts == null) {
                return null;
            } else {
                SeatsDiagnostics sd=new SeatsDiagnostics(config, rslts);
                if (sd.isValid())
                    return sd;
                else
                    return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public SeatsDiagnostics(SeatsDiagnosticsConfiguration config, CompositeResults rslts) {
        bad_ = config.getBad();
        uncertain_ = config.getUncertain();
        test(rslts);
    }

    private void test(CompositeResults rslts) {
        PreprocessingModel pp = GenericSaResults.getPreprocessingModel(rslts);
        SeatsResults srslts = GenericSaResults.getDecomposition(rslts, SeatsResults.class);
        same_ = !srslts.getModel().isChanged();
        cutoff_=srslts.getModel().isCutOff();
        ISeriesDecomposition decomposition = srslts.getComponents();
        UcarimaModel ucm = srslts.getUcarimaModel().clone();
        if (ucm.getComponentsCount() > 3) {
            ucm.compact(2, 2);
        }
        int[] cmps = new int[]{1, -2, 2, 3};
        double err = srslts.getModel().getSer();
        TsData t = decomposition.getSeries(ComponentType.Trend, ComponentInformation.Value);
        TsData s = decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        TsData i = decomposition.getSeries(ComponentType.Irregular, ComponentInformation.Value);
        TsData sa = decomposition.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);

        double[][] data = new double[][]{
            t == null ? null : t.internalStorage(),
            s == null ? null : sa.internalStorage(),
            s == null ? null : s.internalStorage(),
            i == null ? null : i.internalStorage()
        };

        diags_ = WienerKolmogorovDiagnostics.make(ucm, err, data, cmps);
    }

    public WienerKolmogorovDiagnostics getDiagnostics() {
        return diags_;
    }

    public double getBadThreshold() {
        return bad_;
    }

    public double getGoodThreshold() {
        return uncertain_;
    }
    
    public boolean isValid(){
        return diags_ != null;
    }

    @Override
    public String getName() {
        return SeatsDiagnosticsFactory.NAME;
    }

    @Override
    public List<String> getTests() {
        return SeatsDiagnosticsFactory.ALL;
    }

    @Override
    public ProcQuality getDiagnostic(String test) {
        if (diags_ == null) {
            return ProcQuality.Undefined;
        }
        double pval = getValue(test);
        if (Double.isNaN(pval)) {
            return ProcQuality.Undefined;
        }
        if (pval <= bad_) {
            return ProcQuality.Bad;
        } else if (pval > uncertain_) {
            return ProcQuality.Good;
        } else {
            return ProcQuality.Uncertain;
        }
    }
    private static final int I_CMP = 3, S_CMP = 2;

    @Override
    public double getValue(String test) {
        double val = Double.NaN;
        int icmp = -1, jcmp = -1;
        if (test.equals(SeatsDiagnosticsFactory.SEAS_VAR) || test.equals(SeatsDiagnosticsFactory.SEAS_I_CORR)) {
            icmp = S_CMP;
        }
        if (test.equals(SeatsDiagnosticsFactory.IRR_VAR) || test.equals(SeatsDiagnosticsFactory.SEAS_I_CORR)) {
            jcmp = I_CMP;
        }
        if (test.equals(SeatsDiagnosticsFactory.SEAS_VAR)) {
            val = diags_.getPValue(icmp);
        } else if (test.equals(SeatsDiagnosticsFactory.IRR_VAR)) {
            val = diags_.getPValue(jcmp);
        } else if (icmp >= 0 && jcmp >= 0) {
            val = diags_.getPValue(icmp, jcmp);
        }
        return val;
    }

    @Override
    public List<String> getWarnings() {
        if (!same_) {
            return Collections.singletonList(SeatsDiagnosticsFactory.NOTSAME);
        } else if (cutoff_){
             return Collections.singletonList(SeatsDiagnosticsFactory.CUTOFF);
        }else{
           return null;
        }
    }
}
