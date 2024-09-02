/*
 * Copyright 2019-2024 ObjectBox Ltd. All rights reserved.
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

import io.objectbox.annotation.apihint.Internal;
import io.objectbox.sync.SyncCredentialsToken;

/**
 * Internal class to keep configuration for a cluster peer.
 */
@Internal
class PeerInfo {
    String url;
    SyncCredentialsToken credentials;

    PeerInfo(String url, SyncCredentialsToken credentials) {
        this.url = url;
        this.credentials = credentials;
    }
}
