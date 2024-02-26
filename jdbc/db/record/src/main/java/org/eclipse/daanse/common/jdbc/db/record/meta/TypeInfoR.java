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
package org.eclipse.daanse.common.jdbc.db.record.meta;

import java.util.Optional;

import org.eclipse.daanse.common.jdbc.db.api.meta.TypeInfo;

public record TypeInfoR(String typeName, int dataType, int percision, Optional<String> literatPrefix,
        Optional<String> literatSuffix, Optional<String> createPragmas, Nullable nullable, boolean caseSensitive,
        Searchable searchable, boolean unsignesAttribute, boolean fixedPrecScale, boolean autoIncrement,
        Optional<String> localTypeName, short minimumScale, short maximumScale, int numPrecRadix) implements TypeInfo {

}
