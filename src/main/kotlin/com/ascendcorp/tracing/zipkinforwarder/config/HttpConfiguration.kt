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

package com.ascendcorp.tracing.zipkinforwarder.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import zipkin2.codec.Encoding
import zipkin2.reporter.okhttp3.OkHttpSender

@Configuration
@ConditionalOnProperty(name = ["destination.type"], havingValue = "http")
class HttpConfiguration (@Value("\${http.zipkinHost}") val zipkinHost : String,
  val config: ZipkinProperties
) {
  @Bean
  fun httpSender(): OkHttpSender {
    return OkHttpSender.create(this.zipkinHost+"/api/v2/spans")
        .toBuilder()
        .encoding(getEncoding(config.spanEncoding))
        .compressionEnabled(true)
        .build()
  }

  fun getEncoding(encoding: String): Encoding {
    return when (encoding) {
      "PROTO3" -> Encoding.PROTO3
      "THRIFT" -> Encoding.THRIFT
      else -> Encoding.JSON
    }
  }
}