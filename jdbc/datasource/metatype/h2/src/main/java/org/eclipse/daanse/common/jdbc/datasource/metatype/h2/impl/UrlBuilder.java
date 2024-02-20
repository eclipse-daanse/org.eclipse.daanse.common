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
        urlStringBuilder.append(config.identifier());
    }

    private static void appandFileSystem(StringBuilder urlStringBuilder, H2BaseConfig config, Map<String, Object> map) {

        if (map.containsKey(Constants.DATASOURCE_PROPERTY_FILESYSTEM)) {
            switch (config.plugableFileSystem()) {
            case ASYNC:
                urlStringBuilder.append("async:");
            case FILE:
                urlStringBuilder.append("file:");
            case MEM_FS:
                urlStringBuilder.append("memFS:");
            case MEM_LZF:
                urlStringBuilder.append("memLZF:");
            case NIO_MAPPED:
                urlStringBuilder.append("nioMapped:");
            case NIO_MEM_FS:
                urlStringBuilder.append("nioMemFS:");
            case NIO_MEM_LZF:
                urlStringBuilder.append("nioMemLZF:");
            case ZIP:
                urlStringBuilder.append("zip:");
            }
        }
    }

    private static void appandDebug(StringBuilder urlStringBuilder, H2BaseConfig config, Map<String, Object> map) {
        if (config.debug()) {
            urlStringBuilder.append("debug:");
        }

    }
}
