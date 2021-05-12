/*
 * Copyright 2015 National Bank of Belgium
 *  
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved 
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
 /*
 */
package jdplus.sts;

import demetra.sts.Component;
import demetra.sts.SeasonalModel;
import jdplus.data.DataBlock;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.ISsfLoading;
import jdplus.ssf.implementations.Loading;
import jdplus.ssf.univariate.Measurement;
import jdplus.ssf.univariate.Ssf;
import jdplus.ssf.univariate.ISsfMeasurement;
import jdplus.math.matrices.Matrix;

/**
 *
 * @author Jean Palate
 */
public class SsfBsm extends Ssf {

    private SsfBsm(BsmInitialization initialization, BsmDynamics dynamics, ISsfMeasurement measurement) {
        super(initialization, dynamics, measurement);
    }

    public static int searchPosition(BsmData model, Component type) {
        int n = 0;
        if (model.getNoiseVar() > 0) {
            if (type == Component.Noise) {
                return n;
            }
            ++n;
        }
        if (model.getCycleVar() >= 0) {
            if (type == Component.Cycle) {
                return n;
            }
            n += 2;
        }
        if (model.getLevelVar() >= 0) {
            if (type == Component.Level) {
                return n;
            }
            ++n;
        }
        if (model.getSlopeVar() >= 0) {
            if (type == Component.Slope) {
                return n;
            }
            ++n;
        }
        if (model.getSeasonalVar() >= 0 && type == Component.Seasonal) {
            return n;
        } else {
            return -1;
        }
    }

    public static int calcDim(BsmData model) {
        int n = 0;
        if (model.getNoiseVar() > 0) {
            ++n;
        }
        if (model.getCycleVar() >= 0) {
            n += 2;
        }
        if (model.getLevelVar() >= 0) {
            ++n;
        }
        if (model.getSlopeVar() >= 0) {
            ++n;
        }
        if (model.getSeasonalVar() >= 0) {
            n += model.getPeriod() - 1;
        }
        return n;
    }

    /**
     *
     */
    private static int[] calcCmpsIndexes(BsmData model) {
        int n = 0;
        if (model.getNoiseVar() > 0) {
            ++n;
        }
        if (model.getCycleVar() >= 0) {
            ++n;
        }
        if (model.getLevelVar() >= 0) {
            ++n;
        }
        if (model.getSeasonalVar() >= 0) {
            ++n;
        }
        int[] cmps = new int[n];
        int i = 0, j = 0;
        if (model.getNoiseVar() > 0) {
            cmps[i++] = j++;
        }
        if (model.getCycleVar() >= 0) {
            cmps[i++] = j;
            j += 2;
        }
        if (model.getLevelVar() >= 0) {
            cmps[i++] = j++;
        }
        if (model.getSlopeVar() >= 0) {
            ++j;
        }
        if (model.getSeasonalVar() >= 0) {
            cmps[i] = j;
        }
        return cmps;
    }

    public static SsfBsm of(BsmData model) {
        int[] idx = calcCmpsIndexes(model);
        Bsm data = new Bsm(model);
        BsmInitialization initialization = new BsmInitialization(data);
        BsmDynamics dynamics = new BsmDynamics(data);
        ISsfLoading loading = Loading.fromPositions(idx);
        return new SsfBsm(initialization, dynamics, new Measurement(loading, null));
    }

    static class Bsm {

        final Matrix tsvar, ltsvar;
        final double lVar, sVar, seasVar, cVar, nVar, cDump;
        final double ccos, csin;
        final int period;
        final SeasonalModel seasModel;

        Bsm(BsmData model) {
            lVar = model.getLevelVar();
            sVar = model.getSlopeVar();
            seasVar = model.getSeasonalVar();
            nVar = model.getNoiseVar() > 0 ? model.getNoiseVar() : 0;
            cVar = model.getCycleVar();
            if (cVar >= 0) {
                cDump = model.getCycleDumpingFactor();
                double q = Math.PI * 2 / (model.getPeriod() * model.getCycleLength());
                ccos = cDump * Math.cos(q);
                csin = cDump * Math.sin(q);
            } else {
                cDump = 0;
                ccos = 0;
                csin = 0;
            }
            seasModel = model.getSeasonalModel();
            period = model.getPeriod();
            if (seasVar > 0) {
                tsvar = SeasonalComponent.tsVar(seasModel, period);
                tsvar.mul(seasVar);
                if (model.getSeasonalModel() != SeasonalModel.Crude && model.getSeasonalModel() != SeasonalModel.Dummy) {
                    ltsvar = SeasonalComponent.tslVar(seasModel, period);
                    ltsvar.mul(Math.sqrt(seasVar));
                } else {
                    ltsvar = null;
                }
            } else {
                tsvar = null;
                ltsvar = null;
            }
        }
    }

    static class BsmInitialization implements ISsfInitialization {

        private final Bsm data;

        BsmInitialization(Bsm data) {
            this.data = data;
        }

        @Override
        public int getStateDim() {
            int r = 0;
            if (data.nVar > 0) {
                ++r;
            }
            if (data.cVar >= 0) {
                r += 2;
            }
            if (data.lVar >= 0) {
                ++r;
            }
            if (data.sVar >= 0) {
                ++r;
            }
            if (data.seasVar >= 0) {
                r += data.period - 1;
            }
            return r;
        }

        @Override
        public boolean isDiffuse() {
            return data.lVar >= 0 || data.seasVar >= 0;
        }

        @Override
        public int getDiffuseDim() {
            int r = 0;
            if (data.lVar >= 0) {
                ++r;
            }
            if (data.sVar >= 0) {
                ++r;
            }
            if (data.seasVar >= 0) {
                r += data.period - 1;
            }
            return r;
        }

        @Override
        public void diffuseConstraints(Matrix b) {
            int sdim = getStateDim();
            int istart = data.nVar > 0 ? 1 : 0;
            if (data.cVar >= 0) {
                istart += 2;
            }
            int iend = sdim;
            for (int i = istart, j = 0; i < iend; ++i, ++j) {
                b.set(i, j, 1);
            }
        }

        @Override
        public void Pi0(Matrix p) {
            int sdim = getStateDim();
            int istart = data.nVar > 0 ? 1 : 0;
            if (data.cVar >= 0) {
                istart += 2;
            }
            int iend = sdim;
            for (int i = istart; i < iend; ++i) {
                p.set(i, i, 1);
            }
        }

        @Override
        public void a0(DataBlock a0) {
        }

        @Override
        public void Pf0(Matrix p) {
            int i = 0;
            if (data.nVar > 0) {
                p.set(0, 0, data.nVar);
                ++i;
            }
            if (data.cVar > 0) {
                double q = data.cVar / (1 - data.cDump * data.cDump);
                p.set(i, i, q);
                ++i;
                p.set(i, i, q);
                ++i;
            }
            if (data.lVar >= 0) {
                if (data.lVar != 0) {
                    p.set(i, i, data.lVar);
                }
                ++i;
            }
            if (data.sVar >= 0) {
                if (data.sVar != 0) {
                    p.set(i, i, data.sVar);
                }
                ++i;
            }
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    p.set(i, i, data.seasVar);
                } else {
                    int j = data.tsvar.getRowsCount();
                    p.extract(i, j, i, j).copy(data.tsvar);
                }
            }
        }
    }

    static class BsmDynamics implements ISsfDynamics {

        private final Bsm data;

        BsmDynamics(Bsm data) {
            this.data = data;
        }

        @Override
        public boolean isTimeInvariant() {
            return true;
        }

        @Override
        public boolean areInnovationsTimeInvariant() {
            return true;
        }

        @Override
        public int getInnovationsDim() {
            int nr = 0;
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy || data.seasModel == SeasonalModel.Crude) {
                    ++nr;
                } else {
                    nr += data.period - 1;
                }
            }
            if (data.nVar > 0) {
                ++nr;
            }
            if (data.cVar > 0) {
                nr += 2;
            }
            if (data.lVar > 0) {
                ++nr;
            }
            if (data.sVar > 0) {
                ++nr;
            }
            return nr;
        }

        @Override
        public void V(int pos, Matrix v) {
            int i = 0;
            if (data.nVar > 0) {
                v.set(i, i, data.nVar);
                ++i;
            }
            if (data.cVar >= 0) {
                v.set(i, i, data.cVar);
                ++i;
                v.set(i, i, data.cVar);
                ++i;
            }
            if (data.lVar >= 0) {
                if (data.lVar != 0) {
                    v.set(i, i, data.lVar);
                }
                ++i;
            }
            if (data.sVar >= 0) {
                if (data.sVar != 0) {
                    v.set(i, i, data.sVar);
                }
                ++i;
            }
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    v.set(i, i, data.seasVar);
                } else {
                    int j = data.tsvar.getRowsCount();
                    v.extract(i, j, i, j).copy(data.tsvar);
                }
            }
        }

        @Override
        public boolean hasInnovations(int pos) {
            return true;
        }

        @Override
        public void S(int pos, Matrix s) {
            int i = 0, j = 0;
            if (data.nVar > 0) {
                s.set(i++, j++, Math.sqrt(data.nVar));
            }
            if (data.cVar > 0) {
                double ce = Math.sqrt(data.cVar);
                s.set(i++, j++, ce);
                s.set(i++, j++, ce);
            } else if (data.cVar == 0) {
                i += 2;
            }
            if (data.lVar > 0) {
                s.set(i++, j++, Math.sqrt(data.lVar));
            } else if (data.lVar == 0) {
                ++i;
            }
            if (data.sVar > 0) {
                s.set(i++, j++, Math.sqrt(data.sVar));
            } else if (data.sVar == 0) {
                ++i;
            }
            if (data.seasVar > 0) {
                switch (data.seasModel) {
                    case Dummy:
                        s.set(i, j, Math.sqrt(data.seasVar));
                        break;
                    case Crude:
                        s.extract(i, data.period - 1, j, 1).set(Math.sqrt(data.seasVar));
                        break;
                    default:
                        s.extract(i, data.period - 1, j, data.period - 1).copy(data.ltsvar);
                        break;
                }
            }
        }

        @Override
        public void addSU(int pos, DataBlock x, DataBlock u) {
            int i = 0, j = 0;
            if (data.nVar > 0) {
                x.add(i++, u.get(j++) * Math.sqrt(data.nVar));
            }
            if (data.cVar > 0) {
                double ce = Math.sqrt(data.cVar);
                x.add(i++, u.get(j++) * ce);
                x.add(i++, u.get(j++) * ce);
            } else if (data.cVar == 0) {
                i += 2;
            }
            if (data.lVar > 0) {
                x.add(i++, u.get(j++) * Math.sqrt(data.lVar));
            } else if (data.lVar == 0) {
                ++i;
            }
            if (data.sVar > 0) {
                x.add(i++, u.get(j++) * Math.sqrt(data.sVar));
            } else if (data.sVar == 0) {
                ++i;
            }
            if (data.seasVar > 0) {
                switch (data.seasModel) {
                    case Dummy:
                        x.add(i, u.get(j) * Math.sqrt(data.seasVar));
                        break;
                    case Crude:
                        x.range(i, i + data.period - 1).add(Math.sqrt(data.seasVar) * u.get(j));
                        break;
                    default:
                        x.range(i, i + data.period - 1).addProduct(data.ltsvar.rowsIterator(), u.range(j, j + data.period - 1));
                        break;
                }
            }
        }

        @Override
        public void XS(int pos, DataBlock x, DataBlock xs) {
            int i = 0, j = 0;
            if (data.nVar > 0) {
                xs.set(j++, x.get(i++) * Math.sqrt(data.nVar));
            }
            if (data.cVar > 0) {
                double ce = Math.sqrt(data.cVar);
                xs.set(j++, x.get(i++) * ce);
                xs.set(j++, x.get(i++) * ce);
            } else if (data.cVar == 0) {
                i += 2;
            }
            if (data.lVar > 0) {
                xs.set(j++, x.get(i++) * Math.sqrt(data.lVar));
            } else if (data.lVar == 0) {
                ++i;
            }
            if (data.sVar > 0) {
                xs.set(j++, x.get(i++) * Math.sqrt(data.sVar));
            } else if (data.sVar == 0) {
                ++i;
            }
            if (data.seasVar > 0) {
                switch (data.seasModel) {
                    case Dummy:
                        xs.set(j, x.get(i) * Math.sqrt(data.seasVar));
                        break;
                    case Crude:
                        xs.set(j, x.range(i, i + data.period - 1).sum() * Math.sqrt(data.seasVar));
                        break;
                    default:
                        xs.range(j, j + data.period - 1).product(x.range(i, i + data.period - 1), data.ltsvar.columnsIterator());
                        break;
                }
            }
        }

        @Override
        public void T(int pos, Matrix tr) {
            int i = 0;
            if (data.nVar > 0) {
                ++i;
            }
            if (data.cVar >= 0) {
                tr.set(i, i, data.ccos);
                tr.set(i + 1, i + 1, data.ccos);
                tr.set(i, i + 1, data.csin);
                tr.set(i + 1, i, -data.csin);
                i += 2;
            }
            if (data.lVar >= 0) {
                tr.set(i, i, 1);
                if (data.sVar >= 0) {
                    tr.set(i, i + 1, 1);
                    ++i;
                    tr.set(i, i, 1);
                }
                ++i;
            }
            if (data.seasVar >= 0) {
                Matrix seas = tr.extract(i, data.period - 1, i, data.period - 1);
                seas.row(0).set(-1);
                seas.subDiagonal(-1).set(1);
            }
        }

        @Override
        public void TX(int pos, DataBlock x) {
            int i0 = 0;
            if (data.nVar > 0) {
                x.set(0, 0);
                ++i0;
            }
            if (data.cVar >= 0) {
                double a = x.get(i0), b = x.get(i0 + 1);
                x.set(i0, a * data.ccos + b * data.csin);
                x.set(i0 + 1, -a * data.csin + b * data.ccos);
                i0 += 2;
            }
            if (data.lVar >= 0) {
                if (data.sVar >= 0) {
                    x.add(i0, x.get(i0 + 1));
                    i0 += 2;
                } else {
                    ++i0;
                }
            }
            if (data.seasVar >= 0) {
                DataBlock ex = x.extract(i0, data.period - 1, 1);
                ex.fshiftAndNegSum();
            }
        }

        @Override
        public void XT(int pos, DataBlock x) {
            int i0 = 0;
            if (data.nVar > 0) {
                x.set(0, 0);
                ++i0;
            }
            if (data.cVar >= 0) {
                double a = x.get(i0), b = x.get(i0 + 1);
                x.set(i0, a * data.ccos - b * data.csin);
                x.set(i0 + 1, a * data.csin + b * data.ccos);
                i0 += 2;

            }
            if (data.lVar >= 0) {
                if (data.sVar >= 0) {
                    x.add(i0 + 1, x.get(i0));
                    i0 += 2;
                } else {
                    ++i0;
                }
            }
            if (data.seasVar >= 0) {
                int imax = i0 + data.period - 2;
                double xs = x.get(i0);
                for (int i = i0; i < imax; ++i) {
                    x.set(i, x.get(i + 1) - xs);
                }
                x.set(imax, -xs);
            }
        }

        @Override
        public void addV(int pos, Matrix p) {
            int i = 0;
            if (data.nVar > 0) {
                p.add(i, i, data.nVar);
                ++i;
            }
            if (data.cVar >= 0) {
                p.add(i, i, data.cVar);
                ++i;
                p.add(i, i, data.cVar);
                ++i;
            }
            if (data.lVar >= 0) {
                if (data.lVar != 0) {
                    p.add(i, i, data.lVar);
                }
                ++i;
            }
            if (data.sVar >= 0) {
                if (data.sVar != 0) {
                    p.add(i, i, data.sVar);
                }
                ++i;
            }
            if (data.seasVar > 0) {
                if (data.seasModel == SeasonalModel.Dummy) {
                    p.add(i, i, data.seasVar);
                } else {
                    int j = data.tsvar.getRowsCount();
                    p.extract(i, j, i, j).add(data.tsvar);
                }
            }

        }

    }
}
