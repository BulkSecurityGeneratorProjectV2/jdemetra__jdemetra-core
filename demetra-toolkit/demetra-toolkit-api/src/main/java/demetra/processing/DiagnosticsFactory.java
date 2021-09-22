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

package demetra.processing;

import java.util.List;

/**
 *
 * @author Jean Palate
 * @param <C> Configuration
 * @param <R> Original result
 */
public interface DiagnosticsFactory <C, R>{
    
    String getName();

    boolean isActive();
    
    C getConfiguration();

    /**
     * Get the list of the tests
     * @return A non empty list of tests.
     */
    List<String> getTestDictionary();
    
    Diagnostics of(R results);
    
    DiagnosticsFactory<C, R> with(boolean active, C newConfig);
    
}
