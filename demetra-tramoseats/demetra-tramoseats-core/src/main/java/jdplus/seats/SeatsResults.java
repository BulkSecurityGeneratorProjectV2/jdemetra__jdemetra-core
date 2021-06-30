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
package jdplus.seats;

import demetra.sa.SeriesDecomposition;
import jdplus.sarima.SarimaModel;
import jdplus.ucarima.UcarimaModel;
import demetra.information.Explorable;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class SeatsResults implements Explorable {

    private SarimaModel originalModel;
    private SarimaModel finalModel;
    private double innovationVariance;
    private boolean meanCorrection;
    private boolean parametersCutOff, modelChanged;
    private UcarimaModel ucarimaModel, compactUcarimaModel;
    private SeriesDecomposition initialComponents, finalComponents;
}
