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

import com.ascendcorp.tracing.zipkinforwarder.PubSubSender
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import zipkin2.codec.Encoding

@Configuration
//@ConditionalOnProperty(name = ["destination.type"], havingValue = "gcp-pubsub")
class PubSubConfiguration(val config: ZipkinProperties) {
  @Bean
  fun pubSubSender(): PubSubSender {
     return PubSubSender.Builder().encoding(getEncoding(config.spanEncoding))
        .project("acm-product-development")
        .topic("zipkin-trace")
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