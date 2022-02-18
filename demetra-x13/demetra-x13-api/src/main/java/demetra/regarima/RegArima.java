/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.regarima;

import demetra.design.Algorithm;
import demetra.processing.GenericResults;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import nbbrd.design.Development;
import nbbrd.service.Mutability;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class RegArima {

    private final RegArimaLoader.Processor ENGINE = new RegArimaLoader.Processor();
    private final AtomicReference<Processor> LEGACYENGINE=new AtomicReference<Processor>();

    public void setEngine(Processor algorithm) {
        ENGINE.set(algorithm);
    }

    public Processor getEngine() {
        return ENGINE.get();
    }

    public ProcResults process(TsData series, RegArimaSpec spec, ModellingContext context, List<String> items) {
        return ENGINE.get().process(series, spec, context, items);
    }

    public void setLegacyEngine(Processor algorithm) {
        LEGACYENGINE.set(algorithm);
    }

    public Processor getLegacyEngine() {
        return LEGACYENGINE.get();
    }

    public ProcResults  processLegacy(TsData series, RegArimaSpec spec, ModellingContext context, List<String> items) {
        Processor cp = LEGACYENGINE.get();
        if (cp == null)
            throw new RegArimaException("No legacy engine");
        return cp.process(series, spec, context, items);
    }

    public final static class DefProcessor implements Processor{
        
        @Override
        public ProcResults process(TsData series, RegArimaSpec spec, ModellingContext context, List<String> items) {
            return GenericResults.notImplemented();
        }
    }
    
   @Algorithm
    @ServiceDefinition(quantifier = Quantifier.SINGLE, mutability = Mutability.CONCURRENT, fallback=DefProcessor.class)
    @FunctionalInterface
    public static interface Processor {

        public ProcResults process(TsData series, RegArimaSpec spec, ModellingContext context, List<String> addtionalItems);

    }
}
