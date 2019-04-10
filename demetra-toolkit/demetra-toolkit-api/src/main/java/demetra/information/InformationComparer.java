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

package demetra.information;

import demetra.design.Development;

/**
 *
 * @param <S>
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class InformationComparer<S> implements java.util.Comparator<Information<S>>{

    @Override
    public int compare(Information<S> o1, Information<S> o2) {
        return Long.compare(o1.getIndex(), o2.getIndex());
    }
}
