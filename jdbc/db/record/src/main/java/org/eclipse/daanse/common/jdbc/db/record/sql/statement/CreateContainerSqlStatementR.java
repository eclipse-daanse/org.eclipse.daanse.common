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
package org.eclipse.daanse.common.jdbc.db.record.sql.statement;

import java.util.List;

import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnDefinition;
import org.eclipse.daanse.common.jdbc.db.api.sql.TableReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.CreateSqlStatement;

public record CreateContainerSqlStatementR(TableReference table, List<ColumnDefinition> columnDefinitions,
        boolean ifNotExists) implements CreateSqlStatement {

}
