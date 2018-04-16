/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.maths.linearfilters.SymmetricFilter;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;

/**
 *
 * @author Christiane Hofer
 */
@Development(status = Development.Status.Exploratory)
public class DefaultNormalizingStrategie implements INormalizing {

    @Override
    public String getDescription() {
        return "Default Normalizer";
    }

    @Override
    public TsData process(TsData s, TsDomain domain, int freq) {
        SymmetricFilter f = TrendCycleFilterFactory.makeTrendFilter(freq);
        IEndPointsProcessor iep = new CopyEndPoints(f.getLength() / 2);

        IFiltering n = new DefaultTrendFilteringStrategy(f, iep);
        TsData tmp = n.process(s, s.getDomain());

        return tmp;
    }

}
