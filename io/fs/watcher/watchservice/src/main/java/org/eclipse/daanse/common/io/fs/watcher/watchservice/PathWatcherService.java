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
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class PathWatcherService {

    private final Map<FileSystemWatcherListener, FileWatcherRunable> listeners = Collections
            .synchronizedMap(new HashMap<>());

    private final Map<ComponentServiceObjects<FileSystemWatcherListener>, FileSystemWatcherListener> listenersCSO = Collections
            .synchronizedMap(new HashMap<>());

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindPathListener(ComponentServiceObjects<FileSystemWatcherListener> listenerCSO, Map<String, Object> map)
            throws IOException {

        FileSystemWatcherListener listener = listenerCSO.getService();
        FileWatcherRunable fwrRunable = new FileWatcherRunable(listener, map);
        String name = FileWatcherRunable.class.getSimpleName() + " " + fwrRunable.getObservedPath();
        Thread.ofVirtual().name(name).start(fwrRunable);
        listeners.put(listener, fwrRunable);
        listenersCSO.put(listenerCSO, listener);

    }

    void unbindPathListener(ComponentServiceObjects<FileSystemWatcherListener> listenerCSO) {
        FileSystemWatcherListener listener = listenersCSO.remove(listenerCSO);
        listenerCSO.ungetService(listener);
        FileWatcherRunable runable = listeners.remove(listener);
        if (runable != null) {
            runable.shutdown();
        }
    }

}
