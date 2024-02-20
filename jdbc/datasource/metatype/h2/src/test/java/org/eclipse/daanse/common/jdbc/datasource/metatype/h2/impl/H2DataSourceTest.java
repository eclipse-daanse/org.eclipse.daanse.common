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

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class H2DataSourceTest {

    @Test
    void noConfigurationNoServiceTest(@InjectService(cardinality = 0) ServiceAware<DataSource> saDataSource, //
            @InjectService(cardinality = 0) ServiceAware<XADataSource> saXaDataSource, //
            @InjectService(cardinality = 0) ServiceAware<ConnectionPoolDataSource> saCpDataSource) throws Exception {

        assertThat(saDataSource.getServices()).isEmpty();
        assertThat(saXaDataSource.getServices()).isEmpty();
        assertThat(saCpDataSource.getServices()).isEmpty();

    }

    @Test
    @WithFactoryConfiguration(factoryPid = Constants.PID_DATASOURCE, name = "1", location = "?")
    void serviceWithConfigurationTest(@InjectService ServiceAware<DataSource> serviceAwareDataSource, //
            @InjectService ServiceAware<XADataSource> serviceAwareXaDataSource, //
            @InjectService ServiceAware<ConnectionPoolDataSource> serviceAwareCpDataSource) throws Exception {

        assertThat(serviceAwareDataSource.getServices()).hasSize(1);
        assertThat(serviceAwareXaDataSource.getServices()).hasSize(1);
        assertThat(serviceAwareCpDataSource.getServices()).hasSize(1);

        DataSource dataSource = serviceAwareDataSource.waitForService(0);
        XADataSource xaDataSource = serviceAwareXaDataSource.waitForService(0);
        ConnectionPoolDataSource cpDataSource = serviceAwareCpDataSource.waitForService(0);

        // Singleton
        assertThat(dataSource).isEqualTo(xaDataSource).isEqualTo(cpDataSource);

    }
}
