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
package demetra.benchmarking.extractors;

import demetra.information.InformationExtractor;
import nbbrd.design.Development;
import demetra.information.InformationMapping;
import demetra.stats.StatisticalTest;
import demetra.tempdisagg.univariate.ResidualsDiagnostics;
import demetra.timeseries.TsData;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class ResidualsDiagnosticsExtractor extends InformationMapping<ResidualsDiagnostics> {

    public final String FRES = "fullresiduals", MEAN = "mean", SKEWNESS = "skewness",
            KURTOSIS = "kurtosis", DH = "doornikhansen", LJUNGBOX = "ljungbox",
            DW = "durbinwatson", UDRUNS_NUMBER = "nudruns", UDRUNS_LENGTH = "ludruns",
            RUNS_NUMBER = "nruns", RUNS_LENGTH = "lruns";

    public ResidualsDiagnosticsExtractor() {
        set(FRES, TsData.class, source -> source.getFullResiduals());
        set(MEAN, StatisticalTest.class, source -> source.getMean());
        set(SKEWNESS, StatisticalTest.class, source -> source.getSkewness());
        set(KURTOSIS, StatisticalTest.class, source -> source.getKurtosis());
        set(DH, StatisticalTest.class, source -> source.getDoornikHansen());
        set(LJUNGBOX, StatisticalTest.class, source -> source.getLjungBox());
        set(RUNS_NUMBER, StatisticalTest.class, source -> source.getRunsNumber());
        set(RUNS_LENGTH, StatisticalTest.class, source -> source.getRunsLength());
        set(UDRUNS_NUMBER, StatisticalTest.class, source -> source.getUdRunsNumber());
        set(UDRUNS_LENGTH, StatisticalTest.class, source -> source.getUdRunsLength());
        set(DW, Double.class, source -> source.getDurbinWatson());
    }

    @Override
    public Class getSourceClass() {
        return ResidualsDiagnostics.class;
    }

}
