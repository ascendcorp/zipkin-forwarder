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

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import zipkin2.Call
import zipkin2.Span
import zipkin2.codec.Encoding
import zipkin2.codec.SpanBytesDecoder
import zipkin2.codec.SpanBytesEncoder
import zipkin2.reporter.kinesis.KinesisSender
import java.util.ArrayList
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class ProxyController(private val kinesisSender: KinesisSender) {
  private val logger = LoggerFactory.getLogger(ProxyController::class.simpleName)

  // TODO: Refactor this to accept other encodings and move to common functions

  @PostMapping("/v2/spans")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun processV2Spans(@Valid @RequestBody reqSpans: String, request: ServerHttpRequest) {
    val spans = ArrayList<Span>()
    SpanBytesDecoder.JSON_V2.decodeList(reqSpans.toByteArray(), spans)
    send(spans.toTypedArray()).execute()
    logger.info("Kinesis Status {}", kinesisSender.check().toString())
  }

  @PostMapping("/v1/spans")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun processV1Spans(@Valid @RequestBody reqSpans: String, request: ServerHttpRequest) {
    val spans = ArrayList<Span>()
    SpanBytesDecoder.JSON_V1.decodeList(reqSpans.toByteArray(), spans)
    send(spans.toTypedArray()).execute()
    logger.info("Kinesis Status {}", kinesisSender.check().toString())
  }

  private fun send(spans: Array<Span>): Call<Void> {
    val bytesEncoder: SpanBytesEncoder = when (kinesisSender.encoding()) {
      Encoding.JSON -> SpanBytesEncoder.JSON_V2
      Encoding.THRIFT -> SpanBytesEncoder.THRIFT
      Encoding.PROTO3 -> SpanBytesEncoder.PROTO3
      else -> throw UnsupportedOperationException("encoding: " + kinesisSender.encoding())
    }
    return kinesisSender.sendSpans(spans.map(bytesEncoder::encode).toList())
  }
}