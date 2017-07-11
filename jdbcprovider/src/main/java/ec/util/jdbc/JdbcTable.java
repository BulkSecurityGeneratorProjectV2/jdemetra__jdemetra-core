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
package ec.util.jdbc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import ec.tstoolkit.design.Immutable;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Philippe Charles
 */
@Immutable
public final class JdbcTable implements Comparable<JdbcTable> {

    /**
     * Creates a complete list of tables in a database.
     *
     * @param md
     * @return a non-null list of tables
     * @throws SQLException
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Nonnull
    public static List<JdbcTable> allOf(@Nonnull DatabaseMetaData md) throws SQLException {
        return allOf(md, null, null, "%", null);
    }

    /**
     * Creates a partial list of tables in a database by using patterns.
     *
     * @param md
     * @param catalog
     * @param schemaPattern
     * @param tableNamePattern
     * @param types
     * @return a non-null list of tables
     * @throws SQLException
     * @see DatabaseMetaData#getTables(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    @Nonnull
    public static List<JdbcTable> allOf(@Nonnull DatabaseMetaData md,
            @Nullable String catalog, @Nullable String schemaPattern,
            @Nonnull String tableNamePattern, @Nullable String[] types) throws SQLException {
        try (ResultSet rs = md.getTables(catalog, schemaPattern, tableNamePattern, types)) {
            List<JdbcTable> result = new ArrayList<>();
            // some infos are not supported by all drivers!
            String[] normalizedColumnNames = getNormalizedColumnNames(rs.getMetaData());
            Map<String, String> row = new HashMap<>();
            while (rs.next()) {
                for (int i = 0; i < normalizedColumnNames.length; i++) {
                    row.put(normalizedColumnNames[i], rs.getString(i + 1));
                }
                result.add(fromMap(row));
            }
            return result;
        }
    }

    private static String[] getNormalizedColumnNames(ResultSetMetaData md) throws SQLException {
        String[] columnNames = new String[md.getColumnCount()];
        for (int i = 0; i < columnNames.length; i++) {
            // normalize to upper case (postgresql driver returns lower case)
            columnNames[i] = md.getColumnName(i + 1).toUpperCase(Locale.ROOT);
        }
        return columnNames;
    }

    @Nonnull
    private static JdbcTable fromMap(@Nonnull Map<String, String> map) {
        return new JdbcTable(
                get(map, "TABLE_CAT", "TABLE_CATALOG"),
                get(map, "TABLE_SCHEM", "TABLE_SCHEMA"),
                get(map, "TABLE_NAME"),
                get(map, "TABLE_TYPE"),
                get(map, "REMARKS"),
                get(map, "TYPE_CAT"),
                get(map, "TYPE_SCHEM"),
                get(map, "TYPE_NAME"),
                get(map, "SELF_REFERENCING_COL_NAME"),
                get(map, "REF_GENERATION"));
    }

    @Nullable
    private static String get(@Nonnull Map<String, String> map, String... keys) {
        for (String key : keys) {
            String result = map.get(key);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    
    private final String catalog;
    private final String schema;
    private final String name;
    private final String type;
    private final String remarks;
    private final String typesCatalog;
    private final String typesSchema;
    private final String typeName;
    private final String selfReferencingColumnName;
    private final String refGeneration;

    public JdbcTable(String catalog, String schema, String name, String type, String remarks, String typesCatalog, String typesSchema, String typeName, String selfReferencingColumnName, String refGeneration) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = Strings.nullToEmpty(name);
        this.type = Strings.nullToEmpty(type);
        this.remarks = remarks;
        this.typesCatalog = typesCatalog;
        this.typesSchema = typesSchema;
        this.typeName = typeName;
        this.selfReferencingColumnName = selfReferencingColumnName;
        this.refGeneration = refGeneration;
    }

    /**
     * table catalog (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getCatalog() {
        return catalog;
    }

    /**
     * table schema (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getSchema() {
        return schema;
    }

    /**
     * table name
     *
     * @return
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL
     * TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     *
     * @return
     */
    @Nonnull
    public String getType() {
        return type;
    }

    /**
     * explanatory comment on the table
     *
     * @return
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * the types catalog (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getTypesCatalog() {
        return typesCatalog;
    }

    /**
     * the types schema (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getTypesSchema() {
        return typesSchema;
    }

    /**
     * type name (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getTypeName() {
        return typeName;
    }

    /**
     * String => name of the designated "identifier" column of a typed table
     * (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getSelfReferencingColumnName() {
        return selfReferencingColumnName;
    }

    /**
     * specifies how values in SELF_REFERENCING_COL_NAME are created. Values are
     * "SYSTEM", "USER", "DERIVED". (may be
     * <code>null</code>)
     *
     * @return
     */
    @Nullable
    public String getRefGeneration() {
        return refGeneration;
    }
    // we need an ordering that can handle null values
    static final Ordering<String> ORDERING = Ordering.natural().nullsLast();

    @Override
    public int compareTo(JdbcTable that) {
        return ComparisonChain.start()
                .compare(this.type, that.type, ORDERING)
                .compare(this.catalog, that.catalog, ORDERING)
                .compare(this.schema, that.schema, ORDERING)
                .compare(this.name, that.name, ORDERING)
                .result();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JdbcTable ? equals((JdbcTable) obj) : false;
    }

    private boolean equals(JdbcTable that) {
        return Objects.equals(this.catalog, that.catalog)
                && Objects.equals(this.schema, that.schema)
                && Objects.equals(this.name, that.name)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.remarks, that.remarks)
                && Objects.equals(this.typesCatalog, that.typesCatalog)
                && Objects.equals(this.typesSchema, that.typesSchema)
                && Objects.equals(this.typeName, that.typeName)
                && Objects.equals(this.selfReferencingColumnName, that.selfReferencingColumnName)
                && Objects.equals(this.refGeneration, that.refGeneration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, name, type, remarks, typesCatalog, typesSchema, typeName, selfReferencingColumnName, refGeneration);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("catalog", catalog)
                .add("schema", schema)
                .add("name", name)
                .add("type", type)
                .add("remarks", remarks)
                .add("typesCatalog", typesCatalog)
                .add("typesSchema", typesSchema)
                .add("typeName", typeName)
                .add("selfReferencingColumnName", selfReferencingColumnName)
                .add("refGeneration", refGeneration)
                .toString();
    }
}