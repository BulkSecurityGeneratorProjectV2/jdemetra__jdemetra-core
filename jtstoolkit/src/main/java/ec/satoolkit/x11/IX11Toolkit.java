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
package ec.satoolkit.x11;

import ec.tstoolkit.design.Development;

/**
 *
 * @author Frank Osaer, Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IX11Toolkit {

    X11Context getContext();

    /**
     *
     * @return
     */
    IExtremeValuesCorrector getExtremeValuesCorrector();

    /**
     *
     * @return
     */
    IX11Preprocessor getPreprocessor();

    /**
     *
     * @return
     */
    ISeasonalComputer getSeasonalComputer();

    /**
     *
     * @return
     */
    ISeasonalNormalizer getSeasonalNormalizer();

    /**
     *
     * @return
     */
    ITrendCycleComputer getTrendCycleComputer();

    /**
     *
     * @return
     */
    IX11Utilities getUtilities();

    /**
     *
     * @return
     */
    boolean isExcludefcst();

    default BiasCorrection getBiasCorrection() {
        return BiasCorrection.Legacy;
    }

}
