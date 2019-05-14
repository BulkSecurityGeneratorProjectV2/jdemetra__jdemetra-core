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
package demetra.ssf;

import demetra.data.DataBlock;
import demetra.data.DataWindow;
import demetra.maths.matrices.MatrixWindow;
import demetra.ssf.ISsfDynamics;
import demetra.ssf.multivariate.IMultivariateSsf;
import demetra.ssf.univariate.ISsf;
import java.util.List;
import demetra.maths.matrices.Matrix;

/**
 * Dynamics generated by a juxtaposition copyOf several dynamics. The underlying
 * state is the concatenation copyOf the original states.
 *
 * @author Jean Palate
 */
public class CompositeDynamics implements ISsfDynamics {

    private final ISsfDynamics[] dyn;
    private final int[] dim;

    public CompositeDynamics(int[] dim, ISsfDynamics... dyn) {
        this.dim = dim;
        this.dyn = dyn;
    }

    public int getComponentsCount() {
        return dyn.length;
    }

    public ISsfDynamics getComponent(int pos) {
        return dyn[pos];
    }

    @Override
    public boolean isTimeInvariant() {
        for (int i = 0; i < dyn.length; ++i) {
            if (!dyn[i].isTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean areInnovationsTimeInvariant() {
        for (int i = 0; i < dyn.length; ++i) {
            if (!dyn[i].areInnovationsTimeInvariant()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getInnovationsDim() {
        int ni = 0;
        for (int i = 0; i < dyn.length; ++i) {
            ni += dyn[i].getInnovationsDim();
        }
        return ni;
    }

    @Override
    public void V(int pos, Matrix qm) {
        MatrixWindow cur = qm.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].V(pos, cur);
        }
    }

    @Override
    public boolean hasInnovations(int pos) {
        for (int i = 0; i < dyn.length; ++i) {
            if (dyn[i].hasInnovations(pos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void S(int pos, Matrix sm) {
        MatrixWindow cur = sm.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            int rcount = dim[i];
            int rdim = dyn[i].getInnovationsDim();
            cur.next(rcount, rdim);
            if (rdim > 0) {
                dyn[i].S(pos, cur);
            }
        }
    }

    @Override
    public void addSU(int pos, DataBlock x, DataBlock u) {
        DataWindow xwnd = x.left(), uwnd = u.left();
        for (int i = 0; i < dyn.length; ++i) {
            int rcount = dim[i];
            int rdim = dyn[i].getInnovationsDim();
            DataBlock xcur = xwnd.next(rcount);
            if (rdim > 0) {
                dyn[i].addSU(pos, xcur, uwnd.next(rdim));
            }
        }
    }

    @Override
    public void XS(int pos, DataBlock x, DataBlock xs) {
        DataWindow xwnd = x.left(), ywnd = xs.left();
        for (int i = 0; i < dyn.length; ++i) {
            int rcount = dim[i];
            int rdim = dyn[i].getInnovationsDim();
            DataBlock xcur = xwnd.next(rcount);
            if (rdim > 0) {
                dyn[i].XS(pos, xcur, ywnd.next(rdim));
            }
        }
    }

    @Override
    public void T(int pos, Matrix tr) {
        MatrixWindow cur = tr.topLeft();
        for (int i = 0, j = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].T(pos, cur);
        }
    }

    @Override
    public void TX(int pos, DataBlock x) {
        DataWindow cur = x.window(0, dim[0]);
        dyn[0].TX(pos, cur.get());
        for (int i = 1; i < dyn.length; ++i) {
            dyn[i].TX(pos, cur.next(dim[i]));
        }
    }

    @Override
    public void TM(int pos, Matrix x) {
        MatrixWindow cur = x.top(dim[0]);
        dyn[0].TM(pos, cur);
        for (int i = 1; i < dyn.length; ++i) {
            cur.vnext(dim[i]);
            dyn[i].TM(pos, cur);
        }
    }

    @Override
    public void XT(int pos, DataBlock x) {
        DataWindow cur = x.left();
        for (int i = 0; i < dyn.length; ++i) {
            dyn[i].XT(pos, cur.next(dim[i]));
        }
    }

    @Override
    public void TVT(int pos, Matrix v) {
        MatrixWindow D = v.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            int ni = dim[i];
            D.next(ni, ni);
            dyn[i].TVT(pos, D);
            MatrixWindow C = D.clone(), R = D.clone();
            for (int j = i + 1; j < dyn.length; ++j) {
                int nj = dim[j];
                C.vnext(nj);
                R.hnext(nj);
                Matrix Ct = C.transpose();
                dyn[j].TM(pos, C);
                dyn[i].TM(pos, Ct);
                R.copy(Ct);
            }
        }
    }

    @Override
    public void addV(int pos, Matrix p
    ) {
        MatrixWindow cur = p.topLeft();
        for (int i = 0; i < dyn.length; ++i) {
            cur.next(dim[i], dim[i]);
            dyn[i].addV(pos, cur);
        }
    }
}
