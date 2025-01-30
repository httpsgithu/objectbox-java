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

// automatically generated by the FlatBuffers compiler, do not modify

package io.objectbox.sync.server;

/**
 * Bit flags to configure the Sync Server.
 */
@SuppressWarnings("unused")
public final class SyncServerFlags {
  private SyncServerFlags() { }
  /**
   * By default, if the Sync Server allows logins without credentials, it logs a warning message.
   * If this flag is set, the message is logged only as "info".
   */
  public static final int AuthenticationNoneLogInfo = 1;
  /**
   * By default, the Admin server is enabled; this flag disables it.
   */
  public static final int AdminDisabled = 2;
  /**
   * By default, the Sync Server logs messages when it starts and stops; this flag disables it.
   */
  public static final int LogStartStopDisabled = 4;
}

