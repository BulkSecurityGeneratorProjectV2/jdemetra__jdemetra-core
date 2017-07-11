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
package ec.tss.tsproviders.odbc;

import adodb.wsh.AdoDriver;
import com.google.common.base.StandardSystemProperty;
import ec.tss.ITsProvider;
import ec.tss.TsAsyncMode;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IFileLoader;
import ec.tss.tsproviders.db.DbAccessor;
import ec.tss.tsproviders.jdbc.ConnectionSupplier.DriverBasedSupplier;
import ec.tss.tsproviders.jdbc.JdbcAccessor;
import ec.tss.tsproviders.jdbc.JdbcBean;
import ec.tss.tsproviders.jdbc.JdbcProvider;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.design.VisibleForTesting;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Demortier Jeremy
 */
@ServiceProvider(service = ITsProvider.class)
public class OdbcProvider extends JdbcProvider<OdbcBean> implements IFileLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(OdbcProvider.class);
    public static final String SOURCE = "ODBCPRVDR";
    static final String VERSION = "20111201";
    // PROPERTIES
    protected final DriverBasedSupplier connectionSupplier;

    public OdbcProvider() {
        this(new OdbcSupplier());
    }

    @VisibleForTesting
    OdbcProvider(DriverBasedSupplier connectionSupplier) {
        super(SOURCE, VERSION, LOGGER, TsAsyncMode.Once);
        this.connectionSupplier = connectionSupplier;
    }

    @Override
    public boolean isAvailable() {
        return connectionSupplier.isDriverAvailable();
    }

    @Override
    public String getDisplayName() {
        return "ODBC DSNs";
    }

    @Override
    public String getDisplayName(DataSource dataSource) {
        OdbcBean bean = decodeBean(dataSource);
        String dsn = bean.isDsnLess() ? bean.getFile().getPath() : bean.getDbName();
        String options = TsFrequency.Undefined == bean.getFrequency() ? "" : OptionalTsData.Builder.toString(bean.getFrequency(), bean.getAggregationType());
        return String.format("%s ~ %s \u00BB %s %s", dsn, bean.getTableName(), bean.getValueColumn(), options);
    }

    @Override
    protected DbAccessor<OdbcBean> loadFromBean(OdbcBean bean) throws Exception {
        return new JdbcAccessor(logger, bean, connectionSupplier).memoize();
    }

    @Override
    public OdbcBean newBean() {
        return new OdbcBean();
    }

    @Override
    public OdbcBean decodeBean(DataSource dataSource) {
        return new OdbcBean(support.check(dataSource));
    }

    @Override
    public String getFileDescription() {
        return "Access file";
    }

    @Override
    public boolean accept(File pathname) {
        return OdbcBean.FILE_FILTERS.stream().anyMatch(o -> (o.accept(pathname)));
    }

    @Override
    public File[] getPaths() {
        return support.getPaths();
    }

    @Override
    public void setPaths(File[] paths) {
        support.setPaths(paths);
        clearCache();
    }

    public DriverBasedSupplier getConnectionSupplier() {
        return connectionSupplier;
    }

    private static final class OdbcSupplier extends DriverBasedSupplier {

        private AdoDriver fallback;

        @Override
        public Connection getConnection(JdbcBean bean) throws SQLException {
            if (fallback != null) {
                return fallback.connect(AdoDriver.PREFIX + bean.getDbName(), new Properties());
            }
            return super.getConnection(bean);
        }

        @Override
        protected String getUrl(JdbcBean bean) {
            return "jdbc:odbc:" + bean.getDbName();
        }

        @Override
        protected boolean loadDriver() {
            // 64bit version of ODBC driver is bugged
            // https://bugs.openjdk.java.net/browse/JDK-8038751
            if (!is64bit() && isClassAvailable("sun.jdbc.odbc.JdbcOdbcDriver")) {
                LOGGER.info("Using Sun's odbc driver");
                return true;
            }
            fallback = new AdoDriver();
            LOGGER.info("Using ADO driver");
            return true;
        }

        private static boolean isClassAvailable(String className) {
            try {
                Class.forName(className);
                return true;
            } catch (ClassNotFoundException ex) {
                return false;
            }
        }

        private static boolean is64bit() {
            return "amd64".equals(StandardSystemProperty.OS_ARCH.value());
        }
    }
}
