/*
* Copyright 2013 National Bank of Belgium
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

package ec.tss.sa.diagnostics;

import ec.tss.sa.ISaDiagnosticsFactory;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IDiagnostics;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(ISaDiagnosticsFactory.class)
public class OutOfSampleDiagnosticsFactory implements ISaDiagnosticsFactory {

    public static final String MEAN = "mean", MSE = "mse";
    public static final String NAME = "Out-of-sample";
    public static final List<String> ALL = Collections.unmodifiableList(Arrays.asList(MEAN, MSE));

    //public static final OutOfSampleDiagnosticsFactory Default = new OutOfSampleDiagnosticsFactory();
    private OutOfSampleDiagnosticsConfiguration config_;

    public OutOfSampleDiagnosticsFactory() {
        config_ = new OutOfSampleDiagnosticsConfiguration();
    }

    public OutOfSampleDiagnosticsFactory(OutOfSampleDiagnosticsConfiguration config) {
        config_ = config;
    }

    public OutOfSampleDiagnosticsConfiguration getConfiguration() {
        return config_;
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return NAME; 
    }
    
    @Override
    public String getDescription() {
        return "Out of Sample analysis";
    }
    
    @Override
    public List<String> getTestDictionary(){
        return ALL.stream().map(s->s+":2").collect(Collectors.toList());
    }

    @Override
    public boolean isEnabled() {
        return config_.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        config_.setEnabled(enabled);
    }

    @Override
    public Object getProperties() {
        return config_.clone();
    }

    @Override
    public void setProperties(Object obj) {
        OutOfSampleDiagnosticsConfiguration config = (OutOfSampleDiagnosticsConfiguration) obj;
        if (config != null) {
            config.check();
            config_ = config.clone();
        }
    }

    @Override
    public IDiagnostics create(CompositeResults rslts) {
        return OutOfSampleDiagnostics.create(config_, rslts);
    }

    @Override
    public Scope getScope() {
        return Scope.Modelling;
    }

    @Override
    public int getOrder() {
        return 300;
    }
}
