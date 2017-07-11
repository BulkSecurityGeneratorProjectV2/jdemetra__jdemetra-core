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
package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.arima.IArimaModel;
import ec.tstoolkit.arima.estimation.AnsleyFilter;
import ec.tstoolkit.arima.estimation.ConcentratedLikelihoodEstimation;
import ec.tstoolkit.arima.estimation.IArmaFilter;
import ec.tstoolkit.arima.estimation.ModifiedLjungBoxFilter;
import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.data.ReadDataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.eco.RegModel;
import ec.tstoolkit.modelling.IRobustStandardDeviationComputer;
import ec.tstoolkit.timeseries.regression.AbstractOutlierVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.simplets.TsPeriod;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class ResidualsOutlierDetector<T extends IArimaModel> extends AbstractSingleOutlierDetector<T> {

    private IArmaFilter m_filter;
    private double[] m_el;
    private int m_n;

    // EChol^-1 * dy
    /**
     *
     * @param filter
     */
    public ResidualsOutlierDetector() {
        this(IRobustStandardDeviationComputer.mad());
    }

    public ResidualsOutlierDetector(IRobustStandardDeviationComputer computer) {
        this(computer, null);
    }
    /**
     * 
     * @param computer
     * @param filter
     */
    public ResidualsOutlierDetector(IRobustStandardDeviationComputer computer, IArmaFilter filter) {
        super(computer);
        if (filter == null) {
            m_filter = new AnsleyFilter();
        } else {
            m_filter = filter;
        }
    }

    /**
     *
     * @return
     */
    @Override
    protected boolean calc() {
        RegModel dmodel = getModel().getDModel();
        m_n = m_filter.initialize(getModel().getArma(), dmodel.getObsCount());
        if (!initialize(dmodel)) {
            return false;
        }
        for (int i = 0; i < getOutlierFactoriesCount(); ++i) {
            processOutlier(i);
        }
        return true;
    }

    /**
     *
     * @param model
     * @return
     */
    protected boolean initialize(RegModel model) {
        try {
            ConcentratedLikelihoodEstimation estimation = new ConcentratedLikelihoodEstimation();
            if (!estimation.estimate(getModel())) {
                return false;
            }
            m_el = estimation.getResiduals();
            getStandardDeviationComputer().compute(new ReadDataBlock(m_el));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     *
     * @param idx
     */
    protected void processOutlier(int idx) {
        int n = getModel().getY().getLength();
        int d = getModel().getDifferencingFilter().getDegree();
        double[] o = new double[2 * n];
        DataBlock O = new DataBlock(o);
        TsPeriod start = getDomain().getStart();
        IOutlierVariable outlier = getOutlierFactory(idx).create(start.firstday());
        outlier.data(start.minus(n), O);
        double[] od = new double[o.length - d];
        DataBlock OD = new DataBlock(od);
        getModel().getDifferencingFilter().filter(O, OD);

        DataBlock OL = new DataBlock(od, n, 2 * n - d, 1);
        for (int i = 0; i < n; ++i) {
            if (isDefined(i, idx)) {
//                double[] ol = new double[n - d];
//                DataBlock OL = new DataBlock(ol);
//                System.arraycopy(od, n - i , ol, 0, ol.length);
                double[] u = new double[m_n];
                DataBlock U = new DataBlock(u);
                m_filter.filter(OL, U);
                double xx = 0, xy = 0;
                for (int j = 0; j < u.length; ++j) {
                    xx += u[j] * u[j];
                    xy += u[j] * m_el[j];
                }

                if (xx <= 0) {
                    exclude(i, idx);
                } else {
                    setT(i, idx, (xy / (Math.sqrt(xx)) / getMAD()));
                    setCoefficient(i, idx, xy/xx);
                }
            }
            OL.move(-1);
        }
    }

    protected DataBlock filter(DataBlock res) {
        ModifiedLjungBoxFilter f = new ModifiedLjungBoxFilter();
        IArimaModel arma = getModel().getArma();
        int nf = f.initialize(arma, res.getLength());
        DataBlock fres = new DataBlock(nf);
        f.filter(res, fres);
        return fres.drop(nf - getModel().getDModel().getObsCount(), 0);
    }

    @Override
    protected void clear(boolean all) {
        super.clear(all);
    }
}
