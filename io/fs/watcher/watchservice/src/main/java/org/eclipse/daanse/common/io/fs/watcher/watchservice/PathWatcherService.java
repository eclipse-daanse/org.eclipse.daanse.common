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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherListener;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ReferenceCardinality#AT_LEAST_ONE} lets the PathWatcherService be
 * activated only if it is really needed.
 */
@Component(service = {})
public class PathWatcherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PathWatcherService.class);

    private final Map<ComponentServiceObjects<FileSystemWatcherListener>, FileSystemWatcherListener> listenersCSO = Collections
            .synchronizedMap(new HashMap<>());

    private CountDownLatch latchFileWatcherRunableReady = new CountDownLatch(1);
    private FileWatcherRunable fileWatcherRunable;

    /**
     * {@link Activate} Annotation on Constructor forces the
     * {@link ServiceComponentRuntime} to create Instance before Binding a
     * {@link FileSystemWatcherListener}
     *
     * @throws IOException
     */
    @Activate
    public PathWatcherService() throws IOException {
        LOGGER.info("constrcutor");
        fileWatcherRunable = new FileWatcherRunable();
        Executors.newVirtualThreadPerTaskExecutor().execute(fileWatcherRunable);
        LOGGER.info("activated");
        latchFileWatcherRunableReady.getCount();
    }

    @Deactivate
    public void deActivate() {
        LOGGER.info("deactivate - start");

        fileWatcherRunable.shutdown();

        LOGGER.info("deactivate - end");

    }

    @Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC)
    void bindFileSystemWatcherListener(ComponentServiceObjects<FileSystemWatcherListener> listenerCSO,
            Map<String, Object> map) throws IOException, InterruptedException {

        LOGGER.info("bind FileSystemWatcherListener with properties: {}", map);

        FileSystemWatcherListener listener = listenerCSO.getService();
        listenersCSO.put(listenerCSO, listener);

        if (listener == null) {
            LOGGER.warn("Could not get FileSystemWatcherListener-Service of: {}, props: {}", listenerCSO, map);
            return;
        }
        try {
            latchFileWatcherRunableReady.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("FileWatcherRunable not ready: {}, props: {}", listenerCSO, map);
            throw new IllegalStateException("FileWatcherRunable not ready", e);

        }
        fileWatcherRunable.addFileWatcherRunable(listener, map);

    }

    void unbindFileSystemWatcherListener(ComponentServiceObjects<FileSystemWatcherListener> listenerCSO,
            Map<String, Object> map) {
        FileSystemWatcherListener listener = listenersCSO.remove(listenerCSO);

        LOGGER.info("unbind FileSystemWatcherListener with properties: {}", map);

        if (listener == null) {
            LOGGER.warn("Service not handled: {}", listenerCSO);
            return;
        }
        listenerCSO.ungetService(listener);
        fileWatcherRunable.removeFileWatcherRunable(listener);
    }

}
