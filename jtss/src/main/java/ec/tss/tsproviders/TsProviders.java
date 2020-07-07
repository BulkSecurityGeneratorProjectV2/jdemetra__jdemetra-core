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
package ec.tss.tsproviders;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import ec.tss.ITsProvider;
import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.TsInformationType;
import ec.tss.TsMoniker;
import ec.tstoolkit.utilities.Files2;
import ec.tstoolkit.utilities.Trees;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility class that simplify the use of Ts providers.
 *
 * @author Philippe Charles
 * @since 1.0.0
 */
public final class TsProviders {

    private TsProviders() {
        // static class
    }

    @NonNull
    static List<ITsProvider> asList() {
        final String[] providers = TsFactory.instance.getProviders();
        return new AbstractList<ITsProvider>() {
            @Override
            public ITsProvider get(int index) {
                return TsFactory.instance.getProvider(providers[index]);
            }

            @Override
            public int size() {
                return providers.length;
            }
        };
    }

    /**
     * Returns a list of all the Ts providers currently registered in TsFactory.
     *
     * @return
     */
    @NonNull
    public static FluentIterable<ITsProvider> all() {
        return FluentIterable.from(asList());
    }

    @NonNull
    public static <T extends ITsProvider> Optional<T> lookup(@NonNull Class<T> clazz, @NonNull String providerName) {
        ITsProvider result = TsFactory.instance.getProvider(providerName);
        return clazz.isInstance(result) ? Optional.of(clazz.cast(result)) : Optional.<T>absent();
    }

    @NonNull
    public static <T extends ITsProvider> Optional<T> lookup(@NonNull Class<T> clazz, @NonNull DataSource dataSource) {
        return lookup(clazz, dataSource.getProviderName());
    }

    @NonNull
    public static <T extends ITsProvider> Optional<T> lookup(@NonNull Class<T> clazz, @NonNull DataSet dataSet) {
        return lookup(clazz, dataSet.getDataSource());
    }

    @NonNull
    public static <T extends ITsProvider> Optional<T> lookup(@NonNull Class<T> clazz, @NonNull TsMoniker moniker) {
        String providerName = moniker.getSource();
        return providerName != null ? lookup(clazz, providerName) : Optional.<T>absent();
    }

    //<editor-fold defaultstate="collapsed" desc="Deprecated code">
    /**
     *
     * @param <T>
     * @param clazz
     * @return
     * @deprecated use {@link TsProviders#all()} instead
     */
    @Deprecated
    public static <T extends ITsProvider> List<T> findAll(Class<T> clazz) {
        return all().filter(clazz).toList();
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @param providerName
     * @return
     * @deprecated use
     * {@link TsProviders#lookup(java.lang.Class, java.lang.String)} instead
     */
    @Deprecated
    public static <T extends ITsProvider> T find(Class<T> clazz, String providerName) {
        return lookup(clazz, providerName).orNull();
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @param dataSource
     * @return
     * @deprecated use
     * {@link TsProviders#lookup(java.lang.Class, ec.tss.tsproviders.DataSource)}
     * instead
     */
    @Deprecated
    public static <T extends ITsProvider> T find(Class<T> clazz, DataSource dataSource) {
        return lookup(clazz, dataSource.getProviderName()).orNull();
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @param dataSet
     * @return
     * @deprecated use
     * {@link TsProviders#lookup(java.lang.Class, ec.tss.tsproviders.DataSet)}
     * instead
     */
    @Deprecated
    public static <T extends ITsProvider> T find(Class<T> clazz, DataSet dataSet) {
        return lookup(clazz, dataSet.getDataSource()).orNull();
    }

    /**
     *
     * @param <T>
     * @param clazz
     * @param moniker
     * @return
     * @deprecated use
     * {@link TsProviders#lookup(java.lang.Class, ec.tss.TsMoniker)} instead
     */
    @Deprecated
    public static <T extends ITsProvider> T find(Class<T> clazz, TsMoniker moniker) {
        String providerName = moniker.getSource();
        return providerName != null ? lookup(clazz, providerName).orNull() : null;
    }

    /**
     *
     * @param dataSource
     * @return
     * @deprecated use
     * {@link TsProviders#tryGetFile(ec.tss.tsproviders.DataSource) } instead
     */
    @Deprecated
    public static File getFile(DataSource dataSource) {
        return tryGetFile(dataSource).orNull();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Walk provider tree">
    @Deprecated
    public static <P extends IDataSourceProvider> P walkProviderTree(P provider, IProviderVisitor<? super P> visitor) throws Exception {
        for (DataSource o : provider.getDataSources()) {
            if (!walkProviderTree(provider, visitor, o)) {
                break;
            }
        }
        return provider;
    }

    private static <P extends IDataSourceProvider> boolean walkProviderTree(P provider, IProviderVisitor<? super P> visitor, DataSource dataSource) throws IOException {
        if (!visitor.preVisitSource(provider, dataSource)) {
            return false;
        }
        try {
            for (DataSet o : provider.children(dataSource)) {
                if (!walkProviderTree(provider, visitor, o, 0)) {
                    return false;
                }
            }
            return visitor.postVisitSource(provider, dataSource, null);
        } catch (IOException ex) {
            return visitor.postVisitSource(provider, dataSource, ex);
        }
    }

    private static <P extends IDataSourceProvider> boolean walkProviderTree(P provider, IProviderVisitor<? super P> visitor, DataSet dataSet, int level) throws IOException {
        switch (dataSet.getKind()) {
            case COLLECTION:
                visitor.preVisitCollection(provider, dataSet, level);
                try {
                    for (DataSet o : provider.children(dataSet)) {
                        if (!walkProviderTree(provider, visitor, o, level + 1)) {
                            return false;
                        }
                    }
                    return visitor.postVisitCollection(provider, dataSet, level,
                            null);
                } catch (IOException ex) {
                    return visitor.postVisitCollection(provider, dataSet, level, ex);
                }
            case DUMMY:
                return visitor.visitDummy(provider, dataSet, level);
            case SERIES:
                return visitor.visitSeries(provider, dataSet, level);
        }
        throw new UnsupportedOperationException();
    }
    //</editor-fold>

    @NonNull
    public static Optional<File> tryGetFile(@NonNull DataSource dataSource) {
        Optional<IFileLoader> loader = lookup(IFileLoader.class, dataSource.getProviderName());
        if (loader.isPresent()) {
            File file = loader.get().decodeBean(dataSource).getFile();
            File realFile = Files2.getAbsoluteFile(loader.get().getPaths(), file);
            return Optional.fromNullable(realFile);
        }
        return Optional.absent();
    }

    @NonNull
    public static Optional<TsCollection> getTsCollection(@NonNull DataSource dataSource, @NonNull TsInformationType type) {
        IDataSourceProvider provider = find(IDataSourceProvider.class, dataSource);
        if (provider == null) {
            return Optional.absent();
        }
        String name = provider.getDisplayName(dataSource);
        TsMoniker moniker = provider.toMoniker(dataSource);
        return Optional.of(TsFactory.instance.createTsCollection(name, moniker, type));
    }

    @NonNull
    public static Optional<TsCollection> getTsCollection(@NonNull DataSet dataSet, @NonNull TsInformationType type) {
        IDataSourceProvider provider = find(IDataSourceProvider.class, dataSet);
        if (provider == null) {
            return Optional.absent();
        }
        String name = provider.getDisplayName(dataSet);
        TsMoniker moniker = provider.toMoniker(dataSet);
        switch (dataSet.getKind()) {
            case COLLECTION:
                return Optional.of(TsFactory.instance.createTsCollection(name, moniker, type));
            case DUMMY:
                return Optional.of(TsFactory.instance.createTsCollection(name));
            case SERIES:
                TsCollection result = TsFactory.instance.createTsCollection();
                result.quietAdd(TsFactory.instance.createTs(name, moniker, type));
                return Optional.of(result);
        }
        throw new RuntimeException("Not implemented");
    }

    @NonNull
    public static Optional<Ts> getTs(@NonNull DataSet dataSet, @NonNull TsInformationType type) {
        IDataSourceProvider provider = find(IDataSourceProvider.class, dataSet);
        if (provider == null) {
            return Optional.absent();
        }
        String name = provider.getDisplayName(dataSet);
        TsMoniker moniker = provider.toMoniker(dataSet);
        switch (dataSet.getKind()) {
            case SERIES:
                Ts ts = TsFactory.instance.createTs(name, moniker, type);
                return Optional.of(ts);
        }
        throw new RuntimeException("Not implemented");
    }

    public static void prettyPrintTree(
            @NonNull IDataSourceProvider provider,
            @NonNull DataSource dataSource,
            @NonNegative int maxLevel,
            @NonNull PrintStream printStream,
            boolean displayName) throws IOException {

        Function<Object, String> toString = displayName
                ? o -> o instanceof DataSource ? provider.getDisplayName((DataSource) o) : " " + provider.getDisplayNodeName((DataSet) o)
                : o -> o instanceof DataSource ? provider.toMoniker((DataSource) o).getId() : " " + provider.toMoniker((DataSet) o).getId();

        Function<Object, Stream<? extends Object>> children = o -> {
            try {
                return o instanceof DataSource
                        ? provider.children((DataSource) o).stream()
                        : ((DataSet) o).getKind() == DataSet.Kind.COLLECTION ? provider.children((DataSet) o).stream() : Stream.empty();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };

        try {
            Trees.prettyPrint((Object) dataSource, children, maxLevel, toString, printStream);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }
}
