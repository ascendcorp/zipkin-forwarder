/*
 *   Copyright 2018 Ascend Corporation (https://www.ascendcorp.com/)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License
 *   is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *   or implied. See the License for the specific language governing permissions and limitations under
 *   the License.
 */

package com.ascendcorp.tracing.zipkinforwarder

import zipkin2.Endpoint
import zipkin2.Span

class TestObjects {
  companion object {
    // Copied from from Zipkin for testing
    // https://github.com/openzipkin/zipkin/blob/master/zipkin/src/test/java/zipkin2/TestObjects.java

    val FRONTEND =
        Endpoint.newBuilder().serviceName("frontend")
            .ip("127.0.0.1").build();
    val BACKEND =
        Endpoint.newBuilder().serviceName("backend")
            .ip("192.168.99.101").port(9000).build();
    val DB =
        Endpoint.newBuilder().serviceName("db")
            .ip("2001:db8::c001").port(3036).build();

    // Use real time, as most span-stores have TTL logic which looks back several days.
    val TODAY = System.currentTimeMillis();
    val CLIENT_SPAN =
        Span.newBuilder()
            .traceId("7180c278b62e8f6a216a2aea45d08fc9")
            .parentId("1")
            .id("2")
            .name("get")
            .kind(Span.Kind.CLIENT)
            .localEndpoint(FRONTEND)
            .remoteEndpoint(BACKEND)
            .timestamp((TODAY + 50L) * 1000L)
            .duration(200 * 1000L)
            .addAnnotation((TODAY + 100) * 1000L, "foo")
            .putTag("http.path", "/api")
            .putTag("clnt/finagle.version", "6.45.0")
            .build();
    val TRACE = listOf(
        Span.newBuilder().traceId(CLIENT_SPAN.traceId()).id("1")
            .name("get")
            .kind(Span.Kind.SERVER)
            .localEndpoint(FRONTEND)
            .timestamp(TODAY * 1000L)
            .duration(350 * 1000L)
            .build(),
        CLIENT_SPAN,
        Span.newBuilder().traceId(CLIENT_SPAN.traceId())
            .parentId(CLIENT_SPAN.parentId()).id(CLIENT_SPAN.id()).shared(true)
            .name("get")
            .kind(Span.Kind.SERVER)
            .localEndpoint(BACKEND)
            .timestamp((TODAY + 100L) * 1000L)
            .duration(150 * 1000L)
            .build(),
        Span.newBuilder()
            .traceId(CLIENT_SPAN.traceId())
            .parentId("2")
            .id("3")
            .name("query")
            .kind(Span.Kind.CLIENT)
            .localEndpoint(BACKEND)
            .remoteEndpoint(DB)
            .timestamp((TODAY + 150L) * 1000L)
            .duration(50 * 1000L)
            .addAnnotation((TODAY + 190) * 1000L, "⻩")
            .putTag("error", "\uD83D\uDCA9")
            .build())

    val MALFORMED_SPAN =
        Span.newBuilder()
            .traceId("7180c278b62e8f6a216a2aea45d08fc9")
            .id("2")
            .name("get")
            .kind(Span.Kind.CLIENT)
            .localEndpoint(FRONTEND)
            .remoteEndpoint(BACKEND)
            .timestamp((TODAY + 50L) * 1000L)
            .duration(200 * 1000L)
            .addAnnotation((TODAY + 100) * 1000L, "foo")
            .putTag("http.path", "/api")
            .putTag("clnt/finagle.version", "6.45.0")
            .build();
  }
}