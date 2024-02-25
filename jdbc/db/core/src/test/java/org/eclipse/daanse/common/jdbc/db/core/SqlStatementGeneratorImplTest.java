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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.eclipse.daanse.common.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.common.jdbc.db.record.DatabaseInfoR;
import org.eclipse.daanse.common.jdbc.db.record.IdentifierInfoR;
import org.eclipse.daanse.common.jdbc.db.record.MetaInfoR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ColumnDataTypeR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ColumnDefinitionR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ColumnReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.SchemaReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.TableReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ViewReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.CreateContainerSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.CreateSchemaSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.DropContainerSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.DropSchemaSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.InsertSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.TruncateTableSqlStatementR;
import org.junit.jupiter.api.Test;

class SqlStatementGeneratorImplTest {

    private SqlStatementGenerator generator = new SqlStatementGeneratorImpl(
            new MetaInfoR(new DatabaseInfoR("", "", 0, 0), new IdentifierInfoR("#")));

    @Test
    void dropTableNoSchemaNoExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName"), false));
        assertThat(sql).isEqualTo("DROP TABLE #theTableName#");
    }

    @Test
    void dropTableWithSchemaNoExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"), false));
        assertThat(sql).isEqualTo("DROP TABLE #theSchemaName#.#theTableName#");
    }

    @Test
    void dropTableNoSchemaWithExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName"), true));
        assertThat(sql).isEqualTo("DROP TABLE IF EXISTS #theTableName#");
    }

    @Test
    void dropTableWithSchemaWithExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"), true));
        assertThat(sql).isEqualTo("DROP TABLE IF EXISTS #theSchemaName#.#theTableName#");
    }

    @Test
    void dropViewNoSchemaNoExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new ViewReferenceR(Optional.empty(), "theTableName"), false));
        assertThat(sql).isEqualTo("DROP VIEW #theTableName#");
    }

    @Test
    void dropViewWithSchemaNoExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new ViewReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"), false));
        assertThat(sql).isEqualTo("DROP VIEW #theSchemaName#.#theTableName#");
    }

    @Test
    void dropViewNoSchemaWithExist() {
        String sql = generator.getSqlOfStatement(
                new DropContainerSqlStatementR(new ViewReferenceR(Optional.empty(), "theTableName"), true));
        assertThat(sql).isEqualTo("DROP VIEW IF EXISTS #theTableName#");
    }

    @Test
    void dropViewWithSchemaWithExist() {
        String sql = generator.getSqlOfStatement(new DropContainerSqlStatementR(
                new ViewReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"), true));
        assertThat(sql).isEqualTo("DROP VIEW IF EXISTS #theSchemaName#.#theTableName#");
    }

    @Test
    void dropSchemaWithExist() {
        String sql = generator
                .getSqlOfStatement(new DropSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), true));
        assertThat(sql).isEqualTo("DROP SCHEMA IF EXISTS #theSchemaName#");
    }

    @Test
    void dropSchemaNoExist() {
        String sql = generator
                .getSqlOfStatement(new DropSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), false));
        assertThat(sql).isEqualTo("DROP SCHEMA #theSchemaName#");
    }

    @Test
    void createSchemaWithExist() {
        String sql = generator
                .getSqlOfStatement(new CreateSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), true));
        assertThat(sql).isEqualTo("CREATE SCHEMA IF NOT EXISTS #theSchemaName#");
    }

    @Test
    void createSchemaNoExist() {
        String sql = generator
                .getSqlOfStatement(new CreateSchemaSqlStatementR(new SchemaReferenceR("theSchemaName"), false));
        assertThat(sql).isEqualTo("CREATE SCHEMA #theSchemaName#");
    }

    @Test
    void truncateTableNoSchema() {
        String sql = generator.getSqlOfStatement(
                new TruncateTableSqlStatementR(new TableReferenceR(Optional.empty(), "theTableName")));
        assertThat(sql).isEqualTo("TRUNCATE TABLE #theTableName#");
    }

    @Test
    void truncateTableWithSchema() {
        String sql = generator.getSqlOfStatement(new TruncateTableSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName")));
        assertThat(sql).isEqualTo("TRUNCATE TABLE #theSchemaName#.#theTableName#");
    }

    @Test
    void createTableWithSchema() {
        String sql = generator.getSqlOfStatement(new CreateContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"),
                List.of(new ColumnDefinitionR(new ColumnReferenceR("Col1"),
                        new ColumnDataTypeR("int", Optional.empty()))),
                true));
        assertThat(sql).isEqualTo("CREATE TABLE IF NOT EXISTS #theSchemaName#.#theTableName#( #Col1# int)");

    }

    @Test
    void createTableWithColumDetails() {
        String sql = generator.getSqlOfStatement(new CreateContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"),
                List.of(new ColumnDefinitionR(new ColumnReferenceR("Col1"),
                        new ColumnDataTypeR("varchar", Optional.of("200")))),
                true));
        assertThat(sql).isEqualTo("CREATE TABLE IF NOT EXISTS #theSchemaName#.#theTableName#( #Col1# varchar(200))");

    }

    @Test
    void createTableWithMiltiColumn() {
        String sql = generator.getSqlOfStatement(new CreateContainerSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"),
                List.of(new ColumnDefinitionR(new ColumnReferenceR("Col1"),
                        new ColumnDataTypeR("int", Optional.empty())),
                        new ColumnDefinitionR(new ColumnReferenceR("Col2"),
                                new ColumnDataTypeR("int", Optional.empty()))),
                true));
        assertThat(sql).isEqualTo("CREATE TABLE IF NOT EXISTS #theSchemaName#.#theTableName#( #Col1# int, #Col2# int)");
    }

    @Test
    void insertTableWithMiltiColumn() {
        String sql = generator.getSqlOfStatement(new InsertSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"),
                List.of(new ColumnReferenceR("Col1"), new ColumnReferenceR("Col2")), List.of("?", "?")));
        assertThat(sql).isEqualTo("INSERT INTO #theSchemaName#.#theTableName#(#Col1#, #Col2#) VALUES (?, ?)");

    }

    @Test
    void insertTableWithOneColumn() {
        String sql = generator.getSqlOfStatement(new InsertSqlStatementR(
                new TableReferenceR(Optional.of(new SchemaReferenceR("theSchemaName")), "theTableName"),
                List.of(new ColumnReferenceR("Col1")), List.of("?")));
        assertThat(sql).isEqualTo("INSERT INTO #theSchemaName#.#theTableName#(#Col1#) VALUES (?)");

    }

}
