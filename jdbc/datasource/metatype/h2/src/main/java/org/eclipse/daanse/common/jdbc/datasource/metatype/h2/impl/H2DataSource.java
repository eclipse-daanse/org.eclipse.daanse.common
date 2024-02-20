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
package org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants;
import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.LoggerFactory;

@Designate(ocd = H2BaseConfig.class, factory = true)
@Component(service = { DataSource.class, XADataSource.class,
        ConnectionPoolDataSource.class }, scope = ServiceScope.SINGLETON, name = Constants.PID_DATASOURCE)
public class H2DataSource implements ConnectionPoolDataSource, DataSource, XADataSource {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(H2DataSource.class);

    private JdbcDataSource ds;

    @Activate
    public H2DataSource(H2BaseConfig config, Map<String, Object> map) throws SQLException {

        try {

            this.ds = new JdbcDataSource();
            String url = UrlBuilder.buildUrl(config, map);

            LOGGER.debug("composed url: {}", url);

            ds.setURL(url);

            if (map.containsKey(Constants.DATASOURCE_PROPERTY_USERNAME)) {
                ds.setUser(config.username());
            }

            if (map.containsKey(Constants.DATASOURCE_PROPERTY_PASSWORD)) {
                ds.setPassword(config._password());
            }

            if (map.containsKey(Constants.DATASOURCE_PROPERTY_DESCRIPTION)) {
                ds.setDescription(config.description());
            }

        } catch (Exception e) {
            LOGGER.error("Error on activation", e);
            throw e;
        }
    }

    // no @Modified to force consumed Services get new configured connections.

    @Deactivate
    public void deactivate() {
        ds = null;
        LOGGER.debug("deactivated");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return ds.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return ds.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return ds.isWrapperFor(iface);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return ds.getParentLogger();
    }

    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return ds.getPooledConnection();
    }

    @Override
    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        return ds.getPooledConnection(user, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return ds.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        ds.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        ds.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return ds.getLoginTimeout();
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return ds.getXAConnection();
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return ds.getXAConnection(user, password);
    }

}
