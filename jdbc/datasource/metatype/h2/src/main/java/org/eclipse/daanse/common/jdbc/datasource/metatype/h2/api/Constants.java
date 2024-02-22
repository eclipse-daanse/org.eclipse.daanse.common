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
package org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.framework.Bundle;

/**
 * Constants of this {@link Bundle}.
 */
public class Constants {

    private Constants() {
    }

    /**
     * Constant for the {@link org.osgi.framework.Constants#SERVICE_PID} of a
     * {@link DataSource} and {@link XADataSource} and
     * {@link ConnectionPoolDataSource} - Service.
     */
    public static final String PID_DATASOURCE = "org.eclipse.daanse.common.jdbc.datasource.metatype.h2.DataSource";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#PID_DATASOURCE}.
     *
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#debug()}
     */
    public static final String DATASOURCE_PROPERTY_DEBUG = "debug";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#PID_DATASOURCE}.
     *
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#description()}
     */
    public static final String DATASOURCE_PROPERTY_DESCRIPTION = "description";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#PID_DATASOURCE}.
     *
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#plugableFileSystem()}
     */
    public static final String DATASOURCE_PROPERTY_PUGABLE_FILESYSTEM = "plugableFilesystem";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#PID_DATASOURCE}.
     *
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#identifier()}
     */
    public static final String DATASOURCE_PROPERTY_IDENTIFIER = "identifier";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#PID_DATASOURCE}.
     *
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#_password()}
     */
    public static final String DATASOURCE_PROPERTY_PASSWORD = ".password";

    /**
     * Constant for Properties of the Service that could be configured using the
     * {@link Constants#PID_DATASOURCE}.
     *
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#username()}
     */
    public static final String DATASOURCE_PROPERTY_USERNAME = "username";

}
