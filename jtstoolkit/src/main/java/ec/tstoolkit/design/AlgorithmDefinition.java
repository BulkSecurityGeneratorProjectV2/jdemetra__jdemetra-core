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


package ec.tstoolkit.design;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * That annotation indicates a specific (statistical) algorithm.
 * It is equivalent to a ServiceDefinition, but in a more precise domain
 * @author Jean Palate
 */
@Target( { ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface AlgorithmDefinition {
    /**
     * Specifies if this algorithm has an optional position used to register this
     * algorithm relative to others. Lower-numbered algoeithm are returned in the
     * lookup result first. Algorithms with no specified position are returned
     * last.
     * @return 
     */
    boolean hasPosition() default false;
}
