/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.ssf.basic;

import jdplus.ssf.multivariate.IMultivariateSsf;
import jdplus.ssf.univariate.ISsf;
import jdplus.ssf.ISsfDynamics;
import jdplus.ssf.ISsfInitialization;
import jdplus.ssf.multivariate.ISsfMeasurements;
import jdplus.ssf.multivariate.MultivariateSsf;

/**
 *
 * @author Jean Palate
 */
public class MultivariateTimeInvariantSsf extends MultivariateSsf{
    public static IMultivariateSsf of(IMultivariateSsf ssf){
        TimeInvariantDynamics td=TimeInvariantDynamics.of(ssf.getStateDim(), ssf.dynamics());
        if (td == null)
            return null;
        TimeInvariantMeasurements tm=TimeInvariantMeasurements.of(ssf.getStateDim(), ssf.measurements());
        return new MultivariateTimeInvariantSsf(ssf.initialization(), td, tm);
    }
    
    public static IMultivariateSsf of(ISsf ssf){
        TimeInvariantDynamics td=TimeInvariantDynamics.of(ssf.getStateDim(), ssf.dynamics());
        if (td == null)
            return null;
        TimeInvariantMeasurements tm=TimeInvariantMeasurements.of(
                ssf.getStateDim(), ssf.measurement());
        return new MultivariateTimeInvariantSsf(ssf.initialization(), td, tm);
    }

    private MultivariateTimeInvariantSsf(final ISsfInitialization init, final ISsfDynamics dynamics, ISsfMeasurements measurement) {
        super(init, dynamics, measurement);
    }
}
