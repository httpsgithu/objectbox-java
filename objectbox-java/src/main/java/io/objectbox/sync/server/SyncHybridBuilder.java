/*
 * Copyright 2024 ObjectBox Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.objectbox.sync.server;

import io.objectbox.BoxStore;
import io.objectbox.BoxStoreBuilder;
import io.objectbox.InternalAccess;
import io.objectbox.annotation.apihint.Internal;
import io.objectbox.sync.Sync;
import io.objectbox.sync.SyncBuilder;
import io.objectbox.sync.SyncClient;
import io.objectbox.sync.SyncCredentials;

/**
 * Allows to configure the client and server setup to build a {@link SyncHybrid}.
 * To change the server/cluster configuration, call {@link #serverBuilder()}, and for the client configuration
 * {@link #clientBuilder()}.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class SyncHybridBuilder {

    private final BoxStore boxStore;
    private final BoxStore boxStoreServer;
    private final SyncBuilder clientBuilder;
    private final SyncServerBuilder serverBuilder;

    /**
     * Internal API; use {@link Sync#hybrid(BoxStoreBuilder, String, SyncCredentials)} instead.
     */
    @Internal
    public SyncHybridBuilder(BoxStoreBuilder storeBuilder, String url, SyncCredentials authenticatorCredentials) {
        BoxStoreBuilder storeBuilderServer = InternalAccess.clone(storeBuilder, "-server");
        boxStore = storeBuilder.build();
        boxStoreServer = storeBuilderServer.build();
        SyncCredentials clientCredentials = authenticatorCredentials.createClone();
        clientBuilder = new SyncBuilder(boxStore, clientCredentials);  // Do not yet set URL, port may be dynamic
        serverBuilder = new SyncServerBuilder(boxStoreServer, url, authenticatorCredentials);
    }

    /**
     * Allows to customize client options of the hybrid.
     */
    public SyncBuilder clientBuilder() {
        return clientBuilder;
    }

    /**
     * Allows to customize server options of the hybrid.
     */
    public SyncServerBuilder serverBuilder() {
        return serverBuilder;
    }

    /**
     * Builds, starts and returns a SyncHybrid.
     * Note that building and started must be done in one go for hybrids to ensure the correct sequence.
     */
    @SuppressWarnings("resource") // User is responsible for closing
    public SyncHybrid buildAndStart() {
        // Build and start the server first, we may need to get a  port for the client
        SyncServer server = serverBuilder.buildAndStart();

        clientBuilder.lateUrl(server.getUrl());
        SyncClient client = clientBuilder.buildAndStart();

        return new SyncHybrid(boxStore, client, boxStoreServer, server);
    }

}
