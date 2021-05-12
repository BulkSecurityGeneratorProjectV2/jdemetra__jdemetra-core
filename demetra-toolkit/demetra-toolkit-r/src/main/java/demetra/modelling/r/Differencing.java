/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.modelling.r;

import demetra.data.DoubleSeq;
import demetra.modelling.StationaryTransformation;
import demetra.modelling.io.protobuf.ModellingProtosUtility;
import jdplus.data.DataBlock;
import jdplus.modelling.DifferencingResults;
import jdplus.modelling.FastDifferencingModule;
import jdplus.modelling.RangeMeanTest;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class Differencing {
    
    public double[] differences(double[] data, int[] dlags, boolean mean) {
        DataBlock z = DataBlock.of(data.clone());
        for (int i = 0; i < dlags.length; ++i) {
            z.autoApply(-dlags[i], (a, b) -> a - b);
            z = z.drop(dlags[i], 0);
        }
        if (mean) {
            z.sub(z.average());
        }
        return z.toArray();
    }
    
    public StationaryTransformation doStationary(double[] data, int period) {
        DifferencingResults dr = DifferencingResults.of(DoubleSeq.of(data), period, -1, true);
        return StationaryTransformation.builder()
                .meanCorrection(dr.isMean())
                .difference(new StationaryTransformation.Differencing(1, dr.getDifferencingOrder()))
                .stationarySeries(dr.getDifferenced())
                .build();
    }
    
    public StationaryTransformation fastDifferencing(double[] data, int period, boolean mad, double centile, double k) {
        FastDifferencingModule diff = FastDifferencingModule.builder()
                .mad(mad)
                .centile(centile)
                .k(k)
                .build();
        DoubleSeq x = DoubleSeq.of(data);
        int[] D = diff.process(x, new int[]{1, period}, null);
        
        if (D[0] != 0) {
            x = x.delta(1, D[0]);
        }
        if (D[1] != 0) {
            x = x.delta(period, D[1]);
        }
        if (diff.isMeanCorrection()) {
            x = x.removeMean();
        }
        
        return StationaryTransformation.builder()
                .meanCorrection(diff.isMeanCorrection())
                .difference(new StationaryTransformation.Differencing(1, D[0]))
                .difference(new StationaryTransformation.Differencing(period, D[1]))
                .stationarySeries(x)
                .build();
    }
    
    public byte[] toBuffer(StationaryTransformation st) {
        return ModellingProtosUtility.convert(st).toByteArray();
    }
    
}
