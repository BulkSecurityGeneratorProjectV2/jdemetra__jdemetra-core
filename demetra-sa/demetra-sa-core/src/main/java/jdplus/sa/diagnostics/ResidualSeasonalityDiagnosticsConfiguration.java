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
package jdplus.sa.diagnostics;

import demetra.processing.DiagnosticsConfiguration;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author PALATEJ
 */
@lombok.Value
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public class ResidualSeasonalityDiagnosticsConfiguration implements DiagnosticsConfiguration {

    private static final AtomicReference<ResidualSeasonalityDiagnosticsConfiguration> DEFAULT
            = new AtomicReference<>(builder().build());

    public static void setDefault(ResidualSeasonalityDiagnosticsConfiguration config) {
        DEFAULT.set(config);
    }

    public static ResidualSeasonalityDiagnosticsConfiguration getDefault() {
        return DEFAULT.get();
    }

    public static final boolean ACTIVE = true;
    private boolean active;

    public static final double SASEV = 0.01, SABAD = 0.05, SAUNC = 0.1, ISEV = 0.01, IBAD = 0.05, IUNC = 0.1, SA3SEV = 0.01, SA3BAD = 0.05, SA3UNC = 0.1;

    private double severeThresholdForSa;
    private double badThresholdForSa;
    private double uncertainThresholdForSa;
    private double severeThresholdForIrregular;
    private double badThresholdForIrregular;
    private double uncertainThresholdForIrregular;
    private double severeThresholdForLastSa;
    private double badThresholdForLastSa;
    private double uncertainThresholdForLastSa;

    public static Builder builder() {
        return new Builder()
                .active(ACTIVE)
                .severeThresholdForSa(SASEV)
                .badThresholdForSa(SABAD)
                .uncertainThresholdForSa(SAUNC)
                .severeThresholdForIrregular(ISEV)
                .badThresholdForIrregular(IBAD)
                .uncertainThresholdForIrregular(IUNC)
                .severeThresholdForLastSa(SA3SEV)
                .badThresholdForLastSa(SA3BAD)
                .uncertainThresholdForLastSa(SA3UNC);
    }

    @Override
    public DiagnosticsConfiguration activate(boolean active) {
        if (this.active == active) {
            return this;
        } else {
            return toBuilder().active(active).build();
        }
    }

}
