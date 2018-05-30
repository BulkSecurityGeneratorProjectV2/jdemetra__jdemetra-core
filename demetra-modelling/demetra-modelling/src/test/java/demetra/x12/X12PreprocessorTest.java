/*
 * Copyright 2017 National Bank of Belgium
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
package demetra.x12;

import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.regarima.regular.RegArimaModelling;
import demetra.sarima.RegSarimaProcessor;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import demetra.tramo.TramoProcessor;
import demetra.tramo.TramoSpec;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.x13.RegArimaSpecification;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class X12PreprocessorTest {
    
    private final double[] data, datamissing;
    
    public X12PreprocessorTest() {
        data=Data.PROD.clone();
        datamissing=Data.PROD.clone();
        datamissing[2]=Double.NaN;
        datamissing[100]=Double.NaN;
        datamissing[101]=Double.NaN;
        datamissing[102]=Double.NaN;
    }

    //@Test
    public void testProdMissing() {
        X12Preprocessor processor=X12Preprocessor.of(RegArimaSpec.RG5, null);
        TsPeriod start=TsPeriod.monthly(1967,1);
        TsData s=TsData.of(start, DoubleSequence.ofInternal(datamissing));
        processor.process(s, null);
    }
    
    //@Test
    public void testProdLegacyMissing() {
        IPreprocessor processor = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, datamissing, true);
        processor.process(s, null);
    }
    
    @Test
    public void testProd() {
        RegArimaSpec spec=new RegArimaSpec(RegArimaSpec.RG5);
        spec.getOutliers().setDefaultCriticalValue(3);
        X12Preprocessor processor=X12Preprocessor.of(spec, null);
        TsPeriod start=TsPeriod.monthly(1967,1);
        TsData s=TsData.of(start, DoubleSequence.ofInternal(data));
        RegArimaModelling context=new RegArimaModelling();
        processor.process(s, context);
        context.estimate(1e-9);
    }
    
    @Test
    public void testProdLegacy() {
        RegArimaSpecification spec = ec.tstoolkit.modelling.arima.x13.RegArimaSpecification.RG5.clone();spec.getOutliers().setDefaultCriticalValue(3);
        IPreprocessor processor = spec.build();
        ec.tstoolkit.timeseries.simplets.TsData s = new ec.tstoolkit.timeseries.simplets.TsData(ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly, 1967, 0, data, true);
        processor.process(s, null);
    }
}