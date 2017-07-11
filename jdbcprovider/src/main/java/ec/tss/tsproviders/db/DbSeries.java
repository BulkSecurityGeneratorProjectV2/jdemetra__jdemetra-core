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
package ec.tss.tsproviders.db;

import com.google.common.collect.Streams;
import ec.tss.tsproviders.utils.OptionalTsData;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 *
 * @author Philippe Charles
 */
//@Immutable
public final class DbSeries {

    private final DbSetId id;
    private final OptionalTsData data;

    public DbSeries(@Nonnull DbSetId id, @Nonnull OptionalTsData data) {
        this.id = id;
        this.data = data;
    }

    @Nonnull
    public DbSetId getId() {
        return id;
    }

    @Nonnull
    public OptionalTsData getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof DbSeries && equals((DbSeries) obj));
    }

    private boolean equals(DbSeries that) {
        return this.id.equals(that.id) && this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Nonnull
    public static DbSeries findById(@Nonnull Iterable<DbSeries> iterable, @Nonnull DbSetId id) throws NoSuchElementException {
        return Streams.stream(iterable)
                .filter(o -> (o != null) ? id.equals(o.getId()) : false)
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    @Nonnull
    public static List<DbSeries> filterByAncestor(@Nonnull Iterable<DbSeries> iterable, @Nonnull DbSetId ancestor) {
        return Streams.stream(iterable)
                .filter(o -> (o != null) ? isDescendant(ancestor, o.getId()) : false)
                .collect(Collectors.toList());
    }

    private static boolean isDescendant(@Nonnull DbSetId ancestor, @Nonnull DbSetId o) {
        for (int i = ancestor.getLevel() - 1; i >= 0; i--) {
            if (!o.getValue(i).equals(ancestor.getValue(i))) {
                return false;
            }
        }
        return true;
    }
}
