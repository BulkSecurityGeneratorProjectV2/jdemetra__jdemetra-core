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
package jdplus.sa.diagnostics;

import demetra.sa.SaDiagnosticsFactory;
import java.util.function.Function;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.OutOfSampleDiagnosticsFactory;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsConfiguration;
import jdplus.regarima.diagnostics.ResidualsDiagnosticsFactory;
import jdplus.regsarima.regular.ModelEstimation;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author PALATEJ
 * @param <R>
 */
public class SaResidualsDiagnosticsFactory<R> extends ResidualsDiagnosticsFactory<R> implements SaDiagnosticsFactory<R> {

    public SaResidualsDiagnosticsFactory(ResidualsDiagnosticsConfiguration config, Function<R, ModelEstimation> extractor) {
        super(config, extractor);
    }

    @Override
    public Scope getScope() {
        return Scope.Modelling;
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
