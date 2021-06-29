/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.tsprovider.cube;

import nbbrd.design.ThreadSafe;
import demetra.timeseries.TsInformationType;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.HasDataDisplayName;
import demetra.tsprovider.HasDataHierarchy;
import demetra.tsprovider.util.IConfig;
import demetra.tsprovider.util.Param;
import demetra.tsprovider.stream.DataSetTs;
import demetra.tsprovider.util.DataSourcePreconditions;
import internal.util.Strings;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import lombok.AllArgsConstructor;
import demetra.tsprovider.stream.HasTsStream;
import nbbrd.io.function.IOFunction;

/**
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@ThreadSafe
@lombok.AllArgsConstructor(staticName = "of")
public final class CubeSupport implements HasDataHierarchy, HasTsStream, HasDataDisplayName {

    @ThreadSafe
    public interface Resource {

        @NonNull
        CubeAccessor getAccessor(@NonNull DataSource dataSource) throws IOException;

        @NonNull
        Param<DataSet, CubeId> getIdParam(@NonNull CubeId root) throws IOException;
    }

    @lombok.NonNull
    private final String providerName;

    @lombok.NonNull
    private final Resource resource;

    //<editor-fold defaultstate="collapsed" desc="HasDataHierarchy">
    @Override
    public List<DataSet> children(DataSource dataSource) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        CubeAccessor acc = resource.getAccessor(dataSource);
        CubeId parentId = acc.getRoot();

        // special case: we return a fake dataset if no dimColumns
        if (parentId.isVoid()) {
            IOException ex = acc.testConnection();
            if (ex != null) {
                throw ex;
            }
            DataSet fake = DataSet.of(dataSource, DataSet.Kind.SERIES);
            return Collections.singletonList(fake);
        }

        try (Stream<CubeId> children = acc.getChildren(parentId)) {
            DataSet.Builder builder = DataSet.builder(dataSource, DataSet.Kind.COLLECTION);
            return children.map(toDataSetFunc(builder, resource.getIdParam(parentId))).collect(Collectors.toList());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    @Override
    public List<DataSet> children(DataSet parent) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, parent);

        if (!DataSet.Kind.COLLECTION.equals(parent.getKind())) {
            throw new IllegalArgumentException("Not a collection");
        }

        CubeAccessor acc = resource.getAccessor(parent.getDataSource());
        Param<DataSet, CubeId> idParam = resource.getIdParam(acc.getRoot());

        CubeId parentId = idParam.get(parent);

        try (Stream<CubeId> children = acc.getChildren(parentId)) {
            DataSet.Builder builder = parent.toBuilder(parentId.getDepth() > 1 ? DataSet.Kind.COLLECTION : DataSet.Kind.SERIES);
            return children.map(toDataSetFunc(builder, idParam)).collect(Collectors.toList());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasTsStream">
    @Override
    public Stream<DataSetTs> getData(DataSource dataSource, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        CubeAccessor acc = resource.getAccessor(dataSource);
        CubeId parentId = acc.getRoot();

        Function<CubeId, DataSet> toDataSet = toDataSetFunc(DataSet.builder(dataSource, DataSet.Kind.SERIES), resource.getIdParam(parentId));

        return type.encompass(TsInformationType.Data)
                ? acc.getAllSeriesWithData(parentId).map(getSeriesWithDataFunc(toDataSet, acc::getDisplayName))
                : acc.getAllSeries(parentId).map(getSeriesFunc(toDataSet, acc::getDisplayName));
    }

    @Override
    public Stream<DataSetTs> getData(DataSet dataSet, TsInformationType type) throws IOException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        CubeAccessor acc = resource.getAccessor(dataSet.getDataSource());
        Param<DataSet, CubeId> idParam = resource.getIdParam(acc.getRoot());

        CubeId id = idParam.get(dataSet);

        Function<CubeId, DataSet> toDataSet = toDataSetFunc(dataSet.toBuilder(DataSet.Kind.SERIES), idParam);

        boolean collection = DataSet.Kind.COLLECTION.equals(dataSet.getKind());
        return type.encompass(TsInformationType.Data)
                ? (collection ? acc.getAllSeriesWithData(id) : ofNullable(acc.getSeriesWithData(id))).map(getSeriesWithDataFunc(toDataSet, acc::getDisplayName))
                : (collection ? acc.getAllSeries(id) : ofNullable(acc.getSeries(id))).map(getSeriesFunc(toDataSet, acc::getDisplayName));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="HasDataDisplayName">
    @Override
    public String getDisplayName(DataSource dataSource) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSource);

        try {
            return resource.getAccessor(dataSource).getDisplayName();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String getDisplayName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        try {
            CubeAccessor acc = resource.getAccessor(dataSet.getDataSource());
            CubeId id = resource.getIdParam(acc.getRoot()).get(dataSet);
            return acc.getDisplayName(id);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    @Override
    public String getDisplayNodeName(DataSet dataSet) throws IllegalArgumentException {
        DataSourcePreconditions.checkProvider(providerName, dataSet);

        try {
            CubeAccessor acc = resource.getAccessor(dataSet.getDataSource());
            CubeId id = resource.getIdParam(acc.getRoot()).get(dataSet);
            return acc.getDisplayNodeName(id);
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
    //</editor-fold>

    @NonNull
    public static Param<DataSet, CubeId> idByName(@NonNull CubeId root) {
        return new ByNameParam(Objects.requireNonNull(root));
    }

    @NonNull
    public static Param<DataSet, CubeId> idBySeparator(@NonNull CubeId root, @NonNull String separator, @NonNull String name) {
        return new BySeparatorParam(Objects.requireNonNull(separator), Objects.requireNonNull(root), Objects.requireNonNull(name));
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static <E> Stream<E> ofNullable(E e) {
        return e == null ? Stream.empty() : Stream.of(e);
    }

    private static Function<CubeId, DataSet> toDataSetFunc(DataSet.Builder builder, Param<DataSet, CubeId> dimValuesParam) {
        return o -> builder.put(dimValuesParam, o).build();
    }

    private static String getNonNullLabel(String label, CubeId id, IOFunction<CubeId, String> toLabel) {
        if (label != null) {
            return label;
        }
        try {
            return toLabel.applyWithIO(id);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static Function<CubeSeriesWithData, DataSetTs> getSeriesWithDataFunc(Function<CubeId, DataSet> toDataSet, IOFunction<CubeId, String> toLabel) {
        return ts -> new DataSetTs(toDataSet.apply(ts.getId()), getNonNullLabel(ts.getLabel(), ts.getId(), toLabel), ts.getMeta(), ts.getData());
    }

    private static Function<CubeSeries, DataSetTs> getSeriesFunc(Function<CubeId, DataSet> toDataSet, IOFunction<CubeId, String> toLabel) {
        return ts -> new DataSetTs(toDataSet.apply(ts.getId()), getNonNullLabel(ts.getLabel(), ts.getId(), toLabel), ts.getMeta(), DataSetTs.DATA_NOT_REQUESTED);
    }

    @AllArgsConstructor
    private static final class ByNameParam implements Param<DataSet, CubeId> {

        private final CubeId root;

        @Override
        public CubeId defaultValue() {
            return root;
        }

        @Override
        public CubeId get(DataSet config) {
            String[] dimValues = new String[root.getMaxLevel()];
            int i = 0;
            while (i < dimValues.length) {
                String id = root.getDimensionId(i);
                if ((dimValues[i] = config.get(id)) == null) {
                    break;
                }
                i++;
            }
            return root.child(dimValues, i);
        }

        @Override
        public void set(IConfig.Builder<?, DataSet> builder, CubeId value) {
            for (int i = 0; i < value.getLevel(); i++) {
                builder.put(value.getDimensionId(i), value.getDimensionValue(i));
            }
        }
    }

    @lombok.AllArgsConstructor
    private static final class BySeparatorParam implements Param<DataSet, CubeId> {

        private final String separator;
        private final CubeId root;
        private final String name;

        @Override
        public CubeId defaultValue() {
            return root;
        }

        @Override
        public CubeId get(DataSet config) {
            String value = config.get(name);
            if (value == null || value.isEmpty()) {
                return defaultValue();
            }

            String[] dimValues = new String[root.getMaxLevel()];
            int i = 0;
            Iterator<String> iterator = Strings.splitToIterator(separator, value);
            while (i < dimValues.length && iterator.hasNext()) {
                dimValues[i] = iterator.next();
                i++;
            }
            return root.child(dimValues, i);
        }

        @Override
        public void set(IConfig.Builder<?, DataSet> builder, CubeId value) {
            if (value.getLevel() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(value.getDimensionValue(0));
                for (int i = 1; i < value.getLevel(); i++) {
                    sb.append(separator).append(value.getDimensionValue(i));
                }
                builder.put(name, sb.toString());
            }
        }
    }
    //</editor-fold>
}
