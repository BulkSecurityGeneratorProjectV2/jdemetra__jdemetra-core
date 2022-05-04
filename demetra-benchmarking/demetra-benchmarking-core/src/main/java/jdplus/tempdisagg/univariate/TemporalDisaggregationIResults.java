/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.tempdisagg.univariate;

import demetra.information.GenericExplorable;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.math.functions.ObjectiveFunctionPoint;
import demetra.timeseries.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
public class TemporalDisaggregationIResults implements GenericExplorable {

    private @lombok.NonNull
    TsData disaggregatedSeries;
    private double a,b;
    private ObjectiveFunctionPoint maximum;
    private LikelihoodStatistics likelihood;
}
