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
package ec.tss.sa.output;

import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Kristof Bayens
 */
public class SeriesSummary {

    public final String Name;
    private final Map<String, TsData> series_ = new LinkedHashMap<>();

    public SeriesSummary(String[] items, String name, SaDocument<?> document) {
        Name = name;
        fillDictionary(items, document.getResults());
    }

    private void fillDictionary(String[] items, IProcResults results) {
        for (String item : items) {
            item = item.toLowerCase();
            if (results != null) {
                if (InformationSet.hasWildCards(item)) {
                    Map<String, TsData> all = results.searchAll(item, TsData.class);
                    all.keySet().forEach(s->series_.put(s, results.getData(s, TsData.class)));
                } else {
                    series_.put(item, results.getData(item, TsData.class));
                }
            } else {
                series_.put(item, null);
            }

        }
    }

    public TsData getSeries(String name) {
        if (series_.containsKey(name)) {
            return series_.get(name);
        } else {
            return null;
        }
    }

    void fill(Set<String> set) {
        set.addAll(series_.keySet());
    }
}
