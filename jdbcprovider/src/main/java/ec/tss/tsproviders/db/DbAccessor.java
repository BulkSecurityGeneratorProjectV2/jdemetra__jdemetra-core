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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import ec.tss.tsproviders.utils.IConstraint;
import ec.tss.tsproviders.utils.Parsers;
import ec.tss.tsproviders.utils.StrangeParsers;
import ec.tstoolkit.utilities.GuavaCaches;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Demortier Jeremy
 * @author Philippe Charles
 */
public abstract class DbAccessor<BEAN extends DbBean> {

    @NonNull
    protected DbSetId check(@NonNull DbSetId ref, @NonNull IConstraint<DbSetId> constraint) throws IllegalArgumentException {
        String msg = constraint.check(ref);
        Preconditions.checkArgument(msg == null, msg);
        return ref;
    }

    @NonNull
    abstract public BEAN getDbBean();

    @NonNull
    abstract public DbSetId getRoot();

    @Nullable
    public Exception testDbBean() {
        BEAN dbBean = getDbBean();
        if (Strings.isNullOrEmpty(dbBean.getDbName())) {
            return new Exception("Missing db name");
        }
        if (Strings.isNullOrEmpty(dbBean.getTableName())) {
            return new Exception("Missing table name");
        }
        if (Strings.isNullOrEmpty(dbBean.getPeriodColumn())) {
            return new Exception("Missing period column");
        }
        if (Strings.isNullOrEmpty(dbBean.getValueColumn())) {
            return new Exception("Missing value column");
        }
        return null;
    }

    @NonNull
    public final List<DbSetId> getAllSeries(String... dimValues) throws Exception {
        return getAllSeries(check(getRoot().child(dimValues), DbSetId.COLLECTION_CONSTRAINT));
    }

    @NonNull
    abstract protected List<DbSetId> getAllSeries(@NonNull DbSetId ref) throws Exception;

    @NonNull
    public final List<DbSeries> getAllSeriesWithData(String... dimValues) throws Exception {
        return getAllSeriesWithData(check(getRoot().child(dimValues), DbSetId.COLLECTION_CONSTRAINT));
    }

    @NonNull
    abstract protected List<DbSeries> getAllSeriesWithData(@NonNull DbSetId ref) throws Exception;

    @NonNull
    public final DbSeries getSeriesWithData(String... dimValues) throws Exception {
        return getSeriesWithData(check(getRoot().child(dimValues), DbSetId.SERIES_CONSTRAINT));
    }

    @NonNull
    abstract protected DbSeries getSeriesWithData(@NonNull DbSetId ref) throws Exception;

    @NonNull
    public final List<String> getChildren(String... dimValues) throws Exception {
        return getChildren(check(getRoot().child(dimValues), DbSetId.COLLECTION_CONSTRAINT));
    }

    @NonNull
    abstract protected List<String> getChildren(@NonNull DbSetId ref) throws Exception;

    @NonNull
    abstract public DbAccessor<BEAN> memoize();

    public static abstract class Abstract<BEAN extends DbBean> extends DbAccessor<BEAN> {

        protected final BEAN dbBean;
        protected final Parsers.Parser<Date> dateParser;
        protected final Parsers.Parser<Number> numberParser;
        protected final DbSetId root;

        public Abstract(@NonNull BEAN dbBean) {
            this.dbBean = dbBean;
            this.dateParser = dbBean.getDataFormat().dateParser().or(StrangeParsers.yearFreqPosParser());
            this.numberParser = dbBean.getDataFormat().numberParser();
            this.root = DbSetId.root(dbBean.getDimArray());
        }

        @Override
        public BEAN getDbBean() {
            return dbBean;
        }

        @Override
        public DbSetId getRoot() {
            return root;
        }
    }

    /**
     * An implementation of DbAccessor that uses the command pattern.
     */
    public static abstract class Commander<BEAN extends DbBean> extends Abstract<BEAN> {

        public Commander(@NonNull BEAN dbBean) {
            super(dbBean);
        }

        @Override
        protected List<DbSetId> getAllSeries(DbSetId ref) throws Exception {
            return getAllSeriesQuery(ref).call();
        }

        /**
         * Returns a callable that creates all series.
         *
         * @param ref a non-null object that identifies a collection.
         * @return
         */
        @NonNull
        abstract protected Callable<List<DbSetId>> getAllSeriesQuery(@NonNull DbSetId ref);

        @Override
        protected List<DbSeries> getAllSeriesWithData(DbSetId ref) throws Exception {
            return getAllSeriesWithDataQuery(ref).call();
        }

        /**
         * Returns a callable that creates all series with their data.
         *
         * @param ref a non-null object that identifies a collection.
         * @return
         */
        @NonNull
        abstract protected Callable<List<DbSeries>> getAllSeriesWithDataQuery(@NonNull DbSetId ref);

        @Override
        protected DbSeries getSeriesWithData(DbSetId ref) throws Exception {
            return getSeriesWithDataQuery(ref).call();
        }

        /**
         * Returns a callable that creates a series with its data.
         *
         * @param ref a non-null object that identifies a series.
         * @return
         */
        @NonNull
        abstract protected Callable<DbSeries> getSeriesWithDataQuery(@NonNull DbSetId ref);

        @Override
        protected List<String> getChildren(DbSetId ref) throws Exception {
            return getChildrenQuery(ref).call();
        }

        /**
         * Returns a callable that creates a list of children.
         *
         * @param ref a non-null object that identifies a collection.
         * @return
         */
        @NonNull
        abstract protected Callable<List<String>> getChildrenQuery(@NonNull DbSetId ref);
    }

    public static abstract class Forwarding<BEAN extends DbBean> extends DbAccessor<BEAN> {

        abstract protected DbAccessor<BEAN> getDelegate();

        @Override
        public BEAN getDbBean() {
            return getDelegate().getDbBean();
        }

        @Override
        public DbSetId getRoot() {
            return getDelegate().getRoot();
        }

        @Override
        public Exception testDbBean() {
            return getDelegate().testDbBean();
        }

        @Override
        protected List<DbSetId> getAllSeries(DbSetId ref) throws Exception {
            return getDelegate().getAllSeries(ref);
        }

        @Override
        protected List<DbSeries> getAllSeriesWithData(DbSetId ref) throws Exception {
            return getDelegate().getAllSeriesWithData(ref);
        }

        @Override
        protected DbSeries getSeriesWithData(DbSetId ref) throws Exception {
            return getDelegate().getSeriesWithData(ref);
        }

        @Override
        protected List<String> getChildren(DbSetId ref) throws Exception {
            return getDelegate().getChildren(ref);
        }
    }

    public static abstract class BulkAccessor<BEAN extends DbBean> extends Forwarding<BEAN> {

        @NonNull
        public static <X extends DbBean> BulkAccessor<X> from(@NonNull final DbAccessor<X> delegate, int depth, @NonNull Cache<DbSetId, List<DbSeries>> cache) {
            return new BulkAccessor(depth, cache) {
                @Override
                protected DbAccessor<X> getDelegate() {
                    return delegate;
                }

                @Override
                public DbAccessor memoize() {
                    return this;
                }
            };
        }

        @Deprecated
        @NonNull
        public static Cache<DbSetId, List<DbSeries>> newTtlCache(long ttlInMillis) {
            return GuavaCaches.ttlCache(Duration.ofMillis(ttlInMillis));
        }

        protected final Cache<DbSetId, List<DbSeries>> cache;
        protected final int cacheLevel;
        protected final int depth;

        public BulkAccessor(int depth, @NonNull Cache<DbSetId, List<DbSeries>> cache) {
            this.cacheLevel = Math.max(0, getRoot().getMaxLevel() - depth);
            this.cache = cache;
            this.depth = depth;
        }

        protected boolean isCacheEnabled() {
            return depth > 0;
        }

        @NonNull
        protected Optional<DbSetId> getAncestorForCache(@NonNull DbSetId ref) {
            if (cacheLevel < ref.getLevel()) {
                String[] tmp = new String[cacheLevel];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = ref.getValue(i);
                }
                return Optional.of(getRoot().child(tmp));
            }
            return Optional.absent();
        }

        @Override
        protected List<DbSeries> getAllSeriesWithData(DbSetId ref) throws Exception {
            if (isCacheEnabled() /* CONSTRAINT -> */ && !ref.isSeries()) {
                if (ref.getLevel() == cacheLevel) {
                    List<DbSeries> value = cache.getIfPresent(ref);
                    if (value == null) {
                        value = getDelegate().getAllSeriesWithData(ref);
                        cache.put(ref, value);
                    }
                    return value;
                } else {
                    Optional<DbSetId> ancestor = getAncestorForCache(ref);
                    if (ancestor.isPresent()) {
                        return DbSeries.filterByAncestor(getAllSeriesWithData(ancestor.get()), ref);
                    }
                }
            }
            return getDelegate().getAllSeriesWithData(ref);
        }

        @Override
        protected DbSeries getSeriesWithData(DbSetId ref) throws Exception {
            if (isCacheEnabled() /* CONSTRAINT -> */ && ref.isSeries()) {
                Optional<DbSetId> ancestor = getAncestorForCache(ref);
                if (ancestor.isPresent()) {
                    return DbSeries.findById(getAllSeriesWithData(ancestor.get()), ref);
                }
            }
            return getDelegate().getSeriesWithData(ref);
        }
    }
}
