/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regarima.extractors;

import demetra.information.InformationMapping;
import demetra.math.matrices.MatrixType;
import demetra.modelling.ModellingDictionary;
import demetra.timeseries.TsData;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.Variable;
import java.util.Arrays;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.regsarima.regular.RegSarimaModel;
import demetra.information.BasicInformationExtractor;
import demetra.information.InformationExtractor;
import demetra.likelihood.LikelihoodStatistics;
import jdplus.sarima.SarimaModel;
import nbbrd.design.Development;
import nbbrd.service.ServiceProvider;

/**
 *
 * Contains all the descriptors of a linear model, except additional information
 * and information related to the (generic) stochastic model
 *
 * @author palatej
 */
@Development(status = Development.Status.Release)
@ServiceProvider(InformationExtractor.class)
public class RegSarimaModelExtractor extends InformationMapping<RegSarimaModel> {

    public static final int IMEAN = 0, ITD = 10, ILP = 11, IEASTER = 12,
            AO = 20, LS = 21, TC = 22, SO = 23, IOUTLIER = 29,
            IIV = 30, IRAMP = 40, IOTHER = 50;

    public final String SARIMA = "sarima",
            LOG = "log",
            ADJUST = "adjust",
            SPAN = "span", ESPAN = "espan", START = "start", END = "end", N = "n", NM = "missing", PERIOD = "period",
            REGRESSION = "regression", LIKELIHOOD = "likelihood", MAX = "max",
            OUTLIERS = "outlier(*)",
            CALENDAR = "calendar(*)",
            EASTER = "easter",
            NTD = "ntd", NMH = "nmh",
            TD = "td", TD1 = "td(1)", TD2 = "td(2)", TD3 = "td(3)", TD4 = "td(4)", TD5 = "td(5)", TD6 = "td(6)", TD7 = "td(7)",
            TD8 = "td(8)", TD9 = "td(9)", TD10 = "td(10)", TD11 = "td(11)", TD12 = "td(12)", TD13 = "td(13)", TD14 = "td(14)",
            LP = "lp", OUT = "out", OUT1 = "out(1)", OUT2 = "out(2)", OUT3 = "out(3)", OUT4 = "out(4)", OUT5 = "out(5)", OUT6 = "out(6)", OUT7 = "out(7)",
            NOUT = "nout", NOUTAO = "noutao", NOUTLS = "noutls", NOUTTC = "nouttc", NOUTSO = "noutso",
            OUT8 = "out(8)", OUT9 = "out(9)", OUT10 = "out(10)", OUT11 = "out(11)", OUT12 = "out(12)", OUT13 = "out(13)", OUT14 = "out(14)",
            OUT15 = "out(15)", OUT16 = "out(16)", OUT17 = "out(17)", OUT18 = "out(18)", OUT19 = "out(19)", OUT20 = "out(20)",
            OUT21 = "out(21)", OUT22 = "out(22)", OUT23 = "out(23)", OUT24 = "out(24)", OUT25 = "out(25)", OUT26 = "out(26)",
            OUT27 = "out(27)", OUT28 = "out(28)", OUT29 = "out(29)", OUT30 = "out(30)",
            COEFF = "coefficients", COVAR = "covar", COEFFDESC = "description", REGTYPE = "type",
            PCOVAR = "pcovar", PCORR = "pcorr", SCORE = "pscore";

    public RegSarimaModelExtractor() {
        set(PERIOD, Integer.class, source -> source.getDescription().getSeries().getAnnualFrequency());
        set(BasicInformationExtractor.concatenate(SPAN, START), TsPeriod.class, source -> source.getDescription().getSeries().getStart());
        set(BasicInformationExtractor.concatenate(SPAN, END), TsPeriod.class, source -> source.getDescription().getSeries().getDomain().getLastPeriod());
        set(BasicInformationExtractor.concatenate(SPAN, N), Integer.class, source -> source.getDescription().getSeries().length());
        set(BasicInformationExtractor.concatenate(ESPAN, START), TsPeriod.class, source -> source.getDetails().getEstimationDomain().getStartPeriod());
        set(BasicInformationExtractor.concatenate(ESPAN, END), TsPeriod.class, source -> source.getDetails().getEstimationDomain().getLastPeriod());
        set(BasicInformationExtractor.concatenate(ESPAN, N), Integer.class, source -> source.getDetails().getEstimationDomain().getLength());
        set(LOG, Boolean.class, source -> source.getDescription().isLogTransformation());
        set(ADJUST, Boolean.class, source -> source.getDescription().getLengthOfPeriodTransformation() != LengthOfPeriodType.None);
        set(BasicInformationExtractor.concatenate(NM), Integer.class, source -> source.getEstimation().getMissing().length);
        set(ModellingDictionary.Y, TsData.class, source -> source.getDescription().getSeries());
        set(BasicInformationExtractor.concatenate(REGRESSION, COVAR), MatrixType.class, source -> source.getEstimation().getCoefficientsCovariance());
        set(BasicInformationExtractor.concatenate(REGRESSION, COEFF), double[].class, source -> source.getEstimation().getCoefficients().toArray());
        set(BasicInformationExtractor.concatenate(REGRESSION, REGTYPE), int[].class, source -> {
            Variable[] vars = source.getDescription().getVariables();
            if (vars.length == 0) {
                return null;
            }
            int n = Arrays.stream(vars).mapToInt(var -> var.dim()).sum();
            int[] tvars = new int[n];
            for (int i = 0, j = 0; i < vars.length; ++i) {
                int m = vars[i].dim();
                int type = type(vars[i].getCore());
                for (int k = 0; k < m; ++k) {
                    tvars[j++] = type;
                }
            }
            return tvars;
        });
        set(BasicInformationExtractor.concatenate(REGRESSION, COEFFDESC), String[].class, source -> {
            TsDomain domain = source.getDescription().getSeries().getDomain();
            Variable[] vars = source.getDescription().getVariables();
            if (vars.length == 0) {
                return null;
            }
            int n = Arrays.stream(vars).mapToInt(var -> var.dim()).sum();
            String[] nvars = new String[n];
            for (int i = 0, j = 0; i < vars.length; ++i) {
                int m = vars[i].dim();
                if (m == 1) {
                    nvars[j++] = vars[i].getCore().description(domain);
                } else {
                    for (int k = 0; k < m; ++k) {
                        nvars[j++] = vars[i].getCore().description(k, domain);
                    }
                }
            }
            return nvars;
        });

        delegate(SARIMA, SarimaModel.class, source -> source.arima());
        delegate(LIKELIHOOD, LikelihoodStatistics.class, source -> source.getEstimation().getStatistics());
        set(BasicInformationExtractor.concatenate(MAX, PCOVAR), MatrixType.class, source -> source.getEstimation().getParameters().getCovariance());
        set(BasicInformationExtractor.concatenate(MAX, PCORR), MatrixType.class, source -> {
            Matrix cov = Matrix.of(source.getEstimation().getParameters().getCovariance());
            DataBlock diag = cov.diagonal();
            for (int i = 0; i < cov.getRowsCount(); ++i) {
                double vi = diag.get(i);
                for (int j = 0; j < i; ++j) {
                    double vj = diag.get(j);
                    if (vi != 0 && vj != 0) {
                        cov.mul(i, j, 1 / Math.sqrt(vi * vj));
                    }
                }
            }
            SymmetricMatrix.fromLower(cov);
            diag.set(1);
            return cov;
        });
        set(BasicInformationExtractor.concatenate(MAX, SCORE), double[].class, source -> source.getEstimation().getParameters().getScores().toArray());
    }

    private int type(ITsVariable var) {
        if (var instanceof TrendConstant) {
            return IMEAN;
        }
        if (var instanceof ITradingDaysVariable) {
            return ITD;
        }
        if (var instanceof ILengthOfPeriodVariable) {
            return ILP;
        }
        if (var instanceof IEasterVariable) {
            return IEASTER;
        }
        if (var instanceof IOutlier) {
            switch (((IOutlier) var).getCode()) {
                case AdditiveOutlier.CODE:
                    return AO;
                case LevelShift.CODE:
                    return LS;
                case TransitoryChange.CODE:
                    return TC;
                case PeriodicOutlier.CODE:
                case PeriodicOutlier.PO:
                    return SO;
                default:
                    return IOUTLIER;
            }
        }
        if (var instanceof InterventionVariable) {
            return IIV;
        }
        if (var instanceof Ramp) {
            return IRAMP;
        }
        return IOTHER;
    }

    @Override
    public Class<RegSarimaModel> getSourceClass() {
        return RegSarimaModel.class;
    }

}
