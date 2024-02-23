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

import static org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants.DATASOURCE_PROPERTY_DEBUG;
import static org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants.DATASOURCE_PROPERTY_DESCRIPTION;
import static org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants.DATASOURCE_PROPERTY_PUGABLE_FILESYSTEM;
import static org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants.DATASOURCE_PROPERTY_IDENTIFIER;
import static org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants.DATASOURCE_PROPERTY_PASSWORD;
import static org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants.DATASOURCE_PROPERTY_USERNAME;

import org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = H2BaseConfig.L10N_OCD_NAME, description = H2BaseConfig.L10N_POSTFIX_DESCRIPTION)
@interface H2BaseConfig {

    public static final String L10N_PREFIX = "%";

    public static final String L10N_POSTFIX_DESCRIPTION = ".description";
    public static final String L10N_POSTFIX_NAME = ".name";

    public static final String L10N_OCD_NAME = L10N_PREFIX + "ocd" + L10N_POSTFIX_NAME;
    public static final String L10N_OCD_DESCRIPTION = L10N_PREFIX + "ocd" + L10N_POSTFIX_DESCRIPTION;

    public static final String L10N_DEBUG_NAME = L10N_PREFIX + DATASOURCE_PROPERTY_DEBUG + L10N_POSTFIX_NAME;
    public static final String L10N_DEBUG_DESCRIPTION = L10N_PREFIX + DATASOURCE_PROPERTY_DEBUG
            + L10N_POSTFIX_DESCRIPTION;

    public static final String L10N_DESCRIPTION_NAME = L10N_PREFIX + DATASOURCE_PROPERTY_DESCRIPTION
            + L10N_POSTFIX_NAME;
    public static final String L10N_DESCRIPTION_DESCRIPTION = L10N_PREFIX + DATASOURCE_PROPERTY_DESCRIPTION
            + L10N_POSTFIX_DESCRIPTION;

    public static final String L10N_FILESYSTEM_NAME = L10N_PREFIX + DATASOURCE_PROPERTY_PUGABLE_FILESYSTEM + L10N_POSTFIX_NAME;
    public static final String L10N_FILESYSTEM_DESCRIPTION = L10N_PREFIX + DATASOURCE_PROPERTY_PUGABLE_FILESYSTEM
            + L10N_POSTFIX_DESCRIPTION;

    public static final String L10N_IDENTIFIER_NAME = L10N_PREFIX + DATASOURCE_PROPERTY_IDENTIFIER + L10N_POSTFIX_NAME;
    public static final String L10N_IDENTIFIER_DESCRIPTION = L10N_PREFIX + DATASOURCE_PROPERTY_IDENTIFIER
            + L10N_POSTFIX_DESCRIPTION;

    public static final String L10N_PASSWORD_NAME = L10N_PREFIX + DATASOURCE_PROPERTY_PASSWORD + L10N_POSTFIX_NAME;
    public static final String L10N_PASSWORD_DESCRIPTION = L10N_PREFIX + DATASOURCE_PROPERTY_PASSWORD
            + L10N_POSTFIX_DESCRIPTION;

    public static final String L10N_USERNAME_NAME = L10N_PREFIX + DATASOURCE_PROPERTY_USERNAME + L10N_POSTFIX_NAME;
    public static final String L10N_USERNAME_DESCRIPTION = L10N_PREFIX + DATASOURCE_PROPERTY_USERNAME
            + L10N_POSTFIX_DESCRIPTION;

    enum PluggableFileSystem {
        file, zip, nioMapped, async, memFS, memLZF, nioMemFS, nioMemLZF
    }

    /**
     * {@link Constants#DATASOURCE_PROPERTY_DEBUG}.
     *
     */
    @AttributeDefinition(name = L10N_DEBUG_NAME, description = L10N_DEBUG_DESCRIPTION)
    boolean debug() default false;

    /**
     * {@link Constants#DATASOURCE_PROPERTY_DESCRIPTION}.
     *
     */
    @AttributeDefinition(name = L10N_DESCRIPTION_NAME, description = L10N_DESCRIPTION_DESCRIPTION)
    String description();

    /**
     * {@link Constants#DATASOURCE_PROPERTY_DEBUG}.
     *
     */
    @AttributeDefinition(name = L10N_FILESYSTEM_NAME, description = L10N_FILESYSTEM_DESCRIPTION)
    PluggableFileSystem plugableFilesystem();

    /**
     * {@link Constants#DATASOURCE_PROPERTY_PUGABLE_FILESYSTEM}.
     *
     */
    @AttributeDefinition(name = L10N_IDENTIFIER_NAME, description = L10N_IDENTIFIER_DESCRIPTION)
    String identifier();

    /**
     * {@link Constants#DATASOURCE_PROPERTY_PASSWORD}. OSGi Service Component Spec.:
     *
     * Component properties whose names start with full stop are available to the
     * component instance but are not available as service properties of the
     * registered service.
     *
     * A single low line ('_' \u005F) is converted into a full stop ('.' \u002E)
     *
     * @return password
     */
    @AttributeDefinition(name = L10N_PASSWORD_NAME, description = L10N_PASSWORD_DESCRIPTION, type = AttributeType.PASSWORD)
    String _password() default "";

    /**
     * {@link Constants#DATASOURCE_PROPERTY_USERNAME}.
     *
     */
    @AttributeDefinition(name = L10N_USERNAME_NAME, description = L10N_USERNAME_DESCRIPTION)
    String username() default "";

}
