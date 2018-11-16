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

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import zipkin2.Span
import zipkin2.codec.SpanBytesDecoder
import java.util.ArrayList

@RestController
@RequestMapping("/api")
class TransportController() {

  @Autowired
  lateinit var forwarder: SpanTransport

  @PostMapping("/v2/spans")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun processV2Spans(@RequestBody reqSpans: String) {
    val spans = ArrayList<Span>()
    SpanBytesDecoder.JSON_V2.decodeList(reqSpans.toByteArray(), spans)
    forwarder.forward(spans.toList())
  }

  @PostMapping("/v1/spans")
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun processV1Spans(@RequestBody reqSpans: String) {
    val spans = ArrayList<Span>()
    SpanBytesDecoder.JSON_V1.decodeList(reqSpans.toByteArray(), spans)
    forwarder.forward(spans.toList())
  }

}