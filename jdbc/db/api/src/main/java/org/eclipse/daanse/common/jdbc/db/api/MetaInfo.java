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

import java.sql.DatabaseMetaData;

/**
 * MetaInfo holds all essential values of the {@link DatabaseMetaData} in an
 * easy accessible way.
 */
public interface MetaInfo {

    /**
     * Subset of the {@link MetaInfo} holds all Database related values of the
     * {@link DatabaseMetaData}
     */
    DatabaseInfo databaseInfo();

    /**
     * Subset of the {@link MetaInfo} holds all values of the
     * {@link DatabaseMetaData} that are relevant for quoting.
     */
    IdentifierInfo identifierInfo();

    interface DatabaseInfo {

        /**
         * Retrieves the major version number of the underlying database.
         *
         * @return the underlying database's major version
         */
        int databaseMajorVersion();

        /**
         * Retrieves the minor version number of the underlying database.
         *
         * @return underlying database's minor version
         */
        int databaseMinorVersion();

        /**
         * Retrieves the name of this database product.
         *
         * @return database product name
         */
        String databaseProductName();

        /**
         * Retrieves the version number of this database product.
         *
         * @return database version number
         */
        String databaseProductVersion();

    }

    interface IdentifierInfo {

        /**
         * Retrieves the string used to quote SQL identifiers. This method returns a
         * space " " if identifier quoting is not supported.
         *
         * @return the quoting string or a space if quoting is not supported
         */
        String quoteString();

    }

    /**
     * Retrieves the database vendor's preferred term for "catalog".
     *
     * @return the vendor term for "catalog"
     */

}
