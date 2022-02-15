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
package jdplus.x13.spi;

import demetra.information.InformationExtractors;
import demetra.processing.DefaultProcessingLog;
import demetra.processing.GenericResults;
import demetra.processing.ProcResults;
import demetra.timeseries.TsData;
import demetra.timeseries.regression.ModellingContext;
import demetra.x13.X13;
import demetra.x13.X13Spec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jdplus.x13.X13Kernel;
import jdplus.x13.X13Results;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(X13.Processor.class)
public class X13Computer implements X13.Processor {
    
    @Override
    public ProcResults process(TsData series, X13Spec spec, ModellingContext context, List<String> items) {
        X13Kernel x13 = X13Kernel.of(spec, context);
        DefaultProcessingLog log = new DefaultProcessingLog();
        X13Results rslt = x13.process(series, log);
        return GenericResults.of(rslt, items, log) ; 
     }
    
    @Override
    public Map<String, Class> outputDictionary(boolean compact) {
        Map<String, Class> dic = new LinkedHashMap<>();
        InformationExtractors.fillDictionary(jdplus.x13.X13Results.class, null, dic, compact);
        return dic;
    }
}
