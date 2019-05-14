/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.maths.linearfilters.BackFilter;
import demetra.maths.linearfilters.RationalBackFilter;
import demetra.maths.polynomials.UnitRoots;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import demetra.maths.matrices.Matrix;

/**
 *
 * @author palatej
 */
public class PeriodicOutlierFactory implements IOutlierFactory {

    private final boolean zeroEnded;
    private final int period;

    public PeriodicOutlierFactory(int period, boolean zeroEnded) {
        this.zeroEnded = zeroEnded;
        this.period = period;
    }

    @Override
    public PeriodicOutlier make(LocalDateTime position) {
        return new PeriodicOutlier(position, period, zeroEnded);
    }

    @Override
    public void fill(int xpos, DataBlock buffer) {
        double z = -1.0 / (period - 1);
        int len = buffer.length();
        if (zeroEnded) {
            int j = 1;
            do {
                for (; j < period && xpos > 0; ++j) {
                    --xpos;
                    if (xpos < len) {
                        buffer.set(xpos, z);
                    }
                }
                if (xpos > 0) {
                    --xpos;
                    if (xpos < len) {
                        buffer.set(xpos, 1);
                    }
                } else {
                    break;
                }
                j = 1;
            } while (true);
        } else {
            for (int i = xpos; i < len;) {
                if (i >= 0) {
                    buffer.set(i, 1);
                }
                ++i;
                for (int j = 1; j < period && i < len; ++i, ++j) {
                    if (i >= 0) {
                        buffer.set(i, z);
                    }
                }
            }
        }
    }

    @Override
    public FilterRepresentation getFilterRepresentation() {
        return new FilterRepresentation(new RationalBackFilter(
                BackFilter.ONE, new BackFilter(UnitRoots.D(period)), 0), 0);
    }

    @Override
    public int excludingZoneAtStart() {
        return 2 * period;
    }

    @Override
    public int excludingZoneAtEnd() {
        return 2 * period;
    }

    @Override
    public String getCode() {
        return PeriodicOutlier.CODE;
    }
}

class SOFactory implements RegressionVariableFactory<PeriodicOutlier> {

    static SOFactory FACTORY=new SOFactory();

    private SOFactory(){}

    @Override
    public boolean fill(PeriodicOutlier var, TsPeriod start, Matrix buffer) {
        PeriodicOutlierFactory fac = new PeriodicOutlierFactory(var.getPeriod(), var.isZeroEnded());
        TsPeriod p = start.withDate(var.getPosition());
        int opos = start.until(p);
        fac.fill(opos, buffer.column(0));
        return true;
    }

    @Override
    public <D extends TimeSeriesDomain> boolean fill(PeriodicOutlier var, D domain, Matrix buffer) {
        throw new UnsupportedOperationException("Not supported.");
    }

}
