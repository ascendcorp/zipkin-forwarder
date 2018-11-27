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

import com.google.auth.oauth2.GoogleCredentials
import io.grpc.CallOptions
import io.grpc.auth.MoreCallCredentials
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import zipkin2.reporter.stackdriver.StackdriverSender
import java.io.FileInputStream

@Configuration
@ConditionalOnProperty( name =["destination.type"], havingValue = "gcp-stackdriver")
class GCPConfiguration (@Value("\${gcp.projectId}") val projectId : String) {

  final val credentials = GoogleCredentials.getApplicationDefault()

  @Bean
  fun stackDriverSender(): StackdriverSender{
    return StackdriverSender.newBuilder()
        .projectId(this.projectId)
        .callOptions(CallOptions.DEFAULT.withCallCredentials(MoreCallCredentials.from
        (credentials)))
        .build();
  }
}
