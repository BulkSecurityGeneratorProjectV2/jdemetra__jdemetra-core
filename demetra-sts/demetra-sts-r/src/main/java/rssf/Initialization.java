/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rssf;

import jdplus.math.matrices.FastMatrix;
import jdplus.ssf.ISsfInitialization;
import demetra.math.matrices.Matrix;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class Initialization {
    
    public ISsfInitialization of(double[] a, Matrix P) {
        if (P == null) {
            throw new IllegalArgumentException();
        }
        
        return jdplus.ssf.Initialization.builder()
                .dim(P.getRowsCount())
                .a0(a)
                .Pf(FastMatrix.of(P))
                .build();
    }
    
    public ISsfInitialization ofDiffuse(double[] a, FastMatrix P, FastMatrix B, FastMatrix Pi) {
        if (B == null && Pi == null) {
            return of(a, P);
        }
        if (B == null) {
            throw new IllegalArgumentException();
        }
        return jdplus.ssf.Initialization.builder()
                .dim(P.getRowsCount())
                .diffuseDim(B.getColumnsCount())
                .a0(a)
                .Pf(FastMatrix.of(P))
                .B(B==null ? null : FastMatrix.of(B))
                .Pi(Pi==null ? null : FastMatrix.of(Pi))
                .build();
    }
}
