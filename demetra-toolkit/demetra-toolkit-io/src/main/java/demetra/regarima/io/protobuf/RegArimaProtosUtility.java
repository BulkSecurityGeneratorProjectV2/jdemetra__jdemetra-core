/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.regarima.io.protobuf;

import demetra.data.DoubleSeq;
import demetra.data.Iterables;
import demetra.data.Parameter;
import demetra.data.Range;
import demetra.modelling.StationaryTransformation;
import demetra.modelling.implementations.SarimaSpec;
import demetra.modelling.TransformationType;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.calendars.TradingDaysType;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TsContextVariable;
import demetra.timeseries.regression.Variable;
import demetra.toolkit.io.protobuf.ToolkitProtosUtility;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RegArimaProtosUtility {

    public LengthOfPeriodType convert(RegArimaProtos.LengthOfPeriod lp) {
        switch (lp) {
            case LP_LEAPYEAR:
                return LengthOfPeriodType.LeapYear;
            case LP_LENGTHOFPERIOD:
                return LengthOfPeriodType.LengthOfPeriod;
            default:
                return LengthOfPeriodType.None;
        }
    }

    public RegArimaProtos.LengthOfPeriod convert(LengthOfPeriodType lp) {
        switch (lp) {
            case LeapYear:
                return RegArimaProtos.LengthOfPeriod.LP_LEAPYEAR;
            case LengthOfPeriod:
                return RegArimaProtos.LengthOfPeriod.LP_LENGTHOFPERIOD;
            default:
                return RegArimaProtos.LengthOfPeriod.LP_NONE;
        }
    }

    public RegArimaProtos.TradingDays convert(TradingDaysType td) {
        switch (td) {
            case TradingDays:
                return RegArimaProtos.TradingDays.TD_FULL;
            case WorkingDays:
                return RegArimaProtos.TradingDays.TD_WEEK;
            default:
                return RegArimaProtos.TradingDays.TD_NONE;
        }
    }

    public TradingDaysType convert(RegArimaProtos.TradingDays td) {
        switch (td) {
            case TD_FULL:
                return TradingDaysType.TradingDays;
            case TD_WEEK:
                return TradingDaysType.WorkingDays;
            default:
                return TradingDaysType.None;
        }
    }

    public RegArimaProtos.Transformation convert(TransformationType fn) {
        switch (fn) {
            case Log:
                return RegArimaProtos.Transformation.FN_LOG;
            case Auto:
                return RegArimaProtos.Transformation.FN_AUTO;
            default:
                return RegArimaProtos.Transformation.FN_LEVEL;
        }
    }

    public TransformationType convert(RegArimaProtos.Transformation fn) {
        switch (fn) {
            case FN_LOG:
                return TransformationType.Log;
            case FN_AUTO:
                return TransformationType.Auto;
            default:
                return TransformationType.None;
        }
    }

    public SarimaSpec convert(RegArimaProtos.SarimaSpec spec) {
        return SarimaSpec.builder()
                .period(spec.getPeriod())
                .d(spec.getD())
                .bd(spec.getBd())
                .phi(ToolkitProtosUtility.convert(spec.getPhiList()))
                .theta(ToolkitProtosUtility.convert(spec.getThetaList()))
                .bphi(ToolkitProtosUtility.convert(spec.getBphiList()))
                .btheta(ToolkitProtosUtility.convert(spec.getBthetaList()))
                .build();
    }

    public RegArimaProtos.SarimaSpec convert(SarimaSpec spec) {
        RegArimaProtos.SarimaSpec.Builder builder = RegArimaProtos.SarimaSpec.newBuilder()
                .setPeriod(spec.getPeriod())
                .setD(spec.getD())
                .setBd(spec.getBd());

        Parameter[] p = spec.getPhi();
        for (int i = 0; i < p.length; ++i) {
            builder.addPhi(ToolkitProtosUtility.convert(p[i]));
        }
        p = spec.getTheta();
        for (int i = 0; i < p.length; ++i) {
            builder.addTheta(ToolkitProtosUtility.convert(p[i]));
        }
        p = spec.getBphi();
        for (int i = 0; i < p.length; ++i) {
            builder.addBphi(ToolkitProtosUtility.convert(p[i]));
        }
        p = spec.getBtheta();
        for (int i = 0; i < p.length; ++i) {
            builder.addBtheta(ToolkitProtosUtility.convert(p[i]));
        }
        return builder.build();
    }

    public RegArimaProtos.TsVariable convertTsContextVariable(Variable<TsContextVariable> v) {
        return RegArimaProtos.TsVariable.newBuilder()
                .setName(v.getName())
                .setId(v.getCore().getId())
                .setFirstLag(v.getCore().getFirstLag())
                .setLastLag(v.getCore().getLastLag())
                .addAllCoefficient(ToolkitProtosUtility.convert(v.getCoefficients()))
                .build();
    }

    public Variable<TsContextVariable> convert(RegArimaProtos.TsVariable v) {
        return Variable.<TsContextVariable>builder()
                .name(v.getName())
                .core(new TsContextVariable(v.getId(), v.getFirstLag(), v.getLastLag()))
                .attributes(v.getMetadataMap())
                .coefficients(ToolkitProtosUtility.convert(v.getCoefficientList()))
                .build();
    }

    public RegArimaProtos.Ramp convertRamp(Variable<Ramp> v) {
        return RegArimaProtos.Ramp.newBuilder()
                .setName(v.getName())
                .setStart(ToolkitProtosUtility.convert(v.getCore().getStart().toLocalDate()))
                .setEnd(ToolkitProtosUtility.convert(v.getCore().getEnd().toLocalDate()))
                .setCoefficient(ToolkitProtosUtility.convert(v.getCoefficient(0)))
                .putAllMetadata(v.getAttributes())
                .build();
    }

    public Variable<Ramp> convert(RegArimaProtos.Ramp v) {
        LocalDate start = ToolkitProtosUtility.convert(v.getStart());
        LocalDate end = ToolkitProtosUtility.convert(v.getEnd());
        Parameter c = ToolkitProtosUtility.convert(v.getCoefficient());
        return Variable.<Ramp>builder()
                .name(v.getName())
                .core(new Ramp(start.atStartOfDay(), end.atStartOfDay()))
                .attributes(v.getMetadataMap())
                .coefficients(c == null ? null : new Parameter[]{c})
                .build();
    }

    public RegArimaProtos.InterventionVariable convertInterventionVariable(Variable<InterventionVariable> var) {
        InterventionVariable v = var.getCore();
        RegArimaProtos.InterventionVariable.Builder builder = RegArimaProtos.InterventionVariable.newBuilder()
                .setName(var.getName())
                .setDelta(v.getDelta())
                .setSeasonalDelta(v.getDeltaSeasonal())
                .setCoefficient(ToolkitProtosUtility.convert(var.getCoefficient(0)))
                .putAllMetadata(var.getAttributes());

        Range<LocalDateTime>[] sequences = v.getSequences();
        for (int i = 0; i < sequences.length; ++i) {
            Range<LocalDateTime> seq = sequences[i];
            builder.addSequences(RegArimaProtos.InterventionVariable.Sequence.newBuilder()
                    .setStart(ToolkitProtosUtility.convert(seq.start().toLocalDate()))
                    .setEnd(ToolkitProtosUtility.convert(seq.end().toLocalDate()))
                    .build());
        }
        return builder.build();
    }

    public Variable<InterventionVariable> convert(RegArimaProtos.InterventionVariable v) {
        InterventionVariable.Builder builder = InterventionVariable.builder()
                .delta(v.getDelta())
                .deltaSeasonal(v.getSeasonalDelta());
        int n = v.getSequencesCount();
        for (int i = 0; i < n; ++i) {
            RegArimaProtos.InterventionVariable.Sequence seq = v.getSequences(i);
            LocalDate start = ToolkitProtosUtility.convert(seq.getStart());
            LocalDate end = ToolkitProtosUtility.convert(seq.getEnd());
            builder.add(start.atStartOfDay(), end.atStartOfDay());
        }
        Parameter c = ToolkitProtosUtility.convert(v.getCoefficient());
        return Variable.<InterventionVariable>builder()
                .name(v.getName())
                .core(builder.build())
                .coefficients(c == null ? null : new Parameter[]{c})
                .attributes(v.getMetadataMap())
                .build();
    }

    public RegArimaProtos.StationaryTransformation convert(StationaryTransformation st) {
        RegArimaProtos.StationaryTransformation.Builder builder = RegArimaProtos.StationaryTransformation.newBuilder()
                .addAllStationarySeries(Iterables.of(st.getStationarySeries()))
                .setMeanCorrection(st.isMeanCorrection());
        for (StationaryTransformation.Differencing d : st.getDifferences()) {
            builder.addDifferences(RegArimaProtos.StationaryTransformation.Differencing.newBuilder()
                    .setLag(d.getLag())
                    .setOrder(d.getOrder())
                    .build()
            );
        }
        return builder
                .build();
    }

    public StationaryTransformation convert(RegArimaProtos.StationaryTransformation st) {
        double[] ds = new double[st.getStationarySeriesCount()];
        for (int i = 0; i < ds.length; ++i) {
            ds[i] = st.getStationarySeries(i);
        }
        StationaryTransformation.Builder builder = StationaryTransformation.builder()
                .stationarySeries(DoubleSeq.of(ds))
                .meanCorrection(st.getMeanCorrection());
        for (int i = 0; i < st.getDifferencesCount(); ++i) {
            RegArimaProtos.StationaryTransformation.Differencing d = st.getDifferences(i);
            builder.difference(new StationaryTransformation.Differencing(d.getLag(), d.getOrder()));
        }
        return builder
                .build();
    }

}
