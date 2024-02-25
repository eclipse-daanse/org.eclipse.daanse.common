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

import org.eclipse.daanse.common.jdbc.db.api.MetaInfo;
import org.eclipse.daanse.common.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnDataType;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnDefinition;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.ContainerReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.Named;
import org.eclipse.daanse.common.jdbc.db.api.sql.TableReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.ViewReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.CreateSchemaSqlStatement;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.CreateSqlStatement;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.DropContainerSqlStatement;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.DropSchemaSqlStatement;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.InsertSqlStatement;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.SqlStatement;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.TruncateTableSqlStatement;

public class SqlStatementGeneratorImpl implements SqlStatementGenerator {
    public static final String NO_QUOTE_FROM_METADATA = " ";

    private final MetaInfo metaInfo;
    private final String quoteString;

    public SqlStatementGeneratorImpl(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
        quoteString = metaInfo.identifierInfo().quoteString();
    }

    @Override
    public String getSqlOfStatement(SqlStatement statement) {

        StringBuilder sb = switch (statement) {
        case DropContainerSqlStatement dc -> writeDropContainerSqlStatement(dc);
        case DropSchemaSqlStatement ds -> writeDropSchemaSqlStatement(ds);
        case CreateSchemaSqlStatement cs -> writeCreateSchemaSqlStatement(cs);
        case TruncateTableSqlStatement ts -> writeTruncateTableSqlStatement(ts);
        case CreateSqlStatement cc -> writeCreateSqlStatement(cc);
        case InsertSqlStatement is -> writeInsertSqlStatement(is);
        };
        return sb.toString();
    }

    private StringBuilder writeInsertSqlStatement(InsertSqlStatement statement) {
        TableReference table = statement.table();

        StringBuilder sb = new StringBuilder(20);
        sb.append("INSERT INTO ");

        quoteContainerReference(sb, table);

        sb.append("(");

        boolean firstColumn = true;
        for (ColumnReference column : statement.columns()) {

            if (firstColumn) {
                firstColumn = false;
            } else {
                sb.append(", ");

            }

            quoteReference(sb, column);
        }

        sb.append(") VALUES (");

        boolean firstValue = true;
        for (String value : statement.values()) {

            if (firstValue) {
                firstValue = false;
            } else {
                sb.append(", ");
            }
            sb.append(value);
        }

        sb.append(")");

        return sb;
    }

    private StringBuilder writeCreateSqlStatement(CreateSqlStatement statement) {
        ContainerReference container = statement.container();

        StringBuilder sb = new StringBuilder(20);
        sb.append("CREATE ");

        String contrainerTypeWord = switch (container) {
        case TableReference unnamed_ -> "TABLE ";
        case ViewReference unnamed_ -> "VIEW ";
        };

        sb.append(contrainerTypeWord);

        if (statement.ifNotExists()) {
            sb.append("IF NOT EXISTS ");
        }

        quoteContainerReference(sb, container);

        sb.append("( ");

        boolean first = true;
        for (ColumnDefinition columnDefinition : statement.columnDefinitions()) {

            if (first) {
                first = false;
            } else {
                sb.append(", ");

            }

            quoteReference(sb, columnDefinition.column());
            sb.append(" ");

            ColumnDataType dataType = columnDefinition.columnType();
            sb.append(dataType.name());

            dataType.detail().ifPresent(detail -> {

                sb.append("(");
                sb.append(detail);
                sb.append(")");

            });
        }

        sb.append(")");

        return sb;
    }

    private StringBuilder writeTruncateTableSqlStatement(TruncateTableSqlStatement statement) {

        StringBuilder sb = new StringBuilder(30);
        sb.append("TRUNCATE TABLE ");
        quoteContainerReference(sb, statement.table());
        return sb;
    }

    private StringBuilder writeCreateSchemaSqlStatement(CreateSchemaSqlStatement statement) {

        StringBuilder sb = new StringBuilder(30);
        sb.append("CREATE SCHEMA ");

        if (statement.ifNotExists()) {
            sb.append("IF NOT EXISTS ");
        }
        quoteReference(sb, statement.schema());
        return sb;

    }

    private StringBuilder writeDropSchemaSqlStatement(DropSchemaSqlStatement statement) {

        StringBuilder sb = new StringBuilder(30);
        sb.append("DROP SCHEMA ");

        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }
        quoteReference(sb, statement.schema());
        return sb;

    }

    private StringBuilder writeDropContainerSqlStatement(DropContainerSqlStatement statement) {
        ContainerReference container = statement.container();

        StringBuilder sb = new StringBuilder(20);
        sb.append("DROP ");

        String contrainerTypeWord = switch (container) {
        case TableReference unnamed_ -> "TABLE ";
        case ViewReference unnamed_ -> "VIEW ";
        };

        sb.append(contrainerTypeWord);

        if (statement.ifExists()) {
            sb.append("IF EXISTS ");
        }

        quoteContainerReference(sb, container);

        return sb;
    }

    private void quoteContainerReference(final StringBuilder sb, final ContainerReference containerReference) {

        containerReference.schema().ifPresent(schema -> {
            quoteReference(sb, schema);
            sb.append(".");
        });
        quoteReference(sb, containerReference);
    }

    private void quoteReference(final StringBuilder sb, final Named reference) {
        quoteIdentifier(sb, reference.name());
    }

    private void quoteIdentifier(final StringBuilder sb, final String identifiert) {

        if ((quoteString.equals(NO_QUOTE_FROM_METADATA))
                || (identifiert.startsWith(quoteString) && identifiert.endsWith(quoteString))) {
            // no quote or already quoted
            sb.append(identifiert);
            return;
        }

        String cleanedIdentifier = identifiert.replace(quoteString, quoteString + quoteString);
        sb.append(quoteString);
        sb.append(cleanedIdentifier);
        sb.append(quoteString);
    }

}
