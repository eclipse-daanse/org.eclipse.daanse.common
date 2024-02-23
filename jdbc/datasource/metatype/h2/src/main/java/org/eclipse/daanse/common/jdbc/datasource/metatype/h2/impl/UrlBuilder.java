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

import java.util.Map;

import org.eclipse.daanse.common.jdbc.datasource.metatype.h2.api.Constants;

class UrlBuilder {

    private UrlBuilder() {
    }

    private static final String JDBC_H2 = "jdbc:h2:";

    static String buildUrl(H2BaseConfig config, Map<String, Object> map) {

        StringBuilder urlStringBuilder = new StringBuilder(JDBC_H2);
        appandFileSystem(urlStringBuilder, config, map);
        appandDebug(urlStringBuilder, config, map);
        appandIdentifier(urlStringBuilder, config, map);
        return urlStringBuilder.toString();
    }

    private static void appandIdentifier(StringBuilder urlStringBuilder, H2BaseConfig config, Map<String, Object> map) {
        if (map.containsKey(Constants.DATASOURCE_PROPERTY_IDENTIFIER)) {
            urlStringBuilder.append(config.identifier());
        }
    }

    private static void appandFileSystem(StringBuilder urlStringBuilder, H2BaseConfig config, Map<String, Object> map) {

        if (map.containsKey(Constants.DATASOURCE_PROPERTY_PLUGABLE_FILESYSTEM)) {
            String plugableFileSystem = config.plugableFilesystem();
            if (plugableFileSystem != null) {
                urlStringBuilder.append(plugableFileSystem).append(":");
            }
        }
    }

    private static void appandDebug(StringBuilder urlStringBuilder, H2BaseConfig config, Map<String, Object> map) {
        if (config.debug()) {
            urlStringBuilder.append("debug:");
        }
    }
}
