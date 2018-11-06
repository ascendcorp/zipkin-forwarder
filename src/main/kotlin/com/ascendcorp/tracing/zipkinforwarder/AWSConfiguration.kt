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

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import zipkin2.codec.Encoding
import zipkin2.reporter.kinesis.KinesisSender

@Configuration
class AWSConfiguration (
  @Value("\${aws.accesskey}") val accessKey : String,
  @Value("\${aws.secretkey}") val secretKey : String,
  @Value("\${aws.kinesis.streamname}") val streamName: String,
  @Value("\${aws.region}") val region: String,
  @Value("\${aws.serviceEndpoint}") val serviceEndpoint: String,
  @Value("\${zipkin.spanEncoding}") val spanEncoding: String
) {
  val awsCredentials =
      BasicAWSCredentials(this.accessKey, this.secretKey)

  @Bean
  fun kinesisSender(): KinesisSender {

    return KinesisSender.newBuilder()
        .streamName(this.streamName)
        .encoding(getEncoding(spanEncoding))
        .credentialsProvider(AWSStaticCredentialsProvider(awsCredentials))
        .endpointConfiguration(
            AwsClientBuilder.EndpointConfiguration(
                serviceEndpoint,
                region))
        .build()
  }

  // Sender encoding, for example,  spans of different encoding can be collected but sent as a
  // common encoding
  fun getEncoding(encoding: String): Encoding {
    return when (encoding) {
      "PROTO3" -> Encoding.PROTO3
      "THRIFT" -> Encoding.THRIFT
      else -> Encoding.JSON
    }
  }
}
