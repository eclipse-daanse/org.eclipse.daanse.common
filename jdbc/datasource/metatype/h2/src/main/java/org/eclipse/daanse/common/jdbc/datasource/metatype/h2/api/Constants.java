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
     * {@link org.eclipse.daanse.common.jdbc.datasource.metatype.h2.impl.H2BaseConfig#plugableFilesystem()}
     */
    public static final String DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM = "plugableFilesystem";

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

    /**
     * Constant for the Option 'file' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_FILE = "file";

    /**
     * Constant for the Option 'zip' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_ZIP = "zip";
    /**
     * Constant for the Option 'nioMapped' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_NIO_MAPPED = "nioMapped";

    /**
     * Constant for the Option 'async' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_ASYNC = "async";

    /**
     * Constant for the Option 'memFS' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_MEM_FS = "memFS";

    /**
     * Constant for the Option 'memLZF' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_MEM_LZF = "memLZF";

    /**
     * Constant for the Option 'nioMemFS' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_NIO_MEM_FS = "nioMemFS";

    /**
     * Constant for the Option 'nioMemLZF' of the Properties
     * {@link Constants#DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM}
     */
    public static final String OPTION_PLUGABLE_FILESYSTEM_NIO_MEM_LZF = "nioMemLZF";

}
