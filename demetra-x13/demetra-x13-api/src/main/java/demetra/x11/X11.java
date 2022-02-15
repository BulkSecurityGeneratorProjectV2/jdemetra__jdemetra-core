/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.x11;

import demetra.design.Algorithm;
import demetra.processing.GenericResults;
import demetra.processing.ProcResults;
import nbbrd.design.Development;
import nbbrd.service.ServiceDefinition;
import demetra.timeseries.TsData;
import java.util.List;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;

/**
 *
 * @author Thomas Witthohn
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class X11 {

 
    private final X11Loader.Processor PROCESSOR = new X11Loader.Processor();

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public final static class DefProcessor implements Processor{

        @Override
        public ProcResults process(TsData timeSeries, X11Spec spec, List<String> items) {
            return GenericResults.notImplemented();
        }
        
     };
    
    public ProcResults process(@lombok.NonNull TsData timeSeries, @lombok.NonNull X11Spec spec, List<String> items) {
        return PROCESSOR.get().process(timeSeries, spec, items);
    }
    
   @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT, fallback=DefProcessor.class)
    public static interface Processor {

        ProcResults process(@lombok.NonNull TsData timeSeries, @lombok.NonNull X11Spec spec, @lombok.NonNull List<String> items);

    }

}
