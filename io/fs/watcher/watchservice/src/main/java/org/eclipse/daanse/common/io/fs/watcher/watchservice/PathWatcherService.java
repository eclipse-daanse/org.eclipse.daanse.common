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

import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherListener;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, scope = ServiceScope.SINGLETON)
public class PathWatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathWatcherService.class);

    private final Map<ComponentServiceObjects<FileSystemWatcherListener>, FileSystemWatcherListener> listenersCSO = Collections
            .synchronizedMap(new HashMap<>());

    private FileWatcherRunable fileWatcherRunable;

    private Thread virtualThread;

    @Activate
    public PathWatcherService() throws IOException {
        fileWatcherRunable = new FileWatcherRunable();
        virtualThread = Thread.ofVirtual().start(fileWatcherRunable);
    }

    @Deactivate
    public void deActivate() {
        fileWatcherRunable.shutdown();
        virtualThread.interrupt();
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void bindFileSystemWatcherListener(ComponentServiceObjects<FileSystemWatcherListener> listenerCSO,
            Map<String, Object> map) throws IOException {

        FileSystemWatcherListener listener = listenerCSO.getService();
        listenersCSO.put(listenerCSO, listener);

        if (listener == null) {
            LOGGER.warn("Could not get FileSystemWatcherListener-Service of: {}, props: {}", listenerCSO, map);
            return;
        }
        fileWatcherRunable.addFileWatcherRunable(listener, map);

    }

    void unbindFileSystemWatcherListener(ComponentServiceObjects<FileSystemWatcherListener> listenerCSO) {
        FileSystemWatcherListener listener = listenersCSO.remove(listenerCSO);

        if (listener == null) {
            LOGGER.warn("Service not handled: {}", listenerCSO);
            return;
        }
        listenerCSO.ungetService(listener);
        fileWatcherRunable.remove(listener);
    }

}
