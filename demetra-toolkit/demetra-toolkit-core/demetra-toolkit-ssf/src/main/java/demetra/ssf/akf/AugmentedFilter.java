/*
 * Copyright 2016 National Bank copyOf Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions copyOf the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy copyOf the Licence at:
 *  
 * http://ec.europa.eu/idabc/eupl
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.ssf.akf;

import demetra.data.DataBlock;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.State;
import demetra.ssf.StateInfo;
import demetra.ssf.univariate.ISsf;
import demetra.ssf.univariate.ISsfData;
import java.util.Iterator;
import demetra.data.DataBlockIterator;
import demetra.data.DoubleSeqCursor;
import demetra.ssf.ISsfInitialization;
import demetra.ssf.univariate.ISsfError;
import demetra.ssf.ISsfLoading;
import demetra.ssf.univariate.ISsfMeasurement;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class AugmentedFilter {

    private AugmentedState state;
    private AugmentedUpdateInformation pe;
    private ISsf ssf;
    private ISsfLoading loading;
    private ISsfError error;
    private ISsfDynamics dynamics;
    private ISsfData data;
    private boolean missing;
    private final boolean collapsing;
    private int collapsingPos = -1;

    /**
     *
     */
    public AugmentedFilter() {
        collapsing = false;
    }

    public AugmentedFilter(final boolean collapsing) {
        this.collapsing = collapsing;
    }

    protected boolean error(int t) {
        missing = data.isMissing(t);
        if (missing) {
            pe.E().set(0);
            pe.M().set(0);
            // pe_ = null;
            return false;
        } else {
            // pe_ = new PredictionError(ssf_.getStateDim(), 1);
            // K = PZ'/f
            // computes (ZP)' in K'. Missing values are set to 0 
            // Z~v x r, P~r x r, K~r x v
            DataBlock C = pe.M();
            // computes ZPZ'; results in pe_.L
            //measurement.ZVZ(pos_, state_.P.subMatrix(), F);
            loading.ZM(t, state.P(), C);
            double v = loading.ZX(t, C);
            if (error != null) {
                v += error.at(t);
            }
            if (v < State.ZERO) {
                v = 0;
            }
            pe.setVariance(v);
            // We put in K  PZ'*(ZPZ'+H)^-1 = PZ'* F^-1 = PZ'*(LL')^-1/2 = PZ'(L')^-1
            // K L' = PZ' or L K' = ZP

            double y = data.get(t);
            pe.set(y - loading.ZX(t, state.a()));
            loading.ZM(t, state.B(), pe.E());
            pe.E().apply(x -> -x);
            return true;
        }
    }

    protected void update() {
        double v = pe.getVariance(), e = pe.get();
        // P = P - (M)* F^-1 *(M)' --> Symmetric
        // PZ'(LL')^-1 ZP' =PZ'L'^-1*L^-1*ZP'
        // a = a + (M)* F^-1 * v
        state.a().addAY(e / v, pe.M());
        DataBlockIterator acols = state.B().columnsIterator();
        DoubleSeqCursor cell = pe.E().cursor();
        while (acols.hasNext()) {
            acols.next().addAY(cell.getAndNext() / v, pe.M());
        }
        update(state.P(), v, pe.M());
    }

    /**
     *
     * @return
     */
    public AugmentedState getState() {
        return state;
    }

    public int getCollapsingPosition() {
        return collapsingPos;
    }

    private boolean initState() {
        state = AugmentedState.of(ssf);
        if (state == null) {
            return false;
        }
        ISsfInitialization initialization = ssf.initialization();
        pe = new AugmentedUpdateInformation(initialization.getStateDim(), initialization.getDiffuseDim());
        return true;
    }

    /**
     *
     * @param ssf
     * @param data
     * @param rslts
     * @return
     */
    public boolean process(final ISsf ssf, final ISsfData data, final IAugmentedFilteringResults rslts) {
        this.ssf=ssf;
        loading = ssf.loading();
        error=ssf.measurementError();
        dynamics = ssf.dynamics();
        this.data = data;
        if (!initState()) {
            return false;
        }
        int t = 0, end = data.length();
        while (t < end) {
            if (collapse(t, rslts)) {
                break;
            }
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Forecast);
            }
            if (error(t)) {
                if (rslts != null) {
                    rslts.save(t, pe);
                }
                update();
            }
            if (rslts != null) {
                rslts.save(t, state, StateInfo.Concurrent);
            }
            state.next(t++, dynamics);
        }
        return true;
    }

    // P -= c*r
    private void update(Matrix P, double v, DataBlock C) {
        P.addXaXt(-1 / v, C);
    }

    protected boolean collapse(int t, IAugmentedFilteringResults decomp) {
        if (!collapsing) {
            return false;
        }
        // update the state vector
        if (!decomp.collapse(t, state)) {
            return false;
        }
        collapsingPos = t;
        return true;
    }

}
