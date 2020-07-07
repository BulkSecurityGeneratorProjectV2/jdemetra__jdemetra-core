/*
 * Copyright 2013 National Bank of Belgium
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
package ec.tss.tsproviders.jdbc.jndi;

import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.db.DbAccessor;
import ec.tss.tsproviders.jdbc.ConnectionSupplier;
import ec.tss.tsproviders.jdbc.JdbcAccessor;
import ec.tss.tsproviders.jdbc.JdbcBean;
import ec.tss.tsproviders.jdbc.JdbcProvider;
import java.sql.Connection;
import java.sql.SQLException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import javax.naming.Context;
import nbbrd.service.ServiceProvider;
import nbbrd.sql.jdbc.SqlConnectionSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic Jdbc provider that uses Jndi as a connection supplier.
 * <p>
 * Note that you can supply you own connection supplier by using
 * {@link #setConnectionSupplier(ec.tss.tsproviders.jdbc.ConnectionSupplier)}
 * method. It is useful when running under JavaSE since Jndi is not available by
 * default in this environment.
 *
 * @author Philippe Charles
 * @see http://docs.oracle.com/javase/tutorial/jdbc/basics/sqldatasources.html
 * @see javax.sql.DataSource
 * @see Context#lookup(java.lang.String)
 */
@ServiceProvider(ITsProvider.class)
public class JndiJdbcProvider extends JdbcProvider<JdbcBean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JndiJdbcProvider.class);
    public static final String SOURCE = "JNDI-JDBC";
    private static final String VERSION = "20131203";
    // PROPERTIES
    private ConnectionSupplier connectionSupplier;

    public JndiJdbcProvider() {
        super(SOURCE, VERSION, LOGGER, TsAsyncMode.Once);
        this.connectionSupplier = JndiJdbcSupplier.INSTANCE;
    }

    @Override
    protected DbAccessor<JdbcBean> loadFromBean(JdbcBean bean) throws Exception {
        return new JdbcAccessor(logger, bean, connectionSupplier).memoize();
    }

    @Override
    public JdbcBean newBean() {
        return new JdbcBean();
    }

    @Override
    public JdbcBean decodeBean(DataSource dataSource) throws IllegalArgumentException {
        return new JdbcBean(support.check(dataSource));
    }

    @Override
    public String getDisplayName() {
        return "JDBC resource";
    }

    @NonNull
    public ConnectionSupplier getConnectionSupplier() {
        return connectionSupplier;
    }

    public void setConnectionSupplier(@Nullable ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier != null ? connectionSupplier : JndiJdbcSupplier.INSTANCE;
    }

    private enum JndiJdbcSupplier implements ConnectionSupplier {

        INSTANCE;
        
        private final SqlConnectionSupplier delegate = SqlConnectionSupplier.ofJndi();
        
        @Override
        public Connection getConnection(JdbcBean bean) throws SQLException {
            return delegate.getConnection(bean.getDbName());
        }
    }
}
