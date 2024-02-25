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
import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.daanse.common.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.common.jdbc.db.api.MetaInfo;
import org.eclipse.daanse.common.jdbc.db.api.MetaInfo.DatabaseInfo;
import org.eclipse.daanse.common.jdbc.db.api.MetaInfo.IdentifierInfo;
import org.eclipse.daanse.common.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.common.jdbc.db.record.DatabaseInfoR;
import org.eclipse.daanse.common.jdbc.db.record.IdentifierInfoR;
import org.eclipse.daanse.common.jdbc.db.record.MetaInfoR;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = DatabaseService.class)
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

    private MetaInfo readMetaInfo(DatabaseMetaData databaseMetaData) {
        DatabaseInfo databaseInfo = readDatabaseInfo(databaseMetaData);
        IdentifierInfo identifierInfo = readIdentifierInfo(databaseMetaData);
        return new MetaInfoR(databaseInfo, identifierInfo);
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

}
