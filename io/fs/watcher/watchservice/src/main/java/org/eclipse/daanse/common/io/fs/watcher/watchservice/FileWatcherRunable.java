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
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.daanse.common.io.fs.watcher.api.EventKind;
import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherListener;
import org.eclipse.daanse.common.io.fs.watcher.api.FileSystemWatcherWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileWatcherRunable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherRunable.class);

    private WatchService watcheService;
    private final Map<WatchKey, Path> watchKeys = new ConcurrentHashMap<>();
    private FileSystemWatcherListener listener;
    private boolean stop;

    private Path observedPath;
    private Kind<?>[] kinds;

    private Optional<Pattern> oPattern;
    private boolean recursive;

    FileWatcherRunable(FileSystemWatcherListener listener, Map<String, Object> props) throws IOException {
        this.listener = listener;
        this.watcheService = FileSystems.getDefault().newWatchService();
        Object oRecursive = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_RECURSIVE,
                "false");
        if (oRecursive == null) {
            oRecursive = "false";
        }

        this.recursive = Boolean.valueOf(oRecursive.toString());

        Object objPattern = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATTERN,
                FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATTERN_DEFAULT);

        boolean emptyPattern = objPattern == null || objPattern.toString().isBlank();
        oPattern = emptyPattern ? Optional.empty() : Optional.of(Pattern.compile(objPattern.toString()));

        Object oKinds = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_KINDS,
                new String[] { EventKind.ENTRY_CREATE.toString(), EventKind.ENTRY_DELETE.toString(),
                        EventKind.ENTRY_MODIFY.toString() });

        if (oKinds instanceof String[] sKinds) {

            kinds = new WatchEvent.Kind<?>[sKinds.length];
            for (int i = 0; i < sKinds.length; i++) {

                String sKind = sKinds[i];
                if ("ENTRY_CREATE".equals(sKind)) {

                    kinds[i] = EventKind.ENTRY_CREATE.getKind();
                } else if ("ENTRY_DELETE".equals(sKind)) {

                    kinds[i] = EventKind.ENTRY_DELETE.getKind();
                } else if ("ENTRY_MODIFY".equals(sKind)) {

                    kinds[i] = EventKind.ENTRY_MODIFY.getKind();
                }
            }
        }

        FileSystem fs = FileSystems.getDefault();

        Object oPath = props.getOrDefault(FileSystemWatcherWhiteboardConstants.FILESYSTEM_WATCHER_PATH, "");

        boolean emptyPath = oPath == null || oPath.toString().isBlank();
        String sPath = emptyPath ? "" : oPath.toString();
        observedPath = fs.getPath(sPath).toAbsolutePath();

        listener.handleBasePath(observedPath);

        if (recursive) {
            registerPathWithSubDirs(observedPath);
        } else {
            registerPath(observedPath);
        }

    }

    private void registerPath(Path path) throws IOException {

        try (Stream<Path> stream = Files.list(path)) {
            List<Path> currentPaths = stream.toList();
            listener.handleInitialPaths(currentPaths);
        }

        WatchKey watchKey = path.register(watcheService, kinds);
        synchronized (watchKeys) {
            watchKeys.put(watchKey, path);
        }

    }

    private void clear() {

        listener = null;
        observedPath = null;
        kinds = null;
        oPattern = Optional.empty();
    }

    public void shutdown() {
        stop = true;
        synchronized (watchKeys) {
            for (WatchKey key : watchKeys.keySet()) {
                key.cancel();
            }
            watchKeys.clear();
        }
        try {
            watcheService.close();
            watcheService = null;
        } catch (IOException e) {
            LOGGER.error("Exception while WatcheService::close on shutdown", e);
        }
    }

    @Override
    public void run() {

        loopWatchUntilStop();

        clear();
    }

    private void loopWatchUntilStop() {
        try {
            while (!stop) {
                WatchKey key = watcheService.take();

                if (key == null) {
                    // no events, try again
                    continue;
                }

                Path basePath = basePathFromWatchkey(key);

                for (WatchEvent<?> event : key.pollEvents()) {
                    handleEvent(event, basePath);
                }

                boolean resetValid = key.reset();
                if (!resetValid) {
                    LOGGER.warn("invalid WatchKey while reset. basePath: {}  key: {}", basePath, key);
                }
            }
        } catch (ClosedWatchServiceException e) {
            LOGGER.warn("watcheService::take is interrupted, by closing WatchService");
        } catch (InterruptedException e) {
            shutdown();
            Thread.currentThread().interrupt();
            LOGGER.error("watcheService::take is interrupted for unknown reason", e);
        }
    }

    private void handleEvent(WatchEvent<?> event, Path basePath) {
        WatchEvent.Kind<?> kind = event.kind();
        if (kind == StandardWatchEventKinds.OVERFLOW) {
            return;// not registerable
        }

        WatchEvent<Path> watchEvent = (WatchEvent<Path>) event;

        Path filename = watchEvent.context();

        Path resolvedFile = basePath.resolve(filename);

        if (recursive && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
            try {
                if (Files.isDirectory(resolvedFile, LinkOption.NOFOLLOW_LINKS)) {
                    registerPathWithSubDirs(resolvedFile);
                }
            } catch (IOException e) {
                LOGGER.error("Errown while register Path With Subdirs", e);
            }
        }

        oPattern.ifPresent(pattern -> {
            Matcher matcher = pattern.matcher(resolvedFile.toString());
            if (matcher.matches()) {
                listener.handlePathEvent(resolvedFile, watchEvent.kind());
            }
        });

    }

    private Path basePathFromWatchkey(WatchKey key) {
        synchronized (watchKeys) {
            return watchKeys.get(key);
        }
    }

    private void registerPathWithSubDirs(final Path baseDirectory) throws IOException {
        Files.walkFileTree(baseDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path currentDirectory, BasicFileAttributes attrs)
                    throws IOException {
                registerPath(currentDirectory);
                return FileVisitResult.CONTINUE;
            }
        });

    }

    public Path getObservedPath() {
        return observedPath;
    }
}
