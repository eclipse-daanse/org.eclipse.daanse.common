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
 *   SmartCity Jena, Stefan Bischof - initial
 *
 */
package org.eclipse.daanse.common.jdbc.loader.csv.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent.Kind;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.daanse.common.io.fs.watcher.api.EventKind;
import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.common.io.fs.watcher.api.propertytypes.FileSystemWatcherListenerProperties;
import org.eclipse.daanse.common.jdbc.db.api.DatabaseService;
import org.eclipse.daanse.common.jdbc.db.api.SqlStatementGenerator;
import org.eclipse.daanse.common.jdbc.db.api.meta.MetaInfo;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnDataType;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnDefinition;
import org.eclipse.daanse.common.jdbc.db.api.sql.ColumnReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.SchemaReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.TableReference;
import org.eclipse.daanse.common.jdbc.db.api.sql.statement.InsertSqlStatement;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ColumnDataTypeR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ColumnDefinitionR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.ColumnReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.SchemaReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.element.TableReferenceR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.CreateContainerSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.CreateSchemaSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.DropContainerSqlStatementR;
import org.eclipse.daanse.common.jdbc.db.record.sql.statement.InsertSqlStatementR;
import org.eclipse.daanse.common.jdbc.loader.csv.api.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.siegmar.fastcsv.reader.CloseableIterator;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

@Designate(ocd = CsvDataLoaderConfig.class, factory = true)
@FileSystemWatcherListenerProperties(kinds = EventKind.ENTRY_MODIFY, pattern = ".*.csv", recursive = true)
@Component(scope = ServiceScope.SINGLETON, service = FileSystemWatcherListener.class, name = Constants.PID_LOADER_FILEWATCHER)
public class CsvDataLoader implements FileSystemWatcherListener {

    private static final String EXCEPTION_WHILE_WRITING_DATA = "Exception while writing Data";

    private static final String EXCEPTION_WHILE_CREATING_SCHEMA = "Exception while creating schema";

    private static final String EXCEPTION_DATABASE_CONNECTION_ERROR = "Database connection error";

    private static final String EXCEPTION_WHILE_SETTING_VALUE_TO_PREPARED_STATEMENT = "Exception while setting value to PreparedStatement";

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvDataLoader.class);

    @Reference
    private DataSource dataSource;
    @Reference
    DatabaseService databaseService;

    private CsvDataLoaderConfig config;
    private SqlStatementGenerator sqlStatementGenerator;

    private Path basePath;

    @Activate
    public void activate(CsvDataLoaderConfig config) throws SQLException {
        this.config = config;
        MetaInfo metaInfo = databaseService.createMetaInfo(dataSource);
        sqlStatementGenerator = databaseService.createSqlStatementGenerator(metaInfo);
    }

    @Deactivate
    public void deactivate() {
        config = null;
        sqlStatementGenerator = null;
    }

    private void checkPathAndLoadCsv(Path path) {

        if (Files.isDirectory(path)) {
            return;
        }
        if (!path.toString().endsWith(".csv")) {
            return;
        }

        try (Connection connection = dataSource.getConnection()) {
            loadTable(connection, path);
        } catch (SQLException e) {
            throw new CsvDataLoaderException(EXCEPTION_DATABASE_CONNECTION_ERROR, e);
        }

    }

    private void loadTable(Connection connection, Path path) throws SQLException {
        String fileName = getFileNameWithoutExtension(path.getFileName().toString());
        LOGGER.debug("Load table {}", fileName);
        Optional<SchemaReference> schema = getSchemaFromPath(path);

        schema.ifPresent(s -> {

            if (s.name().isBlank()) {
                return;
            }

            String statementCreateSchema = sqlStatementGenerator
                    .getSqlOfStatement(new CreateSchemaSqlStatementR(s, true));

            try {
                connection.createStatement().execute(statementCreateSchema);
            } catch (SQLException e) {
                throw new CsvDataLoaderException(EXCEPTION_WHILE_CREATING_SCHEMA, e);
            }
        });

        TableReference table = new TableReferenceR(schema, fileName, "TABLE");
        dropTable(connection, table);

        if (!path.toFile().exists()) {

            LOGGER.warn("File does not exist - {} {}", fileName, path);
            return;
        }

        CsvReader.CsvReaderBuilder builder = CsvReader.builder().fieldSeparator(config.fieldSeparator())
                .quoteCharacter(config.quoteCharacter()).skipEmptyLines(config.skipEmptyLines())
                .commentCharacter(config.commentCharacter())
                .ignoreDifferentFieldCount(config.ignoreDifferentFieldCount());

        try (CloseableIterator<NamedCsvRecord> it = builder.ofNamedCsvRecord(path).iterator()) {
            if (!it.hasNext()) {
                throw new IllegalStateException("No header found");
            }
            NamedCsvRecord types = it.next();
            List<ColumnDefinition> headersTypeList = getHeadersTypeList(types);
            if (it.hasNext()) {
                createTable(connection, headersTypeList, table);
                insertTable(connection, it, headersTypeList, table);
            }

        } catch (IOException e) {
            throw new CsvDataLoaderException("Exception while Loading csv", e);
        }
    }

    private void insertTable(Connection connection, CloseableIterator<NamedCsvRecord> it,
            List<ColumnDefinition> headersTypeList, TableReference table) throws SQLException {

        List<ColumnReference> columns = headersTypeList.stream().map(ColumnDefinition::column).toList();
        List<String> values = headersTypeList.stream().map(c -> "?").toList();
        InsertSqlStatement insertSqlStatement = new InsertSqlStatementR(table, columns, values);

        String sql = sqlStatementGenerator.getSqlOfStatement(insertSqlStatement);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            batchExecute(connection, ps, it, headersTypeList);
        } catch (SQLException e) {
            throw new CsvDataLoaderException(EXCEPTION_WHILE_WRITING_DATA, e);
        }
    }

    private void dropTable(Connection connection, TableReference table) throws SQLException {
        try {

            String sqlDropTable = sqlStatementGenerator.getSqlOfStatement(new DropContainerSqlStatementR(table, true));
            try (Statement stmnt = connection.createStatement()) {
                stmnt.execute(sqlDropTable);
            }

        } catch (SQLException e) {
            throw new CsvDataLoaderException("Exception while drop Table", e);
        }
    }

    public void createTable(Connection connection, List<ColumnDefinition> headersTypeList, TableReference table)
            throws SQLException {
        try (Statement stmt = connection.createStatement();) {

            CreateContainerSqlStatementR statement = new CreateContainerSqlStatementR(table, headersTypeList, true);

            LOGGER.debug("Created table in given database. {}", statement);

            String sql = sqlStatementGenerator.getSqlOfStatement(statement);
            stmt.execute(sql);
            connection.commit();
        } catch (SQLException e) {
            throw new CsvDataLoaderException("Exception wile create table", e);
        }

    }

    private Optional<SchemaReference> getSchemaFromPath(Path path) {
        Path parent = path.getParent();
        if (basePath.equals(parent)) {
            return Optional.empty();
        }
        String fileName = parent.getFileName().toString();
        return Optional.of(new SchemaReferenceR(fileName));
    }

    private void batchExecute(Connection connection, PreparedStatement ps, CloseableIterator<NamedCsvRecord> it,
            List<ColumnDefinition> columns) throws SQLException {

        connection.setAutoCommit(false);
        long start = System.currentTimeMillis();
        int count = 0;
        while (it.hasNext()) {
            NamedCsvRecord r = it.next();

            int colIndex = 0;
            for (ColumnDefinition columnDefinition : columns) {
                processingTypeValues(ps, columnDefinition, colIndex++, r);
            }
            ps.addBatch();
            ps.clearParameters();
            if (count % config.batchSize() == 0) {
                ps.executeBatch();
                LOGGER.debug("execute batch time {}", (System.currentTimeMillis() - start));
                ps.getConnection().commit();
                LOGGER.debug("execute commit time {}", (System.currentTimeMillis() - start));
                start = System.currentTimeMillis();
            }
            count++;
        }
        LOGGER.debug("---");

        ps.executeBatch();
        LOGGER.debug("execute batch time {}", (System.currentTimeMillis() - start));

        connection.commit();
        LOGGER.debug("execute commit time {}", (System.currentTimeMillis() - start));
        connection.setAutoCommit(true);
    }

    private void processingTypeValues(PreparedStatement ps, ColumnDefinition columnDefinition, int index,
            NamedCsvRecord r) throws SQLException {

        ColumnReference column = columnDefinition.column();
        ColumnDataType type = columnDefinition.columnType();
        String typeName = type.name();
        String field = r.getField(column.name());

        try {
            setPrepareStatement(ps, index, typeName, field);
        } catch (SQLException e) {
            throw new CsvDataLoaderException(EXCEPTION_WHILE_SETTING_VALUE_TO_PREPARED_STATEMENT, e);
        }
    }

    private void setPrepareStatement(PreparedStatement ps, int index, String typeName, String field)
            throws SQLException {

        if (field == null || field.equals(config.nullValue())) {
            ps.setObject(index, null);
            return;
        }
        switch (typeName.toUpperCase()) {
        case "BOOLEAN": {
            ps.setBoolean(index, field.equals("") ? Boolean.FALSE : Boolean.valueOf(field));
            return;
        }
        case "BIGINT": {
            ps.setLong(index, field.equals("") ? 0l : Long.valueOf(field));
            return;
        }
        case "DATE": {
            ps.setDate(index, Date.valueOf(field));
            return;
        }
        case "INTEGER": {
            ps.setInt(index, field.equals("") ? 0 : Integer.valueOf(field));
            return;
        }
        case "DECIMAL": {
            ps.setDouble(index, field.equals("") ? 0.0 : Double.valueOf(field));
            return;
        }
        case "SMALLINT": {
            ps.setShort(index, field.equals("") ? 0 : Short.valueOf(field));
            return;
        }
        case "TIMESTAMP": {
            ps.setTimestamp(index, Timestamp.valueOf(field));
            return;
        }
        case "TIME": {
            ps.setTime(index, Time.valueOf(field));
            return;
        }
        case "VARCHAR": {
            ps.setString(index, field);
            return;
        }
        default:
            ps.setString(index, field);
        }
    }

    private List<ColumnDefinition> getHeadersTypeList(NamedCsvRecord types) {
        List<ColumnDefinition> result = new ArrayList<>();
        if (types != null) {
            for (String header : types.getHeader()) {
                ColumnDataTypeR sqlType = parseColumnDataType(types.getField(header));
                ColumnDefinition dbc = new ColumnDefinitionR(new ColumnReferenceR(header), sqlType);
                result.add(dbc);
            }
        }
        return result;
    }

    private ColumnDataTypeR parseColumnDataType(String stringType) {
        int indexStart = stringType.indexOf("(");
        int indexEnd = stringType.indexOf(")");

        String type = null;
        String detail = null;
        if (indexStart > 0) {
            type = stringType.substring(0, indexStart);
            detail = stringType.substring(indexStart + 1, indexEnd);
        } else {
            type = stringType;
        }
        return new ColumnDataTypeR(type, Optional.ofNullable(detail));
    }

    private String getFileNameWithoutExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    private void delete(Path path) {

        String tableName = getFileNameWithoutExtension(path.getFileName().toString());
        LOGGER.debug("Drop table {}", tableName);

        try (Connection connection = dataSource.getConnection()) {
            Optional<SchemaReference> schema = getSchemaFromPath(path);

            DropContainerSqlStatementR dropStatement = new DropContainerSqlStatementR(
                    new TableReferenceR(schema, tableName, "TABLE"), true);

            String sql = sqlStatementGenerator.getSqlOfStatement(dropStatement);

            try (Statement stmnt = connection.createStatement()) {
                stmnt.execute(sql);
            }
        } catch (SQLException e) {
            throw new CsvDataLoaderException(EXCEPTION_DATABASE_CONNECTION_ERROR, e);

        }

    }

    @Override
    public void handleInitialPaths(List<Path> initialPaths) {
        initialPaths.parallelStream().forEach(this::checkPathAndLoadCsv);
    }

    @Override
    public void handlePathEvent(Path path, Kind<Path> kind) {
        if (Files.isDirectory(path)) {
            return;
        }
        if (kind.name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name())
                || kind.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
            checkPathAndLoadCsv(path);
        }
        if (kind.name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
            delete(path);
        }
    }

    @Override
    public void handleBasePath(Path basePath) {
        this.basePath = basePath;
    }

}
