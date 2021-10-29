/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.LinearTrend;
import demetra.timeseries.TimeSeriesDomain;
import demetra.timeseries.TsPeriod;
import jdplus.math.matrices.FastMatrix;
import demetra.timeseries.TimeSeriesInterval;

/**
 *
 * @author Jean Palate
 */
class LinearTrendFactory implements RegressionVariableFactory<LinearTrend>{

    static LinearTrendFactory FACTORY=new LinearTrendFactory();

    private LinearTrendFactory(){}

    @Override
    public boolean fill(LinearTrend var, TsPeriod start, FastMatrix buffer) {
        int del=TsPeriod.of(start.getUnit(), var.getStart()).until(start);
        buffer.column(0).set(r->r+del);
        return true;
    }

    @Override
    public <P extends TimeSeriesInterval<?>, D extends TimeSeriesDomain<P>>  boolean fill(LinearTrend var, D domain, FastMatrix buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
