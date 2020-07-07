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
package ec.tss.tsproviders.spreadsheet.engine;

import com.google.common.collect.ImmutableList;
import ec.tss.tsproviders.spreadsheet.facade.Sheet;
import ec.tstoolkit.timeseries.TsAggregationType;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Jean Palate
 */
public final class SpreadSheetCollection implements Comparable<SpreadSheetCollection> {

    @Deprecated
    public static SpreadSheetCollection load(Sheet sheet, int ordering, CellParser<String> toName, CellParser<Date> toDate, CellParser<Number> toNumber, TsFrequency frequency, TsAggregationType aggregationType, boolean clean) {
        return Engine.parseCollection(sheet, ordering, toName, toDate, toNumber, frequency, aggregationType, clean);
    }

    public enum AlignType {

        VERTICAL, HORIZONTAL, UNKNOWN
    }

    public final String sheetName; // unique id; don't use ordering
    public final int ordering; // this may change !
    public final AlignType alignType;
    public final ImmutableList<SpreadSheetSeries> series;

    private final Map<String, Integer> map;

    public SpreadSheetCollection(@NonNull String sheetName, int ordering, @NonNull AlignType alignType, @NonNull ImmutableList<SpreadSheetSeries> series) {
        this(sheetName, ordering, alignType, series, new HashMap<>());
    }

    private SpreadSheetCollection(String sheetName, int ordering, AlignType alignType, ImmutableList<SpreadSheetSeries> series, Map<String, Integer> map) {
        this.sheetName = sheetName;
        this.ordering = ordering;
        this.alignType = alignType;
        this.series = series;
        this.map = map;
    }

    @Override
    public int compareTo(SpreadSheetCollection o) {
        int result = Integer.compare(ordering, o.ordering);
        if (result != 0) {
            return result;
        }
        return sheetName.compareTo(o.sheetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordering, sheetName);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SpreadSheetCollection && equals((SpreadSheetCollection) obj));
    }

    private boolean equals(SpreadSheetCollection that) {
        return this.ordering == that.ordering && this.sheetName.equals(that.sheetName);
    }

    // build old names map...
    @Deprecated
    public SpreadSheetSeries searchOldName(String name) {
        int pos = 0;
        for (SpreadSheetSeries item : series) {
            String iname = item.seriesName;
            iname = iname.replace('.', '#');
            if (iname.length() > 64) {
                iname = iname.substring(0, 64);
            }
            int c = 1;
            while (map.containsKey(iname)) {
                String nid = Integer.toString(c++);
                iname = iname.substring(0, iname.length() - nid.length());
                iname += nid;
            }
            map.put(iname, pos++);
        }

        Integer ipos = map.get(name);
        if (ipos == null) {
            return null;
        } else {
            return series.get(ipos);
        }
    }

    @Deprecated
    public static AlignType getAlignType(Sheet sheet, CellParser<String> toName, CellParser<Date> toDate) {
        return Engine.parseAlignType(sheet, toName, toDate);
    }
}
