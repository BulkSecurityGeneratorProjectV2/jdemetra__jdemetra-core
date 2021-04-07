/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.linearmodel;

import java.util.Random;
import jdplus.data.DataBlock;
import jdplus.math.matrices.Matrix;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author palatej
 */
public class LeastSquaresResultsTest {
    
    public LeastSquaresResultsTest() {
    }

    @Test
    public void testEqualities() {
        int N=200;
        DataBlock y=DataBlock.make(N);
        Random rnd=new Random(0);
        y.set(rnd::nextDouble);
        Matrix X=Matrix.make(N, 5);
        X.set((a,b)->rnd.nextDouble());
        
        LinearModel lm = LinearModel.builder()
                .y(y)
                .addX(X)
                .meanCorrection(true)
                .build();
        
        LeastSquaresResults lsr = Ols.compute(lm);
        
        // SST = SSE + SSR
        assertEquals(lsr.getRegressionSumOfSquares()+lsr.getResidualSumOfSquares(), lsr.getTotalSumOfSquares(), 1e-9);
        // R2 = SSR/SST
        assertEquals(lsr.getR2(), lsr.getRegressionSumOfSquares()/lsr.getTotalSumOfSquares(),1e-9);
        // F = MSR/MSE
        assertEquals(lsr.Ftest().getValue(), lsr.getRegressionMeanSquare()/lsr.getResidualMeanSquare(),1e-9);
        // K2 = SSR/MSE
        double khi2=lsr.getRegressionSumOfSquares()/lsr.getResidualMeanSquare();
        // khi2(5), mean=5
        assertTrue(khi2>2 && khi2<15);
        assertEquals(lsr.Ftest().getPvalue(), lsr.Khi2Test().getPvalue(), 0.2);
    }
    
}
