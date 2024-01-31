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
package org.eclipse.daanse.common.io.fs.watcher.watchservice;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class PathWatcherService {

    private final Map<FileSystemWatcherListener, FileWatcherRunable> listeners = Collections
            .synchronizedMap(new HashMap<>());
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindPathListener(FileSystemWatcherListener listener, Map<String, Object> map) throws IOException {

        FileWatcherRunable fwrRunable = new FileWatcherRunable(listener, map);
        executorService.execute(fwrRunable);
        listeners.put(listener, fwrRunable);
    }

    void unbindPathListener(FileSystemWatcherListener listener) {

        FileWatcherRunable runable = listeners.remove(listener);
        if (runable != null) {
            runable.shutdown();
        }
    }

}
