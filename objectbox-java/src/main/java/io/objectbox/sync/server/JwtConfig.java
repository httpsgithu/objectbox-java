/*
 * Copyright 2025 ObjectBox Ltd. All rights reserved.
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

import io.objectbox.flatbuffers.BaseVector;
import io.objectbox.flatbuffers.BooleanVector;
import io.objectbox.flatbuffers.ByteVector;
import io.objectbox.flatbuffers.Constants;
import io.objectbox.flatbuffers.DoubleVector;
import io.objectbox.flatbuffers.FlatBufferBuilder;
import io.objectbox.flatbuffers.FloatVector;
import io.objectbox.flatbuffers.IntVector;
import io.objectbox.flatbuffers.LongVector;
import io.objectbox.flatbuffers.ShortVector;
import io.objectbox.flatbuffers.StringVector;
import io.objectbox.flatbuffers.Struct;
import io.objectbox.flatbuffers.Table;
import io.objectbox.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class JwtConfig extends Table {
    public static void ValidateVersion() { Constants.FLATBUFFERS_23_5_26(); }
    public static JwtConfig getRootAsJwtConfig(ByteBuffer _bb) { return getRootAsJwtConfig(_bb, new JwtConfig()); }
    public static JwtConfig getRootAsJwtConfig(ByteBuffer _bb, JwtConfig obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
    public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
    public JwtConfig __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

    /**
     * URL to fetch the current public key used to verify JWT signatures.
     */
    public String publicKeyUrl() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
    public ByteBuffer publicKeyUrlAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
    public ByteBuffer publicKeyUrlInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 4, 1); }
    /**
     * Fixed public key used to sign JWT tokens; e.g. for development purposes.
     * Supply either publicKey or publicKeyUrl, but not both.
     */
    public String publicKey() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
    public ByteBuffer publicKeyAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
    public ByteBuffer publicKeyInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 6, 1); }
    /**
     * Cache expiration time in seconds for the public key(s) fetched from publicKeyUrl.
     * If absent or zero, the default is used.
     */
    public long publicKeyCacheExpirationSeconds() { int o = __offset(8); return o != 0 ? (long)bb.getInt(o + bb_pos) & 0xFFFFFFFFL : 0L; }
    /**
     * JWT claim "aud" (audience) used to verify JWT tokens.
     */
    public String claimAud() { int o = __offset(10); return o != 0 ? __string(o + bb_pos) : null; }
    public ByteBuffer claimAudAsByteBuffer() { return __vector_as_bytebuffer(10, 1); }
    public ByteBuffer claimAudInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 10, 1); }
    /**
     * JWT claim "iss" (issuer) used to verify JWT tokens.
     */
    public String claimIss() { int o = __offset(12); return o != 0 ? __string(o + bb_pos) : null; }
    public ByteBuffer claimIssAsByteBuffer() { return __vector_as_bytebuffer(12, 1); }
    public ByteBuffer claimIssInByteBuffer(ByteBuffer _bb) { return __vector_in_bytebuffer(_bb, 12, 1); }

    public static int createJwtConfig(FlatBufferBuilder builder,
                                      int publicKeyUrlOffset,
                                      int publicKeyOffset,
                                      long publicKeyCacheExpirationSeconds,
                                      int claimAudOffset,
                                      int claimIssOffset) {
        builder.startTable(5);
        JwtConfig.addClaimIss(builder, claimIssOffset);
        JwtConfig.addClaimAud(builder, claimAudOffset);
        JwtConfig.addPublicKeyCacheExpirationSeconds(builder, publicKeyCacheExpirationSeconds);
        JwtConfig.addPublicKey(builder, publicKeyOffset);
        JwtConfig.addPublicKeyUrl(builder, publicKeyUrlOffset);
        return JwtConfig.endJwtConfig(builder);
    }

    public static void startJwtConfig(FlatBufferBuilder builder) { builder.startTable(5); }
    public static void addPublicKeyUrl(FlatBufferBuilder builder, int publicKeyUrlOffset) { builder.addOffset(0, publicKeyUrlOffset, 0); }
    public static void addPublicKey(FlatBufferBuilder builder, int publicKeyOffset) { builder.addOffset(1, publicKeyOffset, 0); }
    public static void addPublicKeyCacheExpirationSeconds(FlatBufferBuilder builder, long publicKeyCacheExpirationSeconds) { builder.addInt(2, (int) publicKeyCacheExpirationSeconds, (int) 0L); }
    public static void addClaimAud(FlatBufferBuilder builder, int claimAudOffset) { builder.addOffset(3, claimAudOffset, 0); }
    public static void addClaimIss(FlatBufferBuilder builder, int claimIssOffset) { builder.addOffset(4, claimIssOffset, 0); }
    public static int endJwtConfig(FlatBufferBuilder builder) {
        int o = builder.endTable();
        return o;
    }

    public static final class Vector extends BaseVector {
        public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

        public JwtConfig get(int j) { return get(new JwtConfig(), j); }
        public JwtConfig get(JwtConfig obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
    }
}