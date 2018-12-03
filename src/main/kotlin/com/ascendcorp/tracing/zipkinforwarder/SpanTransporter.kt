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
import com.google.common.collect.FluentIterable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import zipkin2.Call
import zipkin2.Callback
import zipkin2.Span
import zipkin2.codec.SpanBytesEncoder
import zipkin2.reporter.kinesis.KinesisSender
import zipkin2.reporter.okhttp3.OkHttpSender
import zipkin2.reporter.stackdriver.StackdriverEncoder
import zipkin2.reporter.stackdriver.StackdriverSender
import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER



@Service
class SpanTransporter: Transport {

  @Autowired(required = false)
  @Qualifier("kinesisSender")
  lateinit var kinesisSender : KinesisSender

  @Autowired(required = false)
  @Qualifier("httpSender")
  lateinit var httpSender: OkHttpSender

  @Autowired(required = false)
  @Qualifier("stackDriverSender")
  lateinit var stackDriverSender: StackdriverSender

  @Autowired(required = false)
  @Qualifier("pubSubSender")
  lateinit var pubSubSender: PubSubSender

  @Autowired
  lateinit var config: ZipkinProperties

  val log = LoggerFactory.getLogger(SpanTransporter::class.java)
  
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
        "gcp-stackdriver" -> sendToStackDriver(spans)
        "gcp-pubsub" -> sendToPubSub(spans, bytesEncoder)
        else -> throw UnsupportedOperationException("Undefined Destination Type: " + config.type)
      }
    } catch (e: Exception){
      log.error("Span Forward Exception {}", e.message)
    }
  }

  private fun sendToKinesis(spans: List<Span>, bytesEncoder: SpanBytesEncoder) {
    kinesisSender.sendSpans(spans.map(bytesEncoder::encode).toList()).execute()
  }

  private fun sendToHttp(spans: List<Span>, bytesEncoder: SpanBytesEncoder) {
    httpSender.sendSpans(spans.map(bytesEncoder::encode).toList()).execute()
  }

  private fun sendToStackDriver(spans: List<Span>) {
    val encodedSpans =
        FluentIterable.from(spans).transform(StackdriverEncoder.V1::encode).toList();
    stackDriverSender.sendSpans(encodedSpans).execute();
  }

  private fun sendToPubSub(spans: List<Span>, bytesEncoder: SpanBytesEncoder) {
    pubSubSender.sendSpans(spans.map(bytesEncoder::encode)
        .toList<ByteArray?>())
    .enqueue(ZipkinCallback())
  }

  internal inner class ZipkinCallback() : Callback<Void> {

    override fun onSuccess(value: Void) {
      log.debug("Successfully wrote span {}")
    }

    override fun onError(t: Throwable) {
      log.error("Unable to write span {}")
    }
  }
}

