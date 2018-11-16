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

import com.ascendcorp.tracing.zipkinforwarder.config.ZipkinProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import zipkin2.Span
import zipkin2.codec.SpanBytesEncoder
import zipkin2.reporter.kinesis.KinesisSender
import zipkin2.reporter.okhttp3.OkHttpSender

@Service
class SpanTransport: Transport {

  @Autowired(required = false)
  @Qualifier("kinesisSender")
  lateinit var kinesisSender : KinesisSender

  @Autowired(required = false)
  @Qualifier("httpSender")
  lateinit var httpSender: OkHttpSender

  @Autowired
  lateinit var config: ZipkinProperties

  val log = LoggerFactory.getLogger(SpanTransport::class.java)
  
  override fun forward(spans: List<Span>)  {
    try {
      val bytesEncoder: SpanBytesEncoder = when (config.spanEncoding) {
        "JSON" -> SpanBytesEncoder.JSON_V2
        "THRIFT" -> SpanBytesEncoder.THRIFT
        "PROTO3" -> SpanBytesEncoder.PROTO3
        else -> throw UnsupportedOperationException("encoding: "+ config.spanEncoding)
      }
      when(config.type) {
        "aws-kinesis" -> sendToKinesis(spans, bytesEncoder)
        "http" -> sendToHttp(spans, bytesEncoder)
        else -> throw UnsupportedOperationException("Undefined Destination Type: " + config.type)
      }
    } catch (e: Exception){
      log.error("Span Forward Exception {}", e.message)
    }
  }

  private fun sendToKinesis(spans: List<Span>, bytesEncoder: SpanBytesEncoder) {
    kinesisSender.sendSpans(spans.map(bytesEncoder::encode).toList()).execute()
    log.debug("Kinesis Sender Status: {} ",kinesisSender.check().toString())
  }

  private fun sendToHttp(spans: List<Span>, bytesEncoder: SpanBytesEncoder) {
    httpSender.sendSpans(spans.map(bytesEncoder::encode).toList()).execute()
    log.debug("Http Sender Status: {} ",httpSender.check().toString())
  }

}