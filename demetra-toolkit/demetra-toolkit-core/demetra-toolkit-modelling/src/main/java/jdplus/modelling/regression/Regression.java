/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.modelling.regression.AdditiveOutlier;
import demetra.modelling.regression.Constant;
import demetra.modelling.regression.EasterVariable;
import demetra.modelling.regression.GenericTradingDaysVariable;
import demetra.modelling.regression.HolidaysCorrectedTradingDays;
import demetra.modelling.regression.ITsVariable;
import demetra.modelling.regression.InterventionVariable;
import demetra.modelling.regression.JulianEasterVariable;
import demetra.modelling.regression.LengthOfPeriod;
import demetra.modelling.regression.LevelShift;
import demetra.modelling.regression.LinearTrend;
import demetra.modelling.regression.PeriodicContrasts;
import demetra.modelling.regression.PeriodicDummies;
import demetra.modelling.regression.PeriodicOutlier;
import demetra.modelling.regression.Ramp;
import demetra.modelling.regression.StockTradingDays;
import demetra.modelling.regression.SwitchOutlier;
import demetra.modelling.regression.TransitoryChange;
import demetra.modelling.regression.TrigonometricVariables;
import demetra.modelling.regression.TsVariable;
import demetra.modelling.regression.TsVariables;
import demetra.modelling.regression.UserMovingHoliday;
import demetra.modelling.regression.UserTradingDays;
import demetra.modelling.regression.UserVariable;
import demetra.modelling.regression.UserVariables;
import jdplus.data.DataBlock;
import jdplus.maths.matrices.CanonicalMatrix;
import jdplus.maths.matrices.MatrixWindow;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Regression {

    private final Map< Class<? extends ITsVariable>, RegressionVariableFactory> FACTORIES
            = new HashMap<>();

    public <V extends ITsVariable, W extends V> boolean register(Class<W> wclass, RegressionVariableFactory<V> factory) {
        synchronized (FACTORIES) {
            if (FACTORIES.containsKey(wclass)) {
                return false;
            }
            FACTORIES.put(wclass, factory);
            return true;
        }
    }

    public <V extends ITsVariable> boolean unregister(Class<V> vclass) {
        synchronized (FACTORIES) {
            RegressionVariableFactory removed = FACTORIES.remove(vclass);
            return removed != null;
        }
    }

    static {
        synchronized (FACTORIES) {
            // Basic
            FACTORIES.put(Constant.class, ConstantFactory.FACTORY);
            FACTORIES.put(LinearTrend.class, LinearTrendFactory.FACTORY);
            
            // Outliers
            FACTORIES.put(AdditiveOutlier.class, AOFactory.FACTORY);
            FACTORIES.put(LevelShift.class, LSFactory.FACTORY);
            FACTORIES.put(TransitoryChange.class, TCFactory.FACTORY);
            FACTORIES.put(SwitchOutlier.class, WOFactory.FACTORY);
            FACTORIES.put(PeriodicOutlier.class, SOFactory.FACTORY);

            // Trading Days
            FACTORIES.put(LengthOfPeriod.class, LPFactory.FACTORY);
            FACTORIES.put(GenericTradingDaysVariable.class, GenericTradingDaysFactory.FACTORY);
            FACTORIES.put(HolidaysCorrectedTradingDays.class, HolidaysCorrectionFactory.FACTORY);
            FACTORIES.put(StockTradingDays.class, StockTDFactory.FACTORY);

            // Moving holidays
            FACTORIES.put(EasterVariable.class, EasterFactory.FACTORY);
            FACTORIES.put(JulianEasterVariable.class, JulianEasterFactory.FACTORY);

            // Others
            FACTORIES.put(Ramp.class, RampFactory.FACTORY);
            FACTORIES.put(InterventionVariable.class, IVFactory.FACTORY);
            FACTORIES.put(PeriodicDummies.class, PeriodicDummiesFactory.FACTORY);
            FACTORIES.put(PeriodicContrasts.class, PeriodicContrastsFactory.FACTORY);
            FACTORIES.put(TrigonometricVariables.class, TrigonometricVariablesFactory.FACTORY);

            FACTORIES.put(TsVariable.class, TsVariableFactory.FACTORY);
            FACTORIES.put(UserVariable.class, TsVariableFactory.FACTORY);
            FACTORIES.put(UserMovingHoliday.class, TsVariableFactory.FACTORY);
            FACTORIES.put(TsVariables.class, TsVariablesFactory.FACTORY);
            FACTORIES.put(UserVariables.class, TsVariablesFactory.FACTORY);
            FACTORIES.put(UserTradingDays.class, TsVariablesFactory.FACTORY);
        }
    }

    public <D extends TimeSeriesDomain> CanonicalMatrix matrix(@NonNull D domain, @NonNull ITsVariable... vars) {
        int nvars = ITsVariable.dim(vars);
        int nobs = domain.length();
        CanonicalMatrix M = CanonicalMatrix.make(nobs, nvars);

        MatrixWindow wnd = M.left(0);
        if (domain instanceof TsDomain) {
            TsPeriod start = ((TsDomain) domain).getStartPeriod();
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariable v = vars[i];
                wnd.hnext(v.dim());
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory != null) {
                    factory.fill(v, start, wnd);
                }
            }
        } else {
            for (int i = 0, j = 0; i < vars.length; ++i) {
                ITsVariable v = vars[i];
                MatrixWindow cur = wnd.right(v.dim());
                RegressionVariableFactory factory = FACTORIES.get(v.getClass());
                if (factory != null) {
                    factory.fill(v, domain, cur);
                }
            }
        }
        return M;
    }

    public <D extends TimeSeriesDomain> DataBlock x(@NonNull D domain, @NonNull ITsVariable vars) {
        if (vars.dim() != 1) {
            throw new IllegalArgumentException();
        }
        CanonicalMatrix m = matrix(domain, vars);
        return DataBlock.of(m.getStorage());
    }

}