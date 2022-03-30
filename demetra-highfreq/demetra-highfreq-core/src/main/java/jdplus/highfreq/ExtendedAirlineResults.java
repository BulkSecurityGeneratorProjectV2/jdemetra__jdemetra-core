/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.highfreq;

import demetra.processing.ProcessingLog;
import demetra.sa.SeriesDecomposition;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder
public class ExtendedAirlineResults {

    private ExtendedRegAirlineModel preprocessing;
    private ExtendedAirlineDecomposition decomposition;
    private SeriesDecomposition finals;
    private ProcessingLog log;
    
}
