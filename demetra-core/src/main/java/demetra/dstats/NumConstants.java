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
package demetra.dstats;

import demetra.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
final class NumConstants {
    /**
         *
         */
    public static final double SQRPI = 1.0 / Math.sqrt(Math.PI * 2);

    /**
         *
         */
    public static final double ROOT32 = Math.sqrt(32.0);
    /**
         *
         */
    public static final double THRSH = 0.66291;
    /**
         *
         */
    public static final double EPS = Double.MIN_VALUE * 0.5;
    /**
         *
         */
    public static final double MIN = Double.NEGATIVE_INFINITY;

    private NumConstants() {
    }
}