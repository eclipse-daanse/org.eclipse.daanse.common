/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.common.jdbc.db.core;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.daanse.common.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.common.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.common.jdbc.db.api.meta.DatabaseInfo;
import org.eclipse.daanse.common.jdbc.db.api.meta.IdentifierInfo;
import org.eclipse.daanse.common.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.common.jdbc.db.api.meta.TypeInfo;
import org.eclipse.daanse.common.jdbc.db.api.meta.TypeInfo.Nullable;
import org.eclipse.daanse.common.jdbc.db.api.meta.TypeInfo.Searchable;
import org.eclipse.daanse.common.jdbc.db.api.sql.CatalogReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.SchemaReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.TableReference;
import org.eclipse.daanse.common.jdbc.db.record.meta.DatabaseInfoR;
import org.eclipse.daanse.common.jdbc.db.record.meta.IdentifierInfoR;
import org.eclipse.daanse.common.jdbc.db.record.meta.MetaInfoR;
import org.eclipse.daanse.common.jdbc.db.record.meta.TypeInfoR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.CatalogReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.SchemaReferenceR;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DatabaseService.class, scope = ServiceScope.SINGLETON)
public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseServiceImpl.class);

    @Override
    public MetaInfo createMetaInfo(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return createMetaInfo(connection);
        }
    }

    @Override
    public MetaInfo createMetaInfo(Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        return readMetaInfo(databaseMetaData);
    }

    private MetaInfo readMetaInfo(DatabaseMetaData databaseMetaData) throws SQLException {
        DatabaseInfo databaseInfo = readDatabaseInfo(databaseMetaData);
        IdentifierInfo identifierInfo = readIdentifierInfo(databaseMetaData);
        List<TypeInfo> typeInfos = readTypeInfo(databaseMetaData);
        List<CatalogReference> catalogs = readCatalogs(databaseMetaData);
        return new MetaInfoR(databaseInfo, identifierInfo, typeInfos, catalogs);
    }

    private List<CatalogReference> readCatalogs(DatabaseMetaData databaseMetaData) throws SQLException {

        List<CatalogReference> catalogs = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getCatalogs()) {
            while (rs.next()) {
                final String catalogName = rs.getString("TABLE_CAT");
                catalogs.add(new CatalogReferenceR(catalogName));
            }
        }
        return List.copyOf(catalogs);
    }

    private List<SchemaReference> readSchemas(DatabaseMetaData databaseMetaData) throws SQLException {

        List<SchemaReference> schemas = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getSchemas()) {
            while (rs.next()) {
                final String schemaName = rs.getString("TABLE_SCHEM");
                final Optional<CatalogReference> catalog = Optional.ofNullable(rs.getString("TABLE_CATALOG"))
                        .map(c -> new CatalogReferenceR(c));

                schemas.add(new SchemaReferenceR(catalog, schemaName));
            }
        }
        return List.copyOf(schemas);
    }



    private List<TypeInfo> readTypeInfo(DatabaseMetaData databaseMetaData) throws SQLException {

        List<TypeInfo> typeInfos = new ArrayList<>();
        try (ResultSet rs = databaseMetaData.getTypeInfo()) {
            while (rs.next()) {
                final String typeName = rs.getString("TYPE_NAME");
                final int dataType = rs.getInt("DATA_TYPE");
                final int percision = rs.getInt("PRECISION");
                final Optional<String> literatPrefix = Optional.ofNullable(rs.getString("LITERAL_PREFIX"));
                final Optional<String> literatSuffix = Optional.ofNullable(rs.getString("LITERAL_SUFFIX"));
                final Optional<String> createPragmas = Optional.ofNullable(rs.getString("CREATE_PARAMS"));
                final Nullable nullable = TypeInfo.Nullable.of(rs.getShort("NULLABLE"));
                final boolean caseSensitive = rs.getBoolean("CASE_SENSITIVE");
                final Searchable searchable = TypeInfo.Searchable.of(rs.getShort("SEARCHABLE"));
                final boolean unsignesAttribute = rs.getBoolean("UNSIGNED_ATTRIBUTE");
                final boolean fixedPrecScale = rs.getBoolean("FIXED_PREC_SCALE");
                final boolean autoIncrement = rs.getBoolean("AUTO_INCREMENT");
                final Optional<String> localTypeName = Optional.ofNullable(rs.getString("LOCAL_TYPE_NAME"));
                final short minimumScale = rs.getShort("MINIMUM_SCALE");
                final short maximumScale = rs.getShort("MAXIMUM_SCALE");
                final int numPrecRadix = rs.getInt("NUM_PREC_RADIX");

                TypeInfoR typeInfo = new TypeInfoR(typeName, dataType, percision, literatPrefix, literatSuffix,
                        createPragmas, nullable, caseSensitive, searchable, unsignesAttribute, fixedPrecScale,
                        autoIncrement, localTypeName, minimumScale, maximumScale, numPrecRadix);
                typeInfos.add(typeInfo);
            }
        }

        return List.copyOf(typeInfos);
    }

    private static DatabaseInfo readDatabaseInfo(DatabaseMetaData databaseMetaData) {

        String productName = "";
        try {
            productName = databaseMetaData.getDatabaseProductName();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading productName", e);
        }

        String productVersion = "";
        try {
            productVersion = databaseMetaData.getDatabaseProductVersion();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading productVersion", e);
        }

        int majorVersion = 0;
        try {
            majorVersion = databaseMetaData.getDatabaseMajorVersion();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading majorVersion", e);
        }

        int minorVersion = 0;
        try {
            minorVersion = databaseMetaData.getDatabaseMinorVersion();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading minorVersion", e);
        }

        return new DatabaseInfoR(productName, productVersion, majorVersion, minorVersion);
    }

    private static IdentifierInfo readIdentifierInfo(DatabaseMetaData databaseMetaData) {

        String quoteString = " ";
        try {
            quoteString = databaseMetaData.getIdentifierQuoteString();
        } catch (SQLException e) {
            LOGGER.error("Exception while reading quoteString", e);
        }
        return new IdentifierInfoR(quoteString);
    }

    @Override
    public SqlStatementGenerator createSqlStatementGenerator(MetaInfo metaInfo) {
        return new SqlStatementGeneratorImpl(metaInfo);
    }

    @Override
    public Optional<Integer> getColumnDataType(Connection connection, TableReference table, ColumnReference column)
            throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        String catalogName = connection.getCatalog();
        String schemaName = table.schema().map(SchemaReference::name).orElse(null);
        try (ResultSet rs = databaseMetaData.getColumns(catalogName, schemaName, table.name(), column.name());) {
            while (rs.next()) {
                return Optional.ofNullable(rs.getInt("DATA_TYPE"));
            }
        }
        return Optional.empty();
    }

}
