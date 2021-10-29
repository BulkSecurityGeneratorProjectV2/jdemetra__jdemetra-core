/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.Constant;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.FastMatrix;
import demetra.timeseries.TimeSeriesInterval;

/**
 *
 * @author Jean Palate
 */
class ConstantFactory implements RegressionVariableFactory<Constant>{

    static ConstantFactory FACTORY=new ConstantFactory();

    private ConstantFactory(){}

    @Override
    public boolean fill(Constant var, TsPeriod start, FastMatrix buffer) {
        buffer.set(1);
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(Constant var, D domain, FastMatrix buffer) {
        buffer.set(1);
        return true;
    }
    
}
