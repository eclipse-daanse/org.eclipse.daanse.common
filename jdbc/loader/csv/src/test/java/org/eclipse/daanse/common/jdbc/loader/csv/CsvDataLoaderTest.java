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
package org.eclipse.daanse.common.jdbc.loader.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.osgi.test.common.dictionary.Dictionaries.dictionaryOf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.eclipse.daanse.common.jdbc.loader.csv.api.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import aQute.bnd.annotation.spi.ServiceProvider;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(MockitoExtension.class)
@RequireConfigurationAdmin
@ServiceProvider(value = DataSource.class)
class CscDataLoaderTest {
    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    Path path;

    @Mock
    DatabaseMetaData databaseMetaData;
    @Mock
    ResultSet resultSet;

    @Mock
    Connection connection;

    @Mock
    Statement statement;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    DataSource dataSource;

    @InjectBundleContext
    BundleContext bc;

    @InjectService
    ConfigurationAdmin ca;

    Configuration conf;

    @BeforeEach
    void beforeEach() throws SQLException, IOException {

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getIdentifierQuoteString()).thenReturn("\"");
        when(databaseMetaData.getDatabaseMajorVersion()).thenReturn(42);
        when(databaseMetaData.getDatabaseMinorVersion()).thenReturn(21);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("MyDB");
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("a");

        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.getConnection()).thenReturn(connection);
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        when(databaseMetaData.getCatalogs()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);

        when(connection.createStatement()).thenReturn(statement);
        bc.registerService(DataSource.class, dataSource, dictionaryOf("ds", "1"));
    }

    private Path copy(String file) throws IOException {

        Path target = path.resolve(file);
        InputStream is = bc.getBundle().getResource(file).openConnection().getInputStream();
        byte[] bytes = is.readAllBytes();
        Files.write(target, bytes);
        return target;

    }

    @AfterEach
    void afterEach() throws IOException {
        if (conf != null) {
            conf.delete();
        }
    }

    private void setupCsvDataLoadServiceImpl(String nullValue, Character quote, Character fieldSeparator,
            String encoding, String stringPath) throws IOException {
        conf = ca.getFactoryConfiguration(Constants.PID_LOADER_FILEWATCHER, "1", "?");
        Dictionary<String, Object> dict = new Hashtable<>();
        if (nullValue != null) {

            dict.put(Constants.PROPERETY_CSV_NULL_VALUE, nullValue);
        }
        if (quote != null) {
            dict.put(Constants.PROPERETY_CSV_QUOTE_CHARACHTER, quote);
        }
        if (fieldSeparator != null) {
            dict.put(Constants.PROPERETY_CSV_FIELD_SEPARATOR, fieldSeparator);
        }
        if (encoding != null) {
            dict.put(Constants.PROPERETY_CSV_ENCODING, encoding);
        }

        dict.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH,
                stringPath != null ? path.resolve(stringPath).toAbsolutePath().toString()
                        : path.toAbsolutePath().toString());
        dict.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_RECURSIVE, true);
        dict.put(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_KINDS, new String[] { "ENTRY_CREATE" });
        conf.update(dict);
    }

    @Test
    void testinsertParamStatement() throws IOException, URISyntaxException, SQLException, InterruptedException {
        Path p = path.resolve("csv");
        Files.createDirectories(p);
        Thread.sleep(200);

        setupCsvDataLoadServiceImpl("NULL", '\"', ',', "UTF-8", "csv");

        Thread.sleep(500);
        copy("csv/test.csv");
        Thread.sleep(1000);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        verify(connection, (times(1))).prepareStatement(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).contains("test");

        verify(preparedStatement, (times(2))).executeBatch();
    }

    @Test
    void testSubDir() throws IOException, URISyntaxException, SQLException, InterruptedException {
        Path p = path.resolve("csv/schema1");
        Files.createDirectories(p);

        Thread.sleep(200);
        setupCsvDataLoadServiceImpl("NULL", '\"', ',', "UTF-8", "csv");

        Thread.sleep(500);
        copy("csv/schema1/test1.csv");
        Thread.sleep(1000);

        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> longCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Integer> integerCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Double> doubleCaptor = ArgumentCaptor.forClass(Double.class);
        ArgumentCaptor<Short> shortCaptor = ArgumentCaptor.forClass(Short.class);
        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);

        verify(connection, (times(1))).prepareStatement(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).contains("schema1\".\"test1");
        verify(statement, (times(3))).execute(stringCaptor.capture());
        assertThat(stringCaptor.getValue()).contains("VARCHAR(40)");
        verify(preparedStatement, (times(4))).setInt(integerCaptor.capture(), integerCaptor.capture());
        verify(preparedStatement, (times(2))).setLong(integerCaptor.capture(), longCaptor.capture());
        verify(preparedStatement, (times(2))).setBoolean(integerCaptor.capture(), booleanCaptor.capture());
        verify(preparedStatement, (times(2))).setDate(integerCaptor.capture(), dateCaptor.capture());
        verify(preparedStatement, (times(2))).setDouble(integerCaptor.capture(), doubleCaptor.capture());
        verify(preparedStatement, (times(2))).setShort(integerCaptor.capture(), shortCaptor.capture());
        verify(preparedStatement, (times(2))).setTimestamp(integerCaptor.capture(), timestampCaptor.capture());
        verify(preparedStatement, (times(2))).setString(integerCaptor.capture(), stringCaptor.capture());

        verify(preparedStatement, (times(2))).executeBatch();
    }

}
