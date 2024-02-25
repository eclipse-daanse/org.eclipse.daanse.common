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
package org.eclipse.daanse.common.jdbc.db.api;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Service that helps to communicate with the Database.
 *
 */
public interface DatabaseService {

    /**
     * Opens a Connection and close this directly
     *
     * @param dataSource to be used to get the {@link DatabaseMetaData} from an the
     *                   {@link Connection} and may snapshot them.
     * @return MetaInfo copy of the essential values of the
     *         {@link DatabaseMetaData}.
     * @throws SQLException
     */
    MetaInfo createMetaInfo(DataSource dataSource) throws SQLException;

    /**
     * Used the Connection and dies not close is.
     *
     * @param connection to be used to get the {@link DatabaseMetaData} from an the
     *                   {@link Connection} and may snapshot them.
     * @return MetaInfo copy of the essential values of the
     *         {@link DatabaseMetaData}.
     * @throws SQLException
     */
    MetaInfo createMetaInfo(Connection connection) throws SQLException;

    /**
     * Creates a new {@link SqlStatementGenerator} that respects the
     * {@link MetaInfo} of the {@link Connection} e.g for Quoting.
     *
     * @param metaInfo {@link MetaInfo}
     * @return SqlStatementGenerator
     */
    SqlStatementGenerator createSqlStatementGenerator(MetaInfo metaInfo);

}
